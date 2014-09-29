package org.jfaster.mango.operator;

import org.jfaster.mango.exception.IncorrectReturnTypeException;
import org.jfaster.mango.jdbc.BeanPropertyRowMapper;
import org.jfaster.mango.jdbc.JdbcUtils;
import org.jfaster.mango.jdbc.RowMapper;
import org.jfaster.mango.jdbc.SingleColumnRowMapper;
import org.jfaster.mango.operator.driver.OperatorDriver;
import org.jfaster.mango.parser.node.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.support.SqlDescriptor;
import org.jfaster.mango.util.reflect.TypeToken;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author ash
 */
public class QueryOperator extends AbstractOperator {

    /**
     * operator驱动
     */
    private OperatorDriver driver;

    protected RowMapper<?> rowMapper;
    protected boolean isForList;
    protected boolean isForSet;
    protected boolean isForArray;

    protected QueryOperator(ASTRootNode rootNode, OperatorDriver driver, Method method) {
        super(rootNode);
        this.driver = driver;
        init(rootNode, method);
    }

    private void init(ASTRootNode rootNode, Method method) {
        TypeToken typeToken = new TypeToken(method.getGenericReturnType());
        isForList = typeToken.isList();
        isForSet = typeToken.isSet();
        isForArray = typeToken.isArray();
        Class<?> mappedClass = typeToken.getMappedClass();
        rowMapper = getRowMapper(mappedClass);

        List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
        if (!jips.isEmpty() && !isForList && !isForSet && !isForArray) {
            throw new IncorrectReturnTypeException("if sql has in clause, return type " +
                    "expected array or implementations of java.util.List or implementations of java.util.Set " +
                    "but " + method.getGenericReturnType()); // sql中使用了in查询，返回参数必须可迭代
        }
    }

    @Override
    public Object execute(Object[] methodArgs) {
        RuntimeContext context = driver.buildRuntimeContext(methodArgs);
        return execute(context);
    }

    protected Object execute(RuntimeContext context) {
        DataSource ds = driver.getDataSource(context);
        rootNode.render(context);
        SqlDescriptor sqlDescriptor = context.getSqlDescriptor();

        // 拦截器
        //handleByInterceptorChain(sqlDescriptor, context.getMethodArgs()); // TODO

        String sql = sqlDescriptor.getSql();
        Object[] args = sqlDescriptor.getArgs().toArray();

        return executeFromDb(ds, sql, args);
    }

    private Object executeFromDb(DataSource ds, String sql, Object[] args) {
        Object r;
        boolean success = false;
        long now = System.nanoTime();
        try {
            if (isForList) {
                r = jdbcTemplate.queryForList(ds, sql, args, rowMapper);
            } else if (isForSet) {
                r = jdbcTemplate.queryForSet(ds, sql, args, rowMapper);
            } else if (isForArray) {
                r= jdbcTemplate.queryForArray(ds, sql, args, rowMapper);
            } else {
                r = jdbcTemplate.queryForObject(ds, sql, args, rowMapper);
            }
            success = true;
        } finally {
            long cost = System.nanoTime() - now;
            if (success) {
                statsCounter.recordExecuteSuccess(cost);
            } else {
                statsCounter.recordExecuteException(cost);
            }
        }
        return r;
    }

    private static <T> RowMapper<T> getRowMapper(Class<T> clazz) {
        return JdbcUtils.isSingleColumnClass(clazz) ?
                new SingleColumnRowMapper<T>(clazz) :
                new BeanPropertyRowMapper<T>(clazz);
    }


}
