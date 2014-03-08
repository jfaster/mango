package cc.concurrent.mango.support;

import cc.concurrent.mango.*;

import java.util.List;

/**
 * @author ash
 */
@Cache(prefix = "man_", expire = Day.class)
public interface ManDao {

    @ReturnGeneratedId
    @CacheIgnored
    @SQL("insert into man(name, age, gender, money, update_time) values(:1.name, :1.age, :1.gender, :1.money, :1.updateTime)")
    int insert(Man user);

    @SQL("delete from man where id=:1")
    public int delete(@CacheBy int id);

    @SQL("update man set name=:1.name, age=:1.age, gender=:1.gender, money=:1.money, update_time=:1.updateTime where id=:1.id")
    public int update(@CacheBy("id") Man user);

    @SQL("select id, name, age, gender, money, update_time from man where id=:1")
    public Man select(@CacheBy() int id);

    @SQL("select id, name, age, gender, money, update_time from man where id in (:1) and name=:2")
    public List<Man> selectList(@CacheBy() List<Integer> ids, String name);

}
