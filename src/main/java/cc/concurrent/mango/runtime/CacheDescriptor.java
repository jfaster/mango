package cc.concurrent.mango.runtime;

import cc.concurrent.mango.CacheExpire;

/**
 * @author ash
 */
public class CacheDescriptor {

    private boolean useCache;

    private String prefix;

    private CacheExpire expire;

    private int num;

    private String parameterName;

    private String propertyPath;

    private String propertyName; // "a in (:1)"中的a

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getExpires() {
        return expire.getExpireTime() * num;
    }

    public void setExpire(CacheExpire expire) {
        this.expire = expire;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getPropertyPath() {
        return propertyPath;
    }

    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

}
