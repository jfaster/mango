package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;

/**
 * @author ash
 */
public abstract class ValuableParameter extends ValuableNode {

    protected String beanName;
    protected String propertyPath; // 为""的时候表示没有属性

    public ValuableParameter(int i) {
        super(i);
    }

    public ValuableParameter(Parser p, int i) {
        super(p, i);
    }

    @Override
    public Object value(RuntimeContext context) {
        return context.getPropertyValue(beanName, propertyPath);
    }


}
