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

import org.jfaster.mango.util.Strings;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ash
 */
public class MethodNameParser {

  private static final String DESC = "Desc";

  private static final String ASC = "Asc";

  private static final String ORDER_BY = "OrderBy";

  private static final String ORDER_BY_REGEX = ORDER_BY + "[A-Z]";

  private static final String LOGIC_REGEX = "((And)|(Or))(?=[A-Z])";

  public static MethodNameInfo parse(String str) {
    OrderUnit ou = parseOrderUnit(str);
    if (ou != null) {
      str = str.substring(0, str.length() - ou.getOrderStrSize());
    }
    List<OpUnit> opUnits = new ArrayList<OpUnit>();
    List<String> logics = new ArrayList<String>();
    Pattern p = Pattern.compile(LOGIC_REGEX);
    Matcher m = p.matcher(str);
    int index = 0;
    while (m.find()) {
      opUnits.add(OpUnit.create(str.substring(index, m.start())));
      logics.add(Strings.firstLetterToLowerCase(m.group()));
      index = m.end();
    }
    opUnits.add(OpUnit.create(str.substring(index)));
    return new MethodNameInfo(opUnits, logics, ou);
  }

  @Nullable
  static OrderUnit parseOrderUnit(String str) {
    Pattern p = Pattern.compile(ORDER_BY_REGEX);
    Matcher m = p.matcher(str);
    if (m.find()) {
      String tailStr = Strings.firstLetterToLowerCase(str.substring(m.end() - 1));
      int size = ORDER_BY.length() + tailStr.length();
      String property;
      OrderType orderType;
      if (tailStr.endsWith(DESC)) {
        property = tailStr.substring(0, tailStr.length() - DESC.length());
        orderType = OrderType.DESC;
      } else if (tailStr.endsWith(ASC)) {
        property = tailStr.substring(0, tailStr.length() - ASC.length());
        orderType = OrderType.ASC;
      } else {
        property = tailStr;
        orderType = OrderType.NONE;
      }
      return new OrderUnit(property, orderType, size);
    }
    return null;
  }

}
