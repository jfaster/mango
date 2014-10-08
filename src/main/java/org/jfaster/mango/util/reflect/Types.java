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

import org.jfaster.mango.exception.NotReadablePropertyException;
import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.util.Strings;

import java.lang.reflect.*;

/**
 * @author ash
 */
public class Types {

    public static Type getPropertyType(Type type, String parameterName, String propertyPath) {
        if (Strings.isNullOrEmpty(propertyPath)) {
            throw new UnreachableCodeException();
        }

        int pos = propertyPath.indexOf('.');
        StringBuffer parentPath = new StringBuffer(propertyPath.length());
        while (pos > -1) {
            String propertyName = propertyPath.substring(0, pos);
            Class<?> clazz = getClassFromType(type);
            if (clazz != null) {
                Method method = BeanInfoCache.getReadMethod(clazz, propertyName);
                if (method != null) {
                    type = method.getGenericReturnType();
                    propertyPath = propertyPath.substring(pos + 1);
                    pos = propertyPath.indexOf('.');
                    appendParentPath(parentPath, propertyName);
                    continue;
                }
            }

            String parentFullName = getFullName(parameterName, parentPath.toString());
            appendParentPath(parentPath, propertyName);
            String fullName = getFullName(parameterName, parentPath.toString());
            throw new NotReadablePropertyException("property " + fullName + " is not readable, " +
                    "the type of " + parentFullName + " is " + type + ", please check it's get method");
        }

        Class<?> clazz = getClassFromType(type);
        if (clazz != null) {
            Method method = BeanInfoCache.getReadMethod(clazz, propertyPath);
            if (method != null) {
                type = method.getGenericReturnType();
                return type;
            }
        }
        String parentFullName = getFullName(parameterName, parentPath.toString());
        appendParentPath(parentPath, propertyPath);
        String fullName = getFullName(parameterName, parentPath.toString());
        throw new NotReadablePropertyException("property " + fullName + " is not readable, " +
                "the type of " + parentFullName + " is " + type + ", please check it's get method");
    }

//    public static Class<?> getRawType(Type type) {
//        Set<Class<?>> set = new HashSet
//        final ImmutableSet.Builder<Class<?>> builder = ImmutableSet.builder();
//        new TypeVisitor() {
//            @Override void visitTypeVariable(TypeVariable<?> t) {
//                visit(t.getBounds());
//            }
//            @Override void visitWildcardType(WildcardType t) {
//                visit(t.getUpperBounds());
//            }
//            @Override void visitParameterizedType(ParameterizedType t) {
//                builder.add((Class<?>) t.getRawType());
//            }
//            @Override void visitClass(Class<?> t) {
//                builder.add(t);
//            }
//            @Override void visitGenericArrayType(GenericArrayType t) {
//                builder.add(Types.getArrayClass(getRawType(t.getGenericComponentType())));
//            }
//
//        }.visit(type);
//    }


    private static Class<?> getClassFromType(Type type) {
        Class<?> clazz = null;
        if (type instanceof Class<?>) {
            clazz = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            clazz = (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return clazz;
    }

    private static void appendParentPath(StringBuffer parentPath, String propertyName) {
        if (parentPath.length() == 0) {
            parentPath.append(propertyName);
        } else {
            parentPath.append(".").append(propertyName);
        }
    }

    private static String getFullName(String parameterName, String propertyPath) {
        return ":" + (!propertyPath.isEmpty() ? parameterName + "." + propertyPath : parameterName);
    }

}















