/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.util;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * @author ash
 */
public class Joiner {

  public static Joiner on(String separator) {
    return new Joiner(separator);
  }

  public static Joiner on(char separator) {
    return new Joiner(String.valueOf(separator));
  }

  private final String separator;

  private Joiner(String separator) {
    if (separator == null) {
      throw new NullPointerException();
    }
    this.separator = separator;
  }

  private Joiner(Joiner prototype) {
    this.separator = prototype.separator;
  }

  public final String join(Iterable<?> parts) {
    return join(parts.iterator());
  }

  public final String join(Iterator<?> parts) {
    return appendTo(new StringBuilder(), parts).toString();
  }

  public StringBuilder appendTo(StringBuilder builder, Iterator<?> parts) {
    if (parts.hasNext()) {
      builder.append(toString(parts.next()));
      while (parts.hasNext()) {
        builder.append(separator);
        builder.append(toString(parts.next()));
      }
    }
    return builder;
  }

  public Joiner useForNull(final String nullText) {
    if (nullText == null) {
      throw new NullPointerException();
    }
    return new Joiner(this) {
      @Override
      CharSequence toString(@Nullable Object part) {
        return (part == null) ? nullText : Joiner.this.toString(part);
      }

      @Override
      public Joiner useForNull(String nullText) {
        throw new UnsupportedOperationException("already specified useForNull");
      }
    };
  }

  CharSequence toString(Object part) {
    if (part == null) {
      throw new NullPointerException();
    }
    return (part instanceof CharSequence) ? (CharSequence) part : part.toString();
  }

}
