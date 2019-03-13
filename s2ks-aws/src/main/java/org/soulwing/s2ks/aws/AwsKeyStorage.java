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
package org.soulwing.s2ks.aws;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.List;

import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.BlobEncoder;
import org.soulwing.s2ks.base.EncryptionKey;
import org.soulwing.s2ks.base.KeyDescriptor;
import org.soulwing.s2ks.base.KeyEncoder;
import org.soulwing.s2ks.KeyUnwrapException;
import org.soulwing.s2ks.KeyWrapException;
import org.soulwing.s2ks.base.KeyWrapOperator;
import org.soulwing.s2ks.base.MasterKeyService;
import org.soulwing.s2ks.base.StorageService;
import org.soulwing.s2ks.base.AbstractMutableKeyStorage;
import org.soulwing.s2ks.base.WrapperKeyResponse;

/**
 * A {@link org.soulwing.s2ks.MutableKeyStorage} implementation that uses
 * the AWS KMS service as the source for wrapper keys and stores wrapped,
 * encoded keys in an S3 bucket.
 *
 * @author Carl Harris
 */
public class AwsKeyStorage extends AbstractMutableKeyStorage {

  static final String WRAPPER_KEY_ALGORITHM = "AWS";
  static final String MASTER_KEY_ID_HEADER = "Key-Id";

  private final MasterKeyService masterKeyService;
  private final StorageService storageService;

  public AwsKeyStorage(
      BlobEncoder blobEncoder,
      KeyEncoder keyEncoder,
      KeyWrapOperator keyWrapOperator,
      MasterKeyService masterKeyService, StorageService storageService) {
    super(blobEncoder, keyEncoder, keyWrapOperator);
    this.storageService = storageService;
    this.masterKeyService = masterKeyService;
  }

  @Override
  protected String idToPath(String id, String suffix) {
    return storageService.idToPath(id, suffix);
  }

  @Override
  protected InputStream getContentStream(String path)
      throws IOException {
    return storageService.getContentStream(path);
  }

  @Override
  protected Key getWrapperKey(List<KeyDescriptor> descriptors)
      throws KeyUnwrapException {
    return masterKeyService.decryptKey(findWrapperKey(descriptors).getKeyData());
  }

  private KeyDescriptor findWrapperKey(List<KeyDescriptor> descriptors)
      throws KeyUnwrapException {
    return descriptors.stream()
        .filter(d -> d.getAlgorithm().equals(WRAPPER_KEY_ALGORITHM))
        .findFirst()
        .orElseThrow(() ->
            new KeyUnwrapException("cannot find wrapper key descriptor"));
  }

  @Override
  protected KeyDescriptor getSubjectKey(List<KeyDescriptor> descriptors)
      throws KeyUnwrapException {
    return descriptors.stream()
        .filter(d -> !d.getAlgorithm().equals(WRAPPER_KEY_ALGORITHM))
        .findFirst()
        .orElseThrow(() ->
            new KeyUnwrapException("cannot find subject key descriptor"));
  }

  @Override
  protected WrapperKeyResponse nextWrapperKey() throws KeyWrapException {
    final EncryptionKey encryptionKey = masterKeyService.newEncryptionKey();
    final KeyDescriptor descriptor = KeyDescriptor.builder()
        .algorithm(WRAPPER_KEY_ALGORITHM)
        .type(KeyDescriptor.Type.SECRET)
        .metadata(MASTER_KEY_ID_HEADER, encryptionKey.getMasterKeyId())
        .build(encryptionKey.getCipherText());
    return WrapperKeyResponse.with(encryptionKey.getKey(), descriptor,
        r -> encryptionKey.destroy());
  }

  @Override
  protected void storeContent(List<Blob> blobs, String path) throws IOException {
    storageService.storeContent(blobs, path);
  }

}
