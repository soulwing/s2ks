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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.Test;
import org.soulwing.s2ks.KeyPairStorage;
import org.soulwing.s2ks.KeyPairStorageLocator;

/**
 * Unit tests for {@link org.soulwing.s2ks.KeyPairStorageLocator}.
 *
 * @author Carl Harris
 */
public class KeyPairStorageLocatorTest {

  @Test
  public void testLocate() throws Exception {
    final Path storageDirectory = Files.createTempDirectory("storageDirectory");
    try {
      final Properties properties = new Properties();
      properties.setProperty(LocalKeyPairStorageProvider.STORAGE_DIRECTORY,
          storageDirectory.toString());
      properties.setProperty(LocalKeyPairStorageProvider.PASSWORD,
          "secret");

      final KeyPairStorage storage = KeyPairStorageLocator.getInstance(
          LocalKeyPairStorageProvider.PROVIDER_NAME, properties);

      assertThat(storage, is(instanceOf(LocalKeyPairStorage.class)));
    }
    finally {
      Files.deleteIfExists(storageDirectory);
    }
  }

}
