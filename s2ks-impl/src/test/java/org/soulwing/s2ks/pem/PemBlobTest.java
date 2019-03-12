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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.junit.Test;
import org.soulwing.s2ks.KeyUtil;

/**
 * Unit tests of {@link PemBlob}.
 *
 * @author Carl Harris
 */
public class PemBlobTest {

  private byte[] DATA = KeyUtil.randomKeyData(128);

  private PemObject object = new PemObject("TYPE", DATA);

  private PemBlob blob = new PemBlob(object);

  @Test
  public void testSize() throws Exception {
    assertThat(blob.size(), is(greaterThan(object.getContent().length)));
  }

  @Test
  public void testGetContentType() throws Exception {
    assertThat(blob.getContentType(), is(equalTo(PemBlob.CONTENT_TYPE)));
  }

  @Test
  public void testGetDelegate() throws Exception {
    assertThat(blob.getDelegate(), is(sameInstance(object)));
  }

  @Test
  public void testGetContentStream() throws Exception {
    try (final InputStream inputStream = blob.getContentStream()) {
      assertThat(IOUtils.toByteArray(inputStream), is(equalTo(DATA)));
    }
  }

  @Test
  public void testWrite() throws Exception {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    blob.write(outputStream);

    final ByteArrayInputStream inputStream =
        new ByteArrayInputStream(outputStream.toByteArray());

    final PemReader reader = new PemReader(
        new InputStreamReader(inputStream, StandardCharsets.US_ASCII));

    final PemObject actual = reader.readPemObject();
    assertThat(actual.getType(), is(equalTo(object.getType())));
    assertThat(actual.getHeaders(), is(equalTo(object.getHeaders())));
    assertThat(actual.getContent(), is(equalTo(object.getContent())));
  }


}
