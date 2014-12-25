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

import org.jfaster.mango.util.Primitives;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class Types {

    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        return lhsType.isPrimitive() ?
                lhsType.equals(Primitives.unwrap(rhsType)) :
                lhsType.isAssignableFrom(Primitives.wrap(rhsType));
    }

    public static boolean isTypeAssignable(Type lhsType, Type rhsType) {
        if (lhsType instanceof ParameterizedType
                && rhsType instanceof ParameterizedType) {
            return lhsType.equals(rhsType);
        }
        if (lhsType instanceof Class
                && rhsType instanceof Class) {
            return isAssignable((Class) lhsType, (Class) rhsType);
        }
        return false;
    }

}















