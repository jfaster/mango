package cc.concurrent.mango.operator;

import cc.concurrent.mango.jdbc.RowMapper;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Map;

/**
 * 处理所有的查询操作
 *
 * @author ash
 */
public class QueryOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(QueryOperator.class);

    private ASTRootNode rootNode;
    private RowMapper<?> rowMapper;
    private boolean isForList;
    private boolean isForSet;
    private boolean isForArray;

    public QueryOperator(ASTRootNode rootNode, RowMapper<?> rowMapper, boolean isForList, boolean isForSet, boolean isForArray) {
        this.rootNode = rootNode;
        this.rowMapper = rowMapper;
        this.isForList = isForList;
        this.isForSet = isForSet;
        this.isForArray = isForArray;
    }

    @Override
    public Object execute(DataSource ds, Object[] methodArgs) {
        Map<String, Object> parameters = Maps.newHashMap();
        for (int i = 0; i < methodArgs.length; i++) {
            parameters.put(String.valueOf(i + 1), methodArgs[i]);
        }
        RuntimeContext context = new RuntimeContextImpl(parameters);
        ParsedSql parsedSql = rootNode.getSqlAndArgs(context);

        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug(Objects.toStringHelper("QueryOperator").add("sql", sql).add("args", Arrays.toString(args)).toString());
        }
        Object r;
        if (isForList) {
            r = jdbcTemplate.queryForList(sql, args, rowMapper);
        } else if (isForSet) {
            r = jdbcTemplate.queryForSet(sql, args, rowMapper);
        } else if (isForArray) {
            r = jdbcTemplate.queryForArray(sql, args, rowMapper);
        } else {
            r = jdbcTemplate.queryForObject(sql, args, rowMapper);
        }
        return r;
    }

}
