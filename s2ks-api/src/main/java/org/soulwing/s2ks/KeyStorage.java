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
 * A key storage provider.
 *
 * @author Carl Harris
 */
public interface KeyStorage {

  /**
   * Retrieves a key.
   * @param id unique identifier of the subject key
   * @return key object
   * @throws NoSuchKeyException if there is no key in storage with
   *    the given identifier
   * @throws KeyUnwrapException if an error occurs in unwrapping the
   *    stored key; e.g. one of the myriad checked exceptions thrown
   *    by the JCA API
   * @throws KeyStorageException if the underlying storage mechanism cannot
   *    successfully read the wrapped key from persistent storage
   */
  Key retrieve(String id)
      throws NoSuchKeyException, KeyUnwrapException, KeyStorageException;

  /**
   * Retrieves a key and its associated metadata.
   * @param id unique identifier of the subject key
   * @return key-with-metadata object
   * @throws NoSuchKeyException if there is no key in storage with
   *    the given identifier
   * @throws KeyUnwrapException if an error occurs in unwrapping the
   *    stored key; e.g. one of the myriad checked exceptions thrown
   *    by the JCA API
   * @throws MetadataUnwrapException if an error occurs in decoding the
   *    metadata
   * @throws KeyStorageException if the underlying storage mechanism cannot
   *    successfully read the wrapped key from persistent storage
   */
  KeyWithMetadata retrieveWithMetadata(String id)
      throws NoSuchKeyException, KeyUnwrapException,
      MetadataUnwrapException, KeyStorageException;


}
