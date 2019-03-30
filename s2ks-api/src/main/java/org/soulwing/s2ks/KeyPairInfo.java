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

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A private key and corresponding certificates.
 *
 * @author Carl Harris
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class KeyPairInfo {

  private String id;
  private PrivateKey privateKey;
  private List<X509Certificate> certificates = new ArrayList<>();

  private KeyPairInfo() {}

  /**
   * A builder that constructs a {@link KeyPairInfo} instance.
   */
  public static class Builder {

    private final KeyPairInfo info = new KeyPairInfo();

    private Builder() {}

    /**
     * Specifies an identifier for this privateKey.
     * @param id identifier
     * @return this builder
     */
    public Builder id(String id) {
      info.id = id;
      return this;
    }

    /**
     * Specifies the private key.
     * @param privateKey the subject privateKey
     * @return this builder
     */
    public Builder privateKey(PrivateKey privateKey) {
      info.privateKey = privateKey;
      return this;
    }

    /**
     * Specifies a list of corresponding certificates.
     * @param certificates certificates to add
     * @return this builder
     */
    public Builder certificates(List<X509Certificate> certificates) {
      info.certificates.addAll(certificates);
      return this;
    }

    /**
     * Builds and returns an instance according to the configuration of this
     * builder.
     * @return privateKey info instance
     */
    public KeyPairInfo build() {
      if (info.privateKey == null) {
        throw new IllegalArgumentException("privateKey is required");
      }
      return info;
    }

  }

  /**
   * Creates a builder that constructs a new instance.
   * @return builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Gets the ID specified for this privateKey.
   * @return ID or {@code null} if none was specified
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the subject privateKey.
   * @return privateKey
   */
  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  /**
   * Gets the list of certificates associated with the privateKey.
   * @return certificate list (possibly empty, but never {@code null})
   */
  public List<X509Certificate> getCertificates() {
    return Collections.unmodifiableList(certificates);
  }

}
