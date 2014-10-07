package org.jfaster.mango.parser;

import org.jfaster.mango.operator.RuntimeContext;
import org.jfaster.mango.operator.TypeContext;
import org.jfaster.mango.parser.visitor.InterablePropertyCollectVisitor;
import org.jfaster.mango.parser.visitor.NodeCollectVisitor;
import org.jfaster.mango.parser.visitor.TextBlankJoinVisitor;
import org.jfaster.mango.parser.visitor.TypeCheckVisitor;
import org.jfaster.mango.util.SQLType;

import java.util.List;

public class ASTRootNode extends AbstractRenderableNode {

    private NodeInfo nodeInfo = new NodeInfo();

    public ASTRootNode(int id) {
        super(id);
    }

    public ASTRootNode(Parser p, int id) {
        super(p, id);
    }

    public ASTRootNode init() {
        getBlock().jjtAccept(new TextBlankJoinVisitor(), null);
        getBlock().jjtAccept(new InterablePropertyCollectVisitor(), null);
        getBlock().jjtAccept(new NodeCollectVisitor(), nodeInfo);
        return this;
    }

    @Override
    public boolean render(RuntimeContext context) {
        ((AbstractRenderableNode) getDDLNode()).render(context);
        ((AbstractRenderableNode) getBlock()).render(context);
        return true;
    }

    public SQLType getSQLType() {
        SQLType sqlType;
        if (getDDLNode() instanceof ASTInsert) {
            sqlType = SQLType.INSERT;
        } else if (getDDLNode() instanceof ASTDelete) {
            sqlType = SQLType.DELETE;
        } else if (getDDLNode() instanceof ASTUpdate) {
            sqlType = SQLType.UPDATE;
        } else if (getDDLNode() instanceof ASTSelect) {
            sqlType = SQLType.SELECT;
        } else {
            throw new IllegalStateException();
        }
        return sqlType;
    }

    /**
     * 检测节点类型
     */
    public void checkType(TypeContext context) {
        getBlock().jjtAccept(new TypeCheckVisitor(), context);
    }


    //TODO
//    /**
//     * 设置全局表名
//     */
//    public void setGlobalTable(String globalTable) {
//        List<ASTGlobalTable> tableNodes = nodeInfo.globalTables;
//        if (!tableNodes.isEmpty() && globalTable == null) {
//            throw new IncorrectDefinitionException("if sql contains #table, @DB.table must define");
//        }
//        if (tableNodes.isEmpty() && globalTable != null) {
//            throw new IncorrectDefinitionException("if @DB.table is defined, sql must contain #table");
//        }
//        if (globalTable != null) {
//            for (ASTGlobalTable tableNode : tableNodes) {
//                tableNode.setTable(globalTable);
//            }
//        }
//    }
//
//    /**
//     * 设置分表信息
//     */
//    public void setPartitionInfo(TablePartition tablePartition, String shardParameterName, String shardPropertyPath) {
//        List<ASTGlobalTable> tableNodes = nodeInfo.globalTables;
//        if (tableNodes.isEmpty()) {
//            throw new IllegalStateException(""); // TODO
//        }
//        for (ASTGlobalTable tableNode : tableNodes) {
//            tableNode.setPartitionInfo(tablePartition, shardParameterName, shardPropertyPath);
//        }
//    }

    public List<ASTJDBCParameter> getJDBCParameters() {
        return nodeInfo.jdbcParameters;
    }

    public List<ASTJDBCIterableParameter> getJDBCIterableParameters() {
        return nodeInfo.jdbcIterableParameters;
    }

    private Node getDDLNode() {
        return jjtGetChild(0);
    }

    private Node getBlock() {
        return jjtGetChild(1);
    }

}