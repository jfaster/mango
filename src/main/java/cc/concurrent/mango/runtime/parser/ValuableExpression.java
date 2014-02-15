package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public abstract class ValuableExpression extends ValuableNode {

    public ValuableExpression(int i) {
        super(i);
    }

    public ValuableExpression(Parser p, int i) {
        super(p, i);
    }

}
