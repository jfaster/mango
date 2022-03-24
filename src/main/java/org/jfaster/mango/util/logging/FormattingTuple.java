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

package org.jfaster.mango.util.logging;

/**
 * Holds the results of formatting done by {@link org.jfaster.mango.util.logging.MessageFormatter}.
 */
class FormattingTuple {

  static final FormattingTuple NULL = new FormattingTuple(null);

  private final String message;
  private final Throwable throwable;
  private final Object[] argArray;

  FormattingTuple(String message) {
    this(message, null, null);
  }

  FormattingTuple(String message, Object[] argArray, Throwable throwable) {
    this.message = message;
    this.throwable = throwable;
    if (throwable == null) {
      this.argArray = argArray;
    } else {
      this.argArray = trimmedCopy(argArray);
    }
  }

  static Object[] trimmedCopy(Object[] argArray) {
    if (argArray == null || argArray.length == 0) {
      throw new IllegalStateException("non-sensical empty or null argument array");
    }
    final int trimmedLen = argArray.length - 1;
    Object[] trimmed = new Object[trimmedLen];
    System.arraycopy(argArray, 0, trimmed, 0, trimmedLen);
    return trimmed;
  }

  public String getMessage() {
    return message;
  }

  public Object[] getArgArray() {
    return argArray;
  }

  public Throwable getThrowable() {
    return throwable;
  }
}
