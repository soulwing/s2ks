/*
 * File created on Mar 13, 2019
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
package org.soulwing.s2ks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.security.Key;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

/**
 * Unit tests for {@link KeyWithMetadata}.
 *
 * @author Carl Harris
 */
public class KeyWithMetadataTest {

  private final Key key = new SecretKeySpec(new byte[16], "AES");

  @Test
  public void testSuccess() throws Exception {
    final Metadata metadata = SimpleMetadata.builder().build();
    final KeyWithMetadata keyWithMetadata = new KeyWithMetadata(key, metadata);
    assertThat(keyWithMetadata.getKey(), is(sameInstance(key)));
    assertThat(keyWithMetadata.getMetadata(), is(sameInstance(metadata)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWhenKeyIsNull() throws Exception {
    new KeyWithMetadata(null, SimpleMetadata.builder().build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWhenMetadataIsNull() throws Exception {
    new KeyWithMetadata(key, null);
  }

}
