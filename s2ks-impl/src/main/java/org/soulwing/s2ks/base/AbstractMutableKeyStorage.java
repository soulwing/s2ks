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
package org.soulwing.s2ks.base;import java.io.IOException;
import java.security.Key;

import org.soulwing.s2ks.Blob;
import org.soulwing.s2ks.BlobReader;
import org.soulwing.s2ks.KeyDescriptor;
import org.soulwing.s2ks.KeyEncoder;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.KeyWrapException;
import org.soulwing.s2ks.KeyWrapOperator;
import org.soulwing.s2ks.MutableKeyStorage;

/**
 * An abstract base for {@link MutableKeyStorage} implementations.
 * @author Carl Harris
 */
public abstract class AbstractMutableKeyStorage extends AbstractKeyStorage
    implements MutableKeyStorage {

  protected AbstractMutableKeyStorage(
      BlobReader blobReader,
      KeyEncoder keyEncoder,
      KeyWrapOperator keyWrapOperator,
      String pathSuffix) {
    super(blobReader, keyEncoder, keyWrapOperator, pathSuffix);
  }

  @Override
  public final void store(String id, Key key) throws KeyStorageException {
    final String path = idToPath(id, pathSuffix);
    final KeyDescriptor descriptor = keyWrapOperator.wrap(key, nextWrapperKey());
    final Blob blob = keyEncoder.encode(descriptor);
    try {
      storeContent(blob, path);
    }
    catch (IOException ex) {
      throw new KeyStorageException(ex.getMessage(), ex);
    }
  }

  /**
   * Gets the next key to be used to wrap a subject key.
   * <p>
   * Values returned are not retained, so an implementation may safely assume
   * that it may return an arbitrary key for any invocation, allowing the
   * subclass to implement any rotation policy.
   * @return wrapper key
   * @throws KeyWrapException if the next wrapper key cannot be derived
   */
  protected abstract Key nextWrapperKey() throws KeyWrapException;

  /**
   * Stores the contents of the given blob at the location identified by
   * the given path, overwriting any existing content at that path.
   * @param blob the blob to be stored
   * @param path virtual path identifying the storage location
   * @throws IOException
   */
  protected abstract void storeContent(Blob blob, String path)
      throws IOException;

}
