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

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author ash
 */
public class MethodDescriptor {

    private final Type returnType;
    private final Class<?> rawReturnType;
    private final List<Annotation> annotations;
    private final List<ParameterDescriptor> parameterDescriptors;

    public MethodDescriptor(Type returnType, Class<?> rawReturnType, List<Annotation> annotations,
                            List<ParameterDescriptor> parameterDescriptors) {
        this.returnType = returnType;
        this.rawReturnType = rawReturnType;
        this.annotations = Collections.unmodifiableList(annotations);
        this.parameterDescriptors = Collections.unmodifiableList(parameterDescriptors);
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

    public Type getReturnType() {
        return returnType;
    }

    public Class<?> getRawReturnType() {
        return rawReturnType;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public List<ParameterDescriptor> getParameterDescriptors() {
        return parameterDescriptors;
    }

}
