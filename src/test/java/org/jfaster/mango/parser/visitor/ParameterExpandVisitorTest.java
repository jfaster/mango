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
import org.jfaster.mango.operator.NameProvider;
import org.jfaster.mango.operator.OperatorType;
import org.jfaster.mango.operator.ParameterContext;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.Parser;
import org.jfaster.mango.reflect.ParameterDescriptor;
import org.jfaster.mango.reflect.TypeToken;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/**
 * @author ash
 */
public class ParameterExpandVisitorTest {

    @Test
    public void testVisitJDBCParameter() throws Exception {
        String sql = "select * from user where id=:id";
        ASTRootNode rootNode = new Parser(sql.trim()).parse().init();

        List<Annotation> empty = Collections.emptyList();
        TypeToken<User> t = new TypeToken<User>() {};
        ParameterDescriptor p = new ParameterDescriptor(0, t.getType(), t.getRawType(), empty, "1");
        List<ParameterDescriptor> pds = Lists.newArrayList(p);
        NameProvider np = new NameProvider(pds);
        ParameterContext ctx = new ParameterContext(pds, np, OperatorType.QUERY);

        rootNode.expandParameter(ctx);
        rootNode.dump("");

    }

    @Test
    public void testVisitJDBCIterableParameter() throws Exception {

    }

    @Test
    public void testVisitJoinParameter() throws Exception {

    }

    @Test
    public void testVisitExpressionParameter() throws Exception {

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

}
