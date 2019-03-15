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
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;

import org.junit.Test;
import org.soulwing.s2ks.KeyUtil;

/**
 * Unit tests for {@link JcaPublicKeyFactory}.
 * @author Carl Harris
 */
public class JcaPublicKeyFactoryTest {

  private JcaPublicKeyFactory factory = new JcaPublicKeyFactory();

  @Test
  public void testRsaPrivateKey() throws Exception {
    final KeyPair kp = KeyUtil.rsaKeyPair();
    final PublicKey actual = factory.generatePublic(kp.getPrivate());
    assertThat(actual, is(equalTo(kp.getPublic())));
  }

  @Test
  public void testEcPrivateKey() throws Exception {
    final KeyPair kp = KeyUtil.ecKeyPair();
    final ECPublicKey actual = (ECPublicKey) factory.generatePublic(kp.getPrivate());
    final ECPublicKey expected = (ECPublicKey) kp.getPublic();
    assertThat(actual.getW(), is(equalTo(expected.getW())));
    assertThat(actual.getParams().getCurve(),
        is(equalTo(expected.getParams().getCurve())));
    assertThat(actual.getParams().getGenerator(),
        is(equalTo(expected.getParams().getGenerator())));
    assertThat(actual.getParams().getCofactor(),
        is(equalTo(expected.getParams().getCofactor())));
    assertThat(actual.getParams().getOrder(),
        is(equalTo(expected.getParams().getOrder())));
  }



}