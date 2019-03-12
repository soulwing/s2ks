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
import static org.hamcrest.Matchers.is;

import java.security.Key;

import org.junit.Test;

/**
 * Unit tests for {@link KmsEncryptionKey}.
 *
 * @author Carl Harris
 */
public class KmsEncryptionKeyTest {

  private static final String KEY_ID = "keyId";

  @Test
  public void test() throws Exception {
    final Key key = KeyUtil.aesKey(256);
    final byte[] cipherText = KeyUtil.randomKeyData(128);
    final KmsEncryptionKey encryptionKey = new KmsEncryptionKey(
        key.getEncoded(), cipherText, KEY_ID);

    assertThat(encryptionKey.getKey(), is(equalTo(key)));
    assertThat(encryptionKey.getCipherText(), is(equalTo(cipherText)));
    assertThat(encryptionKey.getMasterKeyId(), is(equalTo(KEY_ID)));
  }

}
