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

import java.math.BigInteger;

/**
 * @author ash
 */
public class HashUtil {

  private static final BigInteger INIT32 = new BigInteger("811c9dc5", 16);
  private static final BigInteger PRIME32 = new BigInteger("01000193", 16);
  private static final BigInteger MOD32 = new BigInteger("2").pow(32);

  public static int fnv1_31(String str) {
    BigInteger bi32 = fnv1_32(str.getBytes());
    return bi32.intValue() & 0x7fffffff;
  }

  public static int fnv1_31(long n) {
    return fnv1_31(String.valueOf(n));
  }

  private static BigInteger fnv1_32(byte[] data) {
    BigInteger hash = INIT32;

    for (byte b : data) {
      hash = hash.multiply(PRIME32).mod(MOD32);
      hash = hash.xor(BigInteger.valueOf((int) b & 0xff));
    }

    return hash;
  }

}
