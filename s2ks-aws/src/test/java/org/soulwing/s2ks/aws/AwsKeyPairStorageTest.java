/*
 * File created on Mar 31, 2019
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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.json.Json;

import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.soulwing.s2ks.KeyPairInfo;
import org.soulwing.s2ks.KeyPairStorage;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.NoSuchKeyException;
import org.soulwing.s2ks.base.CertificateLoader;
import org.soulwing.s2ks.base.PrivateKeyLoader;
import org.soulwing.s2ks.bc.BcEncryptedPrivateKeyLoader;
import org.soulwing.s2ks.bc.BcPemCertificateLoader;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.AWSSecretsManagerException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;

/**
 * Unit tests for {@link AwsKeyPairStorage}.
 *
 * @author Carl Harris
 */
public class AwsKeyPairStorageTest {

  private static final String KEY_PAIR_ID = "keyPairId";
  private static final String SECRET_ID = "secretId";
  private static final String BUCKET_NAME = "bucketName";
  private static final String PREFIX = "prefix";
  private static final String PASSWORD = "password";

  private static final String AWS_KEY_PAIR_ID = "key-pair-1";
  private static final String AWS_PREFIX = "token-issuer/keys/access-token";

  private static KeyPair keyPair;

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Mock
  private PrivateKeyLoader privateKeyLoader;

  @Mock
  private CertificateLoader certificateLoader;

  @Mock
  private AWSSecretsManager secretsClient;

  @Mock
  private AmazonS3 s3Client;

