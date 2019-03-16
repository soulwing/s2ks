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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.InputStream;
import java.nio.file.Paths;
import java.security.Key;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.soulwing.s2ks.KeyStorageLocator;
import org.soulwing.s2ks.KeyWithMetadata;
import org.soulwing.s2ks.Metadata;
import org.soulwing.s2ks.MutableKeyStorage;
import org.soulwing.s2ks.NoSuchKeyException;
import org.soulwing.s2ks.SimpleMetadata;
import org.soulwing.s2ks.base.AbstractKeyWrapOperator;
import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.KeyDescriptor;
import org.soulwing.s2ks.pem.PemBlobEncoder;
import org.soulwing.s2ks.pem.PemKeyEncoder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Tests for the {@link AwsKeyStorageProvider}.
 *
 * @author Carl Harris
 */
public class AwsKeyStorageProviderTest {

  private static final String PREFIX = "s2ks";

  @Rule
  public final AwsProfileAvailableRule profileAvailableRule =
      new AwsProfileAvailableRule();

  private final String kmsMasterKeyId =
      System.getenv(AwsProfileAvailableRule.KMS_MASTER_KEY);

  private final String s3BucketName =
      System.getenv(AwsProfileAvailableRule.S3_BUCKET_NAME);

  private AmazonS3 s3Client;

  @Before
  public void setUp() throws Exception {
    s3Client = AmazonS3ClientBuilder.standard().build();
  }

  @Test
  public void testStoreAndRetrieveAesKey() throws Exception {
    final SecretKey key = KeyUtil.aesKey(256);
    final KeyDescriptor descriptor = validateStoreAndRetrieve(
        getStorageInstance(getProviderProperties()), key);

    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.SECRET)));
    assertThat(descriptor.getAlgorithm(), is(equalTo("AES")));
    validateKeyDescriptorMetadata(descriptor);

    // since the byte array of a AES key spec is directly encoded, we
    // can use the blob content to try to create a key spec; if the original
    // subject key was properly encrypted, a key created from the blob content
    // shouldn't be the same as the original key

    final SecretKey contentKey = new SecretKeySpec(descriptor.getKeyData(), "AES");
    assertThat(contentKey, is(not(equalTo(key))));

  }

  @Test
  public void testStoreAndRetrieveEcPrivateKey() throws Exception {
    final KeyDescriptor descriptor =
        validateStoreAndRetrieve(getStorageInstance(getProviderProperties()),
            KeyUtil.ecKeyPair().getPrivate());

    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.PRIVATE)));
    assertThat(descriptor.getAlgorithm(), is(equalTo("EC")));
    validateKeyDescriptorMetadata(descriptor);
  }

  @Test
  public void testStoreAndRetrieveRsaPrivateKey() throws Exception {
    final KeyDescriptor descriptor =
        validateStoreAndRetrieve(getStorageInstance(getProviderProperties()),
            KeyUtil.rsaKeyPair().getPrivate());

    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.PRIVATE)));
    assertThat(descriptor.getAlgorithm(), is(equalTo("RSA")));
    validateKeyDescriptorMetadata(descriptor);
  }

  @Test(expected = NoSuchKeyException.class)
  public void testRetrieveWhenNotFound() throws Exception {
    getStorageInstance(getProviderProperties()).retrieve(UUID.randomUUID().toString());
  }

  private MutableKeyStorage getStorageInstance(Properties properties) throws Exception {
    return KeyStorageLocator.getMutableInstance(
        AwsKeyStorageProvider.NAME, properties);
  }

  private Properties getProviderProperties() {
    final Properties properties = new Properties();
    properties.setProperty(AwsKeyStorageProvider.KMS_MASTER_KEY_ID, kmsMasterKeyId);
    properties.setProperty(AwsKeyStorageProvider.S3_BUCKET_NAME, s3BucketName);
    properties.setProperty(AwsKeyStorageProvider.S3_PREFIX, PREFIX);
    return properties;
  }

  private void validateKeyDescriptorMetadata(KeyDescriptor descriptor) {
    assertThat(descriptor.getMetadata()
            .get(AbstractKeyWrapOperator.PROC_TYPE_HEADER),
        is(equalTo(AbstractKeyWrapOperator.PROC_TYPE_VALUE)));

    assertThat(descriptor.getMetadata()
            .get(AbstractKeyWrapOperator.DEK_INFO_HEADER),
        is(not(nullValue())));
  }

  private KeyDescriptor validateStoreAndRetrieve(
      MutableKeyStorage storage, Key key) throws Exception {
    final String id = UUID.randomUUID().toString();
    final Metadata metadata = SimpleMetadata.builder().set("name", "value").build();
    storage.store(id, new KeyWithMetadata(key, metadata));

    final KeyWithMetadata retrieved = storage.retrieveWithMetadata(id);
    assertThat(retrieved.getKey(), is(equalTo(key)));
    assertThat(retrieved.getMetadata(), is(equalTo(metadata)));

    final String path = Paths.get(PREFIX,
        id + PemKeyEncoder.getInstance().getPathSuffix()).toString();

    final S3Object object = s3Client.getObject(s3BucketName, path);

    final List<Blob> blobs;
    try (final InputStream inputStream = object.getObjectContent()) {
      blobs = PemBlobEncoder.getInstance().decode(inputStream);
    }

    assertThat(blobs.size(), is(greaterThanOrEqualTo(2)));
    return PemKeyEncoder.getInstance().decode(blobs.get(1));
  }

}
