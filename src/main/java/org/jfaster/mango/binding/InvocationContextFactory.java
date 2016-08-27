package org.jfaster.mango.binding;

/**
 * @author ash
 */
public class InvocationContextFactory {

  private ParameterContext parameterContext;

  private InvocationContextFactory(ParameterContext parameterContext) {
    this.parameterContext = parameterContext;
  }

  public static InvocationContextFactory create(ParameterContext parameterContext) {
    return new InvocationContextFactory(parameterContext);
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
