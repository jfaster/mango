/*
 * Copyright 2014 mango.concurrent.cc
 *
 * The Netty Project licenses this file to you under the Apache License,
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

package cc.concurrent.mango.runtime.parser;


import cc.concurrent.mango.exception.UnreachableCodeException;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.util.Iterables;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象语法树根节点
 *
 * @author ash
 */
public class ASTRootNode extends SimpleNode {

    public ASTRootNode(int i) {
        super(i);
    }

    public ASTRootNode(Parser p, int i) {
        super(p, i);
    }

    /**
     * 检测节点类型
     *
     * @param context
     */
    public void checkType(TypeContext context) {
        for (int i = 0; i < jjtGetNumChildren(); i++) {
            Node node = jjtGetChild(i);
            if (node instanceof ValuableNode) {
                ((ValuableNode) node).checkType(context);
            }
        }
    }

    /**
     * 获得可迭代参数节点
     *
     * @return
     */
    public List<ASTIterableParameter> getASTIterableParameters() {
        List<ASTIterableParameter> aips = new ArrayList<ASTIterableParameter>();
        for (int i = 0; i < jjtGetNumChildren(); i++) {
            Node node = jjtGetChild(i);
            if (node instanceof ASTIterableParameter) {
                aips.add((ASTIterableParameter) node);
            }
        }
        return aips;
    }

    /**
     * 获得可迭代参数节点与不可迭代参数节点
     *
     * @return
     */
    public List<ValuableParameter> getValueValuableParameters() {
        List<ValuableParameter> vps = new ArrayList<ValuableParameter>();
        for (int i = 0; i < jjtGetNumChildren(); i++) {
            Node node = jjtGetChild(i);
            if (node instanceof ValuableParameter) {
                vps.add((ValuableParameter) node);
            }
        }
        return vps;
    }

    /**
     * 构建sql与参数
     *
     * @param context
     * @return
     */
    public ParsedSql buildSqlAndArgs(RuntimeContext context) {
        StringBuffer sql = new StringBuffer();
        List<Object> args = new ArrayList<Object>();
        for (int i = 0; i < jjtGetNumChildren(); i++) {
            Node node = jjtGetChild(i);
            if (node instanceof ASTText) {
                ASTText text = (ASTText) node;
                sql.append(text.getText());
            } else if (node instanceof ASTNonIterableParameter) {
                ASTNonIterableParameter anip = (ASTNonIterableParameter) node;
                args.add(anip.value(context));
                sql.append("?");
            } else if (node instanceof ASTIterableParameter) {
                ASTIterableParameter aip = (ASTIterableParameter) node;
                sql.append(aip.getInterableProperty()).append(" in (");
                Object objs = aip.value(context);
                int t = 0;
                for (Object obj : new Iterables(objs)) {
                    args.add(obj);
                    if (t == 0) {
                        sql.append("?");
                    } else {
                        sql.append(",?");
                    }
                    t++;
                }
                sql.append(")");
            } else if (node instanceof ASTExpression) {
                sql.append(((ASTExpression) node).value(context));
            } else {
                throw new UnreachableCodeException();
            }
            if (i < jjtGetNumChildren() - 1) {
                sql.append(" "); // 节点之间添加空格
            }
        }
        return new ParsedSql(sql.toString(), args.toArray());
    }

}
