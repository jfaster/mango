package org.jfaster.mango.operator;

import org.jfaster.mango.support.RuntimeContext;

import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public interface CacheableOperatorDriver extends OperatorDriver {

    public boolean isUseMultipleKeys();

    public void setToCache(String key, Object value);

    public void deleteFromCache(String key);

    public void deleteFromCache(Set<String> keys);

    public Object getFromCache(String key);

    public Map<String, Object> getBulkFromCache(Set<String> keys);

    public Class<?> getSuffixClass();

    public String getCacheKey(RuntimeContext context);

    public String getCacheKey(Object suffix);

    public Set<String> getCacheKeys(RuntimeContext context);

    public Object getSuffixObj(RuntimeContext context);

    public void setSuffixObj(RuntimeContext context, Object obj);

    public String getInterableProperty();

}
