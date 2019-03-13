/*
 * File created on Mar 13, 2019
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
package org.soulwing.s2ks.base;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * An abstraction of a storage service that stores arbitrary objects
 * represented by byte streams.
 *
 * @author Carl Harris
 */
public interface StorageService {

  /**
   * Transforms a key ID and a suffix into a suitable path name for the
   * storage provider.
   * @param id key ID
   * @param suffix name suffix
   * @return corresponding path name
   */
  String idToPath(String id, String suffix);

  /**
   * Gets an input stream that can be used to retrieve the content of a stored
   * object.
   * @param path path to the object to be retrieved
   * @return input stream
   * @throws FileNotFoundException if the object cannot be found
   * @throws IOException if some other error occurs in opening the stream
   */
  InputStream getContentStream(String path)
      throws FileNotFoundException, IOException;

  /**
   * Stores the contents of the given blobs as a single object at the location
   * identified by the given path, overwriting any existing content at that path.
   * <p>
   * The blobs are to be concatenated in the output in the order in which they
   * appear in the list.
   * @param blobs the blobs to be stored
   * @param path path identifying the storage location
   * @throws IOException if an error occurs in storing the content
   */
  void storeContent(List<Blob> blobs, String path) throws IOException;

}
