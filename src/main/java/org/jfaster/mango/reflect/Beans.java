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

import org.jfaster.mango.exception.UncheckedException;
import org.jfaster.mango.invoker.InvokerCache;
import org.jfaster.mango.invoker.GetterInvoker;

import java.lang.reflect.InvocationTargetException;

/**
 * @author ash
 */
public class Beans {

    public static Result getPropertyValue(Object object, String propertyPath) {
        try {
            Object value = object;
            int pos = propertyPath.indexOf('.');
            StringBuffer nestedPath = new StringBuffer(propertyPath.length());
            int t = 0;
            while (pos > -1) {
                if (value == null) {
                    return new Result(ErrorType.NULL, nestedPath.toString());
                }
                String propertyName = propertyPath.substring(0, pos);
                if (t != 0) {
                    nestedPath.append(".");
                }
                nestedPath.append(propertyName);
                GetterInvoker invoker = InvokerCache.getGetterInvoker(value.getClass(), propertyName);
                if (invoker == null) {
                    return new Result(ErrorType.NO_PROPERTY, nestedPath.toString());
                }
                value = invoker.invoke(value);
                propertyPath = propertyPath.substring(pos + 1);
                pos = propertyPath.indexOf('.');
                t++;
            }
            if (value == null) {
                return new Result(ErrorType.NULL, nestedPath.toString());
            }
            GetterInvoker invoker = InvokerCache.getGetterInvoker(value.getClass(), propertyPath);
            if (invoker == null) {
                return new Result(ErrorType.NO_PROPERTY, nestedPath.append(".").append(propertyPath).toString());
            }
            value = invoker.invoke(value);
            return new Result(value);
        } catch (InvocationTargetException e) {
            throw new UncheckedException(e.getMessage(), e.getCause());
        } catch (IllegalAccessException e) {
            throw new UncheckedException(e.getMessage(), e.getCause());
        }
    }

    public enum ErrorType {
        NULL, NO_PROPERTY
    }

    public static class Result {

        Object value;

        ErrorType errorType;
        String path;

        public Result(Object value) {
            this.value = value;
        }

        public Result(ErrorType errorType, String path) {
            this.errorType = errorType;
            this.path = path;
        }

        public boolean isError() {
            return errorType != null;
        }

        public Object getValue() {
            return value;
        }

        public ErrorType getErrorType() {
            return errorType;
        }

        public String getPath() {
            return path;
        }

    }

}










