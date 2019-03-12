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
package org.soulwing.s2ks.filesystem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.action.CustomAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.soulwing.s2ks.Blob;
import org.soulwing.s2ks.BlobEncoder;
import org.soulwing.s2ks.FilesUtil;
import org.soulwing.s2ks.KeyUtil;

/**
 * Unit tests for {@link FilesystemStorageService}.
 *
 * @author Carl Harris
 */
public class FilesystemStorageServiceTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Mock
  private Blob blob;

  private MockBlobEncoder blobEncoder = new MockBlobEncoder();

  private Path parent, directory;

  private FilesystemStorageService storageService;

  @Before
  public void setUp() throws Exception {
    parent = Files.createTempDirectory(getClass().getSimpleName());
    directory = parent.resolve("a/b/c");
    storageService = new FilesystemStorageService(
        directory, blobEncoder);
  }

  @After
  public void tearDown() throws Exception {
    FilesUtil.recursivelyDelete(parent);
  }

  @Test
  public void testStoreAndRetrieve() throws Exception {
    final byte[] expected = KeyUtil.randomKeyData(128);
    blobEncoder.data = expected;

    final String path =
        storageService.idToPath(UUID.randomUUID().toString(), ".test");

    storageService.storeContent(Collections.singletonList(blob), path);
    assertThat(blobEncoder.blobs, is(equalTo(Collections.singletonList(blob))));

    final byte[] actual = IOUtils.toByteArray(storageService.getContentStream(path));
    assertThat(actual, is(equalTo(expected)));
  }

  private static class MockBlobEncoder implements BlobEncoder {

    private List<Blob> blobs;
    private byte[] data;

    @Override
    public void encode(List<Blob> blobs, OutputStream outputStream)
        throws IOException {
      outputStream.write(data);
      this.blobs = blobs;
    }

    @Override
    public List<Blob> decode(InputStream inputStream)
        throws IOException {
      throw new UnsupportedOperationException("not implemented");
    }

  }

}