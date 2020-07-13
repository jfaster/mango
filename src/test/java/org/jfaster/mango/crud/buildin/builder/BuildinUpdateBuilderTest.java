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

package org.jfaster.mango.crud.buildin.builder;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class BuildinUpdateBuilderTest {

  @Test
  public void build() throws Exception {
    List<String> properties = Lists.newArrayList("id", "userName", "userAge");
    List<String> columns = Lists.newArrayList("id", "user_name", "user_age");
    BuildinUpdateBuilder b = new BuildinUpdateBuilder("id", "id", properties, columns);
    assertThat(b.buildSql(), equalTo("update #table set #if (:userName != null) user_name = :userName,#end #if (:userAge != null) user_age = :userAge,#end #trim_comma where id = :id"));
  }

  @Test
  public void build2() throws Exception {
    List<String> properties = Lists.newArrayList("id", "orderId", "userAge");
    List<String> columns = Lists.newArrayList("id", "order_id", "user_age");
    BuildinUpdateBuilder b = new BuildinUpdateBuilder("orderId", "id", properties, columns);
    assertThat(b.buildSql(), equalTo("update #table set #if (:userAge != null) user_age = :userAge,#end #trim_comma where order_id = :orderId"));
  }

}