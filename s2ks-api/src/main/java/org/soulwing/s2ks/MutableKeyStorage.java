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

import java.security.Key;

/**
 * A {@link KeyStorage} provider that provides the ability to store
 * new keys at runtime.
 *
 * @author Carl Harris
 */
public interface MutableKeyStorage extends KeyStorage {

  /**
   * Stores a key using the given identifier, replacing any existing
   * key with the same identifier.
   *
   * @param id identifier for the subject key
   * @param key the subject key
   * @throws KeyWrapException if an error occurs in wrapping the key;
   *    e.g. one of the myriad checked exceptions thrown by the JCA API
   * @throws KeyStorageException if the underlying storage mechanism cannot
   *    successfully write the wrapped key to persistent storage
   */
  void store(String id, Key key)
      throws KeyWrapException, KeyStorageException;

  /**
   * Stores a key and associated metadata using the given identifier, replacing
   * any existing key and metadata with the same identifier.
   *
   * @param id identifier for the subject key
   * @param keyWithMetadata the subject key and metadata
   * @throws KeyWrapException if an error occurs in wrapping the key;
   *    e.g. one of the myriad checked exceptions thrown by the JCA API
   * @throws MetadataWrapException if an error occurs in encoding the
   *    metadata
   * @throws KeyStorageException if the underlying storage mechanism cannot
   *    successfully write the wrapped key to persistent storage
   */
  void store(String id, KeyWithMetadata keyWithMetadata)
      throws KeyWrapException, KeyStorageException;

}
