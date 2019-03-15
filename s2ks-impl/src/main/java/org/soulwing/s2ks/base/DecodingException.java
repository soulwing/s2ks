/*
 * File created on Mar 14, 2019
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

import org.soulwing.s2ks.KeyStorageException;

/**
 * An exception thrown to indicate that an unexpected error occurred while
 * decoding a blob.
 *
 * @author Carl Harris
 */
public class DecodingException extends KeyStorageException {

  public DecodingException(String message) {
    this(message, null);
  }

  public DecodingException(String message, Throwable cause) {
    super(message, cause);
  }

}
