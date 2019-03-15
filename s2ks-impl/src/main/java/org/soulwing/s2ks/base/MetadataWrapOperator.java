/*
 * File created on Mar 13, 2019
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
package org.soulwing.s2ks.base;

import java.security.Key;

import org.soulwing.s2ks.KeyWithMetadata;
import org.soulwing.s2ks.Metadata;
import org.soulwing.s2ks.MetadataUnwrapException;
import org.soulwing.s2ks.MetadataWrapException;

/**
 * An operator that performs metadata wrapping and unwrapping operations.
 *
 * @author Carl Harris
 */
public interface MetadataWrapOperator {

  /**
   * Wraps the given metadata.
   * @param keyWithMetadata the subject metadata and its corresponding key
   * @return wrapped metadata
   * @throws MetadataWrapException if an error occurs in wrapping the
   *    metadata
   */
  byte[] wrap(KeyWithMetadata keyWithMetadata) throws MetadataWrapException;

  /**
   * Unwraps the given metadata.
   * @param key key associated with the metadata
   * @param encoded wrapped metadata
   * @return unwrapped metadata
   * @throws MetadataUnwrapException if an error occurs in unwrapping the
   *    metadata
   */
  Metadata unwrap(Key key, byte[] encoded) throws MetadataUnwrapException;

}