  private AwsKeyPairStorage storage;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    keyPair = kpg.generateKeyPair();
  }

  @Before
  public void setUp() throws Exception {
    storage = new AwsKeyPairStorage(
        privateKeyLoader, certificateLoader,
        secretsClient, s3Client, SECRET_ID, BUCKET_NAME, PREFIX);
  }

  @Test
  public void testRetrieve() throws Exception {
    context.checking(secretsManagerExpectations(null));
    context.checking(s3KeyFileExpectations(null));
    context.checking(privateKeyExpectations(null));
    context.checking(s3CertFileExpectations(KeyPairStorage.CERT_FILE_NAME, null));
    context.checking(s3CertFileExpectations(KeyPairStorage.CA_FILE_NAME, null));
    context.checking(certificateExpectations(KeyPairStorage.CERT_FILE_NAME, null));
    context.checking(certificateExpectations(KeyPairStorage.CA_FILE_NAME, null));
    final KeyPairInfo kpi = storage.retrieveKeyPair(KEY_PAIR_ID);
    assertThat(kpi.getId(), is(equalTo(KEY_PAIR_ID)));
    assertThat(kpi.getPrivateKey(), is(sameInstance(keyPair.getPrivate())));
  }

  @Test
  public void testRetrieveWhenPrivateKeyIOException() throws Exception {
    final IOException ex = new IOException();
    context.checking(secretsManagerExpectations(null));
    context.checking(s3KeyFileExpectations(null));
    context.checking(privateKeyExpectations(ex));

    expectedException.expect(KeyStorageException.class);
    expectedException.expectCause(is(sameInstance(ex)));
    storage.retrieveKeyPair(KEY_PAIR_ID);
  }

  @Test
  public void testRetrieveWhenS3Exception() throws Exception {
    final AmazonS3Exception ex = new AmazonS3Exception("error");
    context.checking(secretsManagerExpectations(null));
    context.checking(s3KeyFileExpectations(ex));

    expectedException.expect(KeyStorageException.class);
    expectedException.expectCause(is(sameInstance(ex)));
    storage.retrieveKeyPair(KEY_PAIR_ID);
  }

  @Test
  public void testRetrieveWhenSecretsManagerException() throws Exception {
    final AWSSecretsManagerException ex = new AWSSecretsManagerException("error");
    context.checking(secretsManagerExpectations(ex));

    expectedException.expect(KeyStorageException.class);
    expectedException.expectCause(is(sameInstance(ex)));
    storage.retrieveKeyPair(KEY_PAIR_ID);
  }

  @Test
  public void testRetrieveWhenSecretContainsNoPassword() throws Exception {
    context.checking(secretsManagerExpectations("{}", null));
    expectedException.expect(KeyStorageException.class);
    expectedException.expectMessage(AwsKeyPairStorage.PASSWORD_KEY);
    storage.retrieveKeyPair(KEY_PAIR_ID);
  }

  private String secretString() {
    final StringWriter writer = new StringWriter();
    Json.createWriter(writer).writeObject(Json.createObjectBuilder()
        .add(AwsKeyPairStorage.PASSWORD_KEY, PASSWORD).build());
    return writer.toString();
  }

  private Expectations secretsManagerExpectations(Throwable ex) throws Exception {
    return secretsManagerExpectations(secretString(), ex);
  }

  private Expectations secretsManagerExpectations(String secret,
      Throwable ex) throws Exception {
    final GetSecretValueResult result = new GetSecretValueResult();
    result.setSecretString(secret);

    return new Expectations() {
      {
        oneOf(secretsClient).getSecretValue(
            with(Matchers.<GetSecretValueRequest>hasProperty(
                "secretId", equalTo(SECRET_ID))));
        will(ex != null ? throwException(ex) : returnValue(result));
      }
    };
  }

  private Expectations s3KeyFileExpectations(Throwable ex) throws Exception {
    final S3Object object = new S3Object();
    object.setObjectContent(new ByteArrayInputStream(new byte[0]));

    return new Expectations() {
      {
        oneOf(s3Client).getObject(BUCKET_NAME,
            Paths.get(PREFIX, KEY_PAIR_ID, KeyPairStorage.KEY_FILE_NAME).toString());
        will(ex != null ? throwException(ex) : returnValue(object));
      }
    };
  }

  private Expectations privateKeyExpectations(Throwable ex) throws Exception {
    return new Expectations() {
      {
        oneOf(privateKeyLoader).load(with(any(InputStream.class)),
            with(PASSWORD.toCharArray()));
        will(ex != null ? throwException(ex) : returnValue(keyPair.getPrivate()));
      }
    };
  }

  @Test
  public void testFindCertificateChain() throws Exception {
    context.checking(s3CertFileExpectations(KeyPairStorage.CERT_FILE_NAME, null));
    context.checking(certificateExpectations(KeyPairStorage.CERT_FILE_NAME, null));
    context.checking(s3CertFileExpectations(KeyPairStorage.CA_FILE_NAME, null));
    context.checking(certificateExpectations(KeyPairStorage.CA_FILE_NAME, null));
    assertThat(storage.retrieveCertificates(KEY_PAIR_ID).size(),
        is(greaterThan(1)));
  }

  @Test
  public void testFindCertificateChainWhenNoCacerts() throws Exception {
    final AmazonS3Exception ex = new AmazonS3Exception("error");
    ex.setErrorCode("NoSuchKey");
    context.checking(s3CertFileExpectations(KeyPairStorage.CERT_FILE_NAME, null));
    context.checking(certificateExpectations(KeyPairStorage.CERT_FILE_NAME, null));
    context.checking(s3CertFileExpectations(KeyPairStorage.CA_FILE_NAME, ex));
    assertThat(storage.retrieveCertificates(KEY_PAIR_ID).size(),
        is(equalTo(1)));
  }

  @Test
  public void testFindCertificateChainWhenCertificateIOException() throws Exception {
    final IOException ex = new IOException();
    context.checking(s3CertFileExpectations(KeyPairStorage.CERT_FILE_NAME, null));
    context.checking(certificateExpectations(KeyPairStorage.CERT_FILE_NAME, ex));
    expectedException.expect(KeyStorageException.class);
    expectedException.expectCause(is(sameInstance(ex)));
    storage.retrieveCertificates(KEY_PAIR_ID);
  }

  @Test
  public void testFindCertificateChainWhenCertNotFound() throws Exception {
    final AmazonS3Exception ex = new AmazonS3Exception("error");
    ex.setErrorCode("NoSuchKey");
    context.checking(s3CertFileExpectations(KeyPairStorage.CERT_FILE_NAME, ex));

    expectedException.expect(NoSuchKeyException.class);
    expectedException.expectMessage(KEY_PAIR_ID);
    storage.retrieveCertificates(KEY_PAIR_ID);
  }


  @Test
  public void testFindCertificateChainWhenS3Exception() throws Exception {
    final AmazonS3Exception ex = new AmazonS3Exception("error");
    context.checking(s3CertFileExpectations(KeyPairStorage.CERT_FILE_NAME, ex));

    expectedException.expect(KeyStorageException.class);
    expectedException.expectCause(is(sameInstance(ex)));
    storage.retrieveCertificates(KEY_PAIR_ID);
  }

  private Expectations s3CertFileExpectations(String name, Throwable ex)
      throws Exception {
    final S3Object object = new S3Object();
    object.setObjectContent(new ByteArrayInputStream(new byte[0]));

    return new Expectations() {
      {
        oneOf(s3Client).getObject(BUCKET_NAME,
            Paths.get(PREFIX, KEY_PAIR_ID, name).toString());
        will(ex != null ? throwException(ex) : returnValue(object));
      }
    };
  }

  private Expectations certificateExpectations(String name, Throwable ex)
      throws Exception {
    return new Expectations() {
      {
        oneOf(certificateLoader).load(with(any(InputStream.class)));
        will(ex != null ? throwException(ex) : returnValue(loadCerts(name)));
      }
    };
  }

  private List<X509Certificate> loadCerts(String name)
      throws KeyStorageException, IOException {
    try (final InputStream inputStream = getClass().getResourceAsStream(name)) {
      return BcPemCertificateLoader.getInstance().load(inputStream);
    }
  }

  @Test
  public void testFullIntegration() throws Exception {

    assumeTrue("AWS profile is not available",
        (System.getenv("AWS_PROFILE") != null
            || System.getProperty("aws.profile") != null)
        && System.getenv("SECRET_ID") != null
        && System.getenv("S3_BUCKET_NAME") != null
        && System.getenv("S3_PREFIX") != null
        && System.getenv("KEY_PAIR_ID") != null);

    final String keyId = System.getenv("KEY_PAIR_ID");

    final AwsKeyPairStorage storage = new AwsKeyPairStorage(
        BcEncryptedPrivateKeyLoader.getInstance(),
        BcPemCertificateLoader.getInstance(),
        AWSSecretsManagerClientBuilder.standard().build(),
        AmazonS3ClientBuilder.standard().build(),
        System.getenv("SECRET_ID"),
        System.getenv("S3_BUCKET_NAME"),
        System.getenv("S3_PREFIX"));

    final KeyPairInfo kpi = storage.retrieveKeyPair(keyId);
    assertThat(kpi.getId(), is(equalTo(keyId)));
    assertThat(kpi.getCertificates(), is(not(empty())));
    assertThat(storage.retrieveCertificates(keyId), is(not(empty())));
  }

}