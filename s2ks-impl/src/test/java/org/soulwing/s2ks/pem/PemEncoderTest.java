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
package org.soulwing.s2ks.pem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.Collections;

import org.bouncycastle.util.io.pem.PemHeader;
import org.bouncycastle.util.io.pem.PemObject;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.soulwing.s2ks.KeyUtil;
import org.soulwing.s2ks.Blob;
import org.soulwing.s2ks.KeyDecodeException;
import org.soulwing.s2ks.KeyDescriptor;

/**
 * Unit tests for {@link PemEncoder}.
 *
 * @author Carl Harris
 */
public class PemEncoderTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  private Blob otherBlob;

  @Test
  public void testEncode() throws Exception {
    final byte[] data = KeyUtil.randomKeyData(246);

    final Blob blob = PemEncoder.getInstance().encode(
        KeyDescriptor.builder()
            .algorithm("ALG")
            .type(KeyDescriptor.Type.SECRET)
            .metadata("Name", "Value")
            .build(data));

    assertThat(blob, is(instanceOf(PemBlob.class)));

    final PemObject object = ((PemBlob) blob).getDelegate();
    assertThat(object.getType(), is(equalTo("ALG SECRET KEY")));
    assertThat(object.getHeaders().size(), is(equalTo(1)));
    final PemHeader header = (PemHeader) object.getHeaders().get(0);
    assertThat(header.getName(), is(equalTo("Name")));
    assertThat(header.getValue(), is(equalTo("Value")));
    assertThat(object.getContent(), is(equalTo(data)));
  }

  @Test
  public void testDecode() throws Exception {
    final String type = "ALG " + KeyDescriptor.Type.SECRET.name() + " KEY";
    final PemHeader header = new PemHeader("Name", "Value");
    final byte[] data = KeyUtil.randomKeyData(128);

    final PemObject object =
        new PemObject(type, Collections.singletonList(header), data);

    final KeyDescriptor descriptor = PemEncoder.getInstance()
        .decode(new PemBlob(object));

    assertThat(descriptor.getAlgorithm(), is(equalTo("ALG")));
    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.SECRET)));
    assertThat(descriptor.getMetadata().get("Name"), is(equalTo("Value")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDecodeWithNonPemBlob() throws Exception {
    PemEncoder.getInstance().decode(otherBlob);
  }

  @Test(expected = KeyDecodeException.class)
  public void testDecodeWithUnsupportedType() throws Exception {
    final PemObject object = new PemObject("SOME OTHER TYPE", new byte[0]);
    PemEncoder.getInstance().decode(new PemBlob(object));
  }


}
