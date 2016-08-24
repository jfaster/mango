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

import org.jfaster.mango.base.Strings;
import org.jfaster.mango.invoker.FunctionalGetterInvokerGroup;
import org.jfaster.mango.invoker.GetterInvokerGroup;
import org.jfaster.mango.invoker.UnreachablePropertyException;
import org.jfaster.mango.reflect.ParameterDescriptor;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ash
 */
public class ParameterContext {

    private final List<ParameterDescriptor> parameterDescriptors;
    private final Map<String, Type> typeMap = new LinkedHashMap<String, Type>();

    public ParameterContext(List<ParameterDescriptor> parameterDescriptors, NameProvider nameProvider) {
        this.parameterDescriptors = parameterDescriptors;
        for (int i = 0; i < parameterDescriptors.size(); i++) {
            ParameterDescriptor pd = parameterDescriptors.get(i);
            String parameterName = nameProvider.getParameterName(i);
            typeMap.put(parameterName, pd.getType());
        }
    }

    /**
     * 获得getter调用器
     */
    public GetterInvokerGroup getInvokerGroup(BindingParameter bindingParameter) {
        String parameterName = bindingParameter.getParameterName();
        String propertyPath = bindingParameter.getPropertyPath();
        Type type = typeMap.get(parameterName);
        if (type == null) {
            throw new BindingException("Parameter '" + parameterName + "' not found, " +
                    "available root parameters are " + typeMap.keySet());
        }
        try {
            GetterInvokerGroup invokerGroup = FunctionalGetterInvokerGroup.create(type, propertyPath);
            return invokerGroup;
        } catch (UnreachablePropertyException e) {
            throw new BindingException("Parameter '" + Strings.getFullName(parameterName, propertyPath) +
                    "' can't be readable", e);
        }
    }

    @Nullable
    public String tryExpandParameterName(BindingParameter bindingParameter) {
        if (!typeMap.containsKey(bindingParameter.getParameterName())) { // 根参数不存在才扩展
            String propertyPath = bindingParameter.transToPropertyPath();
            List<String> parameterNames = new ArrayList<String>();
            for (Map.Entry<String, Type> entry : typeMap.entrySet()) {
                Type type = entry.getValue();
                try {
                    FunctionalGetterInvokerGroup.create(type, propertyPath);
                } catch (UnreachablePropertyException e) {
                    // 异常说明扩展失败
                    continue;
                }
                parameterNames.add(entry.getKey());
            }
            int num = parameterNames.size();
            if (num > 0) {
                if (num != 1) {
                    throw new BindingException("parameters " + parameterNames +
                            " has the same property '" + propertyPath + "', so can't expand");
                }
                return parameterNames.get(0);
            }
        }
        return null;
    }

    public List<ParameterDescriptor> getParameterDescriptors() {
        return parameterDescriptors;
    }

}
