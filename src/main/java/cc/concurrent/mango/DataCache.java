package cc.concurrent.mango;

import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public interface DataCache {

    public Object get(String key);

    public Map<String, Object> getBulk(Set<String> keys);

    public void set(String key, Object value);

    public void delete(Set<String> keys);

}
