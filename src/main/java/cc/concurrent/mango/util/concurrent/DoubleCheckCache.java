package cc.concurrent.mango.util.concurrent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ash
 */
public class DoubleCheckCache<K, V> extends AbstractLoadingCache<K, V> {

    private final CacheLoader<K, V> loader;
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<K, V>();
    private final ConcurrentHashMap<K, Object> locks = new ConcurrentHashMap<K, Object>();

    public DoubleCheckCache(CacheLoader<K, V> loader) {
        this.loader = loader;
    }

    @Override
    public V get(K key) throws Exception {
        V value = cache.get(key);
        if (value == null) {
            synchronized (getLock(key)) {
                value = cache.get(key);
                if (value == null) {
                    value = loader.load(key);
                    if (value != null) {
                        cache.put(key, value);
                    }
                }
            }
        }
        return value;
    }

    private Object getLock(K key) {
        Object lock = locks.get(key);
        if (lock == null) {
            lock = new Object();
            Object old = locks.putIfAbsent(key, lock);
            if (old != null) { // 已经存在lock
                lock = old;
            }
        }
        return lock;
    }

}
