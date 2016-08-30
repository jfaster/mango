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

package org.jfaster.mango.support;

import org.apache.commons.lang.math.RandomUtils;

/**
 * @author ash
 */
public class Randoms {

  public static String randomString(int maxLength) {
    int length = randomInt(maxLength) + 1;
    StringBuffer buffer = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    StringBuffer sb = new StringBuffer();
    int range = buffer.length();
    for (int i = 0; i < length; i++) {
      sb.append(buffer.charAt(randomInt(range)));
    }
    return sb.toString();
  }

  public static int randomInt(int n) {
    return RandomUtils.nextInt(n);
  }

}
