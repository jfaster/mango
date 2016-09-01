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

package org.jfaster.mango.parser.visitor;

import com.google.common.collect.Lists;
import org.jfaster.mango.binding.BindingException;
import org.jfaster.mango.binding.DefaultParameterContext;
import org.jfaster.mango.binding.ParameterContext;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.Parser;
import org.jfaster.mango.util.reflect.TypeToken;
import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/**
 * @author ash
 */
public class ParameterExpandVisitorTest {

  @Test
  public void testVisitJDBCParameter() throws Exception {
    String sql = "select * from user where id=:id and #{:id} and #if (:id) #end";
    ASTRootNode rootNode = new Parser(sql.trim()).parse().init();

    List<Annotation> empty = Collections.emptyList();
    TypeToken<User> t = new TypeToken<User>() {
    };
    ParameterDescriptor p = ParameterDescriptor.create(0, t.getType(), empty, "1");
    List<ParameterDescriptor> pds = Lists.newArrayList(p);
    ParameterContext ctx = DefaultParameterContext.create(pds);

    rootNode.expandParameter(ctx);
    rootNode.dump(""); // TODO 返回值监测

  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testVisitJDBCParameter2() throws Exception {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Root parameters [:1, :2] has the same property 'id', so can't auto expand");

    String sql = "select * from user where id=:id and #{:id} and #if (:id) #end";
    ASTRootNode rootNode = new Parser(sql.trim()).parse().init();

    List<Annotation> empty = Collections.emptyList();
    TypeToken<User> t = new TypeToken<User>() {
    };
    ParameterDescriptor p = ParameterDescriptor.create(0, t.getType(), empty, "1");
    TypeToken<User2> t2 = new TypeToken<User2>() {
    };
    ParameterDescriptor p2 = ParameterDescriptor.create(1, t2.getType(), empty, "2");
    List<ParameterDescriptor> pds = Lists.newArrayList(p, p2);
    ParameterContext ctx = DefaultParameterContext.create(pds);
    rootNode.expandParameter(ctx);
  }

  static class User {
    private int id;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }
  }

  static class User2 {
    private int id;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }
  }

}
