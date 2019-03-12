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

/**
 * An exception thrown by a {@link KeyWrapOperator} to indicate that an
 * unexpected error (e.g. JCA exception) occurred while unwrapping a key.
 *
 * @author Carl Harris
 */
public class KeyUnwrapException extends KeyStorageException {

  public KeyUnwrapException(String message) {
    super(message);
  }

  public KeyUnwrapException(String message, Throwable cause) {
    super(message, cause);
  }

}
