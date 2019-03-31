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
package org.soulwing.s2ks.base;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.soulwing.s2ks.KeyPairInfo;
import org.soulwing.s2ks.KeyPairStorage;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.NoSuchKeyException;

/**
 * An abstract base for {@link KeyPairStorage} implementations.
 *
 * @author Carl Harris
 */
public abstract class AbstractKeyPairStorage implements KeyPairStorage {

  private final PrivateKeyLoader privateKeyLoader;
  private final CertificateLoader certificateLoader;

  protected AbstractKeyPairStorage(
      PrivateKeyLoader privateKeyLoader,
      CertificateLoader certificateLoader) {
    this.privateKeyLoader = privateKeyLoader;
    this.certificateLoader = certificateLoader;
  }

  @Override
  public KeyPairInfo retrieveKeyPair(String id) throws KeyStorageException {
    char[] password = null;
    try {
      password = getPassword();
      return KeyPairInfo.builder()
          .id(id)
          .privateKey(loadPrivateKey(id, password))
          .certificates(loadCertificates(id))
          .build();
    }
    catch (FileNotFoundException ex) {
      throw new NoSuchKeyException(id);
    }
    catch (IOException ex) {
      throw new KeyStorageException(ex);
    }
    finally {
      if (password != null) {
        Arrays.fill(password, (char) 0);
      }
    }
  }

  @Override
  public List<X509Certificate> retrieveCertificates(String id)
      throws KeyStorageException {
    try {
      return loadCertificates(id);
    }
    catch (FileNotFoundException ex) {
      throw new NoSuchKeyException(id);
    }
    catch (IOException ex) {
      throw new KeyStorageException(ex);
    }
  }

  private PrivateKey loadPrivateKey(String id, char[] password)
      throws IOException, KeyStorageException {
    try (final InputStream inputStream = openPrivateKeyStream(id)) {
      return privateKeyLoader.load(inputStream, password);
    }
  }

  private List<X509Certificate> loadCertificates(String id)
      throws IOException, KeyStorageException {
    final List<X509Certificate> certs = new ArrayList<>();
    try (final InputStream inputStream = openCertificateStream(id)) {
      certs.addAll(certificateLoader.load(inputStream));
    }

    try (final InputStream inputStream = openCACertificateStream(id)) {
      certs.addAll(certificateLoader.load(inputStream));
    }
    catch (FileNotFoundException ex) {
      assert true;  // CA certificate stream is optional
    }
    return certs;
  }

  /**
   * Gets the password to use to decrypt stored private keys.
   * @return password
   * @throws KeyStorageException if some other unexpected error occurs
   * @throws IOException if an I/O error occurs
   */
  protected abstract char[] getPassword()
      throws KeyStorageException, IOException;

  /**
   * Open an input stream to read the private key data.
   * @param id ID of the key to be read
   * @return input stream
   * @throws FileNotFoundException if the given identifier does not refer to
   *    an existing private key
   * @throws KeyStorageException if an unexpected error occurs in opening the
   *    stream
   * @throws IOException if an I/O error occurs
   */
  protected abstract InputStream openPrivateKeyStream(String id)
      throws FileNotFoundException, KeyStorageException, IOException;

  /**
   * Open an input stream to read the certificate associated with a private key.
   * @param id ID of the key whose certificate is to be read
   * @return input stream
   * @throws FileNotFoundException if the given identifier does not refer to
   *    an existing certificate
   * @throws KeyStorageException if an unexpected error occurs in opening the
   *    stream
   * @throws IOException if an I/O error occurs
   */
  protected abstract InputStream openCertificateStream(String id)
      throws FileNotFoundException, KeyStorageException, IOException;

  /**
   * Open an input stream to read the CA certificates associated with a
   * private key.
   * @param id ID of the key whose CA certificates are to be read
   * @return input stream
   * @throws FileNotFoundException if the given identifier does not refer to
   *    an existing certificate chain
   * @throws KeyStorageException if an unexpected error occurs in opening the
   *    stream
   * @throws IOException if an I/O error occurs
   */
  protected abstract InputStream openCACertificateStream(String id)
      throws FileNotFoundException, KeyStorageException, IOException;

}
