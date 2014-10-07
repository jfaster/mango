package org.jfaster.mango.operator;

/**
 * @author ash
 */
public class RuntimeContextFactoryImpl implements RuntimeContextFactory {

    private NameProvider nameProvider;

    private TableGenerator tableGenerator;

    public RuntimeContextFactoryImpl(NameProvider nameProvider, TableGenerator tableGenerator) {
        this.nameProvider = nameProvider;
        this.tableGenerator = tableGenerator;
    }

    @Override
    public RuntimeContext newRuntimeContext(Object[] values) {
        RuntimeContext context = new RuntimeContextImpl();
        for (int i = 0; i < values.length; i++) {
            context.addParameter(nameProvider.getParameterName(i), values[i]);
        }
        context.setGlobalTable(tableGenerator.getTable(context));
        return context;
    }

}
