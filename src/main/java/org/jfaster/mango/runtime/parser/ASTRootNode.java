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

package org.jfaster.mango.runtime.parser;


import org.jfaster.mango.TablePartition;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.exception.IncorrectSqlException;
import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.runtime.RuntimeContext;
import org.jfaster.mango.runtime.TypeContext;
import org.jfaster.mango.util.Iterables;

import javax.annotation.Nullable;
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
     * 静态sql，不需要根据运行时参数动态生成。给{@link java.sql.PreparedStatement}使用
     */
    private String staticSql;

    /**
     * 可遍历的参数
     */
    private List<ASTIterableParameter> iterableParameters = new LinkedList<ASTIterableParameter>();

    /**
     * 可得到值的参数
     */
    private List<ValuableParameter> valuableParameters = new LinkedList<ValuableParameter>();

    /**
     * table节点，最多只能有1个
     */
    private ASTTable tableNode;

    public ASTRootNode(int i) {
        super(i);
    }

    public ASTRootNode(Parser p, int i) {
        super(p, i);
    }

    /**
     * 简化节点
     */
    public ASTRootNode reduce() {
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
            handleNode(node);
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
        return this;
    }

    /**
     * 初始化
     */
    public void init(@Nullable String table,
                                @Nullable TablePartition tablePartition,
                                @Nullable String shardParameterName,
                                @Nullable String shardPropertyPath) {
        if (tableNode != null && table == null) {
            throw new IncorrectDefinitionException("if sql contains #table, @DB.table must define");
        }
        if (tableNode == null && table != null) {
            throw new IncorrectDefinitionException("if @DB.table is defined, sql must contain #table");
        }
        if (tableNode != null) {
            if ((tablePartition == null && shardParameterName == null && shardPropertyPath == null)
                    || (tablePartition != null && shardParameterName != null && shardPropertyPath != null)) {
                tableNode.setTable(table);
                tableNode.setTablePartition(tablePartition);
                tableNode.setShardParameterName(shardParameterName);
                tableNode.setShardPpropertyPath(shardPropertyPath);
            } else {
                throw new UnreachableCodeException();
            }
        }

        tryBuildSql();
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

    public String getStaticSql() {
        return staticSql;
    }

    /**
     * 获得运行时sql
     *
     * @param context
     * @return
     */
    public String getSql(RuntimeContext context) {
        if (staticSql != null) {
            return staticSql;
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
            } else if (node instanceof ASTTable) {
                ASTTable t = (ASTTable) node;
                if (t.needTablePartition()) {
                    sql.append(t.getTable(context));
                } else {
                    sql.append(t.getTable());
                }
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

    private void handleNode(AbstractNode node) {
        if (node instanceof ASTIterableParameter) {
            iterableParameters.add((ASTIterableParameter) node);
        }
        if (node instanceof ValuableParameter) {
            valuableParameters.add((ValuableParameter) node);
        }
        if (node instanceof ASTTable) {
            if (tableNode != null) {
                throw new IncorrectSqlException("too many #table in sql");
            }
            tableNode = (ASTTable) node;
        }
    }

    /**
     * 尝试构建sql
     */
    private void tryBuildSql() {
        StringBuffer sb = new StringBuffer();
        AbstractNode node = head;
        boolean fail = false;
        while (node != null) {
            if (node instanceof ASTString) {
                ASTString str = (ASTString) node;
                sb.append(str.getGroupValue());
            } else if (node instanceof ASTNonIterableParameter) {
                sb.append("?");
            } else if (node instanceof ASTTable) {
                ASTTable t = (ASTTable) node;
                if (!t.needTablePartition()) {
                    sb.append(t.getTable());
                } else {
                    fail = true;
                }
            } else {
                fail = true;
            }
            if (fail) {
                break;
            }
            node = node.next;
        }
        if (!fail) {
            staticSql = sb.toString();
        }
    }

}
