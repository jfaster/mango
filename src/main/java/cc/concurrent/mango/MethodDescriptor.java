package cc.concurrent.mango;

import cc.concurrent.mango.operator.Operator;
import cc.concurrent.mango.runtime.parser.ASTRootNode;

/**
 * @author ash
 */
public class MethodDescriptor {

    private final ASTRootNode node;

    private final Operator operator;

    public MethodDescriptor(ASTRootNode node, Operator operator) {
        this.node = node;
        this.operator = operator;
    }

    public ASTRootNode getNode() {
        return node;
    }

    public Operator getOperator() {
        return operator;
    }
}
