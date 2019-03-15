/*
 * File created on Mar 15, 2019
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
package org.soulwing.s2ks.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.security.KeyPair;
import javax.crypto.SecretKey;

import org.junit.Test;
import org.soulwing.s2ks.KeyUtil;
import org.soulwing.s2ks.KeyWithMetadata;
import org.soulwing.s2ks.Metadata;
import org.soulwing.s2ks.SimpleMetadata;

/**
 * Unit tests for {@link JwtMetadataWrapOperator}.
 *
 * @author Carl Harris
 */
public class JwtMetadataWrapOperatorTest {

  private final JwtMetadataWrapOperator operator =
      JwtMetadataWrapOperator.getInstance();

  private Metadata metadata = SimpleMetadata.builder()
      .set("name", "value").build();

  @Test
  public void testWrapAndUnwrapWithAesKey() throws Exception {
    final SecretKey key = KeyUtil.aesKey(256);
    final Metadata actual = operator.unwrap(key,
        operator.wrap(new KeyWithMetadata(key, metadata)));
    assertThat(actual, is(equalTo(metadata)));
  }

  @Test
  public void testWrapAndUnwrapWithRsaKey() throws Exception {
    final KeyPair key = KeyUtil.rsaKeyPair();
    final Metadata actual = operator.unwrap(key.getPrivate(),
        operator.wrap(new KeyWithMetadata(key.getPrivate(), metadata)));
    assertThat(actual, is(equalTo(metadata)));
  }

  @Test
  public void testWrapAndUnwrapWithEcKey() throws Exception {
    final KeyPair key = KeyUtil.ecKeyPair();
    final Metadata actual = operator.unwrap(key.getPrivate(),
        operator.wrap(new KeyWithMetadata(key.getPrivate(), metadata)));
    assertThat(actual, is(equalTo(metadata)));
  }

}