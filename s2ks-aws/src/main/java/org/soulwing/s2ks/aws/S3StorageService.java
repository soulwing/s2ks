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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

import org.soulwing.s2ks.base.Blob;
import org.soulwing.s2ks.base.BlobEncoder;
import org.soulwing.s2ks.base.StorageService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

/**
 * A {@link StorageService} implemented using AWS S3.
 *
 * @author Carl Harris
 */
class S3StorageService implements StorageService {

  private final AmazonS3 s3Client;
  private final String bucketName;
  private final String prefix;
  private final BlobEncoder blobEncoder;

  public S3StorageService(AmazonS3 s3Client, String bucketName, String prefix,
      BlobEncoder blobEncoder) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
    this.prefix = prefix;
    this.blobEncoder = blobEncoder;
  }

  @Override
  public String idToPath(String id, String suffix) {
    return Paths.get(prefix, id + suffix).toString();
  }

  @Override
  public InputStream getContentStream(String path) throws IOException {
    try {
      final S3Object s3Object = s3Client.getObject(bucketName, path);
      return s3Object.getObjectContent();
    }
    catch (AmazonS3Exception ex) {
      if ("NoSuchKey".equals(ex.getErrorCode())) {
        throw new FileNotFoundException();
      }
      throw new IOException(ex.getMessage(), ex);
    }
  }

  @Override
  public void storeContent(List<Blob> blobs, String path) throws IOException {
    final ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(blobs.get(0).getContentType());
    metadata.setContentLength(blobs.stream().mapToInt(Blob::size).sum());

    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    blobEncoder.encode(blobs, outputStream);

    final ByteArrayInputStream inputStream =
        new ByteArrayInputStream(outputStream.toByteArray());

    try {
      s3Client.putObject(bucketName, path, inputStream, metadata);
    }
    catch (AmazonS3Exception ex) {
      throw new IOException(ex.getMessage(), ex);
    }

  }

}
