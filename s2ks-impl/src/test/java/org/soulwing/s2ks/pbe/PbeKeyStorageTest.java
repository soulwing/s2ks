/*
 * File created on Mar 12, 2019
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
package org.soulwing.s2ks.pbe;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soulwing.s2ks.KeyDescriptor;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.KeyUtil;
import org.soulwing.s2ks.NoSuchKeyException;
import org.soulwing.s2ks.base.AbstractKeyWrapOperator;
import org.soulwing.s2ks.filesystem.FilesystemStorageService;
import org.soulwing.s2ks.pem.PemBlobEncoder;
import org.soulwing.s2ks.pem.PemEncoder;

/**
 * Tests for {@link PbeKeyStorage}.
 * <p>
 * The tests in this class are really integration tests, but because the
 * filesystem resource is available to every Java SE runtime, and the tests
 * are relatively fast to run, we run them with unit tests.
 *
 * @author Carl Harris
 */
public class PbeKeyStorageTest {

  private static Path parent;

  private PbeKeyStorage storage;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    parent = Files.createTempDirectory(
        PbeKeyStorageTest.class.getSimpleName());
  }

  @Before
  public void setUp() throws Exception {
    storage = new PbeKeyStorage(
        PemBlobEncoder.getInstance(),
        PemEncoder.getInstance(),
        PbeWrapOperator.getInstance(),
        PbeKeyFactory.generateKey("secret".toCharArray()),
        new FilesystemStorageService(parent.resolve("keys"),
            PemBlobEncoder.getInstance()));
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    recursivelyDelete(parent);
  }

  private static void recursivelyDelete(Path directory) throws IOException {
    try (final DirectoryStream<Path> paths =
             Files.newDirectoryStream(directory)) {
      paths.forEach(path -> {
        try {
          if (Files.isDirectory(path)) {
            recursivelyDelete(path);
          }
          Files.delete(path);
        }
        catch (IOException ex) {
          throw new RuntimeException(ex);
        }
      });
    }
  }

  @Test
  public void testStoreAndRetrieveAesKey() throws Exception {
    final SecretKey key = KeyUtil.aesKey(256);
    final KeyDescriptor descriptor = validateStoreAndRetrieve(key);

    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.SECRET)));
    assertThat(descriptor.getAlgorithm(), is(equalTo("AES")));
    validateMetadata(descriptor);

    // since the byte array of a AES key spec is directly encoded, we
    // can use the blob content to try to create a key spec; if the original
    // subject key was properly encrypted, a key created from the blob content
    // shouldn't be the same as the original key

    final SecretKey contentKey = new SecretKeySpec(descriptor.getKeyData(), "AES");
    assertThat(contentKey, is(not(equalTo(key))));

  }

  @Test
  public void testStoreAndRetrieveEcPrivateKey() throws Exception {
    final KeyDescriptor descriptor =
        validateStoreAndRetrieve(KeyUtil.ecKeyPair().getPrivate());

    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.PRIVATE)));
    assertThat(descriptor.getAlgorithm(), is(equalTo("EC")));
    validateMetadata(descriptor);
  }

  @Test
  public void testStoreAndRetrieveRsaPrivateKey() throws Exception {
    final KeyDescriptor descriptor =
        validateStoreAndRetrieve(KeyUtil.rsaKeyPair().getPrivate());

    assertThat(descriptor.getType(), is(equalTo(KeyDescriptor.Type.PRIVATE)));
    assertThat(descriptor.getAlgorithm(), is(equalTo("RSA")));
    validateMetadata(descriptor);
  }

  private void validateMetadata(KeyDescriptor descriptor) {
    assertThat(descriptor.getMetadata()
            .get(AbstractKeyWrapOperator.PROC_TYPE_HEADER),
        is(equalTo(AbstractKeyWrapOperator.PROC_TYPE_VALUE)));

    assertThat(descriptor.getMetadata()
            .get(AbstractKeyWrapOperator.DEK_INFO_HEADER),
        is(not(nullValue())));
  }

  private KeyDescriptor validateStoreAndRetrieve(Key key) throws Exception {
    final String id = UUID.randomUUID().toString();
    storage.store(id, key);
    final Key retrieved = storage.retrieve(id);
    assertThat(retrieved, is(equalTo(key)));

    final String path = storage.idToPath(id,
        PemEncoder.getInstance().getPathSuffix());
    try (final FileInputStream inputStream = new FileInputStream(path)) {
      return PemEncoder.getInstance().decode(
          PemBlobEncoder.getInstance().decode(inputStream).get(0));
    }
  }

  @Test(expected = NoSuchKeyException.class)
  public void testRetrieveWhenNotFound() throws Exception {
    storage.retrieve(UUID.randomUUID().toString());
  }

  @Test(expected = KeyStorageException.class)
  public void testGetSubjectKeyWhenNoDescriptors() throws Exception {
    storage.getSubjectKey(Collections.emptyList());
  }

  @Test(expected = KeyStorageException.class)
  public void testGetSubjectKeyWhenMoreThanOneDescriptor() throws Exception {
    storage.getSubjectKey(Arrays.asList(
        KeyDescriptor.builder()
            .algorithm("ALG")
            .type(KeyDescriptor.Type.SECRET)
            .build(new byte[1]),
        KeyDescriptor.builder()
            .algorithm("ALG")
            .type(KeyDescriptor.Type.SECRET)
            .build(new byte[1])));
  }

}
