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

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.soulwing.s2ks.MetadataUnwrapException;

/**
 * A public key factory implementing using JCA and JCA provider facilities.
 *
 * @author Carl Harris
 */
class JcaPublicKeyFactory implements PublicKeyFactory {

  private final Map<String, PublicKeyStrategy> strategies = new HashMap<>();

  JcaPublicKeyFactory() {
    strategies.put("RSA", new RsaPublicKeyStrategy());
    strategies.put("EC", new EcPublicKeyStrategy());
  }

  @Override
  public PublicKey generatePublic(PrivateKey privateKey)
      throws MetadataUnwrapException {

    final PublicKeyStrategy strategy = strategies.get(privateKey.getAlgorithm());
    if (strategy == null) {
      throw new MetadataUnwrapException("algorithm `"
          + privateKey + "` is unsupported");
    }

    try {
      return strategy.derive(privateKey);
    }
    catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new MetadataUnwrapException(ex.toString(), ex);
    }
  }

  /**
   * A strategy for deriving a public key from a private key.
   */
  private interface PublicKeyStrategy {
    PublicKey derive(PrivateKey privateKey)
        throws NoSuchAlgorithmException, InvalidKeySpecException;
  }

  /**
   * A strategy that produces and RSAPublicKey from an RSAPrivateKey.
   */
  private static class RsaPublicKeyStrategy implements PublicKeyStrategy {

    @Override
    public PublicKey derive(PrivateKey privateKey)
        throws NoSuchAlgorithmException, InvalidKeySpecException {

      final RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey) privateKey;

      final RSAPublicKeySpec spec =
          new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());

      return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

  }

  /**
   * A strategy that produces an ECPublicKey from an ECPrivateKey.
   */
  private static class EcPublicKeyStrategy implements PublicKeyStrategy {

    private final Provider bcProvider = new BouncyCastleProvider();

    @Override
    public PublicKey derive(PrivateKey privateKey)
        throws NoSuchAlgorithmException, InvalidKeySpecException {

      final ECPrivateKey ecKey = (ECPrivateKey) privateKey;
      final ECParameterSpec bcSpec = EC5Util.convertSpec(ecKey.getParams(), false);
      final ECPoint q = bcSpec.getG().multiply(ecKey.getS());
      final ECPoint bcW = bcSpec.getCurve().decodePoint(q.getEncoded(false));
      java.security.spec.ECPoint w = new java.security.spec.ECPoint(
          bcW.getAffineXCoord().toBigInteger(),
          bcW.getAffineYCoord().toBigInteger());
      final ECPublicKeySpec keySpec = new ECPublicKeySpec(w,
          findNamedSpec(bcSpec).orElse(ecKey.getParams()));
      return KeyFactory.getInstance("EC", bcProvider).generatePublic(keySpec);
    }

    @SuppressWarnings("unchecked")
    private static Optional<java.security.spec.ECParameterSpec> findNamedSpec(
        ECParameterSpec spec) {
      final List<String> names = Collections.list(ECNamedCurveTable.getNames());
      return names.stream()
          .map(ECNamedCurveTable::getParameterSpec)
          .filter(nc -> nc.getN().equals(spec.getN())
              && nc.getH().equals(spec.getH())
              && nc.getCurve().equals(spec.getCurve())
              && nc.getG().equals(spec.getG()))
          .findFirst()
          .map(nc -> new ECNamedCurveSpec(nc.getName(), nc.getCurve(),
              nc.getG(), nc.getN(), nc.getH(), nc.getSeed()));
    }

  }


}
