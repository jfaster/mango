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
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.util.Iterables;

import java.util.LinkedList;
import java.util.List;

/**
 * 抽象语法树根节点
 *
 * @author ash
 */
public class ASTRootNode extends AbstractNode {

    /**
     * 头节点
     */
    private AbstractNode head;

    /**
     * 给{@link java.sql.PreparedStatement}使用的sql
     */
    private String sql;

    /**
     * 可遍历的参数
     */
    private List<ASTIterableParameter> iterableParameters = new LinkedList<ASTIterableParameter>();

    /**
     * 可得到值的参数
     */
    private List<ValuableParameter> valuableParameters = new LinkedList<ValuableParameter>();

    /**
     * 会影响sql的节点
     */
    private List<ValuableNode> impactSqlNodes = new LinkedList<ValuableNode>();

    public ASTRootNode(int i) {
        super(i);
    }

    public ASTRootNode(Parser p, int i) {
        super(p, i);
    }

    /**
     * 预处理
     */
    public ASTRootNode preprocessing() {
        init();
        tryBuildSql();
        return this;
    }

    /**
     * 检测节点类型
     *
     * @param context
     */
    public void checkType(TypeContext context) {
        AbstractNode node = head;
        while (node != null) {
            if (node instanceof ValuableNode) {
                ((ValuableNode) node).checkType(context);
            }
            node = node.next;
        }
    }

    /**
     * 获得可迭代参数节点
     *
     * @return
     */
    public List<ASTIterableParameter> getIterableParameters() {
        return iterableParameters;
    }

    /**
     * 获得可迭代参数节点与不可迭代参数节点
     *
     * @return
     */
    public List<ValuableParameter> getValuableParameters() {
        return valuableParameters;
    }

    /**
     * 获得运行时sql
     *
     * @param context
     * @return
     */
    public String getSql(RuntimeContext context) {
        if (sql != null) {
            return sql;
        }
        StringBuffer sql = new StringBuffer();
        AbstractNode node = head;
        while (node != null) {
            if (node instanceof ASTString) {
                ASTString str = (ASTString) node;
                sql.append(str.getGroupValue());
            } else if (node instanceof ASTNonIterableParameter) {
                sql.append("?");
            } else if (node instanceof ASTIterableParameter) {
                ASTIterableParameter aip = (ASTIterableParameter) node;
                sql.append(aip.getInterableProperty()).append(" in (");
                Object objs = aip.value(context);
                if (objs == null) {
                    throw new NullPointerException("value of " + aip.getFullName() + " can't be null");
                }
                Iterables iterables = new Iterables(objs);
                int size = iterables.size();
                if (size == 0) {
                    throw new IllegalArgumentException("value of " + aip.getFullName() + " can't be empty");
                }
                StringBuffer sb = new StringBuffer();
                sb.append("?");
                if (size > 1) {
                    for (int i = 1; i < size; i++) {
                        sb.append(",?");
                    }
                }
                sql.append(sb).append(")");
            } else if (node instanceof ASTExpression) {
                sql.append(((ASTExpression) node).value(context));
            } else {
                throw new UnreachableCodeException();
            }
            node = node.next;
        }
        return sql.toString();
    }

    /**
     * 构建运行时参数列表
     *
     * @param context
     * @return
     */
    public Object[] getArgs(RuntimeContext context) {
        List<Object> args = new LinkedList<Object>();
        for (ValuableParameter node : valuableParameters) {
            if (node instanceof ASTNonIterableParameter) {
                ASTNonIterableParameter anip = (ASTNonIterableParameter) node;
                args.add(anip.value(context));
            } else if (node instanceof ASTIterableParameter) {
                ASTIterableParameter aip = (ASTIterableParameter) node;
                Object objs = aip.value(context);
                if (objs == null) {
                    throw new NullPointerException(aip.getFullName() + " can't be null");
                }
                Iterables iterables = new Iterables(objs);
                int size = iterables.size();
                if (size == 0) {
                    throw new IllegalArgumentException(aip.getFullName() + " can't be empty");
                }
                for (Object obj : iterables) {
                    args.add(obj);
                }
            }
        }
        return args.toArray();
    }

    /**
     * 初始化
     */
    private void init() {
        int num = jjtGetNumChildren();
        int i = 0;
        AbstractNode prev = null;
        while (i < num) {
            AbstractNode node = (AbstractNode) jjtGetChild(i);
            if (prev == null) {
                head = node;
            } else {
                prev.next = node;
            }
            prev = node;
            appendNode(node);
            if (node instanceof ASTString) {
                StringBuffer sb = new StringBuffer();
                while (node instanceof ASTString) {
                    ASTString str = (ASTString) node;
                    sb.append(str.getValue());
                    i++;
                    if (i == num) {
                        break;
                    }
                    node = (AbstractNode) jjtGetChild(i);
                }
                ((ASTString) prev).setGroupValue(sb.toString());
            } else {
                i++;
            }
        }
    }

    private void appendNode(AbstractNode node) {
        if (node instanceof ASTIterableParameter) {
            iterableParameters.add((ASTIterableParameter) node);
        }
        if (node instanceof ValuableParameter) {
            valuableParameters.add((ValuableParameter) node);
        }
        if (node instanceof ASTIterableParameter || node instanceof ASTExpression) {
            impactSqlNodes.add((ValuableNode) node);
        }
    }

    /**
     * 尝试构建sql
     */
    private void tryBuildSql() {
        if (impactSqlNodes.size() == 0) {
            StringBuffer sb = new StringBuffer();
            AbstractNode node = head;
            while (node != null) {
                if (node instanceof ASTString) {
                    ASTString str = (ASTString) node;
                    sb.append(str.getGroupValue());
                } else if (node instanceof ASTNonIterableParameter) {
                    sb.append("?");
                } else {
                    throw new UnreachableCodeException();
                }
                node = node.next;
            }
            sql = sb.toString();
        }
    }

}
