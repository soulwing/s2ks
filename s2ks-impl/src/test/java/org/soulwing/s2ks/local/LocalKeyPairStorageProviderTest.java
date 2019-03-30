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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.soulwing.s2ks.FilesUtil;
import org.soulwing.s2ks.ProviderConfigurationException;
import org.soulwing.s2ks.base.PasswordWriter;

/**
 * Unit tests for {@link LocalKeyPairStorageProvider}.
 * @author Carl Harris
 */
public class LocalKeyPairStorageProviderTest {

  private static final String STORAGE_DIRECTORY = "storagePath";
  private static final String PASSWORD_FILE = "passwordFile";
  private static final String PASSWORD = "secret password";

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  private Properties configuration = new Properties();

  private Path storagePath;
  private Path keyPairPath;
  private Path keyPath;
  private Path keyFile;
  private Path passwordFile;

  private LocalKeyPairStorageProvider provider =
      new LocalKeyPairStorageProvider();

  @Before
  public void setUp() throws Exception {
    storagePath = Files.createTempDirectory(STORAGE_DIRECTORY);
    passwordFile = Files.createTempFile(PASSWORD_FILE, "");
    PasswordWriter.writePassword(PASSWORD, passwordFile.toFile());
    configuration.put(LocalKeyStorageProvider.PASSWORD, PASSWORD);
    configuration.put(LocalKeyStorageProvider.PASSWORD_FILE, passwordFile.toString());
    configuration.put(LocalKeyStorageProvider.STORAGE_DIRECTORY, storagePath.toString());
  }

  @After
  public void tearDown() throws Exception {
    if (Files.exists(storagePath)) {
      FilesUtil.recursivelyDelete(storagePath);
    }
    Files.deleteIfExists(passwordFile);
  }

  @Test
  public void testGetName() throws Exception {
    assertThat(provider.getName(),
        is(equalTo(LocalKeyPairStorageProvider.PROVIDER_NAME)));
  }

  @Test
  public void testGetInstanceWhenNoStoragePath() throws Exception {
    configuration.remove(LocalKeyStorageProvider.STORAGE_DIRECTORY);
    expectedException.expect(ProviderConfigurationException.class);
    expectedException.expectMessage("storage");
    provider.getInstance(configuration);
  }

  @Test
  public void testGetInstanceWhenNoStoragePathNotFound() throws Exception {
    FilesUtil.recursivelyDelete(storagePath);
    expectedException.expect(ProviderConfigurationException.class);
    expectedException.expectMessage("storage");
    provider.getInstance(configuration);
  }

  @Test
  public void testGetInstanceWhenNoPassword() throws Exception {
    configuration.remove(LocalKeyStorageProvider.PASSWORD);
    configuration.remove(LocalKeyStorageProvider.PASSWORD_FILE);
    expectedException.expect(ProviderConfigurationException.class);
    expectedException.expectMessage("password");
    provider.getInstance(configuration);
  }

  @Test
  public void testGetInstanceWhenPasswordFileNotFound() throws Exception {
    configuration.remove(LocalKeyStorageProvider.PASSWORD);
    Files.deleteIfExists(passwordFile);
    expectedException.expect(ProviderConfigurationException.class);
    expectedException.expectMessage(passwordFile.toString());
    expectedException.expectMessage("not found");
    provider.getInstance(configuration);
  }

  @Test
  public void testGetInstanceSuccess() throws Exception {
    assertThat(provider.getInstance(configuration),
        is(instanceOf(LocalKeyPairStorage.class)));
  }

}