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
     * 拦截器链
     */
    protected RuntimeInterceptorChain runtimeInterceptorChain;

    /**
     * 状态统计
     */
    protected StatsCounter statsCounter;

    /**
     * jdbc操作
     */
    protected final JdbcTemplate jdbcTemplate = new JdbcTemplate();

    /**
     * 用于对db进行操作
     */
    protected AbstractOperator(ASTRootNode rootNode) {
        this.rootNode = rootNode;
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
