package cc.concurrent.mango.visitor;

import cc.concurrent.mango.parser.*;

/**
 * User: yanghe.liang
 * Date: 13-11-23
 * Time: 下午6:10
 */
public class DebugVisitor implements ParserVisitor {

    @Override
    public Object visit(SimpleNode node, Object data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(ASTRootNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTParameterNode node, Object data) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object visit(ASTTextNode node, Object data) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
