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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.BlobEncoder;

/**
 * A {@link BlobEncoder} that handles {@link PemBlob} blobs.
 *
 * @author Carl Harris
 */
public class PemBlobEncoder implements BlobEncoder {

  private static final PemBlobEncoder INSTANCE = new PemBlobEncoder();

  /**
   * Gets the singleton instance.
   * @return singleton instance
   */
  public static PemBlobEncoder getInstance() {
    return INSTANCE;
  }

  @Override
  public void encode(List<Blob> blobs, OutputStream outputStream)
      throws IOException {
    for (final Blob blob : blobs) {
      if (!(blob instanceof PemBlob)) {
        throw new IllegalArgumentException("requires PEM blobs");
      }
      blob.write(outputStream);
    }
  }

  @Override
  public List<Blob> decode(InputStream inputStream) throws IOException {
    final List<Blob> blobs = new ArrayList<>();
    final PemReader reader = new PemReader(
        new InputStreamReader(inputStream, StandardCharsets.US_ASCII));

    PemObject object = reader.readPemObject();
    while (object != null) {
      blobs.add(new PemBlob(object));
      object = reader.readPemObject();
    }
    return blobs;
  }

}
