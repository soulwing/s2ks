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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.soulwing.s2ks.Blob;
import org.soulwing.s2ks.BlobReader;

/**
 * A {@link BlobReader} that reads PEM-encoded blobs.
 *
 * @author Carl Harris
 */
public class PemBlobReader implements BlobReader {

  private static final PemBlobReader INSTANCE = new PemBlobReader();

  /**
   * Gets the singleton instance.
   * @return singleton instance
   */
  public static PemBlobReader getInstance() {
    return INSTANCE;
  }

  private PemBlobReader() { }

  @Override
  public Blob read(InputStream inputStream) throws IOException {
    final PemReader reader = new PemReader(
        new InputStreamReader(inputStream, StandardCharsets.US_ASCII));
    return Optional.ofNullable(reader.readPemObject())
        .map(PemBlob::new).orElse(null);
  }

  @Override
  public List<Blob> readAll(InputStream inputStream) throws IOException {
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
