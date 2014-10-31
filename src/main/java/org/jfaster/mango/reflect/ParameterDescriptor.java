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

import org.jfaster.mango.util.Objects;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author ash
 */
public class ParameterDescriptor {

    private final int position;
    private final Type type;
    private final Class<?> rawType;
    private final List<Annotation> annotations;
    private final String name; // 方法参数名

    public ParameterDescriptor(int position, Type type, Class<?> rawType, List<Annotation> annotations, String name) {
        this.position = position;
        this.type = type;
        this.rawType = rawType;
        this.annotations = Collections.unmodifiableList(annotations);
        this.name = name;
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

    public int getPosition() {
        return position;
    }

    public Type getType() {
        return type;
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ParameterDescriptor other = (ParameterDescriptor) obj;
        return Objects.equal(this.position, other.position)
                && Objects.equal(this.type, other.type)
                && Objects.equal(this.rawType, other.rawType)
                && Objects.equal(this.annotations, other.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(position, type, rawType, annotations);
    }

}
