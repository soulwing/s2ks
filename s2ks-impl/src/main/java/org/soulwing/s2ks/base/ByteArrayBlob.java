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
package org.soulwing.s2ks.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.soulwing.s2ks.Blob;

/**
 * A simple blob backed by a byte array.
 *
 * @author Carl Harris
 */
public class ByteArrayBlob implements Blob {

  private final byte[] content;
  private final String contentType;

  public ByteArrayBlob(byte[] content, String contentType) {
    this.content = content;
    this.contentType = contentType;
  }

  @Override
  public int size() {
    return content.length;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public InputStream getContentStream() throws IOException {
    return new ByteArrayInputStream(content);
  }

  @Override
  public void write(OutputStream outputStream) throws IOException {
    outputStream.write(content);
  }

}
