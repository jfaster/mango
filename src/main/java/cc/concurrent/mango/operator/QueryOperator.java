package cc.concurrent.mango.operator;

import cc.concurrent.mango.jdbc.core.BeanPropertyRowMapper;
import cc.concurrent.mango.jdbc.core.RowMapper;
import cc.concurrent.mango.jdbc.core.SingleColumnRowMapper;
import cc.concurrent.mango.jdbc.support.JdbcUtils;
import cc.concurrent.mango.logging.InternalLogger;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import cc.concurrent.mango.runtime.ParsedSql;
import com.google.common.base.Objects;

import javax.sql.DataSource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 处理所有的查询操作
 *
 * @author ash
 */
public class QueryOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(QueryOperator.class);

    private boolean isForList = false;
    private boolean isForSet = false;
    private boolean isForArray = false;
    private RowMapper<?> rowMapper;

    protected QueryOperator(Type returnType) {
        initialize(returnType);
    }

    private void initialize(Type returnType) {
        Class<?> mappedClass = null;
        if (returnType instanceof ParameterizedType) { // 参数化类型
            ParameterizedType parameterizedType = (ParameterizedType) returnType;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class) {
                Class<?> rawClass = (Class<?>) rawType;
                if (List.class.equals(rawClass)) {
                    isForList = true;
                    Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                    if (typeArgument instanceof Class) {
                        mappedClass = (Class<?>) typeArgument;
                    }
                } else if (Set.class.equals(rawClass)) {
                    isForSet = true;
                    Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                    if (typeArgument instanceof Class) {
                        mappedClass = (Class<?>) typeArgument;
                    }
                }
            }
        } else if (returnType instanceof Class) { // 没有参数化
            Class<?> clazz = (Class<?>) returnType;
            if (clazz.isArray()) { // 数组
                isForArray = true;
                mappedClass = clazz.getComponentType();
            } else { // 普通类
                mappedClass = clazz;
            }
        }
        checkNotNull(mappedClass);
        rowMapper = getRowMapper(mappedClass);
    }

    private <T> RowMapper<T> getRowMapper(Class<T> clazz) {
        return JdbcUtils.isSingleColumnClass(clazz) ?
                new SingleColumnRowMapper<T>(clazz) :
                new BeanPropertyRowMapper<T>(clazz);
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
