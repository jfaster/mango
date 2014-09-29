package org.jfaster.mango.operator;

import org.jfaster.mango.annotation.ReturnGeneratedId;
import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.jdbc.GeneratedKeyHolder;
import org.jfaster.mango.operator.driver.OperatorDriver;
import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.support.SQLType;
import org.jfaster.mango.support.SqlDescriptor;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

    /**
     * operator驱动
     */
    private OperatorDriver driver;

    private boolean returnGeneratedId;

    private Class<? extends Number> returnType;

    protected UpdateOperator(ASTRootNode rootNode, OperatorDriver driver, Method method, SQLType sqlType, StatsCounter statsCounter) { // TODO
        super(rootNode, statsCounter);
        this.driver = driver;
        init(method, sqlType);
    }

    private void init(Method method, SQLType sqlType) {
        ReturnGeneratedId returnGeneratedIdAnno = method.getAnnotation(ReturnGeneratedId.class);
        returnGeneratedId = returnGeneratedIdAnno != null // 要求返回自增id
                && sqlType == SQLType.INSERT; // 是插入语句
        if (int.class.equals(method.getReturnType())) {
            returnType = int.class;
        } else if (long.class.equals(method.getReturnType())) {
            returnType = long.class;
        } else {
            throw new UnreachableCodeException();
        }
    }

    @Override
    public Object execute(Object[] methodArgs) {
        RuntimeContext context = driver.buildRuntimeContext(methodArgs);
        return execute(context);
    }

    public Number execute(RuntimeContext context) {
        DataSource ds = driver.getDataSource(context);
        rootNode.render(context);
        SqlDescriptor sqlDescriptor = context.getSqlDescriptor();

        // 拦截器
        // handleByInterceptorChain(sqlDescriptor, context.getMethodArgs()); // TODO

        String sql = sqlDescriptor.getSql();
        Object[] args = sqlDescriptor.getArgs().toArray();
        Number r = executeDb(ds, sql, args);
        return r;
    }

    private Number executeDb(DataSource ds, String sql, Object[] args) {
        Number r = null;
        long now = System.nanoTime();
        try {
            if (returnGeneratedId) {
                GeneratedKeyHolder holder = new GeneratedKeyHolder(returnType);
                jdbcTemplate.update(ds, sql, args, holder);
                r = holder.getKey();
            } else {
                r = jdbcTemplate.update(ds, sql, args);
            }
        } finally {
            long cost = System.nanoTime() - now;
            if (r != null) {
                statsCounter.recordExecuteSuccess(cost);
            } else {
                statsCounter.recordExecuteException(cost);
            }
        }
        return r;
    }

}
