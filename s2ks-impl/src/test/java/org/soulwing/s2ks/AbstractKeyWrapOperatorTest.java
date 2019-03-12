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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.security.Key;
import java.security.PrivateKey;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.SecretKey;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.soulwing.s2ks.base.AbstractKeyWrapOperator;

/**
 * An abstract base for {@link AbstractKeyWrapOperatorTest} implementations.
 *
 * @author Carl Harris
 */
public abstract class AbstractKeyWrapOperatorTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  protected abstract AbstractKeyWrapOperator operator();

  protected abstract Key wrapperKey() throws Exception;

  protected abstract Pattern dekInfoPattern();

  @Test
  public void testWrapAndUnwrapSecretKey() throws Exception {
    final SecretKey subjectKey = KeyUtil.aesKey(512);
    validateWrapAndUnwrap(subjectKey, wrapperKey(), dekInfoPattern());
  }

  @Test
  public void testWrapAndUnwrapPrivateKey() throws Exception {
    final PrivateKey subjectKey = KeyUtil.ecKeyPair().getPrivate();
    validateWrapAndUnwrap(subjectKey, wrapperKey(), dekInfoPattern());
  }

  @Test
  public void testUnwrapWithNoDekInfoHeader() throws Exception {
    expectedException.expect(KeyUnwrapException.class);
    expectedException.expectMessage("header is missing");
    operator().unwrap(KeyDescriptor.builder()
        .algorithm("DONTCARE")
        .type(KeyDescriptor.Type.SECRET)
        .build(new byte[1]), wrapperKey());
  }

  @Test
  public void testUnwrapWithInvalidDekInfoHeader() throws Exception {
    expectedException.expect(KeyUnwrapException.class);
    expectedException.expectMessage("header is invalid");
    operator().unwrap(KeyDescriptor.builder()
        .algorithm("DONTCARE")
        .type(KeyDescriptor.Type.SECRET)
        .metadata(AbstractKeyWrapOperator.DEK_INFO_HEADER, "INVALID VALUE")
        .build(new byte[1]), wrapperKey());
  }

  private void validateWrapAndUnwrap(Key subjectKey, Key wrapperKey,
      Pattern dekInfoPattern) throws KeyWrapException, KeyUnwrapException {

    final KeyDescriptor descriptor = operator().wrap(subjectKey, wrapperKey);

    assertThat(descriptor.getAlgorithm(),
        is(equalTo(subjectKey.getAlgorithm())));
    assertThat(descriptor.getType(),
        is(equalTo(KeyDescriptor.Type.typeOf(subjectKey))));

    assertThat(descriptor.getMetadata()
        .get(AbstractKeyWrapOperator.PROC_TYPE_HEADER),
            is(equalTo(AbstractKeyWrapOperator.PROC_TYPE_VALUE)));

    final String dekInfo =
        descriptor.getMetadata().get(AbstractKeyWrapOperator.DEK_INFO_HEADER);

    final Matcher matcher = dekInfoPattern.matcher(dekInfo);
    assertThat(matcher.matches(), is(true));

    final Key unwrappedKey = operator().unwrap(descriptor, wrapperKey);

    assertThat(unwrappedKey, is(equalTo(subjectKey)));
  }


}
