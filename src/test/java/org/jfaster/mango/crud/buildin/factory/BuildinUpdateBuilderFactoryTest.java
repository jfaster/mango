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

package org.jfaster.mango.crud.buildin.factory;

import com.google.common.collect.Lists;
import org.jfaster.mango.crud.Builder;
import org.jfaster.mango.crud.Order;
import org.jfaster.mango.crud.Order2;
import org.jfaster.mango.crud.Order3;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author ash
 */
public class BuildinUpdateBuilderFactoryTest {

  @Test
  public void test() throws Exception {
    BuildinUpdateBuilderFactory factory = new BuildinUpdateBuilderFactory();
    String name = "update";
    Class<?> entityClass = Order.class;
    Class<Integer> idClass = Integer.class;
    Type returnType = int.class;
    List<Type> parameterTypes = Lists.newArrayList((Type) entityClass);
    Builder b = factory.doTryGetBuilder(name, returnType, parameterTypes, entityClass, idClass);
    assertThat(b, notNullValue());
    assertThat(b.buildSql(), equalTo("update #table set #if (:userId != null) userid = :userId,#end #if (:userAge != null) user_age = :userAge,#end #trim_comma where id = :id"));
  }

  @Test
  public void test2() throws Exception {
    BuildinUpdateBuilderFactory factory = new BuildinUpdateBuilderFactory();
    String name = "update";
    Class<?> entityClass = Order2.class;
    Class<Integer> idClass = Integer.class;
    Type returnType = int.class;
    List<Type> parameterTypes = Lists.newArrayList((Type) entityClass);
    Builder b = factory.doTryGetBuilder(name, returnType, parameterTypes, entityClass, idClass);
    assertThat(b, notNullValue());
    assertThat(b.buildSql(), equalTo("update #table set #if (:userId != null) userid = :userId,#end #if (:userAge != null) user_age = :userAge,#end #trim_comma where order_id = :orderId"));
  }

  @Test
  public void test3() throws Exception {
    BuildinUpdateBuilderFactory factory = new BuildinUpdateBuilderFactory();
    String name = "update";
    Class<?> entityClass = Order3.class;
    Class<Integer> idClass = Integer.class;
    Type returnType = int.class;
    List<Type> parameterTypes = Lists.newArrayList((Type) entityClass);
    Builder b = factory.doTryGetBuilder(name, returnType, parameterTypes, entityClass, idClass);
    assertThat(b, notNullValue());
    assertThat(b.buildSql(), equalTo("update #table set #if (:userId != null) userid = :userId,#end #if (:userAge != null) user_age = :userAge,#end #trim_comma where order_id = :orderId"));
  }

}