package org.jfaster.mango.operator;

/**
 * @author ash
 */
public class RuntimeContextFactoryImpl implements RuntimeContextFactory {

    private NameProvider nameProvider;

    public RuntimeContextFactoryImpl(NameProvider nameProvider) {
        this.nameProvider = nameProvider;
    }

    @Override
    public RuntimeContext newRuntimeContext(Object[] values) {
        RuntimeContext context = new RuntimeContextImpl();
        for (int i = 0; i < values.length; i++) {
            context.addParameter(nameProvider.getParameterName(i), values[i]);
        }
        return context;
    }

}
