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
 * An exception thrown by {@link KeyStorageLocator} to indicate that the
 * requested provider was not found.
 * <p>
 * This is runtime exception because the locator is often used in situations
 * such as dependency injection initialization call backs that aren't supposed
 * to throw checked exceptions.
 *
 * @author Carl Harris
 */
public class NoSuchProviderException extends RuntimeException {

  public NoSuchProviderException(String provider) {
    super("found no key storage provider named `" + provider + "`");
  }

}
