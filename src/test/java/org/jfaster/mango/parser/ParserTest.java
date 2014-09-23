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

import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.support.RuntimeContextImpl;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("1", "id");
        params.put("2", Arrays.asList(9, 5, 2, 7));
        params.put("3", "ash");
        RuntimeContextImpl context = new RuntimeContextImpl(params);
        n.render(context);
        assertThat(context.getSql(), equalTo("select id from user where id in (?,?,?,?) and name=?"));
        assertThat(Arrays.asList(context.getArgs()), contains(new Object[]{9, 5, 2, 7, "ash"}));
    }

    @Test
    public void testIf() throws Exception {
        String sql = "where 1=1 #if(:1) and id>:1 #end";
        ASTRootNode n = new Parser(sql).parse().init();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("1", 100);
        RuntimeContextImpl context = new RuntimeContextImpl(params);
        n.render(context);
        assertThat(context.getSql(), equalTo("where 1=1  and id>? "));
        assertThat(Arrays.asList(context.getArgs()), contains(new Object[]{100}));
    }

    @Test
    public void testIf2() throws Exception {
        String sql = "where 1=1 #if(!:1) and id>:1 #end";
        ASTRootNode n = new Parser(sql).parse().init();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("1", 100);
        RuntimeContextImpl context = new RuntimeContextImpl(params);
        n.render(context);
        assertThat(context.getSql(), equalTo("where 1=1 "));
        assertThat(context.getArgs().length, equalTo(0));
    }

    @Test
    public void testIfElseIf() throws Exception {
        String sql = "where 1=1" +
                "#if(:1>0)" +
                    " and id>:1" +
                "#elseif(:1<0)" +
                    " and id<:1" +
                "#end";
        ASTRootNode n = new Parser(sql).parse().init();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("1", 100);
        RuntimeContextImpl context = new RuntimeContextImpl(params);
        n.render(context);
        assertThat(context.getSql(), equalTo("where 1=1 and id>?"));
        assertThat(Arrays.asList(context.getArgs()), contains(new Object[]{100}));
    }

    @Test
    public void testIfElseIf2() throws Exception {
        String sql = "where 1=1" +
                "#if(:1>0)" +
                    " and id>:1" +
                "#elseif(:1<0)" +
                    " and id<:1" +
                "#end";
        ASTRootNode n = new Parser(sql).parse().init();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("1", -100);
        RuntimeContextImpl context = new RuntimeContextImpl(params);
        n.render(context);
        assertThat(context.getSql(), equalTo("where 1=1 and id<?"));
        assertThat(Arrays.asList(context.getArgs()), contains(new Object[]{-100}));
    }

    @Test
    public void testIfElseIfElse() throws Exception {
        String sql = "where 1=1" +
                "#if(:1>0)" +
                    " and id>:1" +
                "#elseif(:1<0)" +
                    " and id<:1" +
                "#else" +
                    " and id=:1" +
                "#end";
        ASTRootNode n = new Parser(sql).parse().init();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("1", 100);
        RuntimeContextImpl context = new RuntimeContextImpl(params);
        n.render(context);
        assertThat(context.getSql(), equalTo("where 1=1 and id>?"));
        assertThat(Arrays.asList(context.getArgs()), contains(new Object[]{100}));
    }

    @Test
    public void testIfElseIfElse2() throws Exception {
        String sql = "where 1=1" +
                "#if(:1>0)" +
                    " and id>:1" +
                "#elseif(:1<0)" +
                    " and id<:1" +
                "#else" +
                    " and id=:1" +
                "#end";
        ASTRootNode n = new Parser(sql).parse().init();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("1", -100);
        RuntimeContextImpl context = new RuntimeContextImpl(params);
        n.render(context);
        assertThat(context.getSql(), equalTo("where 1=1 and id<?"));
        assertThat(Arrays.asList(context.getArgs()), contains(new Object[]{-100}));
    }

    @Test
    public void testIfElseIfElse3() throws Exception {
        String sql = "where 1=1" +
                "#if(:1>0)" +
                    " and id>:1" +
                "#elseif(:1<0)" +
                    " and id<:1" +
                "#else" +
                    " and id=:1" +
                "#end";
        ASTRootNode n = new Parser(sql).parse().init();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("1", 0);
        RuntimeContextImpl context = new RuntimeContextImpl(params);
        n.render(context);
        assertThat(context.getSql(), equalTo("where 1=1 and id=?"));
        assertThat(Arrays.asList(context.getArgs()), contains(new Object[]{0}));
    }

    @Test
    public void testExpression() throws Exception {
        String sql = "where 1=1 #if(:1==false && :2!=null && :3==true) and id>10 #end";
        ASTRootNode n = new Parser(sql).parse().init();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("1", false);
        params.put("2", new Object());
        params.put("3", true);
        RuntimeContextImpl context = new RuntimeContextImpl(params);
        n.render(context);
        assertThat(context.getSql(), equalTo("where 1=1  and id>10 "));
    }

}

















