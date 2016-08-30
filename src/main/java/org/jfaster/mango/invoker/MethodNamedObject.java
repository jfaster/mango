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

package org.jfaster.mango.invoker;

import java.lang.reflect.Method;

/**
 * @author ash
 */
public abstract class MethodNamedObject implements NamedObject {

  protected String name;
  protected Method method;

  protected MethodNamedObject(String name, Method method) {
    this.name = name;
    this.method = method;
    handleMethod(method);
  }

  @Override
  public String getName() {
    return name;
  }

  private void handleMethod(Method method) {
    method.setAccessible(true); // 提高反射速度
  }

}
