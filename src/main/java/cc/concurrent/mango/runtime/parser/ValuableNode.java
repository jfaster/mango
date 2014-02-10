package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;

/**
 * @author ash
 */
public abstract class ValuableNode extends SimpleNode {

    public ValuableNode(int i) {
        super(i);
    }

    public ValuableNode(Parser p, int i) {
        super(p, i);
    }

    abstract Object value(RuntimeContext context);

    abstract void checkType(TypeContext context);

}
