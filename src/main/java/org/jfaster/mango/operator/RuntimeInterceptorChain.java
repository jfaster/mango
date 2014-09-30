package org.jfaster.mango.operator;

import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.support.SqlDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ash
 */
public class RuntimeInterceptorChain {

    private InterceptorChain interceptorChain;

    private List<ParameterDescriptor> parameterDescriptors;

    public RuntimeInterceptorChain(InterceptorChain interceptorChain, Method method) {
        this.interceptorChain = interceptorChain;
        initParameterDescriptors(method);
    }

    protected void intercept(SqlDescriptor sqlDescriptor, RuntimeContext context) {
        if (interceptorChain.getInterceptors() != null) {
            List<Object> parameterValues = context.getParameterValues();
            List<Parameter> methodParameters = new ArrayList<Parameter>(parameterValues.size());
            for (int i = 0; i < parameterValues.size(); i++) {
                ParameterDescriptor pd = parameterDescriptors.get(i);
                methodParameters.add(new Parameter(pd, parameterValues.get(i)));
            }
            interceptorChain.intercept(sqlDescriptor, methodParameters);
        }
    }

    private void initParameterDescriptors(Method method) {
        parameterDescriptors = new LinkedList<ParameterDescriptor>();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < genericParameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            Type genericType = genericParameterTypes[i];
            Annotation[] annotations = parameterAnnotations[i];
            parameterDescriptors.add(new ParameterDescriptor(type, genericType, annotations));
        }
    }

}
