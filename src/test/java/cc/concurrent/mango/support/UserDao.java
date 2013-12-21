package cc.concurrent.mango.support;

import cc.concurrent.mango.annotation.SQL;

import java.util.Date;
import java.util.List;
import java.util.Set;

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

    @SQL("select id, name, age, gender, money, update_time from user order by id")
    public List<User> selectUserList();

    @SQL("select id, name, age, gender, money, update_time from user order by id")
    public Set<User> selectUserSet();

    @SQL("select id, name, age, gender, money, update_time from user order by id")
    public User[] selectUserArray();

    @SQL("select id from user order by id")
    public Integer[] selectIntegerArray();

    @SQL("select id from user order by id")
    public int[] selectIntArray();

    @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
    public List<User> selectUserInList(List<Integer> ids);

    @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
    public List<User> selectUserInSet(Set<Integer> ids);

    @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
    public List<User> selectUserInIntegerArray(Integer[] ids);

    @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
    public List<User> selectUserInIntArray(int[] ids);

    @SQL("select max(id) from user")
    public int selectMaxInt();

/***********************************************************************/

    @SQL("insert into user(name, age, gender, money, update_time) values(:1.name, :1.age, :1.gender, :1.money, :1.updateTime)")
    public void insertUser(User user);


}
