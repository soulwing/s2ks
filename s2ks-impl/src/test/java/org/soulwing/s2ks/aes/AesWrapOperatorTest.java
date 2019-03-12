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

import java.security.Key;
import java.util.regex.Pattern;

import org.soulwing.s2ks.base.AbstractKeyWrapOperator;
import org.soulwing.s2ks.AbstractKeyWrapOperatorTest;
import org.soulwing.s2ks.KeyUtil;

/**
 * Unit tests for {@link AesWrapOperator}.
 *
 * @author Carl Harris
 */
public class AesWrapOperatorTest extends AbstractKeyWrapOperatorTest {

  @Override
  protected AbstractKeyWrapOperator operator() {
    return AesWrapOperator.getInstance();
  }

  @Override
  protected Key wrapperKey() {
    return KeyUtil.aesKey(256);
  }

  @Override
  protected Pattern dekInfoPattern() {
    return AesWrapOperator.DEK_INFO_PATTERN;
  }

}
