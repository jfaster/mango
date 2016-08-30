/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.jfaster.mango.util;

import javax.annotation.Nullable;

/**
 * @author Clinton Begin
 * @author ash
 */
public class PropertyTokenizer {
  private String name;
  private String children;

  public PropertyTokenizer(@Nullable String fullname) {
    if (fullname != null) {
      int delim = fullname.indexOf('.');
      if (delim > -1) {
        name = fullname.substring(0, delim);
        children = fullname.substring(delim + 1);
      } else {
        name = Strings.emptyToNull(fullname);
        children = null;
      }
    }
  }

  @Nullable
  public String getName() {
    return name;
  }

  @Nullable
  public String getChildren() {
    return children;
  }

  public boolean hasCurrent() {
    return name != null;
  }

  public boolean hasNext() {
    return children != null;
  }

  public PropertyTokenizer next() {
    return new PropertyTokenizer(children);
  }

}
