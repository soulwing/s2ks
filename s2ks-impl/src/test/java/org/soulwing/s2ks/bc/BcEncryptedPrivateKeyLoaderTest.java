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
package org.soulwing.s2ks.bc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link BcEncryptedPrivateKeyLoader}.
 * @author Carl Harris
 */
public class BcEncryptedPrivateKeyLoaderTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  private static final char[] PASSWORD = "secret".toCharArray();

  private BcEncryptedPrivateKeyLoader loader =
      BcEncryptedPrivateKeyLoader.getInstance();

  @Test
  public void testWithNoObject() throws Exception {
    final Path path = Files.createTempFile("key", ".pem");
    try (final InputStream inputStream = new FileInputStream(path.toFile())) {
      expectedException.expect(IOException.class);
      expectedException.expectMessage("no PEM object");
      loader.load(inputStream, PASSWORD);
    }
    finally {
      Files.deleteIfExists(path);
    }
  }

  @Test
  public void testWithUnsupportedObject() throws Exception {
    try (final InputStream inputStream =
             getClass().getResourceAsStream("cert.pem")) {
      expectedException.expect(IOException.class);
      expectedException.expectMessage("unsupported");
      loader.load(inputStream, PASSWORD);
    }
  }

  @Test
  public void testUnencryptedRsaKey() throws Exception {
    validateLoadKey("rsakey.pem");
  }

  @Test
  public void testPemEncryptedRsaKeyUsingDesCbc() throws Exception {
    validateLoadKey("rsakey-des-cbc.pem");
  }

  @Test
  public void testPemEncryptedRsaKeyUsingDesEde3Cbc() throws Exception {
    validateLoadKey("rsakey-des-ede3-cbc.pem");
  }

  @Test
  public void testPemEncryptedRsaKeyUsingAes256Cbc() throws Exception {
    validateLoadKey("rsakey-aes-256-cbc.pem");
  }

  @Test
  public void testPkcs8UnencryptedRsaKey() throws Exception {
    validateLoadKey("rsakey-pkcs8.pem");
  }

  @Test
  public void testPkcs8EncryptedRsaKeyUsingMd5Des() throws Exception {
    validateLoadKey("rsakey-pkcs8-md5-des.pem");
  }

  @Test
  public void testPkcs8EncryptedRsaKeyUsingDesEde3Cbc() throws Exception {
    validateLoadKey("rsakey-pkcs8-des-ede3-cbc.pem");
  }

  @Test
  public void testPkcs8EncryptedRsaKeyUsingAes256Cbc() throws Exception {
    validateLoadKey("rsakey-pkcs8-aes-256-cbc.pem");
  }

  @Test
  public void testUnencryptedEcKey() throws Exception {
    validateLoadKey("eckey.pem");
  }

  @Test
  public void testPemEncryptedEcKeyUsingDesCbc() throws Exception {
    validateLoadKey("eckey-des-cbc.pem");
  }

  @Test
  public void testPemEncryptedEcKeyUsingDesEde3Cbc() throws Exception {
    validateLoadKey("eckey-des-ede3-cbc.pem");
  }

  @Test
  public void testPemEncryptedEcKeyUsingAes256Cbc() throws Exception {
    validateLoadKey("eckey-aes-256-cbc.pem");
  }

  @Test
  public void testPkcs8UnencryptedEcKey() throws Exception {
    validateLoadKey("eckey-pkcs8.pem");
  }

  @Test
  public void testPkcs8EncryptedEcKeyUsingMd5Des() throws Exception {
    validateLoadKey("eckey-pkcs8-md5-des.pem");
  }

  @Test
  public void testPkcs8EncryptedEcKeyUsingDesEde3Cbc() throws Exception {
    validateLoadKey("eckey-pkcs8-des-ede3-cbc.pem");
  }

  @Test
  public void testPkcs8EncryptedEcKeyUsingAes256Cbc() throws Exception {
    validateLoadKey("eckey-pkcs8-aes-256-cbc.pem");
  }

  private void validateLoadKey(String name) throws Exception {
    try (final InputStream inputStream =
        getClass().getResourceAsStream(name)) {
      assertThat(loader.load(inputStream, PASSWORD), is(not(nullValue())));
    }
  }

}