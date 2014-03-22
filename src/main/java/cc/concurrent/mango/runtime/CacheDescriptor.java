package cc.concurrent.mango.runtime;

import cc.concurrent.mango.CacheExpire;

/**
 * @author ash
 */
public class CacheDescriptor {

    private boolean useCache; // 是否使用缓存

    private String prefix; // 缓存key前缀

    private CacheExpire expire; // 缓存过期控制

    private int num; // expire的数量

    private String parameterName; // 缓存参数名

    private String propertyPath; // 缓存参数属性路径

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

}
