package org.jfaster.mango.parser;

import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.binding.ParameterContext;
import org.jfaster.mango.parser.visitor.*;
import org.jfaster.mango.util.jdbc.SQLType;

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
    getBlock().jjtAccept(TextBlankJoinVisitor.INSTANCE, null);
    getBlock().jjtAccept(InterablePropertyCollectVisitor.INSTANCE, null);
    getBlock().jjtAccept(NodeCollectVisitor.INSTANCE, nodeInfo);
    return this;
  }

  @Override
  public boolean render(InvocationContext context) {
    getDMLNode().render(context);
    getBlock().render(context);
    return true;
  }

  public SQLType getSQLType() {
    return getDMLNode().getSQLType();
  }

  /**
   * 扩展简化的参数节点
   */
  public void expandParameter(ParameterContext context) {
    getBlock().jjtAccept(ParameterExpandVisitor.INSTANCE, context);
  }

  /**
   * 类型检测并绑定GetterInvoker
   */
  public void checkAndBind(ParameterContext context) {
    getBlock().jjtAccept(CheckAndBindVisitor.INSTANCE, context);
  }

  public List<ASTJDBCParameter> getJDBCParameters() {
    return nodeInfo.jdbcParameters;
  }

  public List<ASTJDBCIterableParameter> getJDBCIterableParameters() {
    return nodeInfo.jdbcIterableParameters;
  }

  public List<ASTGlobalTable> getASTGlobalTables() {
    return nodeInfo.globalTables;
  }

  private AbstractDMLNode getDMLNode() {
    return (AbstractDMLNode) jjtGetChild(0);
  }

  private ASTBlock getBlock() {
    return (ASTBlock) jjtGetChild(1);
  }

}