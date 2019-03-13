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

  /**
   * Gets a new key storage instance from the specified provider.
   * @param provider provider name; e.g. {@code LOCAL}
   * @param properties provider-defined configuration properties
   * @return key storage instance
   * @throws NoSuchProviderException if there exists no provider with the
   *    given name
   * @throws ProviderConfigurationException if the provider reports an
   *    error in creating and configuring the storage instance
   */
  public static KeyStorage getInstance(String provider, Properties properties)
      throws NoSuchProviderException, ProviderConfigurationException {
    try {
      return getProviderInstance(p -> p.getName().equals(provider))
          .orElseThrow(() -> new NoSuchProviderException(provider))
          .getInstance(properties);
    }
    catch (Exception ex) {
      throw new ProviderConfigurationException(ex.toString(), ex);
    }
  }

  /**
   * Gets a new mutable key storage instance from the specified provider.
   * @param provider provider name; e.g. {@code LOCAL}
   * @param properties provider-defined configuration properties
   * @return key storage instance
   * @throws NoSuchProviderException if there exists no provider with the
   *    given name that supports the mutable interface
   * @throws ProviderConfigurationException if the provider reports an
   *    error in creating and configuring the storage instance
   */
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

  /**
   * Finds a registered service provider
   * @param predicate predicate to use to match a provider
   * @return optional first matching provider
   */
  private static Optional<KeyStorageProvider> getProviderInstance(
      Predicate<KeyStorageProvider> predicate) {
    return StreamSupport.stream(
        ServiceLoader.load(KeyStorageProvider.class).spliterator(), false)
        .filter(predicate)
        .findFirst();
  }

}
