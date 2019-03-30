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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.soulwing.s2ks.KeyPairStorage;
import org.soulwing.s2ks.ProviderConfigurationException;
import org.soulwing.s2ks.bc.BcEncryptedPrivateKeyLoader;
import org.soulwing.s2ks.bc.BcPemCertificateLoader;
import org.soulwing.s2ks.spi.KeyPairStorageProvider;

/**
 * A {@link KeyPairStorageProvider} that provides LOCAL storage of key pairs.
 *
 * @author Carl Harris
 */
public class LocalKeyPairStorageProvider implements KeyPairStorageProvider {

  static final String PROVIDER_NAME = "LOCAL";

  static final String PASSWORD = "password";
  static final String PASSWORD_FILE = "passwordFile";
  static final String STORAGE_DIRECTORY = "storageDirectory";

  @Override
  public String getName() {
    return PROVIDER_NAME;
  }

  @Override
  public KeyPairStorage getInstance(Properties configuration)
      throws ProviderConfigurationException {

    final Path passwordFile = Optional.ofNullable(
        configuration.getProperty(PASSWORD_FILE))
        .map(Paths::get)
        .orElse(null);

    final String password = configuration.getProperty(PASSWORD);

    if (password == null && passwordFile == null) {
      throw new ProviderConfigurationException(
          "must specify either the `" + PASSWORD + "` or `" + PASSWORD_FILE
          + "` configuration property");
    }

    if (password == null) {
      assert passwordFile != null;
      if (!Files.exists(passwordFile)) {
        throw new ProviderConfigurationException(passwordFile + ": not found");
      }
    }

    final Path storagePath = Optional.ofNullable(
        configuration.getProperty(STORAGE_DIRECTORY))
        .map(Paths::get)
        .orElse(null);

    if (storagePath == null) {
      throw new ProviderConfigurationException("must specify the `"
          + STORAGE_DIRECTORY + "` configuration property");
    }

    if (!Files.exists(storagePath)) {
      throw new ProviderConfigurationException(storagePath + ": not found");
    }

    return new LocalKeyPairStorage(
        BcEncryptedPrivateKeyLoader.getInstance(),
        BcPemCertificateLoader.getInstance(),
        storagePath, passwordFile, password);
  }

}
