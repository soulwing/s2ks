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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility methods that should have been in {@link Files}.
 *
 * @author Carl Harris
 */
public class FilesUtil {

  /**
   * Recursively delete the given directory.
   * @param directory the directory to delete
   * @throws IOException if an error occurs
   */
  public static void recursivelyDelete(Path directory) throws IOException {
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

}
