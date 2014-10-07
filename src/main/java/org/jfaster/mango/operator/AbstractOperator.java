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

import org.jfaster.mango.jdbc.JdbcTemplate;
import org.jfaster.mango.operator.interceptor.RuntimeInterceptorChain;
import org.jfaster.mango.operator.stats.StatsCounter;
import org.jfaster.mango.parser.ASTRootNode;

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
    protected RuntimeInterceptorChain runtimeInterceptorChain;

    /**
     * 状态统计
     */
    protected StatsCounter statsCounter;

    /**
     * 数据源
     */
    protected DataSourceGenerator dataSourceGenerator;

    /**
     * 运行时环境工厂
     */
    protected RuntimeContextFactory runtimeContextFactory;

    /**
     * jdbc操作
     */
    protected JdbcTemplate jdbcTemplate;

    @Override
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void setRuntimeContextFactory(RuntimeContextFactory runtimeContextFactory) {
        this.runtimeContextFactory = runtimeContextFactory;
    }

    @Override
    public void setDataSourceGenerator(DataSourceGenerator dataSourceGenerator) {
        this.dataSourceGenerator = dataSourceGenerator;
    }

    @Override
    public void setRuntimeInterceptorChain(RuntimeInterceptorChain runtimeInterceptorChain) {
        this.runtimeInterceptorChain = runtimeInterceptorChain;
    }

    @Override
    public void setStatsCounter(StatsCounter statsCounter) {
        this.statsCounter = statsCounter;
    }

}
