package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.TypeContext;

/**
 * 一元表达式
 *
 * @author ash
 */
public abstract class PrimaryExpression extends ValuableExpression {

    public PrimaryExpression(int i) {
        super(i);
    }

    public PrimaryExpression(Parser p, int i) {
        super(p, i);
    }

    @Override
    Token getFirstToken() {
        return jjtGetFirstToken();
    }

    @Override
    Token getLastToken() {
        return jjtGetLastToken();
    }

}
