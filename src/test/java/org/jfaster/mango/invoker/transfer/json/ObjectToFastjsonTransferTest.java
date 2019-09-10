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

package org.jfaster.mango.invoker.transfer.json;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.jfaster.mango.util.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class ObjectToFastjsonTransferTest {

  @Test
  public void propertyToColumn() {
    ObjectToFastjsonTransfer transfer = new ObjectToFastjsonTransfer();
    assertThat(transfer.propertyToColumn(null), nullValue());
    List<Integer> list = Lists.newArrayList(1, 2, 3);
    assertThat(transfer.propertyToColumn(list), equalTo(JSON.toJSONString(list)));
  }

  @Test
  public void columnToProperty() {
    ObjectToFastjsonTransfer transfer = new ObjectToFastjsonTransfer();
    List<Integer> list = Lists.newArrayList(1, 2, 3);
    Type listType = new TypeToken<List<Integer>>(){}.getType();
    assertThat(transfer.columnToProperty(null, listType), nullValue());
    assertThat(transfer.columnToProperty(JSON.toJSONString(list), listType), equalTo(list));
  }

}