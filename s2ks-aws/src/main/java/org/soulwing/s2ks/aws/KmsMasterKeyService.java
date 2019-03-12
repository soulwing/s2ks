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

import java.nio.ByteBuffer;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.soulwing.s2ks.EncryptionKey;
import org.soulwing.s2ks.KeyUnwrapException;
import org.soulwing.s2ks.KeyWrapException;
import org.soulwing.s2ks.MasterKeyService;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.AWSKMSException;
import com.amazonaws.services.kms.model.DataKeySpec;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.GenerateDataKeyRequest;
import com.amazonaws.services.kms.model.GenerateDataKeyResult;

/**
 * A {@link MasterKeyService} that delegates to the AWS KMS.
 *
 * @author Carl Harris
 */
class KmsMasterKeyService implements MasterKeyService {

  private final AWSKMS kmsClient;
  private final String masterKeyId;
  private final DataKeySpec dataKeySpec;

  public KmsMasterKeyService(AWSKMS kmsClient, String masterKeyId,
      DataKeySpec dataKeySpec) {
    this.kmsClient = kmsClient;
    this.masterKeyId = masterKeyId;
    this.dataKeySpec = dataKeySpec;
  }

  @Override
  public EncryptionKey newEncryptionKey() throws KeyWrapException {
    try {
      final GenerateDataKeyRequest keyRequest = new GenerateDataKeyRequest();
      keyRequest.setKeyId(masterKeyId);
      keyRequest.setKeySpec(dataKeySpec);
      final GenerateDataKeyResult result = kmsClient.generateDataKey(keyRequest);
      return new KmsEncryptionKey(toByteArray(result.getPlaintext()),
          toByteArray(result.getCiphertextBlob()), result.getKeyId());
    }
    catch (AWSKMSException ex) {
      throw new KeyWrapException(ex.toString(), ex);
    }
  }

  @Override
  public SecretKey decryptKey(byte[] cipherText) throws KeyUnwrapException {
    try {
      final DecryptRequest request = new DecryptRequest();
      request.setCiphertextBlob(ByteBuffer.wrap(cipherText));
      final DecryptResult result = kmsClient.decrypt(request);
      return new SecretKeySpec(toByteArray(result.getPlaintext()),
          KmsEncryptionKey.ALGORITHM);
    }
    catch (AWSKMSException ex) {
      throw new KeyUnwrapException(ex.toString(), ex);
    }
  }

  private static byte[] toByteArray(ByteBuffer content) {
    final byte[] buf = new byte[content.remaining()];
    content.get(buf);
    return buf;
  }

}
