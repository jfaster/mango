package cc.concurrent.mango.operator;

import cc.concurrent.mango.CacheDescriptor;
import cc.concurrent.mango.DataCache;
import cc.concurrent.mango.jdbc.JdbcTemplate;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.runtime.TypeContextImpl;
import cc.concurrent.mango.util.Iterables;
import com.google.common.collect.Maps;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    protected JdbcTemplate jdbcTemplate;
    protected DataCache dataCache;
    protected CacheDescriptor cacheDescriptor;

    protected AbstractOperator() {
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setDataCache(DataCache dataCache) {
        this.dataCache = dataCache;
    }

    protected String getSingleKey(RuntimeContext context) {
        return getKey(cacheDescriptor.getPrefix(), context.getPropertyValue(cacheDescriptor.getBeanName(),
                cacheDescriptor.getPropertyName()));
    }

    protected String getKey(String prefix, Object keyObj) {
        return prefix + keyObj;
    }

    protected TypeContext getTypeContext(Class<?>[] methodArgTypes) {
        Map<String, Class<?>> parameterTypeMap = Maps.newHashMap();
        for (int i = 0; i < methodArgTypes.length; i++) {
            parameterTypeMap.put(String.valueOf(i + 1), methodArgTypes[i]);
        }
        return new TypeContextImpl(parameterTypeMap);
    }

    protected TypeContext getTypeContextForBatch(Class<?>[] methodArgTypes) {
        Map<String, Class<?>> parameterTypeMap = Maps.newHashMap();
        for (int i = 0; i < methodArgTypes.length; i++) {
            Class<?> type = methodArgTypes[i];

        }
        return new TypeContextImpl(parameterTypeMap);
    }

}
