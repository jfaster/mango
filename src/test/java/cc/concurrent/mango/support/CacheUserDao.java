package cc.concurrent.mango.support;

import cc.concurrent.mango.*;

/**
 * @author ash
 */
@Cache(prefix = "user_")
public interface CacheUserDao {

    @ReturnGeneratedId
    @CacheIgnored
    @SQL("insert into cache_user(name, age, gender, money, update_time) values(:1.name, :1.age, :1.gender, :1.money, :1.updateTime)")
    int insert(CacheUser user);

    @SQL("update cache_user set name=:1.name, age=:1.age, gender=:1.gender, money=:1.money, update_time=:1.updateTime where id=:1.id")
    public int updateUser(@CacheBy("id") CacheUser user);

}
