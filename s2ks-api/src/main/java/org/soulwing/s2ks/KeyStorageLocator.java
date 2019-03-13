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
package org.soulwing.s2ks;

import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.soulwing.s2ks.spi.KeyStorageProvider;

/**
 * A service locator for {@link KeyStorage}.
 *
 * @author Carl Harris
 */
public final class KeyStorageLocator {

  public static KeyStorage getInstance(String provider, Properties properties)
      throws NoSuchProviderException, ProviderConfigurationException {
    try {
      return getProviderInstance(p -> p.getName().equals(provider))
          .orElseThrow(() -> new NoSuchProviderException(
              "cannot find a provider named `" + provider + "`"))
          .getInstance(properties);
    }
    catch (Exception ex) {
      throw new ProviderConfigurationException(ex.toString(), ex);
    }
  }

  public static MutableKeyStorage getMutableInstance(
      String provider, Properties properties)
      throws NoSuchProviderException, ProviderConfigurationException {
    try {
      return (MutableKeyStorage) getProviderInstance(
              p -> p.getName().equals(provider) && p.isMutable())
          .orElseThrow(() -> new NoSuchProviderException(provider))
          .getInstance(properties);
    }
    catch (Exception ex) {
      throw new ProviderConfigurationException(ex.getMessage(), ex);
    }
  }

  private static Optional<KeyStorageProvider> getProviderInstance(
      Predicate<KeyStorageProvider> predicate) {
    return StreamSupport.stream(
        ServiceLoader.load(KeyStorageProvider.class).spliterator(), false)
        .filter(predicate)
        .findFirst();
  }

}
