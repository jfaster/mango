package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;

/**
 * @author ash
 */
public class ASTExpression extends ValuableExpression {

    public ASTExpression(int i) {
        super(i);
    }

    public ASTExpression(Parser p, int i) {
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

    @Override
    Object value(RuntimeContext context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void checkType(TypeContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
