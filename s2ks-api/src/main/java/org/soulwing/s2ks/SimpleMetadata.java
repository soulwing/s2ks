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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An immutable metadata value holder backed by a map.
 *
 * @author Carl Harris
 */
public final class SimpleMetadata implements Metadata {

  private static final SimpleMetadata EMPTY =
      new SimpleMetadata(Collections.emptyMap());

  private final Map<String, Object> delegate;

  private SimpleMetadata(Map<String, Object> delegate) {
    this.delegate = delegate;
  }

  /**
   * A builder that produces {@link SimpleMetadata} instances.
   */
  public static class Builder implements Metadata.Builder {

    private Map<String, Object> metadata = new HashMap<>();

    private Builder() { }

    @Override
    public Metadata.Builder set(String name, Object value) {
      if (value instanceof String) {
        metadata.put(name, value);
      }
      else if (value instanceof Integer) {
        metadata.put(name, value);
      }
      else if (value instanceof Long) {
        metadata.put(name, value);
      }
      else if (value instanceof Boolean) {
        metadata.put(name, value);
      }
      else if (value instanceof Double) {
        metadata.put(name, value);
      }
      else {
        throw new IllegalArgumentException(
            "value type must be string, boolean, integer, long, or double; "
                + "not " + value.getClass().getSimpleName());
      }
      return this;
    }

    @Override
    public Metadata build() {
      return new SimpleMetadata(metadata);
    }

  }

  /**
   * Gets an instance that is empty.
   * @return empty instance
   */
  public static Metadata empty() {
    return EMPTY;
  }

  /**
   * Gets a builder that builds a new instance.
   * @return builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public Set<String> names() {
    return delegate.keySet();
  }

  @Override
  public Map<String, Object> toMap() {
    return new HashMap<>(delegate);
  }

  @Override
  public <T> T get(String name, Class<? extends T> type) {
    Object value = delegate.get(name);
    if (Number.class.isAssignableFrom(type)
        && value instanceof Number
        && !type.isInstance(value)) {
      if (Integer.class.equals(type)) {
        value = ((Number) value).intValue();
      }
      else if (Long.class.equals(type)) {
        value = ((Number) value).longValue();
      }
      else if (Double.class.equals(type)) {
        value = ((Number) value).doubleValue();
      }
    }
    return type.cast(value);
  }

  @Override
  public int hashCode() {
    return toMap().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this
        || (obj instanceof Metadata
            && ((Metadata) obj).toMap().equals(toMap()));
  }

}
