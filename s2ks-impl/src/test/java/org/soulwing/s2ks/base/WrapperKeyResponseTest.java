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
package org.soulwing.s2ks.base;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.security.Key;
import java.util.function.Consumer;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.soulwing.s2ks.KeyDescriptor;
import org.soulwing.s2ks.KeyUtil;

/**
 * Unit tests for {@link WrapperKeyResponse}.
 *
 * @author Carl Harris
 */
public class WrapperKeyResponseTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  private Consumer<WrapperKeyResponse> destroyHook;

  @Test
  public void testWithDestroyHook() throws Exception {
    final Key key = KeyUtil.aesKey(256);
    final KeyDescriptor descriptor = KeyDescriptor.builder()
        .algorithm("ALGORITHM")
        .type(KeyDescriptor.Type.SECRET)
        .build(key.getEncoded());

    final WrapperKeyResponse response =
        WrapperKeyResponse.with(key, descriptor, destroyHook);

    assertThat(response.getKey(), is(sameInstance(key)));
    assertThat(response.getDescriptor(), is(sameInstance(descriptor)));

    context.checking(new Expectations() {
      {
        oneOf(destroyHook).accept(with(sameInstance(response)));
      }
    });

    response.destroy();
  }

  @Test
  public void testWithNoDestroyHook() throws Exception {
    final Key key = KeyUtil.aesKey(256);
    final KeyDescriptor descriptor = KeyDescriptor.builder()
        .algorithm("ALGORITHM")
        .type(KeyDescriptor.Type.SECRET)
        .build(key.getEncoded());

    final WrapperKeyResponse response =
        WrapperKeyResponse.with(key, descriptor);

    assertThat(response.getKey(), is(sameInstance(key)));
    assertThat(response.getDescriptor(), is(sameInstance(descriptor)));
    response.destroy();
  }

  @Test
  public void testWithNoDescriptor() throws Exception {
    final Key key = KeyUtil.aesKey(256);

    final WrapperKeyResponse response =
        WrapperKeyResponse.with(key);

    assertThat(response.getKey(), is(sameInstance(key)));
    assertThat(response.getDescriptor(), is(nullValue()));
    response.destroy();
  }

}