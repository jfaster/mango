package org.jfaster.mango.operator;

import org.jfaster.mango.datasource.factory.DataSourceFactory;
import org.jfaster.mango.datasource.router.DataSourceRouter;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.util.SQLType;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * @author ash
 */
public class DataSourceGenerator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(DataSourceGenerator.class);

    private DataSourceFactory dataSourceFactory;
    private SQLType sqlType;

    private String dataSourceName;
    private String shardParameterName;
    private String shardPropertyPath; // 为""的时候表示没有属性
    private DataSourceRouter dataSourceRouter; // 分表

    public DataSourceGenerator(DataSourceFactory dataSourceFactory, SQLType sqlType,
                               String dataSourceName, String shardParameterName,
                               String shardPropertyPath, DataSourceRouter dataSourceRouter) {
        this.dataSourceFactory = dataSourceFactory;
        this.sqlType = sqlType;
        this.dataSourceName = dataSourceName;
        this.shardParameterName = shardParameterName;
        this.shardPropertyPath = shardPropertyPath;
        this.dataSourceRouter = dataSourceRouter;
    }

    public DataSource getDataSource(InvocationContext context) {
        return getDataSource(getDataSourceName(context));
    }

    public DataSource getDataSource(String dataSourceName) {
        if (logger.isDebugEnabled()) {
            logger.debug("The name of Datasource is [" + dataSourceName + "]");
        }
        DataSource ds = dataSourceFactory.getDataSource(dataSourceName, sqlType);
        if (ds == null) {
            throw new IncorrectDefinitionException("can't find datasource for name [" + dataSourceName + "]");
        }
        return ds;
    }

    @Nullable
    public String getDataSourceName(InvocationContext context) {
        String realDataSourceName = dataSourceRouter != null ?
                dataSourceRouter.getDataSourceName(context.getPropertyValue(shardParameterName, shardPropertyPath)) :
                dataSourceName;
        return realDataSourceName;
    }

}
