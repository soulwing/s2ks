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

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;

import org.soulwing.s2ks.base.AbstractKeyWrapOperator;
import org.soulwing.s2ks.base.KeyWrapOperator;

/**
 * A {@link KeyWrapOperator} that uses password based encryption.
 *
 * @author Carl Harris
 */
public final class PbeWrapOperator extends AbstractKeyWrapOperator {

  static final String PBE_KEY_ALGORITHM =
      "PBEWithHmacSHA512AndAES_256";

  private static final String ALGORITHM =
      PBE_KEY_ALGORITHM + "/CBC/PKCS5Padding";

  static final Pattern DEK_INFO_PATTERN = Pattern.compile(
      "([A-Za-z0-9_/]+),(\\d+),([A-Za-z0-9+/=]+),([A-Za-z0-9+/=]+)");

  private static final PbeWrapOperator INSTANCE = new PbeWrapOperator();

  /**
   * Gets the singleton instance
   * @return operator instance
   */
  public static PbeWrapOperator getInstance() {
    return INSTANCE;
  }

  private PbeWrapOperator() {
    super(ALGORITHM, DEK_INFO_PATTERN);
  }

  @Override
  protected String encodeParameters(Cipher cipher)
      throws InvalidParameterSpecException {

    final PBEParameterSpec spec = cipher.getParameters()
        .getParameterSpec(PBEParameterSpec.class);

    final IvParameterSpec ivSpec = (IvParameterSpec) spec.getParameterSpec();
    ivSpec.getIV();

    return ALGORITHM
        + "," + spec.getIterationCount()
        + ',' + Base64.getEncoder().encodeToString(spec.getSalt())
        + ',' + Base64.getEncoder().encodeToString(ivSpec.getIV());
  }

  @Override
  protected AlgorithmParameterSpec decodeParameters(Matcher dekInfo) {
    int iterationCount = Integer.parseInt(dekInfo.group(2));
    byte[] salt = Base64.getDecoder().decode(dekInfo.group(3));
    byte[] iv = Base64.getDecoder().decode(dekInfo.group(4));

    return new PBEParameterSpec(salt, iterationCount, new IvParameterSpec(iv));
  }

}
