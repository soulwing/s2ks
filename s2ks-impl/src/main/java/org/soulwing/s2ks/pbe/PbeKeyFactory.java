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
package org.soulwing.s2ks.pbe;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * A factory that produces keys for password-based encryption.
 *
 * @author Carl Harris
 */
public class PbeKeyFactory {

  /**
   * Generates a key for password-based encryption with the given password.
   * @param password password to use in key derivation
   * @return generated key
   * @throws NoSuchAlgorithmException if the PBE algorithm is not supported
   * @throws InvalidKeySpecException if the PBE key specification is invalid
   */
  public static SecretKey generateKey(char[] password)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    final SecretKeyFactory kg =
        SecretKeyFactory.getInstance(PbeWrapOperator.PBE_KEY_ALGORITHM);
    final PBEKeySpec spec = new PBEKeySpec(password);
    return kg.generateSecret(spec);
  }

}
