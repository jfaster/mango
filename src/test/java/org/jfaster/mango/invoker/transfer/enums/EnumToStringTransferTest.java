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
public class EnumToStringTransferTest {

  @Test
  public void propertyToColumn() {
    EnumToStringTransfer transfer = new EnumToStringTransfer();
    assertThat(transfer.propertyToColumn(null), nullValue());
    assertThat(transfer.propertyToColumn(E.X), equalTo("X"));
    assertThat(transfer.propertyToColumn(E.Y), equalTo("Y"));
    assertThat(transfer.propertyToColumn(E.Z), equalTo("Z"));
  }

  @Test
  public void columnToProperty() {
    EnumToStringTransfer transfer = new EnumToStringTransfer();
    assertThat(transfer.columnToProperty(null, E.class), nullValue());
    assertThat(transfer.columnToProperty("X", E.class), equalTo(E.X));
    assertThat(transfer.columnToProperty("Y", E.class), equalTo(E.Y));
    assertThat(transfer.columnToProperty("Z", E.class), equalTo(E.Z));
  }

  enum E {
    X, Y, Z;
  }
}