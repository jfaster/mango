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
import org.jfaster.mango.parser.node.ASTRootNode;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    /**
     * 渲染sql的树节点
     */
    protected final ASTRootNode rootNode;

    /**
     * 统计信息，通过{@link this#setStatsCounter(org.jfaster.mango.operator.StatsCounter)}初始化
     */
    protected final StatsCounter statsCounter;

    /**
     * 用于对db进行操作
     */
    protected final JdbcTemplate jdbcTemplate = new JdbcTemplate();

    protected AbstractOperator(ASTRootNode rootNode, StatsCounter statsCounter) {
        this.rootNode = rootNode;
        this.statsCounter = statsCounter;
    }

}
