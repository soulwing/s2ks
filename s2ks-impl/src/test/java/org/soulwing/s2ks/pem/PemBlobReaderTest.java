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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.Test;
import org.soulwing.s2ks.KeyUtil;
import org.soulwing.s2ks.Blob;

/**
 * Unit tests for {@link PemBlobReader}.
 *
 * @author Carl Harris
 */
public class PemBlobReaderTest {

  @Test
  public void testRead() throws Exception {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final PemObject object = newObject();
    writeBlob(object, outputStream);
    final byte[] blobData = outputStream.toByteArray();

    final Blob blob = PemBlobReader.getInstance().read(
        new ByteArrayInputStream(blobData));

    validateBlob(object, blob);
  }

  @Test
  public void testReadAll() throws Exception {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final List<PemObject> objects = Arrays.asList(newObject(), newObject());

    for (final PemObject object : objects) {
      writeBlob(object, outputStream);
    }

    final byte[] blobData = outputStream.toByteArray();
    final List<Blob> blobs = PemBlobReader.getInstance().readAll(
        new ByteArrayInputStream(blobData));

    assertThat(blobs.size(), is(equalTo(objects.size())));
    for (int i = 0, max = blobs.size(); i < max; i++) {
      validateBlob(objects.get(i), blobs.get(i));
    }
  }

  private void validateBlob(PemObject object, Blob blob) {

    assertThat(blob, is(instanceOf(PemBlob.class)));

    final PemObject actual = ((PemBlob) blob).getDelegate();
    assertThat(actual.getType(), is(equalTo(object.getType())));
    assertThat(actual.getContent(), is(equalTo(object.getContent())));
  }

  private PemObject newObject() {
    return new PemObject("TYPE", KeyUtil.randomKeyData(128));
  }

  private void writeBlob(PemObject object, OutputStream outputStream)
      throws IOException {
    final PemWriter writer = new PemWriter(
        new OutputStreamWriter(outputStream, StandardCharsets.US_ASCII));

    writer.writeObject(object);
    writer.flush();
  }


}
