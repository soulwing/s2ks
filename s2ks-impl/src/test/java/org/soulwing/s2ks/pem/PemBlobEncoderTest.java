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
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.util.io.pem.PemObject;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.KeyUtil;

/**
 * Unit tests for {@link PemBlobEncoder}.
 *
 * @author Carl Harris
 */
public class PemBlobEncoderTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  private Blob otherBlob;

  @Test
  public void testEncodeAndDecode() throws Exception {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    final List<Blob> blobs = Arrays.asList(newBlob(), newBlob());

    PemBlobEncoder.getInstance().encode(blobs, outputStream);

    final List<Blob> actual = PemBlobEncoder.getInstance().decode(
        new ByteArrayInputStream(outputStream.toByteArray()));

    assertThat(blobs.size(), is(equalTo(blobs.size())));
    for (int i = 0, max = blobs.size(); i < max; i++) {
      validateBlob(actual.get(i), blobs.get(i));
    }
  }

  private void validateBlob(Blob actual, Blob expected) {
    final PemObject pemActual = ((PemBlob) actual).getDelegate();
    final PemObject pemExpected = ((PemBlob) expected).getDelegate();
    assertThat(pemActual.getType(), is(equalTo(pemExpected.getType())));
    assertThat(pemActual.getContent(), is(equalTo(pemExpected.getContent())));
  }

  private PemBlob newBlob() {
    return new PemBlob(new PemObject("TYPE", KeyUtil.randomKeyData(128)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEncodeWithNonPemBlob() throws Exception {
    PemBlobEncoder.getInstance().encode(Collections.singletonList(otherBlob),
        null);
  }

}
