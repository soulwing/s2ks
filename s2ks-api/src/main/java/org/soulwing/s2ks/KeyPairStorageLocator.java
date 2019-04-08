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
package org.soulwing.s2ks;

import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import org.soulwing.s2ks.spi.KeyPairStorageProvider;

/**
 * A service locator for {@link KeyPairStorage}.
 *
 * @author Carl Harris
 */
public final class KeyPairStorageLocator {

  /**
   * Gets a new key pair storage instance from the specified provider.
   * @param provider provider name; e.g. {@code LOCAL}
   * @param configuration configuration properties
   * @return key pair storage instance
   * @throws NoSuchProviderException if there exists no provider with the
   *    given name
   * @throws ProviderConfigurationException if the provider reports an
   *    error in creating and configuring the storage instance
   */
  public static KeyPairStorage getInstance(
      String provider, Properties configuration)
      throws NoSuchProviderException, ProviderConfigurationException {
    return getInstance(provider, configuration,
        () -> ServiceLoader.load(KeyPairStorageProvider.class));
  }

  /**
   * Gets a new key pair storage instance from the specified provider.
   * @param provider provider name; e.g. {@code LOCAL}
   * @param configuration configuration properties
   * @param loader a supplier for a service loader
   * @return key pair storage instance
   * @throws NoSuchProviderException if there exists no provider with the
   *    given name
   * @throws ProviderConfigurationException if the provider reports an
   *    error in creating and configuring the storage instance
   */
  public static KeyPairStorage getInstance(
      String provider, Properties configuration,
      Supplier<ServiceLoader<KeyPairStorageProvider>> loader)
      throws NoSuchProviderException, ProviderConfigurationException {
    try {
      return getProviderInstance(loader, p -> p.getName().equals(provider))
          .orElseThrow(() -> new NoSuchProviderException(provider))
          .getInstance(configuration);
    }
    catch (NoSuchProviderException | ProviderConfigurationException ex) {
      throw ex;
    }
    catch (Exception ex) {
      throw new ProviderConfigurationException(ex.toString(), ex);
    }
  }

  /**
   * Finds a registered service provider.
   * @param loader a supplier for a service loader
   * @param predicate predicate to use to match a provider
   * @return optional first matching provider
   */
  private static Optional<KeyPairStorageProvider> getProviderInstance(
      Supplier<ServiceLoader<KeyPairStorageProvider>> loader,
      Predicate<KeyPairStorageProvider> predicate) {
    return StreamSupport.stream(
        loader.get().spliterator(), false)
        .filter(predicate)
        .findFirst();
  }

}
