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
package org.soulwing.s2ks.aws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.ByteArrayInputStream;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.soulwing.s2ks.Blob;
import org.soulwing.s2ks.BlobEncoder;
import org.soulwing.s2ks.EncryptionKey;
import org.soulwing.s2ks.KeyDescriptor;
import org.soulwing.s2ks.KeyEncoder;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.KeyUnwrapException;
import org.soulwing.s2ks.KeyWrapOperator;
import org.soulwing.s2ks.MasterKeyService;
import org.soulwing.s2ks.StorageService;
import org.soulwing.s2ks.base.WrapperKeyResponse;

/**
 * Unit tests for {@link AwsKeyStorage}.
 *
 * @author Carl Harris
 */
public class AwsKeyStorageTest {

  private static final String ID = "id";
  private static final String SUFFIX = "suffix";
  private static final String PATH = "path";
  private static final String KEY_ID = "keyId";

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  private BlobEncoder blobEncoder;

  @Mock
  private KeyEncoder keyEncoder;

  @Mock
  private KeyWrapOperator keyWrapOperator;

  @Mock
  private MasterKeyService masterKeyService;

  @Mock
  private StorageService storageService;

  @Mock
  private Blob blob;

  private AwsKeyStorage storage;

  @Before
  public void setUp() throws Exception {
    storage = new AwsKeyStorage(blobEncoder, keyEncoder,
        keyWrapOperator, masterKeyService, storageService);
  }

  @Test
  public void testIdToPath() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(storageService).idToPath(ID, SUFFIX);
        will(returnValue(PATH));
      }
    });

    assertThat(storage.idToPath(ID, SUFFIX), is(equalTo(PATH)));
  }

  @Test
  public void testGetContentStream() throws Exception {
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
    context.checking(new Expectations() {
      {
        oneOf(storageService).getContentStream(PATH);
        will(returnValue(inputStream));
      }
    });

    assertThat(storage.getContentStream(PATH), is(sameInstance(inputStream)));
  }


  @Test
  public void testGetWrapperKey() throws Exception {
    final Key key = KeyUtil.aesKey(256);
    final KeyDescriptor descriptor = KeyDescriptor.builder()
        .algorithm(AwsKeyStorage.WRAPPER_KEY_ALGORITHM)
        .type(KeyDescriptor.Type.SECRET)
        .build(key.getEncoded());

    context.checking(new Expectations() {
      {
        oneOf(masterKeyService).decryptKey(key.getEncoded());
        will(returnValue(key));
      }
    });
    assertThat(storage.getWrapperKey(Collections.singletonList(descriptor)),
        is(equalTo(key)));
  }

  @Test(expected = KeyUnwrapException.class)
  public void testGetWrapperKeyWhenNotFound() throws Exception {
    storage.getWrapperKey(Collections.emptyList());
  }

  @Test
  public void testNextWrapperKey() throws Exception {
    final byte[] cipherText = KeyUtil.randomKeyData(128);
    final Key key = KeyUtil.aesKey(256);
    final EncryptionKey encryptionKey =
        new KmsEncryptionKey(key.getEncoded(), cipherText, KEY_ID);

    context.checking(new Expectations() {
      {
        oneOf(masterKeyService).newEncryptionKey();
        will(returnValue(encryptionKey));
      }
    });

    final WrapperKeyResponse response = storage.nextWrapperKey();
    assertThat(response.getKey(), is(equalTo(key)));
    final KeyDescriptor descriptor = response.getDescriptor();
    assertThat(descriptor, is(not(nullValue())));
    assertThat(descriptor.getAlgorithm(),
        is(equalTo(AwsKeyStorage.WRAPPER_KEY_ALGORITHM)));
    assertThat(descriptor.getType(),
        is(equalTo(KeyDescriptor.Type.SECRET)));
    assertThat(descriptor.getMetadata().get(AwsKeyStorage.MASTER_KEY_ID_HEADER),
        is(equalTo(KEY_ID)));
  }

  @Test(expected = KeyStorageException.class)
  public void testGetSubjectKeyWhenNoDescriptors() throws Exception {
    storage.getSubjectKey(Collections.emptyList());
  }

  @Test
  public void testGetSubjectKey() throws Exception {
    final KeyDescriptor wrapperKeyDescriptor = KeyDescriptor.builder()
        .algorithm(AwsKeyStorage.WRAPPER_KEY_ALGORITHM)
        .type(KeyDescriptor.Type.SECRET)
        .build(new byte[1]);
    final KeyDescriptor subjectKeyDescriptor = KeyDescriptor.builder()
        .algorithm("AES")
        .type(KeyDescriptor.Type.SECRET)
        .build(new byte[1]);

    assertThat(storage.getSubjectKey(
        Arrays.asList(wrapperKeyDescriptor, subjectKeyDescriptor)),
        is(sameInstance(subjectKeyDescriptor)));
  }

  @Test
  public void testStoreContent() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(storageService).storeContent(Collections.singletonList(blob), PATH);
      }
    });

    storage.storeContent(Collections.singletonList(blob), PATH);
  }

}
