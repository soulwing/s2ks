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

import org.bouncycastle.util.io.pem.PemObject;
import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.DecodingException;
import org.soulwing.s2ks.base.MetadataEncoder;

/**
 * A {@link MetadataEncoder} for PEM encoding.
 *
 * @author Carl Harris
 */
public class PemMetadataEncoder implements MetadataEncoder {

  private static final PemMetadataEncoder INSTANCE = new PemMetadataEncoder();

  /**
   * Gets the singleton instance.
   * @return encoder instance
   */
  public static PemMetadataEncoder getInstance() {
    return INSTANCE;
  }

  private PemMetadataEncoder() { }

  @Override
  public Blob encode(byte[] metadata) {
    return new PemBlob(new PemObject(PemMetadataRecognizer.TYPE, metadata));
  }

  @Override
  public byte[] decode(Blob blob) throws DecodingException {
    if (!(blob instanceof PemBlob)) {
      throw new IllegalArgumentException("requires a PEM blob");
    }
    final PemObject object = ((PemBlob) blob).getDelegate();

    if (!object.getType().equals(PemMetadataRecognizer.TYPE)) {
      throw new DecodingException("`" + object.getType() +
          "` is not a supported PEM object type");
    }

    return object.getContent();
  }

}
