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

package org.jfaster.mango.invoker.function;

import org.jfaster.mango.invoker.SetterFunction;

import javax.annotation.Nullable;

/**
 * String --> long[]
 *
 * @author ash
 */
public class StringToLongArrayFunction implements SetterFunction<String, long[]> {

  private final static String SEPARATOR = ",";

  @Nullable
  @Override
  public long[] apply(@Nullable String input) {
    if (input == null) {
      return null;
    }
    if (input.length() == 0) {
      return new long[0];
    }
    String[] ss = input.split(SEPARATOR);
    long[] r = new long[ss.length];
    for (int i = 0; i < ss.length; i++) {
      r[i] = Long.parseLong(ss[i]);
    }
    return r;
  }

}
