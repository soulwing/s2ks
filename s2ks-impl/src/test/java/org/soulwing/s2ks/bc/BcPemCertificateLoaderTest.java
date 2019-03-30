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
package org.soulwing.s2ks.bc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link BcPemCertificateLoader}.
 *
 * @author Carl Harris
 */
public class BcPemCertificateLoaderTest {

  private BcPemCertificateLoader loader = BcPemCertificateLoader.getInstance();

  @Test
  public void testLoadOneCert() throws Exception {
    try (final InputStream inputStream =
        getClass().getResourceAsStream("cert.pem")) {
      final List<X509Certificate> certs = loader.load(inputStream);
      assertThat(certs.size(), is(equalTo(1)));
      assertThat(certs.get(0), is(not(nullValue())));
    }
  }

  @Test
  public void testLoadMultipleCerts() throws Exception {
    try (final InputStream inputStream =
             getClass().getResourceAsStream("cacerts.pem")) {
      final List<X509Certificate> certs = loader.load(inputStream);
      assertThat(certs.size(), is(greaterThan(1)));
      certs.forEach(cert -> assertThat(cert, is(not(nullValue()))));
    }
  }

}