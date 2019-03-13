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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.action.CustomAction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.BlobEncoder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
 * Unit tests for {@link S3StorageService}.
 *
 * @author Carl Harris
 */
public class S3StorageServiceTest {

  private static final String ID = "id";
  private static final String SUFFIX = ".suffix";
  private static final String BUCKET_NAME = "bucketName";
  private static final String PREFIX = "prefix";

  private static final String PATH = PREFIX + "/" + ID + SUFFIX;

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Mock
  private AmazonS3 s3Client;

  @Mock
  private BlobEncoder blobEncoder;

  @Mock
  private Blob blob1, blob2;

  private S3StorageService storageService;

  @Before
  public void setUp() throws Exception {
    storageService = new S3StorageService(s3Client, BUCKET_NAME, PREFIX,
        blobEncoder);
  }

  @Test
  public void testIdToPath() throws Exception {
    assertThat(storageService.idToPath(ID, SUFFIX), is(equalTo(PATH)));
  }

  @Test
  public void testGetContentStream() throws Exception {
    final S3Object object = new S3Object();
    final S3ObjectInputStream inputStream = new S3ObjectInputStream(
        new ByteArrayInputStream(new byte[0]), null);

    object.setObjectContent(inputStream);
    context.checking(new Expectations() {
      {
        oneOf(s3Client).getObject(BUCKET_NAME, PATH);
        will(returnValue(object));
      }
    });

    assertThat(storageService.getContentStream(PATH),
        is(sameInstance(inputStream)));
  }

  @Test(expected = FileNotFoundException.class)
  public void testGetContentStreamWhenNotFound() throws Exception {
    final AmazonS3Exception ex = new AmazonS3Exception("some error");
    ex.setErrorCode("NoSuchKey");

    context.checking(new Expectations() {
      {
        oneOf(s3Client).getObject(BUCKET_NAME, PATH);
        will(throwException(ex));
      }
    });

    storageService.getContentStream(PATH);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStoreContentWhenNotTwoBlobs() throws Exception {
    storageService.storeContent(Collections.emptyList(), PATH);
  }

  @Test
  public void testStoreContent() throws Exception {
    final byte[] data = KeyUtil.randomKeyData(128);
    final byte[] actual = new byte[data.length];
    context.checking(new Expectations() {
      {
        oneOf(blob1).getContentType();
        will(returnValue("contentType"));
        oneOf(blob1).size();
        will(returnValue(1));
        oneOf(blob2).size();
        will(returnValue(1));

        oneOf(blobEncoder).encode(with(Arrays.asList(blob1, blob2)),
            with(any(OutputStream.class)));
        will(new CustomAction("write content") {
          @Override
          public Object invoke(Invocation invocation) throws Throwable {
            ((OutputStream) invocation.getParameter(1)).write(data);
            return null;
          }
        });

        oneOf(s3Client).putObject(with(BUCKET_NAME), with(PATH),
            with(any(InputStream.class)), with(Matchers.<ObjectMetadata>allOf(
                hasProperty("contentType", equalTo("contentType")),
                hasProperty("contentLength", equalTo(2L)))));
        will(new CustomAction("capture stream") {
          @Override
          public Object invoke(Invocation invocation) throws Throwable {
            final InputStream stream = (InputStream) invocation.getParameter(2);
            assertThat(stream.read(actual), is(equalTo(actual.length)));
            assertThat(stream.read(), is(equalTo(-1)));
            return null;
          }
        });
      }
    });

    storageService.storeContent(Arrays.asList(blob1, blob2), PATH);
    assertThat(actual, is(equalTo(data)));
  }

  @Test
  public void testStoreContentWhenS3Exception() throws Exception {
    final byte[] data = KeyUtil.randomKeyData(128);
    final byte[] actual = new byte[data.length];
    final AmazonS3Exception ex = new AmazonS3Exception("S3 message");
    context.checking(new Expectations() {
      {
        oneOf(blob1).getContentType();
        will(returnValue("contentType"));
        oneOf(blob1).size();
        will(returnValue(1));
        oneOf(blob2).size();
        will(returnValue(1));

        oneOf(blobEncoder).encode(with(Arrays.asList(blob1, blob2)),
            with(any(OutputStream.class)));
        will(new CustomAction("write content") {
          @Override
          public Object invoke(Invocation invocation) throws Throwable {
            ((OutputStream) invocation.getParameter(1)).write(data);
            return null;
          }
        });

        oneOf(s3Client).putObject(with(BUCKET_NAME), with(PATH),
            with(any(InputStream.class)), with(Matchers.<ObjectMetadata>allOf(
                hasProperty("contentType", equalTo("contentType")),
                hasProperty("contentLength", equalTo(2L)))));
        will(throwException(ex));
      }
    });

    expectedException.expect(IOException.class);
    expectedException.expectMessage("S3 message");
    expectedException.expectCause(is(sameInstance(ex)));
    storageService.storeContent(Arrays.asList(blob1, blob2), PATH);
  }

}
