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

package org.jfaster.mango.reflect.descriptor;

import java.lang.reflect.Method;

/**
 * 根据参数序号定义参数名字
 *
 * @author ash
 */
public class SerialNumberParameterNameDiscover implements ParameterNameDiscover {

  @Override
  public String[] getParameterNames(Method method) {
    String[] names = new String[method.getGenericParameterTypes().length];
    for (int i = 0; i < names.length; i++) {
      names[i] = String.valueOf(i + 1);
    }
    return names;
  }

}
