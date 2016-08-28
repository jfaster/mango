package org.jfaster.mango.parser;

import org.jfaster.mango.binding.InvocationContext;

public class ASTStringLiteral extends AbstractExpression {

  private String value;

  public ASTStringLiteral(int i) {
    super(i);
  }

  public ASTStringLiteral(Parser p, int i) {
    super(p, i);
  }

  public void init(String str) {
    value = str.substring(1, str.length() - 1);
  }

  @Override
  public boolean evaluate(InvocationContext context) {
    return !value.isEmpty();
  }

  @Override
  public Object value(InvocationContext context) {
    return value;
  }

  @Override
  public String toString() {
    return super.toString() + "[" + value + "]";
  }

  @Override
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

}