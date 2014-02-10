package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;

/**
 * 整数字面值
 *
 * @author ash
 */
public class ASTIntegerLiteral extends PrimaryExpression {

    private Integer value;

    public ASTIntegerLiteral(int i) {
        super(i);
    }

    public ASTIntegerLiteral(Parser p, int i) {
        super(p, i);
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public Object value(RuntimeContext context) {
        return value;
    }

    @Override
    public void checkType(TypeContext context) {
    }

}