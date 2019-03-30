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

/**
 * A service that provides storage for key pairs.
 *
 * @author Carl Harris
 */
public interface KeyPairStorage {

  /**
   * Name of the file or file-like object used to store a private key
   */
  String KEY_FILE_NAME = "key.pem";

  /**
   * Name of the file or file-like object used to store a certificate
   */
  String CERT_FILE_NAME = "cert.pem";

  /**
   * Name of the file or file-like object used to store additional CA certificates
   */
  String CA_FILE_NAME = "cacerts.pem";

  /**
   * Retrieves the private key with the given identifier.
   * @param id identifier of the key to retrieve
   * @return private key
   * @throws NoSuchKeyException if there exists no private key with the
   *    given identifier
   * @throws KeyStorageException if an unexpected error occurs in loading
   *    the key from storage
   */
  KeyPairInfo retrieve(String id)
      throws NoSuchKeyException, KeyStorageException;

}
