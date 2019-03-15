/*
 * File created on Mar 15, 2019
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
import static org.hamcrest.Matchers.is;

import java.nio.charset.StandardCharsets;

import org.bouncycastle.util.io.pem.PemObject;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.DecodingException;

/**
 * Unit tests for {@link PemMetadataEncoder}.
 *
 * @author Carl Harris
 */
public class PemMetadataEncoderTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  private Blob blob;

  private final PemMetadataEncoder encoder = PemMetadataEncoder.getInstance();

  @Test
  public void testEncodeAndDecode() throws Exception {
    final byte[] expected = "metadata".getBytes(StandardCharsets.UTF_8);
    final byte[] actual = encoder.decode(encoder.encode(expected));
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithNonPemBlob() throws Exception {
    encoder.decode(blob);
  }

  @Test(expected = DecodingException.class)
  public void testWithNonMetadataObject() throws Exception {
    final PemObject object =
        new PemObject(PemMetadataRecognizer.TYPE + " OTHER", new byte[1]);
    encoder.decode(new PemBlob(object));
  }

}