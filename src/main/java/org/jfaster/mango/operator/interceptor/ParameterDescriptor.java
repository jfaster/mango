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

package org.jfaster.mango.operator.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author ash
 */
public class ParameterDescriptor {

    private final Class<?> type;
    private final Type genericType;
    private final List<Annotation> annotations;

    public ParameterDescriptor(Class<?> type, Type genericType, Annotation[] annotations) {
        this.type = type;
        this.genericType = genericType;
        this.annotations = Collections.unmodifiableList(Arrays.asList(annotations));
    }

    public Class<?> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

}
