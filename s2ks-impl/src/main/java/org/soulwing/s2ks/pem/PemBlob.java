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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.soulwing.s2ks.Blob;

/**
 * A {@link Blob} that holds a {@link PemObject}.
 *
 * @author Carl Harris
 */
class PemBlob implements Blob {

  private final PemObject delegate;

  PemBlob(PemObject delegate) {
    this.delegate = delegate;
  }

  PemObject getDelegate() {
    return delegate;
  }

  @Override
  public InputStream getContentStream() {
    return new ByteArrayInputStream(delegate.getContent());
  }

  @Override
  public void write(OutputStream outputStream) throws IOException {
    final PemWriter writer = new PemWriter(
        new OutputStreamWriter(outputStream, StandardCharsets.US_ASCII));
    writer.writeObject(delegate);
    writer.flush();
  }

}
