package cc.concurrent.mango;

import java.util.concurrent.TimeUnit;

/**
 * @author ash
 */
public class Hour implements CacheExpire {

    @Override
    public int getExpireTime() {
        return (int) TimeUnit.HOURS.toSeconds(1);
    }

}
