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

/**
 * @author ash
 */
public class Strings {

  public static boolean isEmpty(@Nullable String str) {
    return str == null || str.length() == 0; // string.isEmpty() in Java 6
  }

  public static boolean isNotEmpty(@Nullable String str) {
    return !Strings.isEmpty(str);
  }

  @Nullable
  public static String emptyToNull(@Nullable String string) {
    return isEmpty(string) ? null : string;
  }

}
