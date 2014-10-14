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

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ash
 */
public abstract class TypeToken<T> extends TypeCapture {

    private final Type runtimeType;

    protected TypeToken() {
        this.runtimeType = capture();
        if (runtimeType instanceof TypeVariable) {
            throw new IllegalStateException("Cannot construct a TypeToken for a type variable");
        }
    }

    private TypeToken(Type type) {
        this.runtimeType = type;
    }

    public static <T> TypeToken<T> of(Class<T> type) {
        return new SimpleTypeToken<T>(type);
    }

    public final Type getType() {
        return runtimeType;
    }

    public final Class<? super T> getRawType() {
        Class<?> rawType = getRawType(runtimeType);
        @SuppressWarnings("unchecked") // raw type is |T|
        Class<? super T> result = (Class<? super T>) rawType;
        return result;
    }

    static Class<?> getRawType(Type type) {
        return getRawTypes(type).iterator().next();
    }

    static Set<Class<?>> getRawTypes(Type type) {
        final Set<Class<?>> set = new HashSet<Class<?>>();
        new TypeVisitor() {
            @Override
            void visitTypeVariable(TypeVariable<?> t) {
                visit(t.getBounds());
            }

            @Override
            void visitWildcardType(WildcardType t) {
                visit(t.getUpperBounds());
            }

            @Override
            void visitParameterizedType(ParameterizedType t) {
                set.add((Class<?>) t.getRawType());
            }

            @Override
            void visitClass(Class<?> t) {
                set.add(t);
            }

            @Override
            void visitGenericArrayType(GenericArrayType t) {
                set.add(getArrayClass(getRawType(t.getGenericComponentType())));
            }

            private Class<?> getArrayClass(Class<?> componentType) {
                return Array.newInstance(componentType, 0).getClass();
            }

        }.visit(type);
        return set;
    }

    private static final class SimpleTypeToken<T> extends TypeToken<T> {

        SimpleTypeToken(Type type) {
            super(type);
        }

    }

}
