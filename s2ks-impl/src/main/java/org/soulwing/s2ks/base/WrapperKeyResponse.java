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
package org.soulwing.s2ks.base;

import java.security.Key;
import java.util.function.Consumer;

import org.soulwing.s2ks.KeyDescriptor;

/**
 * A response to a request to get the wrapper key for a subject key to be
 * stored.
 *
 * @author Carl Harris
 */
public class WrapperKeyResponse {

  private final Key key;
  private final KeyDescriptor descriptor;
  private final Consumer<WrapperKeyResponse> destroyHook;

  /**
   * Creates a new instance that contains only a key.
   * @param key the subject key
   * @return response object
   */
  public static WrapperKeyResponse with(Key key) {
    return with(key, null, null);
  }

  /**
   * Creates a new instance that contains a key and a descriptor for the key.
   * @param key the subject key
   * @param descriptor descriptor for {@code key}
   * @return response object
   */
  public static WrapperKeyResponse with(Key key, KeyDescriptor descriptor) {
    return with(key, descriptor, null);
  }

  /**
   * Creates a new instance that contains a key and a descriptor for the key.
   * @param key the subject key
   * @param descriptor descriptor for {@code key}
   * @param destroyHook a callback that will be invoked to wipe the key from
   *    memory when it is no longer needed
   * @return response object
   */
  public static WrapperKeyResponse with(Key key, KeyDescriptor descriptor,
      Consumer<WrapperKeyResponse> destroyHook) {
    return new WrapperKeyResponse(key, descriptor, destroyHook);
  }

  private WrapperKeyResponse(Key key, KeyDescriptor descriptor,
      Consumer<WrapperKeyResponse> destroyHook) {
    this.key = key;
    this.descriptor = descriptor;
    this.destroyHook = destroyHook;
  }

  /**
   * Gets the wrapper key.
   * @return key object
   */
  Key getKey() {
    return key;
  }

  /**
   * Gets the descriptor.
   * @return descriptor or {@code null} if this response has no descriptor
   */
  KeyDescriptor getDescriptor() {
    return descriptor;
  }

  /**
   * Invokes a registered callback to wipe the key from memory, if any.
   */
  void destroy() {
    if (destroyHook != null) {
      destroyHook.accept(this);
    }
  }

}
