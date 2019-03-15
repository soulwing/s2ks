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
package org.soulwing.s2ks;

import java.security.Key;

/**
 * An immutable value holder for a key and its associated metadata.
 *
 * @author Carl Harris
 */
public final class KeyWithMetadata {

  private final Key key;
  private final Metadata metadata;

  public KeyWithMetadata(Key key, Metadata metadata) {
    if (key == null || metadata == null) {
      throw new IllegalArgumentException("key and metadata are required");
    }
    this.key = key;
    this.metadata = metadata;
  }

  /**
   * Gets the subject key.
   * @return key
   */
  public Key getKey() {
    return key;
  }

  /**
   * Gets the metadata associated with the key.
   * @return
   */
  public Metadata getMetadata() {
    return metadata;
  }

}
