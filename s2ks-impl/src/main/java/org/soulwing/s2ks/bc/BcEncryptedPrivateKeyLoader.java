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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Provider;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.base.PrivateKeyLoader;

/**
 * A {@link PrivateKeyLoader} that loads an encrypted key stored in PEM
 * using OpenSSL formats.
 *
 * @author Carl Harris
 */
public class BcEncryptedPrivateKeyLoader implements PrivateKeyLoader {

  private static final BcEncryptedPrivateKeyLoader INSTANCE =
      new BcEncryptedPrivateKeyLoader();

  private final Provider cryptoProvider;

  /**
   * Gets the singleton instance.
   * @return singleton instance
   */
  public static BcEncryptedPrivateKeyLoader getInstance() {
    return INSTANCE;
  }

  private BcEncryptedPrivateKeyLoader() {
    this(new BouncyCastleProvider());
  }

  private BcEncryptedPrivateKeyLoader(Provider cryptoProvider) {
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public PrivateKey load(InputStream inputStream, char[] password)
      throws KeyStorageException, IOException {
    try {
      final PEMParser reader = new PEMParser(
          new InputStreamReader(inputStream, StandardCharsets.US_ASCII));

      final Object object = reader.readObject();
      if (object == null) {
        throw new IOException("no PEM object found in stream");
      }

      final JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
      converter.setProvider(cryptoProvider);

      // OpenSSL PEM-encoded unencrypted private key
      if (object instanceof PEMKeyPair) {
        return converter.getKeyPair((PEMKeyPair) object).getPrivate();
      }

      // OpenSSL PEM-encoded encrypted private key
      if (object instanceof PEMEncryptedKeyPair) {

        final PrivateKeyInfo pkInfo = ((PEMEncryptedKeyPair) object)
            .decryptKeyPair(pemDecryptor(password))
            .getPrivateKeyInfo();

        return converter.getPrivateKey(pkInfo);
      }

      // OpenSSL PEM-encoded PKCS#8 unencrypted private key
      if (object instanceof PrivateKeyInfo) {
        return converter.getPrivateKey((PrivateKeyInfo) object);
      }

      // OpenSSL PEM-encoded PKCS#8 encrypted private key
      if (object instanceof PKCS8EncryptedPrivateKeyInfo) {

        final PrivateKeyInfo pkInfo = ((PKCS8EncryptedPrivateKeyInfo) object)
            .decryptPrivateKeyInfo(pkcs8Decryptor(password));

        return converter.getPrivateKey(pkInfo);
      }

      throw new IOException("unsupported PEM object");
    }
    catch (OperatorCreationException  | PKCSException ex) {
      throw new KeyStorageException(ex);
    }
  }

  private InputDecryptorProvider pkcs8Decryptor(char[] password)
      throws OperatorCreationException {
    return new JceOpenSSLPKCS8DecryptorProviderBuilder()
        .setProvider(cryptoProvider)
        .build(password);
  }

  private PEMDecryptorProvider pemDecryptor(char[] password) {
    return new JcePEMDecryptorProviderBuilder()
        .setProvider(cryptoProvider)
        .build(password);
  }

}
