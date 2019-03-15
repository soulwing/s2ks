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

import java.util.Map;
import java.util.Set;

/**
 * An immutable collection of name-value pairs for simple types.
 * <p>
 * Implementations must ensure that the only allowed data types for metadata
 * property values are String, Boolean, Integer, Long, and Double. This should
 * satisfy virtually any legitimate metadata need, while ensuring that metadata
 * can be easily represented using two-dimensional structures.
 *
 * @author Carl Harris
 */
public interface Metadata {

  /**
   * A builder that creates instances of {@link Metadata}.
   */
  interface Builder {

    /**
     * Specifies a metadata property of an arbitrary type, replacing any
     * existing property with the same name.
     * @param name property name
     * @param value property value
     * @return this builder
     * @throws IllegalArgumentException if the value is of a type other
     *    than string, boolean, integer, long, or double
     */
    Builder set(String name, Object value);

    /**
     * Builds a {@link Metadata} object in accordance with the configuration
     * of this builder.
     * @return resulting {@link Metadata} instance
     */
    Metadata build();
  }

  /**
   * Tests whether this metadata object has any properties.
   * @return {@code true} if this metadata object has no properties
   */
  boolean isEmpty();

  /**
   * Gets the names of the properties in this metadata collection.
   * @return set of names
   */
  Set<String> names();

  /**
   * Gets the contents of this metadata collection as a map.
   * @return map representation of this metadata; subsequent changes to the
   *    returned map will have no effect on this metadata collection
   */
  Map<String, Object> toMap();

  /**
   * Gets a metadata property value.
   * <p>
   * As a special case, when retrieving a numeric property value
   * (any of the supported subtypes of {@link Number}, if the stored value is
   * not directly assignable to the specified type, it will be coerced
   * to the specified type using one of methods provided on the Number class
   * for doing so.
   *
   * @param name name of the property to retrieve.
   * @param type property type; the underlying implementation will restrict
   *    property values to String, Boolean, Integer, Long, Double
   * @param <T> return type
   * @return property value or {@code null} if there is no property with the
   *    given name
   * @throws ClassCastException if the value associated with the given name
   *    cannot be cast or coerced to {@code type}.
   */
  <T> T get(String name, Class<? extends T> type);

}
