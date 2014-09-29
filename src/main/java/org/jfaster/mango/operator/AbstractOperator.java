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
