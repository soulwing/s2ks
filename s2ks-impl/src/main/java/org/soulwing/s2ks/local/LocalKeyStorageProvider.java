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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import org.soulwing.s2ks.KeyStorage;
import org.soulwing.s2ks.StorageService;
import org.soulwing.s2ks.filesystem.FilesystemStorageService;
import org.soulwing.s2ks.pbe.PbeKeyFactory;
import org.soulwing.s2ks.pbe.PbeKeyStorage;
import org.soulwing.s2ks.pbe.PbeWrapOperator;
import org.soulwing.s2ks.pem.PemBlobEncoder;
import org.soulwing.s2ks.pem.PemEncoder;
import org.soulwing.s2ks.spi.KeyStorageProvider;

/**
 * A {@link KeyStorageProvider} that stores passwords on the local filesystem
 * using password-based encryption.
 *
 * @author Carl Harris
 */
public class LocalKeyStorageProvider implements KeyStorageProvider {

  static final int BUFFER_SIZE = 1024;

  static final String NAME = "LOCAL";
  static final String PASSWORD = "password";
  static final String PASSWORD_FILE = "passwordFile";
  static final String STORAGE_DIRECTORY = "storageDirectory";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isMutable() {
    return true;
  }

  @Override
  public KeyStorage getInstance(Properties properties) throws Exception {
    final Path directory = getStorageDirectory(properties);
    final char[] password = getPassword(properties);

    final StorageService storageService =
        new FilesystemStorageService(directory, PemBlobEncoder.getInstance());

    final PbeKeyStorage storage = new PbeKeyStorage(
        PemBlobEncoder.getInstance(),
        PemEncoder.getInstance(),
        PbeWrapOperator.getInstance(),
        PbeKeyFactory.generateKey(password),
        storageService);

    Arrays.fill(password, (char) 0);
    return storage;
  }

  /**
   * Gets the path to the storage directory from the given properties.
   * @param properties subject properties
   * @return storage directory
   * @throws IllegalArgumentException if the properties don't contain a
   *    path to storage directory.
   */
  private Path getStorageDirectory(Properties properties) {
    return Optional.ofNullable(properties.getProperty(STORAGE_DIRECTORY))
        .map(Paths::get)
        .orElseThrow(() -> new IllegalArgumentException(
            "must specify the `" + STORAGE_DIRECTORY + "` property"));
  }

  /**
   * Gets the master password from the given properties.
   * @param properties subject properties
   * @return storage directory
   * @throws IllegalArgumentException if the properties don't specify a
   *    password
   */
  private char[] getPassword(Properties properties) throws IOException {
    final char[] password = getPasswordFromFile(properties);
    if (password.length > 0) {
      return password;
    }
    if (properties.getProperty(PASSWORD) == null) {
      throw new IllegalArgumentException(
          "must specify either the `" + PASSWORD_FILE + "` or `"
              + PASSWORD + "` property");
    }
    return properties.getProperty(PASSWORD).toCharArray();
  }

  private char[] getPasswordFromFile(Properties properties) throws IOException {
    final String path = properties.getProperty(PASSWORD_FILE);
    if (path == null) return new char[0];
    return readPassword(path);
  }

  /**
   * Reads a password from a file at the given path.
   * <p>
   * The sequence of characters leading up to the first newline character or
   * at most {@link #BUFFER_SIZE} characters are used as the password.
   *
   * @param path path of the file to read
   * @return password array (possibly empty, but not null)
   * @throws IOException if an error occurs in reading the file
   */
  char[] readPassword(String path) throws IOException {
    try (Reader reader = new InputStreamReader(
        new FileInputStream(path), StandardCharsets.US_ASCII)) {
      final char[] buf = new char[BUFFER_SIZE];
      int numRead = reader.read(buf);
      if (numRead == buf.length && reader.read() != -1) {
        throw new IllegalArgumentException(
            "password must not be longer than " + buf.length + " characters");
      }
      int length = 0;
      while (length < numRead && buf[length] != '\r' && buf[length] != '\n') {
        length++;
      }

      return Arrays.copyOfRange(buf, 0, length);
    }

  }

}
