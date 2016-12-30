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

package org.jfaster.mango.descriptor;

import java.lang.reflect.Method;

/**
 * @author ash
 */
public class ParamNameResolver {

  private static final String PARAMETER_CLASS = "java.lang.reflect.Parameter";
  private static Method GET_NAME;
  private static Method GET_PARAMS;

  static {
    try {
      Class<?> paramClass = Class.forName(PARAMETER_CLASS);
      GET_NAME = paramClass.getMethod("getName");
      GET_PARAMS = Method.class.getMethod("getParameters");
    } catch (Exception e) {
      // ignore
    }
  }

  public static String getActualParamName(Method method, int paramIndex) {
    if (GET_PARAMS == null) {
      return null;
    }
    try {
      Object[] params = (Object[]) GET_PARAMS.invoke(method);
      return (String) GET_NAME.invoke(params[paramIndex]);
    } catch (Exception e) {
      throw new IllegalStateException("Error occurred when invoking Method#getParameters().", e);
    }
  }

}
