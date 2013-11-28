package cc.concurrent.mango.parser;

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class ASTIntegerLiteral extends SimpleNode {

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
    public String toString() {
        return Objects.toStringHelper(this).addValue(value).toString();
    }
}