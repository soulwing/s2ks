/*
 * File created on Mar 30, 2019
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
package org.soulwing.s2ks.local;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.BlobEncoder;
import org.soulwing.s2ks.base.StorageService;
import org.soulwing.s2ks.pbe.PbeKeyStorage;

/**
 * A {@link StorageService} that uses the local filesystem.
 *
 * @author Carl Harris
 */
public class LocalStorageService implements StorageService {

  private static final Logger logger =
      LoggerFactory.getLogger(PbeKeyStorage.class);

  private final Path directory;
  private final BlobEncoder blobEncoder;

  public LocalStorageService(Path directory, BlobEncoder blobEncoder) {
    this.directory = directory;
    this.blobEncoder = blobEncoder;
  }

  @Override
  public String idToPath(String id, String suffix) {
    return directory.resolve(id + suffix).toString();
  }

  @Override
  public InputStream getContentStream(String path) throws IOException {
    logger.debug("retrieving key at path {}", path);
    return new FileInputStream(path);
  }

  @Override
  public void storeContent(List<Blob> blobs, String path) throws IOException {
    logger.debug("storing key at path {}", path);
    createParentIfNeeded(Paths.get(path));
    try (final OutputStream outputStream = new FileOutputStream(path)) {
      blobEncoder.encode(blobs, outputStream);
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
