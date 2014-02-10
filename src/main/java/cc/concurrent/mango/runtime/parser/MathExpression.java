package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;

/**
 * @author ash
 */
public abstract class MathExpression extends ValuableExpression {

    public MathExpression(int i) {
        super(i);
    }

    public MathExpression(Parser p, int i) {
        super(p, i);
    }

    @Override
    public Object value(RuntimeContext context) {
        Object left = ((ValuableExpression) jjtGetChild(0)).value(context);
        Object right = ((ValuableExpression) jjtGetChild(1)).value(context);

        Object special = handleSpecial(left, right);
        if (special != null) {
            return special;
        }

        if (!(left instanceof Integer) || !(right instanceof Integer)) {
            throw new IllegalStateException("error type");
        }
        return perform((Integer) left, (Integer) right);
    }

    @Override
    public void checkType(TypeContext context) {
        return;
    }

    protected Object handleSpecial(Object left, Object right) {
        return null;
    }

    public abstract Integer perform(Integer left, Integer right);

}
