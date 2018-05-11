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

package org.jfaster.mango.crud.custom.parser;

import org.jfaster.mango.crud.custom.parser.op.EqualsOp;
import org.jfaster.mango.crud.custom.parser.op.LessThanOp;
import org.jfaster.mango.crud.custom.parser.op.Op;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class MethodNameParserTest {

  @Test
  public void parse() throws Exception {
    MethodNameInfo info = MethodNameParser.parse("EmailAddressAndLastnameLessThanOrIdOrderByUserIdDesc");
    assertThat(info.getLogics(), equalTo(Arrays.asList("and", "or")));
    OrderUnit ou = info.getOrderUnit();
    assertThat(ou.getOrderType(), equalTo(OrderType.DESC));
    assertThat(ou.getProperty(), equalTo("userId"));
    assertThat(ou.getOrderStrSize(), equalTo(17));
    List<OpUnit> ous = info.getOpUnits();
    assertThat(ous.size(), equalTo(3));
    assertThat(ous.get(0).getOp(), equalTo((Op) new EqualsOp()));
    assertThat(ous.get(0).getProperty(), equalTo("emailAddress"));
    assertThat(ous.get(1).getOp(), equalTo((Op) new LessThanOp()));
    assertThat(ous.get(1).getProperty(), equalTo("lastname"));
    assertThat(ous.get(2).getOp(), equalTo((Op) new EqualsOp()));
    assertThat(ous.get(2).getProperty(), equalTo("id"));
  }

  @Test
  public void parseOrderUnit() throws Exception {
    OrderUnit ou = MethodNameParser.parseOrderUnit("AgeAndNameOrderByIdDesc");
    assertThat(ou.getProperty(), equalTo("id"));
    assertThat(ou.getOrderType(), equalTo(OrderType.DESC));
    assertThat(ou.getOrderStrSize(), equalTo("OrderByIdDesc".length()));

    ou = MethodNameParser.parseOrderUnit("AgeAndNameOrderByUserNameAsc");
    assertThat(ou.getProperty(), equalTo("userName"));
    assertThat(ou.getOrderType(), equalTo(OrderType.ASC));
    assertThat(ou.getOrderStrSize(), equalTo("OrderByUserNameAsc".length()));

    ou = MethodNameParser.parseOrderUnit("AgeAndNameOrderByAgeU");
    assertThat(ou.getProperty(), equalTo("ageU"));
    assertThat(ou.getOrderType(), equalTo(OrderType.NONE));
    assertThat(ou.getOrderStrSize(), equalTo("OrderByAgeU".length()));
  }

}