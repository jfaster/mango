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

import java.lang.reflect.Type;

/**
 * @author ash
 */
public interface BindingParameterInvoker {

  /**
   * 获得目标类型
   */
  public Type getTargetType();

  /**
   * 执行get方法链
   */
  public Object invoke(Object obj);

  /**
   * 获得绑定参数
   */
  public BindingParameter getBindingParameter();

}
