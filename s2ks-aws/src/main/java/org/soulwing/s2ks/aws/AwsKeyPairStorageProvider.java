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
package org.soulwing.s2ks.aws;

import java.util.Properties;

import org.soulwing.s2ks.KeyPairStorage;
import org.soulwing.s2ks.ProviderConfigurationException;
import org.soulwing.s2ks.bc.BcEncryptedPrivateKeyLoader;
import org.soulwing.s2ks.bc.BcPemCertificateLoader;
import org.soulwing.s2ks.spi.KeyPairStorageProvider;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;

/**
 * A {@link KeyPairStorageProvider} that provides storage on AWS.
 *
 * @author Carl Harris
 */
public class AwsKeyPairStorageProvider implements KeyPairStorageProvider {

  static final String PROVIDER_NAME = "AWS";

  static final String SECRET_ID = "secretId";
  static final String S3_BUCKET_NAME = "s3BucketName";
  static final String S3_PREFIX = "s3Prefix";

  @Override
  public String getName() {
    return PROVIDER_NAME;
  }

  @Override
  public KeyPairStorage getInstance(Properties configuration)
      throws ProviderConfigurationException {

    final String secretId = configuration.getProperty(SECRET_ID);
    final String bucketName = configuration.getProperty(S3_BUCKET_NAME);
    final String prefix = configuration.getProperty(S3_PREFIX, "");

    if (secretId == null) {
      throw new ProviderConfigurationException("must set AWS secret ID");
    }
    if (bucketName == null) {
      throw new ProviderConfigurationException("must set S3 bucket name");
    }

    return new AwsKeyPairStorage(
        BcEncryptedPrivateKeyLoader.getInstance(),
        BcPemCertificateLoader.getInstance(),
        AWSSecretsManagerClientBuilder.standard().build(),
        AmazonS3ClientBuilder.standard().build(),
        secretId,
        bucketName,
        prefix);
  }


}
