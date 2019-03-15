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
import java.util.Arrays;
import java.util.Collections;

import org.bouncycastle.util.io.pem.PemObject;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.soulwing.s2ks.base.Blob;

/**
 * Unit tests for {@link PemMetadataRecognizer}.
 * @author Carl Harris
 */
public class PemMetadataRecognizerTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  private Blob otherBlob;

  private final PemMetadataRecognizer recognizer =
      PemMetadataRecognizer.getInstance();

  @Test
  public void testWithEmptyList() throws Exception {
    assertThat(recognizer.indexOfMetadata(Collections.emptyList()),
        is(equalTo(-1)));
  }

  @Test
  public void testWithRecognizableBlob() throws Exception {
    final Blob blob = PemMetadataEncoder.getInstance()
        .encode("metadata".getBytes(StandardCharsets.UTF_8));
    assertThat(recognizer.indexOfMetadata(Collections.singletonList(blob)),
        is(equalTo(0)));
  }

  @Test
  public void testWhenOtherPemBlobPresent() throws Exception {
    final Blob otherBlob = new PemBlob(new PemObject("OTHER OBJECT", new byte[1]));
    final Blob blob = PemMetadataEncoder.getInstance()
        .encode("metadata".getBytes(StandardCharsets.UTF_8));
    assertThat(recognizer.indexOfMetadata(Arrays.asList(otherBlob, blob)),
        is(equalTo(1)));
  }


  @Test
  public void testWhenNonPemBlobPresent() throws Exception {
    final Blob blob = PemMetadataEncoder.getInstance()
        .encode("metadata".getBytes(StandardCharsets.UTF_8));
    assertThat(recognizer.indexOfMetadata(Arrays.asList(otherBlob, blob)),
        is(equalTo(1)));
  }

}