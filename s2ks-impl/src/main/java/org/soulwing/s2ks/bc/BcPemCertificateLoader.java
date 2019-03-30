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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.base.CertificateLoader;

/**
 * A {@link CertificateLoader} that loads PEM-encoded certificates.
 *
 * @author Carl Harris
 */
public class BcPemCertificateLoader implements CertificateLoader {

  private static final BcPemCertificateLoader INSTANCE =
      new BcPemCertificateLoader();

  /**
   * Gets the singleton instance.
   * @return singleton instance
   */
  public static BcPemCertificateLoader getInstance() {
    return INSTANCE;
  }

  private BcPemCertificateLoader() {}

  @Override
  public List<X509Certificate> load(InputStream inputStream)
      throws KeyStorageException, IOException {
    return toCertificates(loadPemObjects(inputStream));
  }

  private List<PemObject> loadPemObjects(InputStream inputStream)
      throws IOException {
    final PemReader reader = new PemReader(new InputStreamReader(
        inputStream, StandardCharsets.US_ASCII));
    final List<PemObject> objects = new LinkedList<>();
    PemObject object = reader.readPemObject();
    while (object != null) {
      objects.add(object);
      object = reader.readPemObject();
    }
    return objects;
  }

  private List<X509Certificate> toCertificates(List<PemObject> objects)
      throws KeyStorageException  {
    try {
      final List<X509Certificate> certificates = new ArrayList<>();
      final CertificateFactory factory = CertificateFactory.getInstance("X.509");
      for (final PemObject object : objects) {
        final ByteArrayInputStream bos =
            new ByteArrayInputStream(object.getContent());
        certificates.add((X509Certificate) factory.generateCertificate(bos));
      }
      return certificates;
    }
    catch (Exception ex) {
      throw new KeyStorageException(ex);
    }

  }

}
