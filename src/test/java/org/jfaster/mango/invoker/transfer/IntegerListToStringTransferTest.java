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

package org.jfaster.mango.invoker.transfer;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author ash
 */
public class IntegerListToStringTransferTest {

  @Test
  public void propertyToColumn() {
    IntegerListToStringTransfer transfer = (IntegerListToStringTransfer) new PropertyTransferFactory().makeInstanceFromFactory("IntegerListToStringTransfer");
    assertThat(transfer.propertyToColumn(null), nullValue());
    assertThat(transfer.propertyToColumn(Lists.newArrayList()), equalTo(""));
    assertThat(transfer.propertyToColumn(Lists.newArrayList(1,2,3)), equalTo("1,2,3"));
  }

  @Test
  public void columnToProperty() {
    IntegerListToStringTransfer transfer = (IntegerListToStringTransfer) new PropertyTransferFactory().makeInstanceFromFactory("IntegerListToStringTransfer");
    assertThat(transfer.columnToProperty(null, null), nullValue());
    assertThat(transfer.columnToProperty("", null), hasSize(0));
    List<Integer> r = transfer.columnToProperty("1,2,3", null);
    assertThat(r, hasSize(3));
    assertThat(r, hasItems(1, 2, 3));
  }
}