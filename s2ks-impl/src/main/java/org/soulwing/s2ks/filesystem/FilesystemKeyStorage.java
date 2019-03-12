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
package org.soulwing.s2ks.filesystem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soulwing.s2ks.MutableKeyStorage;
import org.soulwing.s2ks.base.AbstractMutableKeyStorage;
import org.soulwing.s2ks.Blob;
import org.soulwing.s2ks.BlobReader;
import org.soulwing.s2ks.KeyDescriptor;
import org.soulwing.s2ks.KeyEncoder;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.KeyWrapOperator;

/**
 * A {@link MutableKeyStorage} implementation that stores
 * keys on the host filesystem.
 *
 * @author Carl Harris
 */
public class FilesystemKeyStorage extends AbstractMutableKeyStorage {

  private static final Logger logger =
      LoggerFactory.getLogger(FilesystemKeyStorage.class);

  private final SecretKey pbeKey;
  private final Path directory;

  public FilesystemKeyStorage(
      BlobReader blobReader,
      KeyEncoder keyEncoder,
      KeyWrapOperator keyWrapOperator,
      SecretKey pbeKey, Path directory,
      String pathSuffix) {
    super(blobReader, keyEncoder, keyWrapOperator, pathSuffix);
    this.directory = directory;
    this.pbeKey = pbeKey;
  }

  @Override
  protected String idToPath(String id, String suffix) {
    return directory.resolve(id + suffix).toString();
  }

  @Override
  protected InputStream getContentStream(String path) throws IOException {
    logger.debug("retrieving key at path {}", path);
    return new FileInputStream(path);
  }

  @Override
  protected Key getWrapperKey(List<KeyDescriptor> descriptors) {
    return pbeKey;
  }

  @Override
  protected KeyDescriptor getSubjectKey(List<KeyDescriptor> descriptors)
      throws KeyStorageException {
    if (descriptors.size() != 1) {
      throw new KeyStorageException(
          "requires exactly one key descriptor; got " + descriptors.size());
    }
    return descriptors.get(0);
  }

  @Override
  protected Key nextWrapperKey() {
    return pbeKey;
  }

  @Override
  protected void storeContent(Blob blob, String path) throws IOException {
    logger.debug("storing key at path {}", path);
    createParentIfNeeded(Paths.get(path));
    try (final OutputStream outputStream = new FileOutputStream(path)) {
      blob.write(outputStream);
    }
  }

  /**
   * Recursively creates the parent directory for the given path.
   * @param path the subject path
   * @throws IOException if thrown by when creating a directory
   */
  private void createParentIfNeeded(Path path) throws IOException {
    final Path parent = path.getParent();
    if (parent != null && !Files.exists(parent)) {
      createParentIfNeeded(parent);
      Files.createDirectory(parent);
      logger.debug("created directory {}", parent);
    }
  }

}
