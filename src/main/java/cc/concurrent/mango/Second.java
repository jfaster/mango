package cc.concurrent.mango;

/**
 * @author ash
 */
public class Second implements CacheExpire {

    @Override
    public int getExpireTime() {
        return 1;
    }

}
