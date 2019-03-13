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

import org.soulwing.s2ks.KeyUnwrapException;
import org.soulwing.s2ks.KeyWrapException;

/**
 * An operator that performs key wrapping and unwrapping operations.
 *
 * @author Carl Harris
 */
public interface KeyWrapOperator {

  /**
   * Wraps the given key.
   * @param subjectKey the subject key to be wrapped
   * @param wrapperKey the key to use to create the encrypted wrapper
   * @return key descriptor for the wrapped key
   * @throws KeyWrapException if an unexpected error occurs;
   *    e.g. one of the myriad checked exceptions thrown by the JCA API
   */
  KeyDescriptor wrap(Key subjectKey, Key wrapperKey) throws KeyWrapException;

  /**
   * Unwraps a wrapped key.
   * @param descriptor description of the wrapped key
   * @param wrapperKey the key to use to decrypt the wrapper
   * @return original key (decoded and unwrapped from {@code descriptor})
   * @throws KeyUnwrapException if an unexpected error occurs;
   *    e.g. one of the myriad checked exceptions thrown by the JCA API
   */
  Key unwrap(KeyDescriptor descriptor, Key wrapperKey) throws KeyUnwrapException;

}
