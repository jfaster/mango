package cc.concurrent.mango.support;

import cc.concurrent.mango.SQL;

/**
 * @author ash
 */
public interface ExceptionDao {

    @SQL("update user set name=:1.name where id=:2.id")
    public int updateUser(User user);

    @SQL("update user set name=:1.namm where id=:2.id")
    public int updateUser2(User user);

    @SQL("update user set name=:1 where id=:2.id")
    public int updateUser3(User user);

}
