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
package org.soulwing.s2ks.spi;

import java.util.Properties;

import org.soulwing.s2ks.KeyStorage;

/**
 * A service provider interface for {@link KeyStorage}.
 *
 * @author Carl Harris
 */
public interface KeyStorageProvider {

  /**
   * Gets the name of this provider.
   * @return provider name
   */
  String getName();

  /**
   * Tests whether this provider supports the
   * {@link org.soulwing.s2ks.MutableKeyStorage} interface.
   * @return {@code true} if this provider supports the mutable API
   */
  boolean isMutable();

  /**
   * Gets a key storage instance from this provider.
   * @param properties configuration properties
   * @return key storage instance
   * @throws Exception to indicate that an instance could not be initialized
   */
  KeyStorage getInstance(Properties properties) throws Exception;

}
