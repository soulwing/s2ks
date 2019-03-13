/*
 * File created on Mar 12, 2019
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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.util.Properties;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.soulwing.s2ks.FilesUtil;
import org.soulwing.s2ks.base.KeyDescriptor;
import org.soulwing.s2ks.KeyStorageLocator;
import org.soulwing.s2ks.KeyUtil;
import org.soulwing.s2ks.MutableKeyStorage;
import org.soulwing.s2ks.NoSuchKeyException;
import org.soulwing.s2ks.ProviderConfigurationException;
import org.soulwing.s2ks.base.AbstractKeyWrapOperator;
import org.soulwing.s2ks.pem.PemBlobEncoder;
import org.soulwing.s2ks.pem.PemEncoder;

/**
 * Tests for the local storage provider.
 * <p>
 * The tests in this class are really integration tests, but because the
 * filesystem resource is available to every Java SE runtime, and the tests
 * are relatively fast to run, we run them with unit tests.
 *
 * @author Carl Harris
 */
public class LocalKeyStorageProviderTest {

  private static Path parent;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    parent = Files.createTempDirectory(
        LocalKeyStorageProviderTest.class.getSimpleName());
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    FilesUtil.recursivelyDelete(parent);
  }

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testGetInstanceWithNoStorageDirectory() throws Exception {
    final Properties properties = new Properties();
    properties.setProperty(LocalKeyStorageProvider.PASSWORD, "secret");

    expectedException.expect(ProviderConfigurationException.class);
    expectedException.expectMessage("storageDirectory");

    assertThat(
        KeyStorageLocator.getInstance(LocalKeyStorageProvider.NAME, properties),
        is(not(nullValue())));
  }

  @Test
  public void testGetInstanceWithNoPassword() throws Exception {
    final Properties properties = new Properties();
    properties.setProperty(LocalKeyStorageProvider.STORAGE_DIRECTORY,
        parent.toString());

    expectedException.expect(ProviderConfigurationException.class);
    expectedException.expectMessage("password");

    assertThat(
        KeyStorageLocator.getInstance(LocalKeyStorageProvider.NAME, properties),
        is(not(nullValue())));
  }

  @Test
  public void testGetInstanceWithSimplePassword() throws Exception {
    final Properties properties = new Properties();
    properties.setProperty(LocalKeyStorageProvider.PASSWORD, "secret");
    properties.setProperty(LocalKeyStorageProvider.STORAGE_DIRECTORY,
        parent.toString());
    assertThat(
        KeyStorageLocator.getInstance(LocalKeyStorageProvider.NAME, properties),
        is(not(nullValue())));
  }

  @Test
  public void testGetInstanceWithPasswordFile() throws Exception {
    final Properties properties = new Properties();
    final Path path = createPasswordFile("secret");
    properties.setProperty(LocalKeyStorageProvider.PASSWORD_FILE,
        path.toString());
    properties.setProperty(LocalKeyStorageProvider.STORAGE_DIRECTORY,
        parent.toString());
    try {
      assertThat(
          KeyStorageLocator.getInstance(LocalKeyStorageProvider.NAME, properties),
          is(not(nullValue())));
    }
    finally {
      Files.delete(path);
    }
  }

  @Test
  public void testStoreAndRetrieveAesKey() throws Exception {
    final SecretKey key = KeyUtil.aesKey(256);
    final KeyDescriptor descriptor = validateStoreAndRetrieve(
        getStorageInstance(), key);

    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.SECRET)));
    assertThat(descriptor.getAlgorithm(), is(equalTo("AES")));
    validateMetadata(descriptor);

    // since the byte array of a AES key spec is directly encoded, we
    // can use the blob content to try to create a key spec; if the original
    // subject key was properly encrypted, a key created from the blob content
    // shouldn't be the same as the original key

    final SecretKey contentKey = new SecretKeySpec(descriptor.getKeyData(), "AES");
    assertThat(contentKey, is(not(equalTo(key))));

  }

  @Test
  public void testStoreAndRetrieveEcPrivateKey() throws Exception {
    final KeyDescriptor descriptor =
        validateStoreAndRetrieve(getStorageInstance(),
            KeyUtil.ecKeyPair().getPrivate());

    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.PRIVATE)));
    assertThat(descriptor.getAlgorithm(), is(equalTo("EC")));
    validateMetadata(descriptor);
  }

  @Test
  public void testStoreAndRetrieveRsaPrivateKey() throws Exception {
    final KeyDescriptor descriptor =
        validateStoreAndRetrieve(getStorageInstance(),
            KeyUtil.rsaKeyPair().getPrivate());

    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.PRIVATE)));
    assertThat(descriptor.getAlgorithm(), is(equalTo("RSA")));
    validateMetadata(descriptor);
  }

  @Test(expected = NoSuchKeyException.class)
  public void testRetrieveWhenNotFound() throws Exception {
    getStorageInstance().retrieve(UUID.randomUUID().toString());
  }

  private MutableKeyStorage getStorageInstance() throws Exception {
    final Properties properties = new Properties();

    properties.setProperty(LocalKeyStorageProvider.PASSWORD, "secret");
    properties.setProperty(LocalKeyStorageProvider.STORAGE_DIRECTORY,
        parent.toString());

    return KeyStorageLocator.getMutableInstance(
        LocalKeyStorageProvider.NAME, properties);
  }

  private void validateMetadata(KeyDescriptor descriptor) {
    assertThat(descriptor.getMetadata()
            .get(AbstractKeyWrapOperator.PROC_TYPE_HEADER),
        is(equalTo(AbstractKeyWrapOperator.PROC_TYPE_VALUE)));

    assertThat(descriptor.getMetadata()
            .get(AbstractKeyWrapOperator.DEK_INFO_HEADER),
        is(not(nullValue())));
  }

  private KeyDescriptor validateStoreAndRetrieve(
      MutableKeyStorage storage, Key key) throws Exception {
    final String id = UUID.randomUUID().toString();
    storage.store(id, key);
    final Key retrieved = storage.retrieve(id);
    assertThat(retrieved, is(equalTo(key)));

    final Path path = parent.resolve(
        id + PemEncoder.getInstance().getPathSuffix());
    try (final FileInputStream inputStream = new FileInputStream(path.toFile())) {
      return PemEncoder.getInstance().decode(
          PemBlobEncoder.getInstance().decode(inputStream).get(0));
    }
  }

  @Test()
  public void testReadFullyWithTooMuchData() throws Exception {
    final char[] password = new char[LocalKeyStorageProvider.BUFFER_SIZE + 1];
    final Path path = createPasswordFile(password);
    try {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("password");
      new LocalKeyStorageProvider().readPassword(path.toString());
    }
    finally {
      Files.delete(path);
    }
  }

  @Test
  public void testReadFullyWithMaxLengthPassword() throws Exception {
    final char[] password = new char[LocalKeyStorageProvider.BUFFER_SIZE];
    final Path path = createPasswordFile(password);
    try {
      new LocalKeyStorageProvider().readPassword(path.toString());
    }
    finally {
      Files.delete(path);
    }
  }

  private Path createPasswordFile(String password) throws IOException {
    return createPasswordFile(password.toCharArray());
  }

  private Path createPasswordFile(char[] password)
      throws IOException {
    final Path path = Files.createTempFile("password", ".txt");
    try (final Writer writer = new OutputStreamWriter(
        new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
      writer.write(password);
    }
    return path;
  }

}
