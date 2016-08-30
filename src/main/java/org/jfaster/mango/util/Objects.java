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
public class Objects {

  public static boolean equal(@Nullable Object a, @Nullable Object b) {
    return a == b || (a != null && a.equals(b));
  }

  public static int hashCode(@Nullable Object... objects) {
    return java.util.Arrays.hashCode(objects);
  }

}
