package cc.concurrent.mango.operator;

import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.base.Objects;

import javax.sql.DataSource;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(UpdateOperator.class);

    private ASTRootNode rootNode;
    private boolean returnGeneratedId;

    public UpdateOperator(ASTRootNode rootNode, boolean returnGeneratedId) {
        this.rootNode = rootNode;
        this.returnGeneratedId = returnGeneratedId;
    }

    @Override
    public Object execute(DataSource ds, ParsedSql... parsedSqls) {
        ParsedSql parsedSql = parsedSqls[0];
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug(Objects.toStringHelper("UpdateOperator").add("sql", sql).add("args", Arrays.toString(args)).toString());
        }
        return jdbcTemplate.update(ds, sql, args, returnGeneratedId);
    }
}
