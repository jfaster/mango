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

package org.jfaster.mango.type;

import org.jfaster.mango.util.jdbc.JdbcType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class TypeHandlerRegistryTest {

  @Test
  public void test() throws Exception {
    assertThat(TypeHandlerRegistry.getTypeHandler(int.class), notNullValue());
    assertThat(TypeHandlerRegistry.getTypeHandler(Integer.class), notNullValue());
    assertThat(TypeHandlerRegistry.getNullableTypeHandler(StringBuffer.class), nullValue());
    assertThat(TypeHandlerRegistry.getNullableTypeHandler(Integer.class), notNullValue());
    assertThat(TypeHandlerRegistry.getTypeHandler(String.class, JdbcType.CHAR), notNullValue());
    assertThat(TypeHandlerRegistry.getNullableTypeHandler(String.class, JdbcType.DATE), notNullValue());
    assertThat(TypeHandlerRegistry.hasTypeHandler(String.class), notNullValue());
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testException() throws Exception {
    thrown.expect(TypeException.class);
    thrown.expectMessage("Can't get type handle, java type is 'class java.lang.StringBuffer', jdbc type is 'null'");
    TypeHandlerRegistry.getTypeHandler(StringBuffer.class);
  }

}
