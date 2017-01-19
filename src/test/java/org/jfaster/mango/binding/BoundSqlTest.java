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

package org.jfaster.mango.binding;

import com.google.common.collect.Lists;
import org.jfaster.mango.type.IntegerTypeHandler;
import org.jfaster.mango.type.LongTypeHandler;
import org.jfaster.mango.type.StringTypeHandler;
import org.jfaster.mango.type.TypeHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class BoundSqlTest {

  @Test
  public void test() throws Exception {
    String sql = "select xxx";
    BoundSql bs = new BoundSql(sql);
    List<Object> args = new ArrayList<Object>();
    List<TypeHandler<?>> typeHandlers = new ArrayList<TypeHandler<?>>();
    List<Class<? extends TypeHandler>> typeHandlerClasses = new ArrayList<Class<? extends TypeHandler>>();

    args.add(1);
    typeHandlers.add(new IntegerTypeHandler());
    typeHandlerClasses.add(IntegerTypeHandler.class);
    args.add(null);
    typeHandlers.add(new LongTypeHandler());
    typeHandlerClasses.add(LongTypeHandler.class);
    args.add("ash");
    typeHandlers.add(new StringTypeHandler());
    typeHandlerClasses.add(StringTypeHandler.class);

    for (Object arg : args) {
      if (arg == null) {
        bs.addNullArg(Long.class);
      } else {
        bs.addArg(arg);
      }
    }
    assertThat(bs.getSql(), equalTo(sql));
    assertThat(bs.getArgs(), equalTo(args));
    List<Class<? extends TypeHandler>> newTypeHandlerClasses = new ArrayList<Class<? extends TypeHandler>>();
    for (TypeHandler<?> typeHandler : bs.getTypeHandlers()) {
      newTypeHandlerClasses.add(typeHandler.getClass());
    }
    assertThat(newTypeHandlerClasses, equalTo(typeHandlerClasses));

    bs = new BoundSql(sql, Lists.newArrayList(args), Lists.newArrayList(typeHandlers));
    bs.addArg(0, "lucy");
    bs.addNullArg(1, String.class);

    args.add(0, "lucy");
    args.add(1, null);
    assertThat(bs.getSql(), equalTo(sql));
    assertThat(bs.getArgs(), equalTo(args));
    typeHandlerClasses.add(0, StringTypeHandler.class);
    typeHandlerClasses.add(1, StringTypeHandler.class);
    newTypeHandlerClasses = new ArrayList<Class<? extends TypeHandler>>();
    for (TypeHandler<?> typeHandler : bs.getTypeHandlers()) {
      newTypeHandlerClasses.add(typeHandler.getClass());
    }
    assertThat(newTypeHandlerClasses, equalTo(typeHandlerClasses));

    sql = "update ...";
    bs.setSql(sql);
    assertThat(bs.getSql(), equalTo(sql));
  }

}
