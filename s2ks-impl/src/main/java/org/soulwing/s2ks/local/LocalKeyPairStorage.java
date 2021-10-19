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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;

import org.soulwing.s2ks.KeyPairStorage;
import org.soulwing.s2ks.base.AbstractKeyPairStorage;
import org.soulwing.s2ks.base.CertificateLoader;
import org.soulwing.s2ks.base.PasswordReader;
import org.soulwing.s2ks.base.PrivateKeyLoader;

/**
 * A {@link org.soulwing.s2ks.KeyPairStorage} implementation that uses local
 * filesystem storage.
 *
 * @author Carl Harris
 */
class LocalKeyPairStorage extends AbstractKeyPairStorage {

  private final String password;
  private final Path storageDirectory;
  private final Path passwordFile;

  LocalKeyPairStorage(
      PrivateKeyLoader privateKeyLoader,
      CertificateLoader certificateLoader,
      Path storageDirectory,
      Path passwordFile,
      String password) {
    super(privateKeyLoader, certificateLoader);
    this.storageDirectory = storageDirectory;
    this.passwordFile = passwordFile;
    this.password = password;
  }

  @Override
  protected char[] getPassword(String id) throws IOException {
    if (password != null) {
      return Arrays.copyOf(password.toCharArray(), password.length());
    }
    return PasswordReader.readPassword(passwordFile.toFile());
  }

  @Override
  protected InputStream openPrivateKeyStream(String id) throws IOException {
    return new FileInputStream(storageDirectory.resolve(id)
        .resolve(KeyPairStorage.KEY_FILE_NAME).toFile());
  }

  @Override
  protected InputStream openCertificateStream(String id) throws IOException {
    return new FileInputStream(
        storageDirectory.resolve(id).resolve(CERT_FILE_NAME).toFile());
  }

  @Override
  protected InputStream openCACertificateStream(String id) throws IOException {
    return new FileInputStream(
        storageDirectory.resolve(id).resolve(CA_FILE_NAME).toFile());
  }

}
