package cc.concurrent.mango.util.concurrent;

import cc.concurrent.mango.exception.UncheckedException;

/**
 * @author ash
 */
public abstract class AbstractLoadingCache<K, V> implements LoadingCache<K, V> {

    @Override
    public V getUnchecked(K key) {
        try {
            return get(key);
        } catch (Exception e) {
            throw new UncheckedException(e.getCause());
        }
    }

}
