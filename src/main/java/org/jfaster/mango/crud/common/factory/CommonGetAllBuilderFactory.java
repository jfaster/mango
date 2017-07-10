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

package org.jfaster.mango.crud.common.factory;

import org.jfaster.mango.crud.CrudMeta;
import org.jfaster.mango.crud.common.builder.AbstractCommonBuilder;
import org.jfaster.mango.crud.common.builder.CommonGetAllBuilder;
import org.jfaster.mango.util.reflect.DynamicTokens;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class CommonGetAllBuilderFactory extends AbstractCommonBuilderFactory {

  @Override
  String expectedMethodName() {
    return "getAll";
  }

  @Override
  Type expectedReturnType(Class<?> entityClass) {
    return DynamicTokens.listToken(TypeToken.of(entityClass)).getType();
  }

  @Override
  List<Type> expectedParameterType(Class<?> entityClass, Class<?> idClass) {
    return new ArrayList<Type>();
  }

  @Override
  AbstractCommonBuilder createCommonBuilder(CrudMeta cm) {
    return new CommonGetAllBuilder(cm.getColumns());
  }

}
