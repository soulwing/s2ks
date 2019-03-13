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
package org.soulwing.s2ks.base;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 * A simple immutable descriptor for a key.
 * <p>
 * A key descriptor includes both the byte-encoded representation of the
 * subject key as well as any metadata that may be needed in writing it
 * to an external storage medium; e.g. information such as the initialization
 * vector (IV) for a cipher that was used to wrap the key.
 *
 * @author Carl Harris
 */
public final class KeyDescriptor {

  /**
   * An enumeration of key types for a {@link KeyDescriptor}.
   */
  public enum Type {
    SECRET(Cipher.SECRET_KEY),
    PRIVATE(Cipher.PRIVATE_KEY),
    PUBLIC(Cipher.PUBLIC_KEY);

    private final int cipherKeyType;

    Type(int cipherKeyType) {
      this.cipherKeyType = cipherKeyType;
    }

    /**
     * Gets the key type to use with the
     * {@link Cipher#unwrap(byte[], String, int)} method.
     * @return cipher key type
     */
    public int getCipherKeyType() {
      return cipherKeyType;
    }

    /**
     * Gets the type that corresponds to a given key.
     * @param key the subject key
     * @return key type
     */
    public static <T extends Key> Type typeOf(T key) {
      if (key instanceof SecretKey) {
        return Type.SECRET;
      }
      else if (key instanceof PrivateKey) {
        return Type.PRIVATE;
      }
      else if (key instanceof PublicKey) {
        return Type.PUBLIC;
      }
      throw new IllegalArgumentException("unrecognized key class");
    }
  }

  private final Map<String, String> metadata = new LinkedHashMap<>();

  private String algorithm;
  private Type type;
  private byte[] keyData;

  private KeyDescriptor() { }

  /**
   * A builder that produces a {@link KeyDescriptor}.
   */
  public static class Builder {

    private final KeyDescriptor descriptor = new KeyDescriptor();

    /**
     * Specifies the JCA algorithm used to produce the key.
     * @param algorithm JCA algorithm name
     * @return this builder
     */
    public Builder algorithm(String algorithm) {
      descriptor.algorithm = algorithm;
      return this;
    }

    /**
     * Specifies the key type.
     * @param type key type
     * @return this builder
     */
    public Builder type(Type type) {
      descriptor.type = type;
      return this;
    }


    /**
     * Adds a metadata data value to the key descriptor.
     * @param name name under which to return the value
     * @param value string-encoded metadata value
     * @return this builder
     */
    public Builder metadata(String name, String value) {
      descriptor.metadata.put(name, value);
      return this;
    }

    /**
     * Builds a key descriptor using the current configuration of this builder.
     * @param keyData byte-encoded key representation
     * @return
     */
    public KeyDescriptor build(byte[] keyData) {
      if (descriptor.type == null) {
        throw new IllegalArgumentException("type is required");
      }
      if (descriptor.algorithm == null
          || descriptor.algorithm.trim().isEmpty()) {
        throw new IllegalArgumentException("algorithm is required");
      }
      if (keyData == null || keyData.length == 0) {
        throw new IllegalArgumentException("key is required");
      }
      descriptor.keyData = keyData;
      return descriptor;
    }

  }

  /**
   * Get a builder that creates a new instance.
   * @return builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Gets the algorithm that was used to generate the key.
   * @return JCA algorithm name
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * Gets the type of key.
   * @return key type
   */
  public Type getType() {
    return type;
  }

  /**
   * Gets the metadata associated with the key.
   * @return key metadata as an immutable map
   */
  public Map<String, String> getMetadata() {
    return Collections.unmodifiableMap(metadata);
  }

  /**
   * Gets the byte-encoded key data.
   * <p>
   * @return byte-encoded key data; to protected against modification
   *    of the array in this instance, a copy of the data is returned
   */
  public byte[] getKeyData() {
    return Arrays.copyOf(keyData, keyData.length);
  }

}
