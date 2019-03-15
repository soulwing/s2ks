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
package org.soulwing.s2ks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 * Unit tests for {@link SimpleMetadata}.
 * 
 * @author Carl Harris
 */
public class SimpleMetadataTest {

  @Test
  public void testSuccessfulBuild() throws Exception {

    final Metadata metadata = SimpleMetadata.builder()
        .set("string", "string")
        .set("boolean", true)
        .set("int", 1)
        .set("long", 2L)
        .set("double", 3.0)
        .build();

    assertThat(metadata.isEmpty(), is(false));
    assertThat(metadata.names(), containsInAnyOrder(
        "string", "boolean", "int", "long", "double"));
    assertThat(metadata.get("string", String.class),
        is(equalTo("string")));
    assertThat(metadata.get("boolean", Boolean.class),
        is(equalTo(true)));
    assertThat(metadata.get("int", Integer.class),
        is(equalTo(1)));
    assertThat(metadata.get("long", Long.class),
        is(equalTo(2L)));
    assertThat(metadata.get("double", Double.class),
        is(equalTo(3.0)));


  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithUnsupportedType() throws Exception {
    SimpleMetadata.builder().set("name", new Object());
  }

  @Test
  public void testEmpty() throws Exception {
    assertThat(SimpleMetadata.empty().isEmpty(), is(true));
  }

  @Test
  public void testCoerceInt() throws Exception {
    final Metadata metadata = SimpleMetadata.builder()
        .set("name", 1)
        .build();

    assertThat(metadata.get("name", Long.class), is(equalTo(1L)));
    assertThat(metadata.get("name", Double.class), is(equalTo(1.0)));
  }

  @Test
  public void testCoerceLong() throws Exception {
    final Metadata metadata = SimpleMetadata.builder()
        .set("name", 1L)
        .build();

    assertThat(metadata.get("name", Integer.class), is(equalTo(1)));
    assertThat(metadata.get("name", Double.class), is(equalTo(1.0)));
  }

  @Test
  public void testCoerceDouble() throws Exception {
    final Metadata metadata = SimpleMetadata.builder()
        .set("name", 1.0)
        .build();

    assertThat(metadata.get("name", Integer.class), is(equalTo(1)));
    assertThat(metadata.get("name", Long.class), is(equalTo(1L)));
  }

  @Test(expected = ClassCastException.class)
  public void testWrongTargetType() throws Exception {
    SimpleMetadata.builder()
        .set("name", "string")
        .build()
        .get("name", Integer.class);
  }

  @Test
  public void testMissingProperty() throws Exception {
    final Metadata metadata = SimpleMetadata.builder().build();
    assertThat(metadata.isEmpty(), is(true));
    assertThat(metadata.get("name", Object.class), is(nullValue()));
  }

  @Test
  public void testEqualsAndHashCode() throws Exception {
    final Metadata a = SimpleMetadata.builder().set("name", "value").build();
    final Metadata b = SimpleMetadata.builder().set("name", "value").build();
    final Metadata c = SimpleMetadata.builder().set("name", "otherValue").build();

    assertThat(a, is(equalTo(a)));
    assertThat(a, is(equalTo(b)));
    assertThat(b, is(equalTo(a)));
    assertThat(a.hashCode(), is(equalTo(b.hashCode())));

    assertThat(a, is(not(equalTo(c))));
  }

}