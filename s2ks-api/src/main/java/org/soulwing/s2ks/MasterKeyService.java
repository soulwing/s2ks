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

import javax.crypto.SecretKey;

/**
 * An abstraction of the master key service offered by a KMS. The KMS is
 * assumed to provide just two operations; one which generates data encryption
 * keys that can be used for an encryption operation, and another which provides
 * the means to decrypt a previously generated data encryption key.
 *
 * @author Carl Harris
 */
public interface MasterKeyService {

  /**
   * Generates a new encryption key.
   * @return encryption key
   * @throws KeyWrapException if an error occurs in generating the key
   */
  EncryptionKey newEncryptionKey() throws KeyWrapException;

  /**
   * Decrypts a previously generated encryption key.
   * @param cipherText cipher text of the data encryption key (encrypted by
   *    the KMS)
   * @return decrypted data encryption key
   * @throws KeyUnwrapException if an error occurs in decrypting the key
   */
  SecretKey decryptKey(byte[] cipherText) throws KeyUnwrapException;

}
