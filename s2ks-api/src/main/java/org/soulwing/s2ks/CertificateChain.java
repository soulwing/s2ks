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
package org.soulwing.s2ks;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * An abstract representation of a chain of certificates.
 * <p>
 * A chain is a subject certificate, followed by zero or more certificates
 * for certification authorities (CA), such that each CA certificate is the
 * signatory for the certificate which immediately precedes it in the chain,
 * and the last certificate in the chain is self-signed.
 *
 * @author Carl Harris
 */
public interface CertificateChain {

  /**
   * Gets the certificates in this chain as a list.
   * @return certificate list
   */
  List<X509Certificate> getCertificates();

  /**
   * Gets the media type that best describes the representation of this chain
   * which is produced by the {@link #write(OutputStream)} method.
   * @return media type
   */
  String getMediaType();

  /**
   * Produces a common external representation of this chain on the given output
   * stream.
   * @param outputStream the target output stream
   * @throws IOException if an I/O occurs in writing the stream
   */
  void write(OutputStream outputStream) throws IOException;

}
