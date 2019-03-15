/*
 * File created on Mar 14, 2019
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

import java.util.List;

import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.MetadataRecognizer;

/**
 * A {@link MetadataRecognizer} that recognizes PEM-encoded metadata
 * @author Carl Harris
 */
public class PemMetadataRecognizer implements MetadataRecognizer {

  private static final PemMetadataRecognizer INSTANCE =
      new PemMetadataRecognizer();

  static final String TYPE = "SIGNED METADATA";

  /**
   * Gets the singleton instance.
   * @return singleton instance
   */
  public static PemMetadataRecognizer getInstance() {
    return INSTANCE;
  }

  private PemMetadataRecognizer() { }

  @Override
  public int indexOfMetadata(List<Blob> blobs) {
    for (int index = 0, max = blobs.size(); index < max; index++) {
      final Blob blob = blobs.get(index);
      if (!(blob instanceof PemBlob)) continue;
      if (TYPE.equals(((PemBlob) blob).getDelegate().getType())) return index;
    }
    return -1;
  }

}
