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

package org.jfaster.mango.stat;

import org.jfaster.mango.util.jdbc.OperatorType;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class MetaStatTest {

  @Test
  public void test() throws Exception {
    MetaStat stat = MetaStat.create();
    Method m = MetaStatTest.class.getDeclaredMethod("test");
    assertThat(m, notNullValue());

    stat.setMethod(m);
    stat.setOperatorType(OperatorType.UPDATE);
    stat.setCacheable(true);
    stat.setUseMultipleKeys(true);
    stat.setCacheNullObject(true);

    assertThat(stat.getMethod(), equalTo(m));
    assertThat(stat.getOperatorType(), equalTo(OperatorType.UPDATE));
    assertThat(stat.isCacheable(), equalTo(true));
    assertThat(stat.isUseMultipleKeys(), equalTo(true));
    assertThat(stat.isCacheNullObject(), equalTo(true));
  }
}
