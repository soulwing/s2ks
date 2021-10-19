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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Paths;
import javax.json.Json;
import javax.json.JsonObject;

import org.soulwing.s2ks.KeyPairStorage;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.base.AbstractKeyPairStorage;
import org.soulwing.s2ks.base.CertificateLoader;
import org.soulwing.s2ks.base.PrivateKeyLoader;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;

/**
 * A {@link org.soulwing.s2ks.KeyPairStorage} implementation that uses AWS S3 and the AWS
 * Secrets manager.
 *
 * @author Carl Harris
 */
class AwsKeyPairStorage extends AbstractKeyPairStorage {

  static final String PASSWORD_KEY = "password";

  private final AWSSecretsManager secretsClient;
  private final AmazonS3 s3Client;

  private final String secretId;
  private final String bucketName;
  private final String prefix;

  AwsKeyPairStorage(
      PrivateKeyLoader privateKeyLoader,
      CertificateLoader certificateLoader,
      AWSSecretsManager secretsClient,
      AmazonS3 s3Client,
      String secretId,
      String bucketName,
      String prefix) {
    super(privateKeyLoader, certificateLoader);
    this.secretsClient = secretsClient;
    this.s3Client = s3Client;
    this.secretId = secretId;
    this.bucketName = bucketName;
    this.prefix = prefix;
  }

  @Override
  protected char[] getPassword(String id) throws KeyStorageException {
    final GetSecretValueResult result = getSecretValue();

    // FIXME: using a JSON-P reader puts the password onto the heap in a string
    // which cannot be easily evicted; need to make a stream parser for JSON
    // that allows string values to be copied into a character array which
    // can be subsequently wiped

    final JsonObject secret =
        Json.createReader(new StringReader(result.getSecretString())).readObject();

    return getPassword(id, secret);
  }

  private char[] getPassword(String id, JsonObject secret)
      throws KeyStorageException {
    // if there's a secret for the given key ID, use it
    final JsonObject keySecret = secret.getJsonObject(id);
    if (keySecret != null) {
      secret = keySecret;
    }
    final String password = secret.getString(PASSWORD_KEY, null);
    if (password == null) {
      throw new KeyStorageException("secret for key `" + id
          + "` does not contain `" + PASSWORD_KEY + "` key");
    }
    return password.toCharArray();
  }

  /**
   * Gets the value of the AWS secret.
   * @return AWS secret value for the specified {@link #secretId}
   * @throws KeyStorageException if an error occurs in retrieving the secret
   */
  private GetSecretValueResult getSecretValue() throws KeyStorageException {
    final GetSecretValueRequest request = new GetSecretValueRequest();
    request.setSecretId(secretId);
    try {
      return secretsClient.getSecretValue(request);
    }
    catch (AmazonClientException ex) {
      throw new KeyStorageException(ex);
    }
  }

  @Override
  protected InputStream openPrivateKeyStream(String id)
      throws KeyStorageException, IOException {
    return openStreamToS3Object(
        Paths.get(prefix, id, KeyPairStorage.KEY_FILE_NAME).toString());
  }

  @Override
  protected InputStream openCertificateStream(String id)
      throws KeyStorageException, IOException {
    return openStreamToS3Object(
        Paths.get(prefix, id, KeyPairStorage.CERT_FILE_NAME).toString());
  }

  @Override
  protected InputStream openCACertificateStream(String id)
      throws KeyStorageException, IOException {
    return openStreamToS3Object(
        Paths.get(prefix, id, KeyPairStorage.CA_FILE_NAME).toString());
  }

  private InputStream openStreamToS3Object(String path)
      throws KeyStorageException, IOException {
    try {
      final S3Object object = s3Client.getObject(bucketName, path);
      return object.getObjectContent();
    }
    catch (AmazonS3Exception ex) {
      if ("NoSuchKey".equals(ex.getErrorCode())) {
        throw new FileNotFoundException(path);
      }
      throw new KeyStorageException(ex.getMessage(), ex);
    }
  }

}
