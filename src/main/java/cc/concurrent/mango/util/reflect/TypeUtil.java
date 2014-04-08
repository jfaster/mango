/*
 * Copyright 2014 mango.concurrent.cc
 *
 * The Netty Project licenses this file to you under the Apache License,
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

package cc.concurrent.mango.util.reflect;

import cc.concurrent.mango.exception.NotReadablePropertyException;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class TypeUtil {

    public static Type getPropertyType(Type type, String parameterName, String propertyPath) {
        int pos = propertyPath.indexOf('.');
        StringBuffer nestedPath = new StringBuffer(propertyPath.length());
        while (pos > -1) {
            String propertyName = propertyPath.substring(0, pos);
            nestedPath.append(propertyName);
            Class<?> clazz = getClassFromType(type);
            if (clazz != null) {
                Method method = BeanInfoCache.getReadMethod(clazz, propertyName);
                if (method != null) {
                    type = method.getGenericReturnType();
                    propertyPath = propertyPath.substring(pos + 1);
                    pos = propertyPath.indexOf('.');
                    nestedPath.append(".");
                    continue;
                }
            }
            throw new NotReadablePropertyException("property ':" + parameterName + "." + nestedPath + "' is not readable");
        }

        Class<?> clazz = getClassFromType(type);
        if (clazz != null) {
            Method method = BeanInfoCache.getReadMethod((Class<?>) type, propertyPath);
            if (method != null) {
                type = method.getGenericReturnType();
                return type;
            }
        }
        nestedPath.append(propertyPath);
        throw new NotReadablePropertyException("property ':" + parameterName + "." + nestedPath + "' is not readable");
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

}















