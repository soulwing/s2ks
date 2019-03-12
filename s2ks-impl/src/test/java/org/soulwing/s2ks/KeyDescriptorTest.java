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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

import java.security.Key;
import javax.crypto.Cipher;

import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link KeyDescriptor}.
 *
 * @author Carl Harris
 */
public class KeyDescriptorTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Mock
  private Key key;

  @Test
  public void testBuildAndInspectProperties() throws Exception {
    final byte[] data = KeyUtil.randomKeyData(256);

    final KeyDescriptor descriptor = KeyDescriptor.builder()
        .algorithm("NAME")
        .type(KeyDescriptor.Type.SECRET)
        .metadata("name1", "value1")
        .metadata("name2", "value2")
        .build(data);

    assertThat(descriptor.getAlgorithm(), is(equalTo("NAME")));
    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.SECRET)));
    assertThat(descriptor.getKeyData(), is(not(sameInstance(data))));
    assertThat(descriptor.getKeyData(), is(equalTo(data)));
    assertThat(descriptor.getMetadata().get("name1"), is(equalTo("value1")));
    assertThat(descriptor.getMetadata().get("name2"), is(equalTo("value2")));
  }

  @Test
  public void testWhenAlgorithmIsNull() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("algorithm");
    KeyDescriptor.builder()
        .type(KeyDescriptor.Type.SECRET)
        .build(new byte[1]);
  }

  @Test
  public void testWhenAlgorithmIsBlank() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("algorithm");
    KeyDescriptor.builder()
        .algorithm(" ")
        .type(KeyDescriptor.Type.SECRET)
        .build(new byte[1]);
  }

  @Test
  public void testWhenTypeIsNull() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("type");
    KeyDescriptor.builder()
        .algorithm("NAME")
        .build(new byte[1]);
  }

  @Test
  public void testWhenKeyDataIsNull() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("key");
    KeyDescriptor.builder()
        .algorithm("NAME")
        .type(KeyDescriptor.Type.SECRET)
        .build(null);
  }

  @Test
  public void testWhenKeyDataIsEmpty() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("key");
    KeyDescriptor.builder()
        .algorithm("NAME")
        .type(KeyDescriptor.Type.SECRET)
        .build(new byte[0]);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMetadataImmutable() throws Exception {
    KeyDescriptor.builder()
        .algorithm("NAME")
        .type(KeyDescriptor.Type.SECRET)
        .build(new byte[1])
        .getMetadata()
        .put("name", "value");
  }

  @Test
  public void testTypeOf() throws Exception {
    assertThat(KeyDescriptor.Type.typeOf(KeyUtil.aesKey(245)),
        is(equalTo(KeyDescriptor.Type.SECRET)));

    assertThat(KeyDescriptor.Type.typeOf(
        KeyUtil.rsaKeyPair().getPrivate()),
            is(equalTo(KeyDescriptor.Type.PRIVATE)));

    assertThat(KeyDescriptor.Type.typeOf(
        KeyUtil.rsaKeyPair().getPublic()),
            is(equalTo(KeyDescriptor.Type.PUBLIC)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTypeOfUnrecognizedKeyType() throws Exception {
    KeyDescriptor.Type.typeOf(key);
  }

  @Test
  public void testTypeCipherKeyType() throws Exception {
    assertThat(KeyDescriptor.Type.SECRET.getCipherKeyType(),
        is(equalTo(Cipher.SECRET_KEY)));
    assertThat(KeyDescriptor.Type.PRIVATE.getCipherKeyType(),
        is(equalTo(Cipher.PRIVATE_KEY)));
    assertThat(KeyDescriptor.Type.PUBLIC.getCipherKeyType(),
        is(equalTo(Cipher.PUBLIC_KEY)));
  }

}
