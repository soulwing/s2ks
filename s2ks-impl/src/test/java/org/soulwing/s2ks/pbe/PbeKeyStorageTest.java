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
import static org.hamcrest.Matchers.sameInstance;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import javax.crypto.SecretKey;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.BlobEncoder;
import org.soulwing.s2ks.base.KeyDescriptor;
import org.soulwing.s2ks.base.KeyEncoder;
import org.soulwing.s2ks.KeyStorageException;
import org.soulwing.s2ks.base.KeyWrapOperator;
import org.soulwing.s2ks.base.StorageService;

/**
 * Unit tests for {@link PbeKeyStorage}.
 *
 * @author Carl Harris
 */
public class PbeKeyStorageTest {

  private static final String ID = "id";
  private static final String SUFFIX = "suffix";
  private static final String PATH = "path";
  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  private BlobEncoder blobEncoder;

  @Mock
  private KeyEncoder keyEncoder;

  @Mock
  private KeyWrapOperator keyWrapOperator;

  @Mock
  private StorageService storageService;

  @Mock
  private SecretKey pbeKey;

  @Mock
  private Blob blob;

  private PbeKeyStorage storage;

  @Before
  public void setUp() throws Exception {
    storage = new PbeKeyStorage(blobEncoder, keyEncoder,
        keyWrapOperator, pbeKey, storageService);
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
    assertThat(storage.getWrapperKey(null), is(sameInstance(pbeKey)));
  }

  @Test
  public void testNextWrapperKey() throws Exception {
    assertThat(storage.nextWrapperKey().getKey(), is(sameInstance(pbeKey)));
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

  @Test
  public void testStoreContent() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(storageService).storeContent(Collections.singletonList(blob),
            PATH);
      }
    });

    storage.storeContent(Collections.singletonList(blob), PATH);
  }


}
