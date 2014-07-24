package cc.concurrent.mango;

import cc.concurrent.mango.exception.UnreachableCodeException;

/**
 * {@link DB#dataSourceRouter()}的默认值，表示不使用数据源路由
 *
 * @author ash
 */
public final class IgnoreDataSourceRouter implements DataSourceRouter {

    @Override
    public String getDataSourceName(Object shardByParam) {
        throw new UnreachableCodeException();
    }

}
