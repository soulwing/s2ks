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
import java.util.ArrayList;
import java.util.List;

import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.KeyWithMetadata;
import org.soulwing.s2ks.KeyWrapException;
import org.soulwing.s2ks.MutableKeyStorage;
import org.soulwing.s2ks.SimpleMetadata;

/**
 * An abstract base for {@link MutableKeyStorage} implementations.
 * @author Carl Harris
 */
public abstract class AbstractMutableKeyStorage extends AbstractKeyStorage
    implements MutableKeyStorage {

  protected AbstractMutableKeyStorage(
      BlobEncoder blobEncoder,
      KeyWrapOperator keyWrapOperator,
      KeyEncoder keyEncoder,
      MetadataWrapOperator metadataWrapOperator,
      MetadataEncoder metadataEncoder,
      MetadataRecognizer metadataRecognizer) {
    super(blobEncoder, keyWrapOperator, keyEncoder, metadataWrapOperator,
        metadataRecognizer, metadataEncoder);
  }

  @Override
  public final void store(String id, Key key) throws KeyStorageException {
    store(id, new KeyWithMetadata(key, SimpleMetadata.empty()));
  }

  @Override
  public void store(String id, KeyWithMetadata keyWithMetadata)
      throws KeyStorageException {
    final String path = idToPath(id, keyEncoder.getPathSuffix());
    final WrapperKeyResponse response = nextWrapperKey();
    final KeyDescriptor descriptor =
        keyWrapOperator.wrap(keyWithMetadata.getKey(), response.getKey());

    final List<Blob> blobs = new ArrayList<>();
    if (response.getDescriptor() != null) {
      blobs.add(keyEncoder.encode(response.getDescriptor()));
    }
    blobs.add(keyEncoder.encode(descriptor));
    if (!keyWithMetadata.getMetadata().isEmpty()) {
      blobs.add(metadataEncoder.encode(
          metadataWrapOperator.wrap(keyWithMetadata)));
    }
    try {
      storeContent(blobs, path);
    }
    catch (IOException ex) {
      throw new KeyStorageException(ex.getMessage(), ex);
    }
    finally {
      response.destroy();
    }
  }

  /**
   * Gets the next key to be used to wrap a subject key.
   * <p>
   * Values returned are not retained, so an implementation may safely assume
   * that it may return an arbitrary key for any invocation, allowing the
   * subclass to implement any rotation policy.
   * <p>
   * The response object contains the wrapper key, and may optionally contain
   * a key descriptor for the key, which will be encoded and written to storage
   * such that it appears before the blob for the subject key.
   *
   * @return wrapper key response
   * @throws KeyWrapException if the next wrapper key cannot be derived
   */
  protected abstract WrapperKeyResponse nextWrapperKey() throws KeyWrapException;

  /**
   * Stores the contents of the given blobs at the location identified by
   * the given path, overwriting any existing content at that path.
   * <p>
   * The blobs are to be concatenated in the output in the order in which they
   * appear in the list.
   * @param blobs the blobs to be stored
   * @param path virtual path identifying the storage location
   * @throws IOException if an error occurs in storing the blob content
   */
  protected abstract void storeContent(List<Blob> blobs, String path)
      throws IOException;

}
