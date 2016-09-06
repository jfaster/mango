package org.jfaster.mango.binding;

import org.jfaster.mango.type.TypeHandler;
import org.jfaster.mango.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class BoundSql {

  private String sql;
  private final List<Object> args;
  private final List<TypeHandler<?>> typeHandlers;

  public BoundSql(String sql) {
    this.sql = sql;
    this.args = new ArrayList<Object>();
    this.typeHandlers = new ArrayList<TypeHandler<?>>();
  }

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

  public void addNonNullArg(Object obj) {
    TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(obj.getClass());
    args.add(obj);
    typeHandlers.add(typeHandler);
  }

  public void addNonNullArg(int index, Object obj) {
    TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(obj.getClass());
    args.add(index, obj);
    typeHandlers.add(index, typeHandler);
  }

  public void addNullArg(Class<?> type) {
    TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(type);
    args.add(null);
    typeHandlers.add(typeHandler);
  }

  public void addNullArg(int index, Class<?> type) {
    TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(type);
    args.add(index, null);
    typeHandlers.add(index, typeHandler);
  }

  public List<Object> getArgs() {
    return args;
  }

  public List<TypeHandler<?>> getTypeHandlers() {
    return typeHandlers;
  }

}
