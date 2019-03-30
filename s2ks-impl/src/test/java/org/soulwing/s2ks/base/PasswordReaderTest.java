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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link PasswordReader}.
 *
 * @author Carl Harris
 */
public class PasswordReaderTest {

  static final String PASSWORD = "password";

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  private Path path;

  @Before
  public void setUp() throws Exception {
    path = Files.createTempFile("password", "");
  }

  @After
  public void tearDown() throws Exception {
    Files.deleteIfExists(path);
  }

  @Test
  public void testReadPasswordFile() throws Exception {
    PasswordWriter.writePassword(PASSWORD, path.toFile());
    assertThat(PasswordReader.readPassword(path.toFile()),
        is(equalTo(PASSWORD.toCharArray())));
  }

  @Test
  public void testReadFullyWithTooMuchData() throws Exception {
    final char[] password = new char[PasswordReader.MAX_PASSWORD_LENGTH + 1];
    PasswordWriter.writePassword(password, path.toFile());
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("password");
    PasswordReader.readPassword(path.toFile());
  }

  @Test
  public void testReadFullyWithMaxLengthPassword() throws Exception {
    final char[] password = new char[PasswordReader.MAX_PASSWORD_LENGTH];
    PasswordWriter.writePassword(password, path.toFile());
    PasswordReader.readPassword(path.toFile());
  }

}