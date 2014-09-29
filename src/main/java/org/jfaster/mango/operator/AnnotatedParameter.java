package org.jfaster.mango.operator;

/**
 * @author ash
 */
public class AnnotatedParameter {

    private int parameterIndex;

    private String annotationValue;

    public AnnotatedParameter(int parameterIndex, String annotationValue) {
        this.parameterIndex = parameterIndex;
        this.annotationValue = annotationValue;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public String getAnnotationValue() {
        return annotationValue;
    }

}
