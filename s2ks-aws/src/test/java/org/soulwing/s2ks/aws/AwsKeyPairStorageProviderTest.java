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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.soulwing.s2ks.ProviderConfigurationException;

/**
 * Unit tests for {@link AwsKeyPairStorageProvider}.
 * @author Carl Harris
 */
public class AwsKeyPairStorageProviderTest {

  private static final String BUCKET_NAME = "bucket name";
  private static final String PREFIX = "storage prefix";
  private static final String SECRET_ID = "secret ID";

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  private Properties configuration = new Properties();

  private AwsKeyPairStorageProvider provider = new AwsKeyPairStorageProvider();

  @Before
  public void setUp() throws Exception {
    configuration.put(AwsKeyPairStorageProvider.S3_BUCKET_NAME, BUCKET_NAME);
    configuration.put(AwsKeyPairStorageProvider.S3_PREFIX, PREFIX);
    configuration.put(AwsKeyPairStorageProvider.SECRET_ID, SECRET_ID);
  }

  @Test
  public void testGetName() throws Exception {
    assertThat(provider.getName(),
        is(equalTo(AwsKeyPairStorageProvider.PROVIDER_NAME)));
  }

  @Test
  public void testGetInstanceWhenNoSecret() throws Exception {
    configuration.remove(AwsKeyPairStorageProvider.SECRET_ID);
    expectedException.expect(ProviderConfigurationException.class);
    expectedException.expectMessage("secret");
    provider.getInstance(configuration);
  }

  @Test
  public void testGetInstanceWhenNoBucketName() throws Exception {
    configuration.remove(AwsKeyPairStorageProvider.S3_BUCKET_NAME);
    expectedException.expect(ProviderConfigurationException.class);
    expectedException.expectMessage("bucket");
    provider.getInstance(configuration);
  }

  @Test
  public void testGetInstanceSuccess() throws Exception {
    assumeTrue("AWS profile not available",
        (System.getProperty("aws.profile") != null
            || System.getenv("AWS_PROFILE") != null)
            && System.getenv("S3_BUCKET_NAME") != null
            && System.getenv("SECRET_ID") != null);

    final Properties properties = new Properties();
    properties.setProperty(AwsKeyPairStorageProvider.SECRET_ID,
        System.getenv("SECRET_ID"));
    properties.setProperty(AwsKeyPairStorageProvider.S3_BUCKET_NAME,
        System.getenv("S3_BUCKET_NAME"));
    properties.setProperty(AwsKeyPairStorageProvider.S3_PREFIX,
        System.getenv("S3_PREFIX"));
    assertThat(provider.getInstance(configuration),
        is(instanceOf(AwsKeyPairStorage.class)));
  }

}