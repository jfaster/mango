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

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author ash
 */
public class MethodParameter {

    private final MethodParameterDescriptor methodParameterDescriptor;
    private final Object value;

    public MethodParameter(MethodParameterDescriptor methodParameterDescriptor, Object value) {
        this.methodParameterDescriptor = methodParameterDescriptor;
        this.value = value;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    @Nullable
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        for (Annotation annotation : getAnnotations()) {
            if (annotationType.isInstance(annotation)) {
                return annotationType.cast(annotation);
            }
        }
        return null;
    }

    public Object getValue() {
        return value;
    }

    public Class<?> getType() {
        return methodParameterDescriptor.getType();
    }

    public Type getGenericType() {
        return methodParameterDescriptor.getGenericType();
    }

    public List<Annotation> getAnnotations() {
        return methodParameterDescriptor.getAnnotations();
    }

}
