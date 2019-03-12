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
package org.soulwing.s2ks.base;import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import org.soulwing.s2ks.Blob;
import org.soulwing.s2ks.BlobReader;
import org.soulwing.s2ks.KeyDescriptor;
import org.soulwing.s2ks.KeyEncoder;
import org.soulwing.s2ks.KeyStorage;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.KeyUnwrapException;
import org.soulwing.s2ks.KeyWrapOperator;
import org.soulwing.s2ks.NoSuchKeyException;

/**
 * An abstract base for {@link KeyStorage} implementations.
 * @author Carl Harris
 */
public abstract class AbstractKeyStorage implements KeyStorage {

  private final BlobReader blobReader;
  final KeyEncoder keyEncoder;
  final KeyWrapOperator keyWrapOperator;
  final String pathSuffix;

  protected AbstractKeyStorage(BlobReader blobReader, KeyEncoder keyEncoder,
      KeyWrapOperator keyWrapOperator, String pathSuffix) {
    this.blobReader = blobReader;
    this.keyEncoder = keyEncoder;
    this.keyWrapOperator = keyWrapOperator;
    this.pathSuffix = pathSuffix;
  }

  @Override
  public final Key retrieve(String id) throws KeyStorageException {
    final String path = idToPath(id, pathSuffix);
    try (final InputStream contentStream = getContentStream(path)) {
      final List<Blob> blobs = blobReader.readAll(contentStream);
      final List<KeyDescriptor> descriptors = toDescriptors(blobs);
      final Key wrapperKey = getWrapperKey(descriptors);
      return keyWrapOperator.unwrap(getSubjectKey(descriptors), wrapperKey);
    }
    catch (FileNotFoundException ex) {
      throw new NoSuchKeyException(id);
    }
    catch (IOException ex) {
      throw new KeyStorageException(ex.getMessage(), ex);
    }
  }

  /**
   * Decodes the given blobs to their corresponding descriptors.
   * @param blobs the subject blobs
   * @return corresponding descriptors
   * @throws KeyStorageException if thrown by the decoder
   */
  private List<KeyDescriptor> toDescriptors(List<Blob> blobs)
      throws KeyStorageException {
    final List<KeyDescriptor> descriptors = new ArrayList<>();
    for (final Blob blob : blobs) {
      descriptors.add(keyEncoder.decode(blob));
    }
    return descriptors;
  }

  /**
   * Transforms a key ID into a virtual path understood by the underlying
   * storage provider.
   * @param id the subject key ID
   * @param suffix suffix for path
   * @return id transformed to a path
   */
  protected abstract String idToPath(String id, String suffix);

  /**
   * Gets an input stream that can be used to obtain the contents of a key
   * at the given virtual path
   * @param path virtual path for the subject key
   * @return input stream
   * @throws IOException if an error occurs in opening the stream
   */
  protected abstract InputStream getContentStream(String path)
      throws IOException;

  /**
   * Gets the wrapper key to use to load the subject key from the specified
   * collection of blobs.
   * @param descriptors list of descriptors in the order in which they
   *    appeared in the content input stream
   * @return wrapper key
   * @throws KeyUnwrapException if the appropriate wrapper key cannot be
   *    determined from the blob collection
   * @throws KeyStorageException if some other error occurs in obtaining or
   *    deriving the key
   */
  protected abstract Key getWrapperKey(List<KeyDescriptor> descriptors)
      throws KeyUnwrapException, KeyStorageException;

  /**
   * Gets the blob that represents the subject key from the given collection
   * of descriptors.
   * @param descriptors list of blobs in the order in which they appeared in
   *    the content input stream
   * @return subject key blob
   * @throws KeyUnwrapException if the subject key blob cannot be determined
   * @throws KeyStorageException if some other error occurs in obtaining or
   *    deriving the key
   */
  protected abstract KeyDescriptor getSubjectKey(List<KeyDescriptor> descriptors)
      throws KeyUnwrapException, KeyStorageException;

}
