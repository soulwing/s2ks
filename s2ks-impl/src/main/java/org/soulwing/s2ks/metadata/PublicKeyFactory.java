/*
 * File created on Mar 15, 2019
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
package org.soulwing.s2ks.metadata;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.soulwing.s2ks.MetadataUnwrapException;

/**
 * A factory that produces public keys from the corresponding private key.
 *
 * @author Carl Harris
 */
public interface PublicKeyFactory {

  /**
   * Derives the public key that corresponds to the given private key.
   * @param privateKey the subject private key
   * @return corresponding public key
   * @throws MetadataUnwrapException if the key cannot be successfully
   *    derived
   */
  PublicKey generatePublic(PrivateKey privateKey)
      throws MetadataUnwrapException;

}
