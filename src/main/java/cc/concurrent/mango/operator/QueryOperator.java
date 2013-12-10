package cc.concurrent.mango.operator;

import cc.concurrent.mango.logging.InternalLogger;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import cc.concurrent.mango.runtime.ParsedSql;
import com.google.common.base.Objects;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author ash
 */
public class QueryOperator extends AbstractOperator {

    private final InternalLogger logger = InternalLoggerFactory.getInstance(QueryOperator.class);

    private final boolean isForList;
    private final Class<?> requiredType;
    private final Class<?> elementType;

    protected QueryOperator(TypeToken returnType) {
        super(returnType);
        if (returnType.isArray()) {
            isForList = true;
            requiredType = null;
            elementType = returnType.getComponentType().getRawType();
        } else if (Collection.class.isAssignableFrom(returnType.getRawType())) {
            isForList = true;
            requiredType = null;
            elementType = returnType.resolveType(Collection.class.getTypeParameters()[0]).getRawType();
        } else {
            isForList = false;
            requiredType = returnType.wrap().getRawType();
            elementType = null;
        }
    }

    @Override
    public Object execute(ParsedSql... parsedSqls) {
        ParsedSql parsedSql = parsedSqls[0];
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug(Objects.toStringHelper("QueryOperator").add("sql", sql).add("args", Arrays.toString(args)).toString());
        }
        return isForList ?
                jdbcTemplate.queryForList(sql, args, elementType) :
                jdbcTemplate.queryForObject(sql, args, requiredType);
    }

}
