/*
 * File created on Mar 11, 2019
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
package org.soulwing.s2ks.aes;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import org.soulwing.s2ks.KeyWrapOperator;
import org.soulwing.s2ks.base.AbstractKeyWrapOperator;

/**
 * A {@link KeyWrapOperator} that uses AES key wrapping.
 *
 * @author Carl Harris
 */
public final class AesWrapOperator extends AbstractKeyWrapOperator {

  private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

  static final Pattern DEK_INFO_PATTERN = Pattern.compile(
      "([A-Za-z0-9_/]+),([A-Za-z0-9+/=]+)");

  private static final AesWrapOperator INSTANCE = new AesWrapOperator();

  /**
   * Gets the singleton instance.
   * @return operator instance
   */
  public static AesWrapOperator getInstance() {
    return INSTANCE;
  }

  private AesWrapOperator() {
    super(ALGORITHM, DEK_INFO_PATTERN);
  }

  @Override
  protected String encodeParameters(Cipher cipher)
      throws InvalidParameterSpecException {
    final IvParameterSpec spec =
        cipher.getParameters().getParameterSpec(IvParameterSpec.class);
    return ALGORITHM + ',' + Base64.getEncoder().encodeToString(spec.getIV());
  }

  @Override
  protected AlgorithmParameterSpec decodeParameters(Matcher dekInfo) {
    return new IvParameterSpec(Base64.getDecoder().decode(dekInfo.group(2)));
  }

}
