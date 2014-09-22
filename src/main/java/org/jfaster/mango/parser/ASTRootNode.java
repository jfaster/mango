package org.jfaster.mango.parser;

import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.partition.TablePartition;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.support.TypeContext;

import java.util.List;

public class ASTRootNode extends AbstractRenderableNode {

    private NodeCollectVisitor.NodeInfo nodeInfo = new NodeCollectVisitor.NodeInfo();

    public ASTRootNode(int id) {
        super(id);
    }

    public ASTRootNode(Parser p, int id) {
        super(p, id);
    }

    public ASTRootNode init() {
        jjtGetChild(0).jjtAccept(new TextBlankJoinVisitor(), null);
        jjtGetChild(0).jjtAccept(new InterablePropertyCollectVisitor(), null);
        jjtGetChild(0).jjtAccept(new NodeCollectVisitor(), nodeInfo);
        return this;
    }

    @Override
    public boolean render(RuntimeContext context) {
        ((AbstractRenderableNode) jjtGetChild(0)).render(context);
        return true;
    }

    /**
     * 检测节点类型
     */
    public void checkType(TypeContext context) {
        jjtGetChild(0).jjtAccept(new TypeCheckVisitor(), context);
    }

    /**
     * 设置全局表名
     */
    public void setGlobalTable(String globalTable) {
        List<ASTGlobalTable> tableNodes = nodeInfo.globalTables;
        if (!tableNodes.isEmpty() && globalTable == null) {
            throw new IncorrectDefinitionException("if sql contains #table, @DB.table must define");
        }
        if (tableNodes.isEmpty() && globalTable != null) {
            throw new IncorrectDefinitionException("if @DB.table is defined, sql must contain #table");
        }
        if (globalTable != null) {
            for (ASTGlobalTable tableNode : tableNodes) {
                tableNode.setTable(globalTable);
            }
        }
    }

    /**
     * 设置分表信息
     */
    public void setPartitionInfo(TablePartition tablePartition, String shardParameterName, String shardPropertyPath) {
        List<ASTGlobalTable> tableNodes = nodeInfo.globalTables;
        if (tableNodes.isEmpty()) {
            throw new IllegalStateException("");
        }
        for (ASTGlobalTable tableNode : tableNodes) {
            tableNode.setPartitionInfo(tablePartition, shardParameterName, shardPropertyPath);
        }
    }

    public List<ASTJDBCParameter> getJDBCParameters() {
        return nodeInfo.jdbcParameters;
    }

    public List<ASTJDBCIterableParameter> getJDBCIterableParameters() {
        return nodeInfo.jdbcIterableParameters;
    }

}