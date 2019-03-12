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
package org.soulwing.s2ks.pem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bouncycastle.util.io.pem.PemHeader;
import org.bouncycastle.util.io.pem.PemObject;
import org.soulwing.s2ks.Blob;
import org.soulwing.s2ks.KeyDecodeException;
import org.soulwing.s2ks.KeyDescriptor;
import org.soulwing.s2ks.KeyEncodeException;
import org.soulwing.s2ks.KeyEncoder;

/**
 * A {@link KeyEncoder} for PEM encoding.
 *
 * @author Carl Harris
 */
public class PemEncoder implements KeyEncoder {

  private static final PemEncoder INSTANCE = new PemEncoder();

  private static final String TYPES =
      Arrays.stream(KeyDescriptor.Type.values())
          .map(Enum::name)
          .collect(Collectors.joining("|"));

  private static final Pattern TYPE_PATTERN = Pattern.compile(
      "^([A-Za-z0-9_-]+) (" + TYPES + ") KEY$");

  /**
   * Gets the singleton instance.
   * @return encoder instance
   */
  public static PemEncoder getInstance() {
    return INSTANCE;
  }

  private PemEncoder() { }

  @Override
  public Blob encode(KeyDescriptor descriptor) throws KeyEncodeException {

    final Map<String, String> metadata = descriptor.getMetadata();
    final List<PemHeader> headers = new ArrayList<>();

    metadata.keySet().stream()
        .map(k -> new PemHeader(k, metadata.get(k)))
        .forEach(headers::add);

    final String type =
        descriptor.getAlgorithm() + " " + descriptor.getType() + " KEY";

    return new PemBlob(new PemObject(type, headers, descriptor.getKeyData()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public KeyDescriptor decode(Blob blob) throws KeyDecodeException {
    if (!(blob instanceof PemBlob)) {
      throw new IllegalArgumentException("requires a PEM blob");
    }
    final PemObject object = ((PemBlob) blob).getDelegate();

    final Matcher matcher = TYPE_PATTERN.matcher(object.getType());
    if (!matcher.matches()) {
      throw new KeyDecodeException("`" + object.getType() +
          "` is not a supported PEM object type");
    }

    final KeyDescriptor.Builder builder = KeyDescriptor.builder()
        .algorithm(matcher.group(1))
        .type(KeyDescriptor.Type.valueOf(matcher.group(2)));

    ((List<PemHeader>) object.getHeaders())
        .forEach(h -> builder.metadata(h.getName(), h.getValue()));

    return builder.build(object.getContent());
  }

}
