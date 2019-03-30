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
package org.soulwing.s2ks.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Utility methods for writing a password to a file or stream.
 *
 * @author Carl Harris
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PasswordWriter {

  /**
   * Writes a password to an output stream.
   * @param password the password to write
   * @param file the target file
   * @throws IOException if an I/O error occurs
   */
  public static void writePassword(String password, File file)
      throws IOException {
    final FileOutputStream outputStream = new FileOutputStream(file);
    writePassword(password.toCharArray(), outputStream);
  }

  /**
   * Writes a password to an output stream.
   * @param password the password to write
   * @param file the target file
   * @throws IOException if an I/O error occurs
   */
  public static void writePassword(char[] password, File file)
      throws IOException {
    final FileOutputStream outputStream = new FileOutputStream(file);
    writePassword(password, outputStream);
  }

  /**
   * Writes a password to an output stream.
   * @param password the password to write
   * @param outputStream the target output stream
   * @throws IOException if an I/O error occurs
   */
  private static void writePassword(char[] password, OutputStream outputStream)
      throws IOException {
    try (final Writer writer = new OutputStreamWriter(
        outputStream, PasswordReader.CHARSET)) {
      writer.write(password);
    }
  }

}
