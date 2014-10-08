package org.jfaster.mango.operator;

/**
 * @author ash
 */
public class InvocationContextFactory {

    private NameProvider nameProvider;

    public InvocationContextFactory(NameProvider nameProvider) {
        this.nameProvider = nameProvider;
    }

    public InvocationContext newInvocationContext(Object[] values) {
        InvocationContext context = new InvocationContext();
        for (int i = 0; i < values.length; i++) {
            context.addParameter(nameProvider.getParameterName(i), values[i]);
        }
        return context;
    }

}
