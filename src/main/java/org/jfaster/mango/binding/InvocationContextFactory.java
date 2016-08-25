package org.jfaster.mango.binding;

/**
 * @author ash
 */
public class InvocationContextFactory {

    private ParameterContext parameterContext;

    public InvocationContextFactory(ParameterContext parameterContext) {
        this.parameterContext = parameterContext;
    }

    public InvocationContext newInvocationContext(Object[] values) {
        InvocationContext context = DefaultInvocationContext.create();
        for (int i = 0; i < values.length; i++) {
            String parameterName = parameterContext.getParameterNameByPosition(i);
            context.addParameter(parameterName, values[i]);
        }
        return context;
    }

}
