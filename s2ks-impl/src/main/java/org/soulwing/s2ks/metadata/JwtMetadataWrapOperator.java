/*
 * File created on Mar 14, 2019
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

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.time.Clock;
import javax.crypto.SecretKey;

import org.soulwing.jwt.api.Claims;
import org.soulwing.jwt.api.JWS;
import org.soulwing.jwt.api.JWTProvider;
import org.soulwing.jwt.api.JWTProviderLocator;
import org.soulwing.jwt.api.SingletonKeyProvider;
import org.soulwing.jwt.api.exceptions.JWTConfigurationException;
import org.soulwing.jwt.api.exceptions.JWTException;
import org.soulwing.s2ks.KeyWithMetadata;
import org.soulwing.s2ks.Metadata;
import org.soulwing.s2ks.MetadataUnwrapException;
import org.soulwing.s2ks.MetadataWrapException;
import org.soulwing.s2ks.SimpleMetadata;
import org.soulwing.s2ks.base.MetadataWrapOperator;

/**
 * A {@link MetadataWrapOperator} that encodes to a JWT signed with the subject key.
 *
 * @author Carl Harris
 */
public class JwtMetadataWrapOperator implements MetadataWrapOperator {

  private static final JwtMetadataWrapOperator INSTANCE = new JwtMetadataWrapOperator();

  private final PublicKeyFactory publicKeyFactory = new JcaPublicKeyFactory();

  private final JWTProvider provider;

  /**
   * Gets the singleton instance.
   * @return singleton instance
   */
  public static JwtMetadataWrapOperator getInstance() {
    return INSTANCE;
  }

  private JwtMetadataWrapOperator() {
    this.provider = JWTProviderLocator.getProvider();
  }

  @Override
  public byte[] wrap(KeyWithMetadata keyWithMetadata)
      throws MetadataWrapException {
    try {
      final Key key = keyWithMetadata.getKey();
      return provider.generator()
          .signature(signatureOperator(key))
          .build()
          .generate(metadataToClaims(keyWithMetadata.getMetadata()))
          .getBytes(StandardCharsets.UTF_8);
    }
    catch (JWTException ex) {
      throw new MetadataWrapException(ex.toString(), ex);
    }
  }

  @Override
  public Metadata unwrap(Key key, byte[] encoded)
      throws MetadataUnwrapException {
    try {
      return claimsToMetadata(provider.validator()
          .signatureValidation(signatureOperator(deriveValidationKey(key)))
          .claimsAssertions(provider.assertions().build())
          .clock(Clock.systemUTC())
          .build()
          .validate(new String(encoded, StandardCharsets.UTF_8)));
    }
    catch (JWTException ex) {
      throw new MetadataUnwrapException(ex.toString(), ex);
    }
  }

  private Key deriveValidationKey(Key key) throws MetadataUnwrapException {
    if (key instanceof SecretKey) {
      return key;
    }
    if (key instanceof PrivateKey) {
      return publicKeyFactory.generatePublic((PrivateKey) key);
    }
    throw new MetadataUnwrapException("unsupported key or algorithm type");
  }

  private JWS signatureOperator(Key key) throws JWTConfigurationException {
    return provider.signatureOperator()
        .algorithm(keyToAlgorithm(key))
        .keyProvider(SingletonKeyProvider.with(key))
        .build();
  }

  private JWS.Algorithm keyToAlgorithm(Key key)
      throws IllegalArgumentException {
    if (key.getAlgorithm().equals("AES")) {
      final int bits = key.getEncoded().length * Byte.SIZE;
      return JWS.Algorithm.of(String.format("HS%d", bits));
    }
    if (key.getAlgorithm().equals("RSA")) {
      return JWS.Algorithm.RS256;
    }
    if (key.getAlgorithm().equals("EC")) {
      return JWS.Algorithm.ES256;
    }
    throw new IllegalArgumentException("unsupported key algorithm");
  }

  private Claims metadataToClaims(Metadata metadata) {
    final Claims.Builder builder = provider.claims();
    metadata.names().forEach(
        name -> builder.set(name, metadata.get(name, Object.class)));
    return builder.build();
  }

  private Metadata claimsToMetadata(Claims claims) {
    final SimpleMetadata.Builder builder = SimpleMetadata.builder();
    claims.names().forEach(
        name -> builder.set(name, claims.get(name, Object.class)));
    return builder.build();
  }

}
