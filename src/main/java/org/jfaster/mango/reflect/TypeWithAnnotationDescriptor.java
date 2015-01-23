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

import org.jfaster.mango.exception.IncorrectTypeException;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;

/**
 * @author ash
 */
public abstract class TypeWithAnnotationDescriptor {

    private final Type type;
    private final Class<?> rawType;
    private final List<Annotation> annotations;
    private final boolean isList;
    private final boolean isSet;
    private final boolean isArray;
    private final Class<?> mappedClass;

    public TypeWithAnnotationDescriptor(Type type, List<Annotation> annotations) {
        this.type = type;
        this.rawType = TypeToken.of(type).getRawType();
        this.annotations = annotations;

        new TypeVisitor() {
            @Override
            void visitGenericArrayType(GenericArrayType t) {
                visit(t.getGenericComponentType());
            }

            @Override
            void visitParameterizedType(ParameterizedType t) {
                visit(t.getOwnerType());
                visit(t.getActualTypeArguments());
            }

            @Override
            void visitTypeVariable(TypeVariable<?> t) {
                throw new IncorrectTypeException("Cannot contain type variable.");
            }

            @Override
            void visitWildcardType(WildcardType t) {
                throw new IncorrectTypeException("Cannot contain wildcard type.");
            }
        }.visit(type);

        TypeWrapper tw = new TypeWrapper(type);
        isList = tw.isList();
        isSet = tw.isSet();
        isArray = tw.isArray();
        mappedClass = tw.getMappedClass();
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

    public Type getType() {
        return type;
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public boolean isIterable() {
        return isList || isSet || isArray;
    }

    public boolean isList() {
        return isList;
    }

    public boolean isSet() {
        return isSet;
    }

    public boolean isArray() {
        return isArray;
    }

    public Class<?> getMappedClass() {
        return mappedClass;
    }
}
