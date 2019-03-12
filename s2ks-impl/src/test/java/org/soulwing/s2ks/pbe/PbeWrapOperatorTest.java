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

import java.security.Key;
import java.util.regex.Pattern;

import org.soulwing.s2ks.base.AbstractKeyWrapOperator;
import org.soulwing.s2ks.AbstractKeyWrapOperatorTest;


/**
 * Unit tests for {@link PbeWrapOperator}.
 *
 * @author Carl Harris
 */
public class PbeWrapOperatorTest extends AbstractKeyWrapOperatorTest {

  @Override
  protected AbstractKeyWrapOperator operator() {
    return PbeWrapOperator.getInstance();
  }

  @Override
  protected Key wrapperKey() throws Exception {
    return PbeKeyFactory.generateKey("secret".toCharArray());
  }

  @Override
  protected Pattern dekInfoPattern() {
    return PbeWrapOperator.DEK_INFO_PATTERN;
  }

}
