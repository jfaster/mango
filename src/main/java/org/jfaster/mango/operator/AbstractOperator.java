package org.jfaster.mango.operator;

import org.jfaster.mango.jdbc.JdbcTemplate;
import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(AbstractOperator.class);

    protected AbstractOperator(ASTRootNode rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * 渲染sql的树节点
     */
    protected ASTRootNode rootNode;

    /**
     * 用于对db进行操作
     */
    protected JdbcTemplate jdbcTemplate = new JdbcTemplate();

    /**
     * 统计信息，通过{@link this#setStatsCounter(org.jfaster.mango.operator.StatsCounter)}初始化
     */
    protected StatsCounter statsCounter;


}
