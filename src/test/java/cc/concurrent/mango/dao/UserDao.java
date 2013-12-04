package cc.concurrent.mango.dao;

import cc.concurrent.mango.annotation.SQL;

/**
 * @author ash
 */
public interface UserDao {

    @SQL("insert into user(id, name) values(:1, :2)")
    public Integer insert(int uid, String name);

}
