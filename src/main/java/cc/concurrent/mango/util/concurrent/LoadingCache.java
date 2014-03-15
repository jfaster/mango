package cc.concurrent.mango.util.concurrent;

/**
 * @author ash
 */
public interface LoadingCache<K, V> {

    public V get(K key) throws Exception;

    public V getUnchecked(K key);

}
