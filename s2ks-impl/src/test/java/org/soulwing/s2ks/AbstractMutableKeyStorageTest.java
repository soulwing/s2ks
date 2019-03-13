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
package org.soulwing.s2ks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.soulwing.s2ks.base.AbstractMutableKeyStorage;
import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.BlobEncoder;
import org.soulwing.s2ks.base.KeyDescriptor;
import org.soulwing.s2ks.base.KeyEncoder;
import org.soulwing.s2ks.base.KeyWrapOperator;
import org.soulwing.s2ks.base.WrapperKeyResponse;

/**
 * Unit tests for {@link AbstractMutableKeyStorage}.
 *
 * @author Carl Harris
 */
public class AbstractMutableKeyStorageTest {

  private static final String SUFFIX = ".suffix";
  private static final String ID = UUID.randomUUID().toString();

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Mock
  private BlobEncoder blobEncoder;

  @Mock
  private KeyEncoder keyEncoder;

  @Mock
  private KeyWrapOperator keyWrapOperator;

  @Mock
  private Key wrapperKey;

  @Mock
  private Blob subjectBlob, wrapperBlob;

  private Key subjectKey = KeyUtil.aesKey(256);

  private KeyDescriptor subjectKeyDescriptor, wrapperKeyDescriptor;

  private ByteArrayInputStream contentStream =
      new ByteArrayInputStream(new byte[0]);


  private MockKeyStorage storage;

  @Before
  public void setUp() throws Exception {
    subjectKeyDescriptor = KeyDescriptor.builder()
        .algorithm("ALGORITHM")
        .type(KeyDescriptor.Type.SECRET)
        .build(subjectKey.getEncoded());

    wrapperKeyDescriptor = KeyDescriptor.builder()
        .algorithm("ALGORITHM")
        .type(KeyDescriptor.Type.SECRET)
        .build(subjectKey.getEncoded());

    storage  = new MockKeyStorage(
        blobEncoder, keyEncoder, keyWrapOperator, wrapperKey,
        contentStream, subjectKeyDescriptor);

    context.checking(new Expectations() {
      {
        allowing(keyEncoder).getPathSuffix();
        will(returnValue(SUFFIX));
      }
    });
  }

  @Test
  public void testRetrieve() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(blobEncoder).decode(contentStream);
        will(returnValue(Collections.singletonList(subjectBlob)));
        oneOf(keyEncoder).decode(subjectBlob);
        will(returnValue(subjectKeyDescriptor));
        oneOf(keyWrapOperator).unwrap(subjectKeyDescriptor, wrapperKey);
        will(returnValue(subjectKey));
      }
    });

    final Key actual = storage.retrieve(ID);
    assertThat(actual, is(sameInstance(subjectKey)));
  }

  @Test
  public void testRetrieveWhenIOException() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(blobEncoder).decode(contentStream);
        will(returnValue(Collections.singletonList(subjectBlob)));
      }
    });

    storage.ioException = new IOException("I/O error");
    expectedException.expect(KeyStorageException.class);
    expectedException.expectCause(is(sameInstance(storage.ioException)));
    expectedException.expectMessage("I/O error");

    storage.retrieve(ID);
  }

  @Test
  public void testRetrieveWhenFileNotFoundException() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(blobEncoder).decode(contentStream);
        will(returnValue(Collections.singletonList(subjectBlob)));
      }
    });

    storage.ioException = new FileNotFoundException();
    expectedException.expect(NoSuchKeyException.class);
    expectedException.expectMessage(ID);

    storage.retrieve(ID);
  }

  @Test
  public void testStore() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(keyWrapOperator).wrap(subjectKey, wrapperKey);
        will(returnValue(subjectKeyDescriptor));
        oneOf(keyEncoder).encode(subjectKeyDescriptor);
        will(returnValue(subjectBlob));
      }
    });

    storage.store(ID, subjectKey);

    assertThat(storage.blobs, is(equalTo(Collections.singletonList(subjectBlob))));
    assertThat(storage.path, startsWith(ID));
    assertThat(storage.path, endsWith(SUFFIX));
  }

  @Test
  public void testStoreWhenIncludesKeyDescriptor() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(keyEncoder).encode(wrapperKeyDescriptor);
        will(returnValue(wrapperBlob));
        oneOf(keyWrapOperator).wrap(subjectKey, wrapperKey);
        will(returnValue(subjectKeyDescriptor));
        oneOf(keyEncoder).encode(subjectKeyDescriptor);
        will(returnValue(subjectBlob));
      }
    });

    storage.response = WrapperKeyResponse.with(wrapperKey, wrapperKeyDescriptor);
    storage.store(ID, subjectKey);

    assertThat(storage.blobs,
        is(equalTo(Arrays.asList(wrapperBlob, subjectBlob))));
    assertThat(storage.path, startsWith(ID));
    assertThat(storage.path, endsWith(SUFFIX));
  }


  @Test
  public void testStoreWhenIOException() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(keyWrapOperator).wrap(subjectKey, wrapperKey);
        will(returnValue(subjectKeyDescriptor));
        oneOf(keyEncoder).encode(subjectKeyDescriptor);
        will(returnValue(subjectBlob));
      }
    });

    storage.ioException = new IOException("I/O error");
    expectedException.expect(KeyStorageException.class);
    expectedException.expectCause(is(sameInstance(storage.ioException)));
    expectedException.expectMessage("I/O error");

    storage.store(ID, subjectKey);

    assertThat(storage.blobs, is(equalTo(Collections.singletonList(subjectBlob))));
    assertThat(storage.path, startsWith(ID));
  }


  private static class MockKeyStorage extends AbstractMutableKeyStorage {

    private final Key wrapperKey;
    private final InputStream contentStream;
    private final KeyDescriptor subjectKeyDescriptor;

    private WrapperKeyResponse response;
    private List<Blob> blobs;
    private String path;
    private IOException ioException;

    MockKeyStorage(BlobEncoder blobEncoder, KeyEncoder keyEncoder,
        KeyWrapOperator keyWrapOperator, Key wrapperKey,
        InputStream contentStream,
        KeyDescriptor subjectKeyDescriptor) {
      super(blobEncoder, keyEncoder, keyWrapOperator);
      this.wrapperKey = wrapperKey;
      this.contentStream = contentStream;
      this.subjectKeyDescriptor = subjectKeyDescriptor;
      this.response = WrapperKeyResponse.with(wrapperKey);
    }

    @Override
    protected WrapperKeyResponse nextWrapperKey() {
      return response;
    }

    @Override
    protected void storeContent(List<Blob> blobs, String path) throws IOException {
      assertThat(path, endsWith(SUFFIX));
      this.blobs = blobs;
      this.path = path;
      if (ioException != null) throw ioException;
    }

    @Override
    protected String idToPath(String id, String suffix) {
      assertThat(suffix, is(sameInstance(SUFFIX)));
      return id + suffix;
    }

    @Override
    protected InputStream getContentStream(String path) throws IOException {
      assertThat(path, endsWith(SUFFIX));
      if (ioException != null) throw ioException;
      return contentStream;
    }

    @Override
    protected Key getWrapperKey(List<KeyDescriptor> descriptors) {
      return wrapperKey;
    }

    @Override
    protected KeyDescriptor getSubjectKey(List<KeyDescriptor> descriptors) {
      return subjectKeyDescriptor;
    }

  }
}
