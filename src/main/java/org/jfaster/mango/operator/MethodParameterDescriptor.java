package org.jfaster.mango.operator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author ash
 */
public class MethodParameterDescriptor {

    private final Class<?> type;
    private final Type genericType;
    private final List<Annotation> annotations;

    public MethodParameterDescriptor(Class<?> type, Type genericType, Annotation[] annotations) {
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
