package cc.concurrent.mango;

import java.util.concurrent.TimeUnit;

/**
 * @author ash
 */
public class Minute implements CacheExpire {

    @Override
    public int getExpireTime() {
        return (int) TimeUnit.MINUTES.toSeconds(1);
    }

}
