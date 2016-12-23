/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.parser;

import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.jfaster.mango.binding.*;
import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.support.ParserVisitorAdapter;
import org.jfaster.mango.util.jdbc.JdbcType;
import org.jfaster.mango.util.jdbc.SQLType;
import org.jfaster.mango.util.reflect.TypeToken;
import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author ash
 */
public class ParserTest {

  @Test
  public void testBase() throws Exception {
    String sql = "select #{:1} from user where id in (:2) and name=:3";
    ASTRootNode n = new Parser(sql).parse().init();
    Type listType = new TypeToken<List<Integer>>() {
    }.getType();
    ParameterContext ctx = getParameterContext(Lists.newArrayList(String.class, listType, String.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", "id");
    context.addParameter("2", Arrays.asList(9, 5, 2, 7));
    context.addParameter("3", "ash");
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("select id from user where id in (?,?,?,?) and name=?"));
    assertThat(boundSql.getArgs(), contains(new Object[]{9, 5, 2, 7, "ash"}));
  }

  @Test
  public void testIf() throws Exception {
    String sql = "select where 1=1 #if(:1) and id>:1 #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Integer.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", 100);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("select where 1=1  and id>? "));
    assertThat(boundSql.getArgs(), contains(new Object[]{100}));
  }

  @Test
  public void testIf2() throws Exception {
    String sql = "select where 1=1 #if(!:1) and id>:1 #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Integer.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", 100);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("select where 1=1 "));
    assertThat(boundSql.getArgs().size(), equalTo(0));
  }

  @Test
  public void testIfElseIf() throws Exception {
    String sql = "select where 1=1" +
        "#if(:1>0)" +
        " and id>:1" +
        "#elseif(:1<0)" +
        " and id<:1" +
        "#end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Integer.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", 100);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("select where 1=1 and id>?"));
    assertThat(boundSql.getArgs(), contains(new Object[]{100}));
  }

  @Test
  public void testIfElseIf2() throws Exception {
    String sql = "select where 1=1" +
        "#if(:1>0)" +
        " and id>:1" +
        "#elseif(:1<0)" +
        " and id<:1" +
        "#end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Integer.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", -100);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("select where 1=1 and id<?"));
    assertThat(boundSql.getArgs(), contains(new Object[]{-100}));
  }

  @Test
  public void testIfElseIfElse() throws Exception {
    String sql = "select where 1=1" +
        "#if(:1>0)" +
        " and id>:1" +
        "#elseif(:1<0)" +
        " and id<:1" +
        "#else" +
        " and id=:1" +
        "#end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Integer.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", 100);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("select where 1=1 and id>?"));
    assertThat(boundSql.getArgs(), contains(new Object[]{100}));
  }

  @Test
  public void testIfElseIfElse2() throws Exception {
    String sql = "select where 1=1" +
        "#if(:1>0)" +
        " and id>:1" +
        "#elseif(:1<0)" +
        " and id<:1" +
        "#else" +
        " and id=:1" +
        "#end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Integer.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", -100);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("select where 1=1 and id<?"));
    assertThat(boundSql.getArgs(), contains(new Object[]{-100}));
  }

  @Test
  public void testIfElseIfElse3() throws Exception {
    String sql = "select where 1=1" +
        "#if(:1>0)" +
        " and id>:1" +
        "#elseif(:1<0)" +
        " and id<:1" +
        "#else" +
        " and id=:1" +
        "#end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Integer.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", 0);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("select where 1=1 and id=?"));
    assertThat(boundSql.getArgs(), contains(new Object[]{0}));
  }

  @Test
  public void testExpression() throws Exception {
    String sql = "select where 1=1 #if(:1==false && :2!=null && :3==true) and id>10 #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Boolean.class,
        Object.class, Boolean.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", false);
    context.addParameter("2", new Object());
    context.addParameter("3", true);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("select where 1=1  and id>10 "));
  }

  @Test
  public void testParse() throws Exception {
    String sql = "SELECT * from user where id in ( select id from user2 )";
    ASTRootNode n = new Parser(sql).parse().init();
    InvocationContext context = DefaultInvocationContext.create();
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("SELECT * from user where id in ( select id from user2 )"));
  }

  @Test
  public void testIntegerLiteral() throws Exception {
    String sql = "select #if (:1 > 9223372036854775800) ok #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Integer.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", Long.MAX_VALUE);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select  ok "));
  }

  @Test
  public void testIntegerLiteral2() throws Exception {
    String sql = "select #if (:1 > 10) ok #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Integer.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", Long.MAX_VALUE);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select  ok "));
  }

  @Test
  public void testIntegerLiteral3() throws Exception {
    String sql = "select #if (:1 > 9223372036854775800) ok #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) Integer.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", 100);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select "));
  }

  @Test
  public void testReplace() throws Exception {
    String sql = "replace xxx into replace xxx";
    ASTRootNode n = new Parser(sql).parse().init();
    List<Type> types = Lists.newArrayList();
    ParameterContext ctx = getParameterContext(types);
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("replace xxx into replace xxx"));
    assertThat(n.getSQLType(), is(SQLType.REPLACE));
  }

  @Test
  public void testMerge() throws Exception {
    String sql = "merge xxx into merge xxx";
    ASTRootNode n = new Parser(sql).parse().init();
    List<Type> types = Lists.newArrayList();
    ParameterContext ctx = getParameterContext(types);
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("merge xxx into merge xxx"));
    assertThat(n.getSQLType(), is(SQLType.MERGE));
  }

  @Test
  public void testStringLiteral() throws Exception {
    String sql = "select #if (:1 == 'hello') ok #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) String.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", "hello");
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select  ok "));
  }

  @Test
  public void testStringLiteral2() throws Exception {
    String sql = "select #if (:1 == 'hello') ok #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) String.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", "hello2");
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select "));
  }

  @Test
  public void testStringLiteral3() throws Exception {
    String sql = "select #if ('') ok #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) String.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", "hello2");
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select "));
  }

  @Test
  public void testStringLiteral4() throws Exception {
    String sql = "select #if (!'') ok #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) String.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", "hello2");
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select  ok "));
  }

  @Test
  public void testStringLiteral5() throws Exception {
    String sql = "select #if (:1) ok #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) String.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", "he");
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select  ok "));
  }

  @Test
  public void testStringLiteral6() throws Exception {
    String sql = "select #if (:1) ok #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList((Type) String.class));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    context.addParameter("1", "");
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select "));
  }

  @Test
  public void testQuote() throws Exception {
    String sql = "insert into table ... values(':dd',':xx')";
    ASTRootNode n = new Parser(sql).parse().init();
    List<Type> types = Lists.newArrayList();
    ParameterContext ctx = getParameterContext(types);
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql().toString(), equalTo("insert into table ... values(':dd',':xx')"));
    assertThat(boundSql.getArgs(), hasSize(0));
  }

  @Test
  public void testExpressionParameter4In() throws Exception {
    String sql = "select #if (:1) id in (:1) #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList(new TypeToken<List<Integer>>(){}.getType()));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    List<Integer> ids = Lists.newArrayList(1, 2, 3);
    context.addParameter("1", ids);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select  id in (?,?,?) "));
  }

  @Test
  public void testExpressionParameter4InEmpty() throws Exception {
    String sql = "select #if (:1) id in (:1) #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList(new TypeToken<List<Integer>>(){}.getType()));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    List<Integer> ids = Lists.newArrayList();
    context.addParameter("1", ids);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select "));
  }

  @Test
  public void testExpressionParameter4InNull() throws Exception {
    String sql = "select #if (:1) id in (:1) #end";
    ASTRootNode n = new Parser(sql).parse().init();
    ParameterContext ctx = getParameterContext(Lists.newArrayList(new TypeToken<List<Integer>>(){}.getType()));
    n.checkAndBind(ctx);
    InvocationContext context = DefaultInvocationContext.create();
    List<Integer> ids = null;
    context.addParameter("1", ids);
    n.render(context);
    BoundSql boundSql = context.getBoundSql();
    assertThat(boundSql.getSql(), Matchers.equalTo("select "));
  }

  @Test
  public void testJdbcType() throws Exception {
    String sql = "insert into table ... values(:1.b.c@blob) a in (:2.x.y@clob)";
    ASTRootNode n = new Parser(sql).parse().init();
    final AtomicInteger t = new AtomicInteger(0);
    n.jjtAccept(new ParserVisitorAdapter() {
      @Override
      public Object visit(ASTJDBCParameter node, Object data) {
        BindingParameter bp = node.getBindingParameter();
        assertThat(bp.getParameterName(), equalTo("1"));
        assertThat(bp.getPropertyPath(), equalTo("b.c"));
        assertThat(bp.getJdbcType(), equalTo(JdbcType.BLOB));
        t.incrementAndGet();
        return super.visit(node, data);
      }

      @Override
      public Object visit(ASTJDBCIterableParameter node, Object data) {
        BindingParameter bp = node.getBindingParameter();
        assertThat(bp.getParameterName(), equalTo("2"));
        assertThat(bp.getPropertyPath(), equalTo("x.y"));
        assertThat(bp.getJdbcType(), equalTo(JdbcType.CLOB));
        t.incrementAndGet();
        return super.visit(node, data);
      }
    }, null);
    assertThat(t.intValue(), equalTo(2));
  }

  private ParameterContext getParameterContext(List<Type> types) {
    List<Annotation> empty = Collections.emptyList();
    List<ParameterDescriptor> pds = Lists.newArrayList();
    int pos = 0;
    for (Type type : types) {
      ParameterDescriptor pd = ParameterDescriptor.create(pos++, type, empty, String.valueOf(pos));
      pds.add(pd);
    }
    ParameterContext ctx = DefaultParameterContext.create(pds);
    return ctx;
  }



}

















