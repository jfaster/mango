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

package org.jfaster.mango.operator;

import org.jfaster.mango.jdbc.JdbcOperations;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.RuntimeEmptyParameter;
import org.jfaster.mango.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    /**
     * 渲染sql的树节点
     */
    protected final ASTRootNode rootNode;

    /**
     * 用于对db进行操作
     */
    protected AbstractOperator(ASTRootNode rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * 拦截器链
     */
    protected InvocationInterceptorChain invocationInterceptorChain;

    /**
     * 状态统计
     */
    protected StatsCounter statsCounter;

    /**
     * 全局表
     */
    protected TableGenerator tableGenerator;

    /**
     * 数据源
     */
    protected DataSourceGenerator dataSourceGenerator;

    /**
     * 运行时环境工厂
     */
    protected InvocationContextFactory invocationContextFactory;

    /**
     * jdbc操作
     */
    protected JdbcOperations jdbcOperations;

    /**
     * mango全局配置信息
     */
    protected Config config;

    @Override
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public void setInvocationContextFactory(InvocationContextFactory invocationContextFactory) {
        this.invocationContextFactory = invocationContextFactory;
    }

    @Override
    public void setTableGenerator(TableGenerator tableGenerator) {
        this.tableGenerator = tableGenerator;
    }

    @Override
    public void setDataSourceGenerator(DataSourceGenerator dataSourceGenerator) {
        this.dataSourceGenerator = dataSourceGenerator;
    }

    @Override
    public void setInvocationInterceptorChain(InvocationInterceptorChain invocationInterceptorChain) {
        this.invocationInterceptorChain = invocationInterceptorChain;
    }

    @Override
    public void setStatsCounter(StatsCounter statsCounter) {
        this.statsCounter = statsCounter;
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    protected void throwEmptyParametersException(InvocationContext context) {
        List<String> fullNames = new ArrayList<String>();
        for (RuntimeEmptyParameter rep : context.getRuntimeEmptyParameters()) {
            fullNames.add(Strings.getFullName(rep.getParameterName(), rep.getPropertyPath()));
        }
        String str = fullNames.size() == 1 ? fullNames.get(0) : fullNames.toString();
        throw new IllegalArgumentException("value of " +
                str + " can't be empty, error SQL [" + context.getPreparedSql().getSql() + "]");
    }

}
