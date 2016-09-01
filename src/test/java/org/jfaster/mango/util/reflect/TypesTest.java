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

package org.jfaster.mango.util.reflect;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class TypesTest {

  @Test
  public void testIsAssignable() throws Exception {
    assertThat(Types.isAssignable(int.class, Integer.class), is(true));
    assertThat(Types.isAssignable(Integer.class, Integer.class), is(true));
    assertThat(Types.isAssignable(Integer.class, int.class), is(true));
    assertThat(Types.isAssignable(int.class, int.class), is(true));

    assertThat(Types.isAssignable(int.class, long.class), is(false));
    assertThat(Types.isAssignable(Integer.class, Long.class), is(false));

    assertThat(Types.isAssignable(A.class, B.class), is(true));
    assertThat(Types.isAssignable(A.class, A.class), is(true));
    assertThat(Types.isAssignable(B.class, B.class), is(true));
    assertThat(Types.isAssignable(B.class, A.class), is(false));

  }

  static class A {

  }

  static class B extends A {

  }

}
