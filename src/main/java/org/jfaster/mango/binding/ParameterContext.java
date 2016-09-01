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

import org.jfaster.mango.descriptor.ParameterDescriptor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author ash
 */
public interface ParameterContext {

  /**
   * 根据参数位置获得参数名
   */
  public String getParameterNameByPosition(int position);

  /**
   * 获得绑定参数调用器
   */
  public BindingParameterInvoker getBindingParameterInvoker(BindingParameter bindingParameter);

  /**
   * 获得参数描述
   */
  public List<ParameterDescriptor> getParameterDescriptors();

  /**
   * 尝试自动扩展参数名
   */
  @Nullable
  public BindingParameter tryExpandBindingParameter(BindingParameter bindingParameter);

}
