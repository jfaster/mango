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

package org.jfaster.mango.reflect;

import java.lang.reflect.*;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class TypeWrapper {

    /**
     * type映射的原始类型
     * <p>
     * 如果type为{@code Integer}，它的值为{@code Integer.class}，
     * 如果type为{@code List<Integer>}，它的值为{@code Integer.class}，
     * 如果type为{@code List<List<String>>}，{@code List<T>>}等，它的值为{@code null}
     */
    private Class<?> mappedClass;

    /**
     * type映射的原始类型，抛出异常时需要用到
     */
    private Type mappedType;

    private boolean isList;
    private boolean isSet;
    private boolean isArray;

    public TypeWrapper(final Type type) {
        if (byte[].class.equals(type)) { // byte[]是jdbc中的一个基础类型,所以不把它作为数组处理
            mappedType = mappedClass = byte[].class;
        } else {
            new TypeVisitor() {
                @Override
                void visitClass(Class<?> t) {
                    mappedType = t;
                    if (t.isArray()) { // 数组
                        isArray = true;
                        mappedType = t.getComponentType();
                    }
                }

                @Override
                void visitGenericArrayType(GenericArrayType t) {
                    isArray = true;
                    mappedType = t.getGenericComponentType();
                }

                @Override
                void visitParameterizedType(ParameterizedType t) {
                    Type rawType = t.getRawType();
                    if (List.class.equals(rawType)) {
                        isList = true;
                        mappedType = t.getActualTypeArguments()[0];
                    } else if (Set.class.equals(rawType)) {
                        isSet = true;
                        mappedType = t.getActualTypeArguments()[0];
                    } else {
                        throw new RuntimeException(); // TODO
                    }
                }

                @Override
                void visitTypeVariable(TypeVariable<?> t) {
                    throw new IllegalStateException("Does not support the type " + type);
                }

                @Override
                void visitWildcardType(WildcardType t) {
                    throw new IllegalStateException("Does not support the type " + type);
                }
            }.visit(type);
            mappedClass = TypeToken.of(mappedType).getRawType();
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










