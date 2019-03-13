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
package org.soulwing.s2ks.pbe;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.List;
import javax.crypto.SecretKey;

import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.BlobEncoder;
import org.soulwing.s2ks.base.KeyDescriptor;
import org.soulwing.s2ks.base.KeyEncoder;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.base.KeyWrapOperator;
import org.soulwing.s2ks.MutableKeyStorage;
import org.soulwing.s2ks.base.StorageService;
import org.soulwing.s2ks.base.AbstractMutableKeyStorage;
import org.soulwing.s2ks.base.WrapperKeyResponse;

/**
 * A {@link MutableKeyStorage} implementation that stores keys using
 * password-based encryption.
 *
 * @author Carl Harris
 */
public class PbeKeyStorage extends AbstractMutableKeyStorage {

  private final SecretKey pbeKey;
  private final StorageService storageService;

  public PbeKeyStorage(
      BlobEncoder blobEncoder,
      KeyEncoder keyEncoder,
      KeyWrapOperator keyWrapOperator,
      SecretKey pbeKey,
      StorageService storageService) {
    super(blobEncoder, keyEncoder, keyWrapOperator);
    this.pbeKey = pbeKey;
    this.storageService = storageService;
  }

  @Override
  protected String idToPath(String id, String suffix) {
    return storageService.idToPath(id, suffix);
  }

  @Override
  protected InputStream getContentStream(String path) throws IOException {
    return storageService.getContentStream(path);
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
  protected WrapperKeyResponse nextWrapperKey() {
    return WrapperKeyResponse.with(pbeKey);
  }

  @Override
  protected void storeContent(List<Blob> blobs, String path) throws IOException {
    storageService.storeContent(blobs, path);
  }

}
