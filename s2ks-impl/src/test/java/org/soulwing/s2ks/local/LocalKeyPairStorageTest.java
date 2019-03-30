/*
 * File created on Mar 30, 2019
 *
 * Copyright (c) 2019 Carl Harris, Jr
 * and others as noted
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soulwing.s2ks.local;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.soulwing.s2ks.FilesUtil;
import org.soulwing.s2ks.KeyPairInfo;
import org.soulwing.s2ks.KeyPairStorage;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.base.CertificateLoader;
import org.soulwing.s2ks.base.PasswordWriter;
import org.soulwing.s2ks.base.PrivateKeyLoader;
import org.soulwing.s2ks.bc.BcPemCertificateLoader;

/**
 * Unit tests for {@link LocalKeyPairStorage}.
 *
 * @author Carl Harris
 */
public class LocalKeyPairStorageTest {

  private static final String KEY_PAIR_ID = "someId";

  private static final String PASSWORD_PATH = "passwordPath";

  private static final String PASSWORD = "password";

  private static final String STORAGE_PATH = "storagePath";

  private static KeyPair keyPair;

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Mock
  private PrivateKeyLoader privateKeyLoader;

  @Mock
  private CertificateLoader certificateLoader;

  private Path storagePath;
  private Path keyPath;
  private Path keyFile;
  private Path certFile;
  private Path caFile;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    keyPair = kpg.generateKeyPair();
  }

  @Before
  public void setUp() throws Exception {
    storagePath = Files.createTempDirectory(STORAGE_PATH);
    keyPath = storagePath.resolve(KEY_PAIR_ID);
    keyFile = keyPath.resolve(KeyPairStorage.KEY_FILE_NAME);
    certFile = keyPath.resolve(KeyPairStorage.CERT_FILE_NAME);
    caFile = keyPath.resolve(KeyPairStorage.CA_FILE_NAME);
    Files.createDirectory(keyPath);
    Files.createFile(keyFile);
    Files.createFile(certFile);
    Files.createFile(caFile);
  }

  @After
  public void tearDown() throws Exception {
    if (Files.exists(storagePath)) {
      FilesUtil.recursivelyDelete(storagePath);
    }
  }

  @Test
  public void testRetrieveWithPassword() throws Exception {
    final X509Certificate cert = loadCerts("cert.pem").get(0);
    final List<X509Certificate> cacerts = loadCerts("cacerts.pem");
    final List<X509Certificate> chain = new ArrayList<>();
    chain.add(cert);
    chain.addAll(cacerts);
    validateRetrieve(null, PASSWORD, chain);
  }

  @Test
  public void testRetrieveWhenNoCACerts() throws Exception {
    Files.deleteIfExists(caFile);
    final X509Certificate cert = loadCerts("cert.pem").get(0);
    validateRetrieve(null, PASSWORD, Collections.singletonList(cert));
  }

  @Test
  public void testRetrieveWithPasswordFile() throws Exception {
    Files.deleteIfExists(caFile);
    final X509Certificate cert = loadCerts("cert.pem").get(0);
    final Path passwordFile = Files.createTempFile(PASSWORD_PATH, "");
    PasswordWriter.writePassword(PASSWORD, passwordFile.toFile());
    validateRetrieve(passwordFile, null, Collections.singletonList(cert));
  }

  private void validateRetrieve(Path passwordFile,
      String password, List<X509Certificate> chain) throws Exception {

    final LocalKeyPairStorage storage = newStorage(passwordFile, password);

    context.checking(new Expectations() {
      {
        oneOf(privateKeyLoader).load(with(any(InputStream.class)),
            with(PASSWORD.toCharArray()));
        will(returnValue(keyPair.getPrivate()));
        oneOf(certificateLoader).load(with(any(InputStream.class)));
        will(returnValue(chain.subList(0, 1)));
        if (chain.size() > 1) {
          oneOf(certificateLoader).load(with(any(InputStream.class)));
          will(returnValue(chain.subList(1, chain.size())));
        }
      }
    });

    final KeyPairInfo kpi = storage.retrieve(KEY_PAIR_ID);
    assertThat(kpi.getId(), is(equalTo(KEY_PAIR_ID)));
    assertThat(kpi.getPrivateKey(), is(sameInstance(keyPair.getPrivate())));
    assertThat(kpi.getCertificates(), is(equalTo(chain)));
  }

  @Test
  public void testRetrieveWithPrivateKeyIOException() throws Exception {
    final LocalKeyPairStorage storage = newStorage(null, PASSWORD);
    final IOException ex = new IOException();

    context.checking(new Expectations() {
      {
        oneOf(privateKeyLoader).load(with(any(InputStream.class)),
            with(PASSWORD.toCharArray()));
        will(throwException(ex));
      }
    });

    expectedException.expect(KeyStorageException.class);
    expectedException.expectCause(is(sameInstance(ex)));
    storage.retrieve(KEY_PAIR_ID);
  }

  private List<X509Certificate> loadCerts(String name) throws Exception {
    try (final InputStream inputStream =
             getClass().getResourceAsStream(name)) {
      if (inputStream == null) {
        throw new FileNotFoundException(name);
      }
      return BcPemCertificateLoader.getInstance().load(inputStream);
    }
  }

  private LocalKeyPairStorage newStorage(Path passwordFile,
      String password) throws IOException {
    return new LocalKeyPairStorage(
        privateKeyLoader, certificateLoader, storagePath, passwordFile,
        password);
  }


}