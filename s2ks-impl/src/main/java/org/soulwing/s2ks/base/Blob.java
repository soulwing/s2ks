/*
 * File created on Mar 13, 2019
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An opaque byte-encoded object.
 *
 * @author Carl Harris
 */
public interface Blob {

  /**
   * Gets the size of this blob.
   * @return blob size in bytes
   */
  int size();

  /**
   * Gets the MIME media type that best describes the content of this blob.
   * @return MIME media type (e.g. {@code application/octet-stream})
   */
  String getContentType();

  /**
   * Gets an input stream that can be used to retrieve the contents
   * of this blob.
   * @return input streams
   * @throws IOException
   */
  InputStream getContentStream() throws IOException;

  /**
   * Writes the contents of this blob to the given output stream.
   * <p>
   * After writing the stream will be flushed, but will not be closed.
   *
   * @param outputStream the target output stream
   * @throws IOException if an error occurs in writing the stream
   */
  void write(OutputStream outputStream) throws IOException;

}
