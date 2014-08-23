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

package org.jfaster.mango.util.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class TypeToken {

    private Class<?> mappedClass = null;
    private Type mappedType = null; // 抛出异常时需要用到
    private boolean isList;
    private boolean isSet;
    private boolean isArray;

    public TypeToken(Type type) {
        if (byte[].class.equals(type)) { // byte[]是jdbc中的一个基础类型,所以不把它作为数组处理
            mappedClass = byte[].class;
        } else if (type instanceof ParameterizedType) { // 参数化类型
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class) {
                Class<?> rawClass = (Class<?>) rawType;
                if (List.class.equals(rawClass)) {
                    isList = true;
                    Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                    if (typeArgument instanceof Class) {
                        mappedClass = (Class<?>) typeArgument;
                    }
                    mappedType = typeArgument;
                } else if (Set.class.equals(rawClass)) {
                    isSet = true;
                    Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                    if (typeArgument instanceof Class) {
                        mappedClass = (Class<?>) typeArgument;
                    }
                    mappedType = typeArgument;
                }
            }
        } else if (type instanceof Class) { // 没有参数化
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) { // 数组
                isArray = true;
                mappedClass = clazz.getComponentType();
                mappedType = mappedClass;
            } else { // 普通类
                mappedClass = clazz;
            }
        }
    }

    public boolean isIterable() {
        return isList || isSet || isArray;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isSet() {
        return isSet;
    }

    public boolean isList() {
        return isList;
    }

    public boolean isCollection() {
        return isList || isSet;
    }

    public Class<?> getMappedClass() {
        return mappedClass;
    }

    public Type getMappedType() {
        return mappedType;
    }
}










