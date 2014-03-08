package cc.concurrent.mango;

import java.util.concurrent.TimeUnit;

/**
 * @author ash
 */
public class Day implements CacheExpire {

    @Override
    public int getExpireTime() {
        return (int) TimeUnit.DAYS.toSeconds(1);
    }

}
