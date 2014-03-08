package cc.concurrent.mango;

import java.util.Calendar;

/**
 * @author ash
 */
public class EndOfDay implements CacheExpire {

    @Override
    public int getExpireTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long d = Math.max(cal.getTimeInMillis() - System.currentTimeMillis(), 1);
        return (int) d / 1000;
    }

}
