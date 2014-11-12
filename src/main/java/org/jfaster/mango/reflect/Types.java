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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class Types {

    public static Result getPropertyType(Type type, String propertyPath) {
        int pos = propertyPath.indexOf('.');
        StringBuffer parentPath = new StringBuffer(propertyPath.length());
        while (pos > -1) {
            String propertyName = propertyPath.substring(0, pos);
            Class<?> clazz = getClassFromType(type);
            if (clazz != null) {
                GetterInvoker invoker = BeanInfoCache.getGetterInvoker(clazz, propertyName);
                if (invoker != null) {
                    type = invoker.getReturnType();
                    propertyPath = propertyPath.substring(pos + 1);
                    pos = propertyPath.indexOf('.');
                    appendParentPath(parentPath, propertyName);
                    continue;
                }
            }
            String parentPathStr = parentPath.toString();
            appendParentPath(parentPath, propertyName);
            return new Result(parentPath.toString(), parentPathStr, type);
        }
        Class<?> clazz = getClassFromType(type);
        if (clazz != null) {
            GetterInvoker invoker = BeanInfoCache.getGetterInvoker(clazz, propertyPath);
            if (invoker != null) {
                type = invoker.getReturnType();
                return new Result(type);
            }
        }
        String parentPathStr = parentPath.toString();
        appendParentPath(parentPath, propertyPath);
        return new Result(parentPath.toString(), parentPathStr, type);
    }

    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        return lhsType.isPrimitive() ?
                lhsType.equals(Primitives.unwrap(rhsType)) :
                lhsType.isAssignableFrom(Primitives.wrap(rhsType));
    }

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

    public static class Result {

        Type type;

        String path;

        String parentPath;

        Type parentType;

        public Result(Type type) {
            this.type = type;
        }

        public Result(String path, String parentPath, Type parentType) {
            this.path = path;
            this.parentPath = parentPath;
            this.parentType = parentType;
        }

        public boolean isError() {
            return type == null;
        }

        public Type getType() {
            return type;
        }

        public String getPath() {
            return path;
        }

        public String getParentPath() {
            return parentPath;
        }

        public Type getParentType() {
            return parentType;
        }
    }

}















