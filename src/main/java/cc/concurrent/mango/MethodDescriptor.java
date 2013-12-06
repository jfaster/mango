package cc.concurrent.mango;

import cc.concurrent.mango.operator.Operator;
import cc.concurrent.mango.runtime.parser.ASTRootNode;

/**
 * @author ash
 */
public class MethodDescriptor {

    private final Operator operator;

    private final ASTRootNode rootNode;

    public MethodDescriptor(Operator operator, ASTRootNode rootNode) {
        this.operator = operator;
        this.rootNode = rootNode;
    }

    public Operator getOperator() {
        return operator;
    }

    public ASTRootNode getRootNode() {
        return rootNode;
    }

}
