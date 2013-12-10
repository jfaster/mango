package cc.concurrent.mango.support;

import cc.concurrent.mango.annotation.SQL;

import java.util.Date;

/**
 * @author ash
 */
public interface UserDao {

    @SQL("select id from user where id = :1")
    public Integer selectInteger(int id);

    @SQL("select id from user where id = :1")
    public int selectInt(int id);

    @SQL("select name from user where id = :1")
    public String selectString(int id);

    @SQL("select gender from user where id = :1")
    public Boolean selectBooleanObj(int id);

    @SQL("select gender from user where id = :1")
    public boolean selectBoolean(int id);

    @SQL("select money from user where id = :1")
    public Long selectLongObj(int id);

    @SQL("select money from user where id = :1")
    public long selectLong(int id);

    @SQL("select update_time from user where id = :1")
    public Date selectDate(int id);

    @SQL("select id, name, age, gender, money, update_time from user where id = :1")
    public User selectUser(int id);


}
