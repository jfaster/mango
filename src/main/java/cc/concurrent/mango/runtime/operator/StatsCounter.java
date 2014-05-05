package cc.concurrent.mango.runtime.operator;

/**
 * @author ash
 */
public interface StatsCounter {

    public void recordHits(int count);

    public void recordMisses(int count);

    public void recordLoadSuccess(long loadTime);

    public void recordLoadException(long loadTime);

    public void recordEviction(int count);

    public void snapshot();

}
