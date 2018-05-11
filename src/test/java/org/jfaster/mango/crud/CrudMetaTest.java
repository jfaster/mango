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

package org.jfaster.mango.crud;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class CrudMetaTest {

  @Test
  public void test() throws Exception {
    CrudMeta cm = new CrudMeta(Order.class);
    assertThat(cm.getPropertyId(), equalTo("id"));
    assertThat(cm.getColumnId(), equalTo("id"));
    List<String> props = Lists.newArrayList("id", "userId", "userAge");
    assertThat(cm.getProperties(), equalTo(props));
    List<String> cols = Lists.newArrayList("id", "userid", "user_age");
    assertThat(cm.getColumns(), equalTo(cols));
  }

}