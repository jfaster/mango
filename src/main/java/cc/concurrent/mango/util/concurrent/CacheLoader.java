package cc.concurrent.mango.util.concurrent;

/**
 * @author ash
 */
public interface CacheLoader<K, V> {

    public V load(K key) throws Exception;

}
