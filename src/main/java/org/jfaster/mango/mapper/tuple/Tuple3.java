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

package org.jfaster.mango.mapper.tuple;

/**
 * @author ash
 */
public class Tuple3<T1, T2, T3> {

  private final T1 value1;
  private final T2 value2;
  private final T3 value3;

  public Tuple3(T1 value1, T2 value2, T3 value3) {
    this.value1 = value1;
    this.value2 = value2;
    this.value3 = value3;
  }

  public T1 value1() {
    return value1;
  }

  public T2 value2() {
    return value2;
  }

  public T3 value3() {
    return value3;
  }

}
