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

import java.util.Properties;

import org.soulwing.s2ks.KeyStorage;
import org.soulwing.s2ks.aes.AesWrapOperator;
import org.soulwing.s2ks.base.MasterKeyService;
import org.soulwing.s2ks.base.StorageService;
import org.soulwing.s2ks.metadata.JwtMetadataWrapOperator;
import org.soulwing.s2ks.pem.PemBlobEncoder;
import org.soulwing.s2ks.pem.PemKeyEncoder;
import org.soulwing.s2ks.pem.PemMetadataEncoder;
import org.soulwing.s2ks.pem.PemMetadataRecognizer;
import org.soulwing.s2ks.spi.KeyStorageProvider;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DataKeySpec;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * A {@link KeyStorageProvider} that encrypts keys using AWS KMS and
 * store them using S3.
 *
 * @author Carl Harris
 */
public class AwsKeyStorageProvider implements KeyStorageProvider {

  static final DataKeySpec DEFAULT_DATA_KEY_SPEC = DataKeySpec.AES_256;

  static final String NAME = "AWS";
  static final String CREDENTIALS_PROVIDER = "credentialsProvider";
  static final String REGION = "region";
  static final String KMS_MASTER_KEY_ID = "kmsMasterKeyId";
  static final String KMS_DATA_KEY_SPEC = "kmsDataKeySpec";
  static final String S3_BUCKET_NAME = "s3BucketName";
  static final String S3_PREFIX = "s3Prefix";


  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isMutable() {
    return true;
  }

  @Override
  public KeyStorage getInstance(Properties properties) throws Exception {
    final AWSCredentialsProvider credentialsProvider =
        credentialsProvider(properties);
    final Regions region = getRegion(properties);

    return new AwsKeyStorage(PemBlobEncoder.getInstance(),
        AesWrapOperator.getInstance(), PemKeyEncoder.getInstance(),
        JwtMetadataWrapOperator.getInstance(), PemMetadataEncoder.getInstance(), PemMetadataRecognizer.getInstance(), newMasterKeyService(properties, credentialsProvider, region),
        newStorageService(properties, credentialsProvider, region));
  }

  private MasterKeyService newMasterKeyService(Properties properties,
      AWSCredentialsProvider credentialsProvider, Regions region) {
    final String masterKeyId =
        getRequiredProperty(KMS_MASTER_KEY_ID, properties);

    final DataKeySpec dataKeySpec = DataKeySpec.valueOf(
        properties.getProperty(KMS_DATA_KEY_SPEC,
            DEFAULT_DATA_KEY_SPEC.name()));

    final AWSKMS kmsClient = AWSKMSClientBuilder.standard()
        .withRegion(region)
        .withCredentials(credentialsProvider)
        .build();

    return new KmsMasterKeyService(kmsClient, masterKeyId, dataKeySpec);
  }

  private StorageService newStorageService(Properties properties,
      AWSCredentialsProvider credentialsProvider, Regions region) {

    final String bucketName =
        getRequiredProperty(S3_BUCKET_NAME, properties);
    final String prefix = properties.getProperty(S3_PREFIX, "");

    final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withCredentials(credentialsProvider)
        .build();

    return new S3StorageService(s3Client, bucketName, prefix,
        PemBlobEncoder.getInstance());
  }

  private AWSCredentialsProvider credentialsProvider(Properties properties) {
    final Object credentialsProvider = properties.get(CREDENTIALS_PROVIDER);
    if (credentialsProvider instanceof AWSCredentialsProvider) {
      return (AWSCredentialsProvider) credentialsProvider;
    }

    throw new IllegalArgumentException("the `" + CREDENTIALS_PROVIDER
        + "` property must hold an instance of "
        + AWSCredentialsProvider.class.getSimpleName());
  }

  private Regions getRegion(Properties properties) {
    return Regions.valueOf(getRequiredProperty(REGION, properties));
  }

  private String getRequiredProperty(String name, Properties properties) {
    final String value = properties.getProperty(name);
    if (value != null) return value;
    throw new IllegalArgumentException(
        "the `" + name + "` property is required");
  }

}
