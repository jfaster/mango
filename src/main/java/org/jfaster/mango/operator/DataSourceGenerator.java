package org.jfaster.mango.operator;

import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.DataSourceType;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.invoker.GetterInvokerChain;
import org.jfaster.mango.partition.DataSourceRouter;
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
    private final GetterInvokerChain shardByInvokerChain;
    private final DataSourceRouter dataSourceRouter; // 分表

    public DataSourceGenerator(DataSourceFactory dataSourceFactory, DataSourceType dataSourceType,
                               String dataSourceName, String shardParameterName,
                               GetterInvokerChain shardByInvokerChain, DataSourceRouter dataSourceRouter) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataSourceType = dataSourceType;
        this.dataSourceName = dataSourceName;
        this.shardParameterName = shardParameterName;
        this.shardByInvokerChain = shardByInvokerChain;
        this.dataSourceRouter = dataSourceRouter;
    }

    public DataSource getDataSource(InvocationContext context) {
        return getDataSource(getDataSourceName(context));
    }

    public DataSource getDataSource(String dataSourceName) {
        if (logger.isDebugEnabled()) {
            logger.debug("The name of Datasource is [" + dataSourceName + "]");
        }
        DataSource ds = dataSourceFactory.getDataSource(dataSourceName, dataSourceType);
        if (ds == null) {
            throw new IncorrectDefinitionException("can't find datasource for name [" + dataSourceName + "]");
        }
        return ds;
    }

    @Nullable
    public String getDataSourceName(InvocationContext context) {
        String realDataSourceName = dataSourceRouter != null ?
                dataSourceRouter.getDataSourceName(context.getPropertyValue(shardParameterName, shardByInvokerChain)) :
                dataSourceName;
        return realDataSourceName;
    }

}
