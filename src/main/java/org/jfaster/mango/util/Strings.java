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

  public static String getFullName(String name, String path) {
    return ":" + (Strings.isNotEmpty(path) ? name + "." + path : name);
  }

  public static String underscoreName(String name) {
    if (Strings.isEmpty(name)) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    result.append(name.substring(0, 1).toLowerCase());
    for (int i = 1; i < name.length(); i++) {
      String s = name.substring(i, i + 1);
      String slc = s.toLowerCase();
      if (!s.equals(slc)) {
        result.append("_").append(slc);
      } else {
        result.append(s);
      }
    }
    return result.toString();
  }

  public static String firstLetterToLowerCase(String str) {
    return str.substring(0, 1).toLowerCase() + str.substring(1);
  }

  public static String firstLetterToUpperCase(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

}
