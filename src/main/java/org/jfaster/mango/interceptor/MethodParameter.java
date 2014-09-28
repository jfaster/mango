package org.jfaster.mango.interceptor;

import org.jfaster.mango.operator.MethodParameterDescriptor;

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
