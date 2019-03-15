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
package org.soulwing.s2ks.base;

/**
 * An operator that performs metadata encoding and decoding operations.
 *
 * @author Carl Harris
 */
public interface MetadataEncoder {

  /**
   * Encodes the information in the given wrapped metadata as an opaque blob.
   * @param metadata wrapped metadata to be encoded
   * @return the resulting blob
   * @throws EncodingException if an error occurs in encoding the key;
   *    typically wraps a checked exception from an underlying provider
   */
  Blob encode(byte[] metadata) throws EncodingException;

  /**
   * Decodes a blob representation of a key to recover a descriptor for the
   * key it represents.
   * @param blob opaque blob representation of the key to decode
   * @return key descriptor
   * @throws DecodingException if an error occurs in decoding the key;
   *    typically wraps a checked exception from an underlying provider
   */
  byte[] decode(Blob blob) throws DecodingException;



}
