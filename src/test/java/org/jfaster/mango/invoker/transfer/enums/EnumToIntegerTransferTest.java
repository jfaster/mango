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

package org.jfaster.mango.invoker.transfer.enums;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class EnumToIntegerTransferTest {

  @Test
  public void propertyToColumn() {
    EnumToIntegerTransfer transfer = new EnumToIntegerTransfer();
    assertThat(transfer.propertyToColumn(null), nullValue());
    assertThat(transfer.propertyToColumn(E.X), equalTo(0));
    assertThat(transfer.propertyToColumn(E.Y), equalTo(1));
    assertThat(transfer.propertyToColumn(E.Z), equalTo(2));
  }

  @Test
  public void columnToProperty() {
    EnumToIntegerTransfer transfer = new EnumToIntegerTransfer();
    assertThat(transfer.columnToProperty(null, E.class), nullValue());
    assertThat(transfer.columnToProperty(0, E.class), equalTo(E.X));
    assertThat(transfer.columnToProperty(1, E.class), equalTo(E.Y));
    assertThat(transfer.columnToProperty(2, E.class), equalTo(E.Z));
  }

  enum E {
    X, Y, Z;
  }
}