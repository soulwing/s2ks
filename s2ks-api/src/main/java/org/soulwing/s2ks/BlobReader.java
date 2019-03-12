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
import java.util.List;

/**
 * A reader for stored {@link Blob} objects.
 *
 * @author Carl Harris
 */
public interface BlobReader {

  /**
   * Reads a single blob from the given input stream.
   * <p>
   * The input stream remains open when this method returns.
   *
   * @param inputStream the source input stream
   * @return blob or {@code null} if the stream contains no recognizable blob.
   * @throws IOException if an error occurs in reading the stream
   */
  Blob read(InputStream inputStream) throws IOException;

  /**
   * Reads all blobs that appear on the given input stream.
   * <p>
   * The input stream remains open when this method returns.
   *
   * @param inputStream the source input stream
   * @return list of blobs; will be empty if the stream contains no
   *    recognizable blob
   * @throws IOException if an error occurs in reading the stream
   */
  List<Blob> readAll(InputStream inputStream) throws IOException;

}
