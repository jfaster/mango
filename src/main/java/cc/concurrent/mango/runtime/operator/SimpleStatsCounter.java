package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.util.concurrent.atomic.LongAddable;
import cc.concurrent.mango.util.concurrent.atomic.LongAddables;

/**
 * @author ash
 */
public class SimpleStatsCounter implements StatsCounter {

    private final LongAddable hitCount = LongAddables.create();
    private final LongAddable missCount = LongAddables.create();
    private final LongAddable loadSuccessCount = LongAddables.create();
    private final LongAddable loadExceptionCount = LongAddables.create();
    private final LongAddable totalLoadTime = LongAddables.create();
    private final LongAddable evictionCount = LongAddables.create();

    public SimpleStatsCounter() {}

    @Override
    public void recordHits(int count) {
        hitCount.add(count);
    }

    @Override
    public void recordMisses(int count) {
        missCount.add(count);
    }

    @Override
    public void recordLoadSuccess(long loadTime) {
        loadSuccessCount.increment();
        totalLoadTime.add(loadTime);
    }

    @Override
    public void recordLoadException(long loadTime) {
        loadExceptionCount.increment();
        totalLoadTime.add(loadTime);
    }

    @Override
    public void recordEviction(int count) {
        evictionCount.add(count);
    }

    @Override
    public void snapshot() {
        return;
    }

}
