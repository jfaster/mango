package cc.concurrent.mango;

import java.util.List;
import java.util.Map;

/**
 * @author ash
 */
public interface DataCache {

    public Object get(String key);

    public Map<String, Object> getBulk(List<String> keys);

    public void set(String key, Object value);

    public void delete(String key);

}
