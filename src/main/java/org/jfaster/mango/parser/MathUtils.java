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

package org.jfaster.mango.parser;

/**
 * @author ash
 */
public class MathUtils {

  public static int compare(Number op1, Number op2) {
    checkInteger(op1);
    checkInteger(op2);
    long l1 = op1.longValue();
    long l2 = op2.longValue();
    return (l1 < l2) ? -1 : ((l1 > l2) ? 1 : 0);
  }

  private static void checkInteger(Number n) {
    if (!Integer.class.equals(n.getClass()) && !Long.class.equals(n.getClass())) {
      throw new ClassCastException(n.getClass() + " cannot be cast to " + Integer.class + " or " + Long.class);
    }
  }

}
