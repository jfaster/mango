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

package org.jfaster.mango.operator;

import org.jfaster.mango.exception.IncorrectParameterCountException;
import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.exception.NotReadableParameterException;
import org.jfaster.mango.exception.NotReadablePropertyException;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.reflect.ParameterDescriptor;
import org.jfaster.mango.util.reflect.TypeWrapper;
import org.jfaster.mango.util.reflect.Types;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author ash
 */
public class ParameterContext {

    private final Map<String, ParameterDescriptor> parameterDescriptorMap = new HashMap<String, ParameterDescriptor>();
    private final List<ParameterDescriptor> parameterDescriptors = new LinkedList<ParameterDescriptor>();
    private final Map<String, Type> cache = new HashMap<String, Type>();

    public ParameterContext(List<ParameterDescriptor> pds, NameProvider nameProvider, OperatorType operatorType) {
        if (operatorType == OperatorType.BATCHUPDATYPE) {
            if (pds.size() != 1) {
                throw new IncorrectParameterCountException("batch update expected one and " +
                        "only one parameter but " + pds.size()); // 批量更新只能有一个参数
            }
            ParameterDescriptor pd = pds.get(0);
            TypeWrapper tw = new TypeWrapper(pd.getType());
            Class<?> mappedClass = tw.getMappedClass();
            if (mappedClass == null || !tw.isIterable()) {
                throw new IncorrectParameterTypeException("parameter of batch update expected array or " +
                        "implementations of java.util.List or implementations of java.util.Set " +
                        "but " + pd.getType()); // 批量更新的参数必须可迭代
            }
            pds = new ArrayList<ParameterDescriptor>(1);
            pds.add(new ParameterDescriptor(0, mappedClass, mappedClass, pd.getAnnotations()));
        }
        for (int i = 0; i < pds.size(); i++) {
            ParameterDescriptor pd = pds.get(i);
            parameterDescriptorMap.put(nameProvider.getParameterName(i), pd);
            parameterDescriptors.add(pd);
        }
    }

    public Type getPropertyType(String parameterName, String propertyPath) {
        String key = getCacheKey(parameterName, propertyPath);
        Type cachedType = cache.get(key);
        if (cachedType != null) { // 缓存命中，直接返回
            return cachedType;
        }
        ParameterDescriptor pd = parameterDescriptorMap.get(parameterName);
        if (pd == null ) {
            throw new NotReadableParameterException("parameter :" + parameterName + " is not readable");
        }
        Type parameterType = pd.getType();
        Type type;
        if (Strings.isEmpty(propertyPath)) {
            type = parameterType;
        } else {
            Types.Result tr = Types.getPropertyType(parameterType, propertyPath);
            if (tr.isError()) {
                String fullName = Strings.getFullName(parameterName, tr.getPath());
                String parentFullName = Strings.getFullName(parameterName, tr.getParentPath());
                Type parentType = tr.getParentType();
                throw new NotReadablePropertyException("property " + fullName + " is not readable, " +
                        "the type of " + parentFullName + " is " + parentType + ", please check it's get method");
            }
            type = tr.getType();
        }
        cache.put(key, type);
        return type;
    }

    public List<ParameterDescriptor> getParameterDescriptors() {
        return parameterDescriptors;
    }

    private String getCacheKey(String parameterName, String propertyPath) {
        return propertyPath.isEmpty() ? parameterName : parameterName + "." + propertyPath;
    }

}
