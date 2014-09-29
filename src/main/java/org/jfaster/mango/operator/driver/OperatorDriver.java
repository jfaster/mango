package org.jfaster.mango.operator.driver;

import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.support.TypeContext;

import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * @author ash
 */
public interface OperatorDriver {

    public TypeContext getTypeContext();

    public RuntimeContext buildRuntimeContext(Object[] methodArgs); // TODO 参数换名字

    public DataSource getDataSource(RuntimeContext context);

    public DataSource getDataSource(String dataSourceName);

    @Nullable
    public String getDataSourceName(RuntimeContext context);

}
