package org.jfaster.mango.binding;

import org.jfaster.mango.type.TypeHandler;

import java.util.List;

/**
 * @author ash
 */
public class BoundSql {

  private String sql;
  private List<Object> args;
  private List<TypeHandler<?>> typeHandlers;

  public BoundSql(String sql, List<Object> args, List<TypeHandler<?>> typeHandlers) {
    this.sql = sql;
    this.args = args;
    this.typeHandlers = typeHandlers;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public List<Object> getArgs() {
    return args;
  }

  public void setArgs(List<Object> args) {
    this.args = args;
  }

  public List<TypeHandler<?>> getTypeHandlers() {
    return typeHandlers;
  }

  public void setTypeHandlers(List<TypeHandler<?>> typeHandlers) {
    this.typeHandlers = typeHandlers;
  }
}
