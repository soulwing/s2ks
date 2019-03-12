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
package org.soulwing.s2ks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * An encoder that encodes blobs into blobs.
 *
 * @author Carl Harris
 */
public interface BlobEncoder {

  /**
   * Encodes a sequence of blobs onto the given output stream in order,
   * such that subsequent call to {@link #decode(InputStream)} will produce
   * the same sequence of blobs.
   * @param blobs the sequence of blobs to encode
   * @param outputStream the target output stream
   * @throws IOException if an error occurs in performing the encoding
   */
  void encode(List<Blob> blobs, OutputStream outputStream) throws IOException;

  /**
   * Decodes a blob into a sequence of blobs.
   * @param inputStream an input stream which may contain zero or more
   *    encoded blobs
   * @return list of blobs in the order in which they appeared in the input
   * @throws IOException if an error occurs in performing the decoding
   */
  List<Blob> decode(InputStream inputStream) throws IOException;

}
