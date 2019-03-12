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
package org.soulwing.s2ks.base;import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.soulwing.s2ks.KeyDescriptor;
import org.soulwing.s2ks.KeyUnwrapException;
import org.soulwing.s2ks.KeyWrapException;
import org.soulwing.s2ks.KeyWrapOperator;

/**
 * An abstract base for {@link KeyWrapOperator} implementations.
 *
 * @author Carl Harris
 */
public abstract class AbstractKeyWrapOperator implements KeyWrapOperator {

  public static final String PROC_TYPE_HEADER = "Proc-Type";
  public static final String PROC_TYPE_VALUE = "4,ENCRYPTED";
  public static final String DEK_INFO_HEADER = "DEK-Info";

  private final String wrapAlgorithm;
  private final Pattern dekInfoPattern;

  protected AbstractKeyWrapOperator(String wrapAlgorithm,
      Pattern dekInfoPattern) {
    this.wrapAlgorithm = wrapAlgorithm;
    this.dekInfoPattern = dekInfoPattern;
  }

  @Override
  public final KeyDescriptor wrap(Key subjectKey, Key wrapperKey)
      throws KeyWrapException {
    try {
      final Cipher cipher = Cipher.getInstance(wrapAlgorithm);
      cipher.init(Cipher.WRAP_MODE, wrapperKey);

      final String parameters = encodeParameters(cipher);

      return KeyDescriptor.builder()
          .algorithm(subjectKey.getAlgorithm())
          .type(KeyDescriptor.Type.typeOf(subjectKey))
          .metadata(PROC_TYPE_HEADER, PROC_TYPE_VALUE)
          .metadata(DEK_INFO_HEADER, parameters)
          .build(cipher.wrap(subjectKey));
    }
    catch (NoSuchAlgorithmException
          | NoSuchPaddingException
          | InvalidKeyException
          | IllegalBlockSizeException
          | InvalidParameterSpecException ex) {
      throw new KeyWrapException(ex.toString(), ex);
    }
  }

  @Override
  public final Key unwrap(KeyDescriptor descriptor, Key wrapperKey)
      throws KeyUnwrapException {
    try {
      final Cipher cipher = Cipher.getInstance(wrapAlgorithm);

      final String header = descriptor.getMetadata().get(DEK_INFO_HEADER);
      if (header == null) {
        throw new KeyUnwrapException(DEK_INFO_HEADER + " header is missing");
      }

      final Matcher matcher = dekInfoPattern.matcher(header);
      if (!matcher.matches()) {
        throw new KeyUnwrapException(DEK_INFO_HEADER + " header is invalid");
      }

      final AlgorithmParameterSpec spec = decodeParameters(matcher);


      cipher.init(Cipher.UNWRAP_MODE, wrapperKey, spec);

      return cipher.unwrap(descriptor.getKeyData(), descriptor.getAlgorithm(),
          descriptor.getType() == KeyDescriptor.Type.SECRET ?
              Cipher.SECRET_KEY : Cipher.PRIVATE_KEY);

    }
    catch (NoSuchAlgorithmException
          | NoSuchPaddingException
          | InvalidKeyException
          | InvalidAlgorithmParameterException ex) {
      throw new KeyUnwrapException(ex.toString(), ex);
    }
  }

  /**
   * Encodes algorithm parameters to a string.
   * @param cipher the cipher that was just used to wrap a key
   * @return string-encoded algorithm name and associated parameters
   * @throws InvalidParameterSpecException if thrown by {@code cipher}
   */
  protected abstract String encodeParameters(Cipher cipher)
      throws InvalidParameterSpecException;

  /**
   * Decodes algorithm parameters from a string.
   * @param dekInfo value of the {@code DEK-Info} key descriptor's metadata
   * @return algorithm parameters
   */
  protected abstract AlgorithmParameterSpec decodeParameters(Matcher dekInfo);

}
