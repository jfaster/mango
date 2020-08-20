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

package org.jfaster.mango.page;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class SortTest {

  @Test
  public void by() throws Exception {
    assertThat(Sort.by("a").toString().intern(), equalTo(" order by a asc"));
    assertThat(Sort.by("a", "b").toString().intern(), equalTo(" order by a asc, b asc"));
    assertThat(Sort.by(Direction.ASC, "a", "b").toString().intern(), equalTo(" order by a asc, b asc"));
    assertThat(Sort.by(Direction.DESC, "a", "b").toString().intern(), equalTo(" order by a desc, b desc"));
    assertThat(Sort.by(Order.by("a"), Order.by(Direction.DESC, "b")).toString().intern(), equalTo(" order by a asc, b desc"));
    assertThat(Sort.by(Arrays.asList(Order.by("a"), Order.by(Direction.DESC, "b"))).toString().intern(), equalTo(" order by a asc, b desc"));
    assertThat(Sort.by(new Order[0]).toString().intern(), equalTo(""));
  }

}