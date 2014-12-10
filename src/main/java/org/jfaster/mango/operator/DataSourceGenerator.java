package org.jfaster.mango.operator;

import org.jfaster.mango.datasource.factory.DataSourceFactory;
import org.jfaster.mango.datasource.factory.DataSourceType;
import org.jfaster.mango.datasource.router.DataSourceRouter;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.transaction.TransactionSynchronizationManager;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * @author ash
 */
public class DataSourceGenerator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(DataSourceGenerator.class);

    private final DataSourceFactory dataSourceFactory;
    private final DataSourceType dataSourceType;
    private final String dataSourceName;
    private final String shardParameterName;
    private final String shardPropertyPath; // 为""的时候表示没有属性
    private final DataSourceRouter dataSourceRouter; // 分表

    public DataSourceGenerator(DataSourceFactory dataSourceFactory, DataSourceType dataSourceType,
                               String dataSourceName, String shardParameterName,
                               String shardPropertyPath, DataSourceRouter dataSourceRouter) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataSourceType = dataSourceType;
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
        DataSourceType dst = dataSourceType;
        if (TransactionSynchronizationManager.inTransaction()) { // 事务使用主数据源
            dst = DataSourceType.MASTER;
        }
        DataSource ds = dataSourceFactory.getDataSource(dataSourceName, dst);
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
