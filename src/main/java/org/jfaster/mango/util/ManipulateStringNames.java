package org.jfaster.mango.util;

public class ManipulateStringNames {
  private ManipulateStringNames() {
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
