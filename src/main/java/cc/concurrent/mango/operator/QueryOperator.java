package cc.concurrent.mango.operator;

import cc.concurrent.mango.jdbc.RowMapper;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.base.Objects;

import javax.sql.DataSource;
import java.util.Arrays;

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
    public Object execute(DataSource ds, ParsedSql... parsedSqls) {
        ParsedSql parsedSql = parsedSqls[0];
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug(Objects.toStringHelper("QueryOperator").add("sql", sql).add("args", Arrays.toString(args)).toString());
        }
        Object r;
        if (isForList) {
            r = jdbcTemplate.queryForList(ds, sql, args, rowMapper);
        } else if (isForSet) {
            r = jdbcTemplate.queryForSet(ds, sql, args, rowMapper);
        } else if (isForArray) {
            r = jdbcTemplate.queryForArray(ds, sql, args, rowMapper);
        } else {
            r = jdbcTemplate.queryForObject(ds, sql, args, rowMapper);
        }
        return r;
    }

}
