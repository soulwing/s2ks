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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Utility methods for reading a password from a file or stream.
 *
 * @author Carl Harris
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PasswordReader {

  public static final Charset CHARSET = StandardCharsets.US_ASCII;
  public static final int MAX_PASSWORD_LENGTH = 1024;

  /**
   * Reads a password from a file.
   * <p>
   * The sequence of {@link #CHARSET} characters leading up to the first
   * newline character or at most {@link #MAX_PASSWORD_LENGTH} characters are
   * used as the password.
   *
   * @param file the file to read
   * @return password array (possibly empty, but not null)
   * @throws IOException if an error occurs in reading the file
   */
  public static char[] readPassword(File file) throws IOException {
    try (final InputStream inputStream = new FileInputStream(file)) {
      return readPassword(inputStream);
    }
  }

  /**
   * Reads a password from an input stream.
   * <p>
   * The sequence of {@link #CHARSET} characters leading up to the first
   * newline character or at most {@link #MAX_PASSWORD_LENGTH} characters are
   * used as the password.
   *
   * @param inputStream the source input stream
   * @return password array (possibly empty, but not null)
   * @throws IOException if an error occurs in reading the file
   */
  public static char[] readPassword(InputStream inputStream) throws IOException {
    final Reader reader = new InputStreamReader(inputStream, CHARSET);
    final char[] buf = new char[MAX_PASSWORD_LENGTH];
    int numRead = reader.read(buf);
    if (numRead == buf.length && reader.read() != -1) {
      throw new IllegalArgumentException(
          "password must not be longer than " + buf.length + " characters");
    }
    int length = 0;
    while (length < numRead && buf[length] != '\r' && buf[length] != '\n') {
      length++;
    }

    final char[] password = Arrays.copyOfRange(buf, 0, length);
    Arrays.fill(buf, (char) 0);
    return password;
  }

}
