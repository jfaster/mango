package cc.concurrent.mango.dao;

import cc.concurrent.mango.annotation.SQL;

/**
 * @author ash
 */
public interface StudentDao {

    @SQL("select name from student where id=:1")
    public String getNameById(int id);

}
