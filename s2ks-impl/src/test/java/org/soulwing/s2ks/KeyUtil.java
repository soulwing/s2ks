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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility methods for keys.
 *
 * @author Carl Harris
 */
public class KeyUtil {

  private static final SecureRandom random = new SecureRandom();
  private static final KeyPair rsaKeyPair;
  private static final KeyPair ecKeyPair;

  static {
    try {
      rsaKeyPair = generateRsaKeyPair();
      ecKeyPair = generateEcKeyPair();
    }
    catch (RuntimeException ex) {
      throw ex;
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static KeyPair rsaKeyPair() {
    return rsaKeyPair;
  }

  private static KeyPair generateRsaKeyPair() throws Exception {
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    return kpg.generateKeyPair();
  }

  public static KeyPair ecKeyPair() {
    return ecKeyPair;
  }

  private static KeyPair generateEcKeyPair() throws Exception {
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
    kpg.initialize(256);
    return kpg.generateKeyPair();
  }

  public static SecretKey aesKey(int size) {
    return new SecretKeySpec(KeyUtil.randomKeyData(size / Byte.SIZE), "AES");
  }

  public static byte[] randomKeyData(int size) {
    final byte[] data = new byte[size];
    random.nextBytes(data);
    return data;
  }

}
