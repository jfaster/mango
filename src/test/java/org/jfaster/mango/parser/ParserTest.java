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

import org.jfaster.mango.operator.InvocationContext;
import org.jfaster.mango.operator.PreparedSql;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * @author ash
 */
public class ParserTest {

    @Test
    public void testBase() throws Exception {
        String sql = "select #{:1} from user where id in (:2) and name=:3";
        ASTRootNode n = new Parser(sql).parse().init();
        InvocationContext context = new InvocationContext();
        context.addParameter("1", "id");
        context.addParameter("2", Arrays.asList(9, 5, 2, 7));
        context.addParameter("3", "ash");
        n.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        assertThat(preparedSql.getSql().toString(), equalTo("select id from user where id in (?,?,?,?) and name=?"));
        assertThat(preparedSql.getArgs(), contains(new Object[]{9, 5, 2, 7, "ash"}));
    }

    @Test
    public void testIf() throws Exception {
        String sql = "select where 1=1 #if(:1) and id>:1 #end";
        ASTRootNode n = new Parser(sql).parse().init();
        InvocationContext context = new InvocationContext();
        context.addParameter("1", 100);
        n.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        assertThat(preparedSql.getSql().toString(), equalTo("select where 1=1  and id>? "));
        assertThat(preparedSql.getArgs(), contains(new Object[]{100}));
    }

    @Test
    public void testIf2() throws Exception {
        String sql = "select where 1=1 #if(!:1) and id>:1 #end";
        ASTRootNode n = new Parser(sql).parse().init();
        InvocationContext context = new InvocationContext();
        context.addParameter("1", 100);
        n.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        assertThat(preparedSql.getSql().toString(), equalTo("select where 1=1 "));
        assertThat(preparedSql.getArgs().size(), equalTo(0));
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
        InvocationContext context = new InvocationContext();
        context.addParameter("1", 100);
        n.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        assertThat(preparedSql.getSql().toString(), equalTo("select where 1=1 and id>?"));
        assertThat(preparedSql.getArgs(), contains(new Object[]{100}));
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
        InvocationContext context = new InvocationContext();
        context.addParameter("1", -100);
        n.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        assertThat(preparedSql.getSql().toString(), equalTo("select where 1=1 and id<?"));
        assertThat(preparedSql.getArgs(), contains(new Object[]{-100}));
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
        InvocationContext context = new InvocationContext();
        context.addParameter("1", 100);
        n.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        assertThat(preparedSql.getSql().toString(), equalTo("select where 1=1 and id>?"));
        assertThat(preparedSql.getArgs(), contains(new Object[]{100}));
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
        InvocationContext context = new InvocationContext();
        context.addParameter("1", -100);
        n.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        assertThat(preparedSql.getSql().toString(), equalTo("select where 1=1 and id<?"));
        assertThat(preparedSql.getArgs(), contains(new Object[]{-100}));
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
        InvocationContext context = new InvocationContext();
        context.addParameter("1", 0);
        n.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        assertThat(preparedSql.getSql().toString(), equalTo("select where 1=1 and id=?"));
        assertThat(preparedSql.getArgs(), contains(new Object[]{0}));
    }

    @Test
    public void testExpression() throws Exception {
        String sql = "select where 1=1 #if(:1==false && :2!=null && :3==true) and id>10 #end";
        ASTRootNode n = new Parser(sql).parse().init();
        InvocationContext context = new InvocationContext();
        context.addParameter("1", false);
        context.addParameter("2", new Object());
        context.addParameter("3", true);
        n.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        assertThat(preparedSql.getSql().toString(), equalTo("select where 1=1  and id>10 "));
    }

    @Test
    public void testParse() throws Exception {
        String sql = "SELECT * from user where id in ( select id from user2 )";
        ASTRootNode n = new Parser(sql).parse().init();
        InvocationContext context = new InvocationContext();
        n.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        assertThat(preparedSql.getSql().toString(), equalTo("SELECT * from user where id in ( select id from user2 )"));
    }

}

















