package cc.concurrent.mango;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author ash
 */
public class Test {

    public static void main(String[] args) {
        try {
            Connection con = null; //定义一个MYSQL链接对象
            Class.forName("com.mysql.jdbc.Driver").newInstance(); //MYSQL驱动
            con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/test", "root", "root"); //链接本地MYSQL

            Statement stmt; //创建声明
            stmt = con.createStatement();

            //新增一条数据
            stmt.executeUpdate("INSERT INTO user (uid) VALUES (100)");
            ResultSet res = stmt.executeQuery("select LAST_INSERT_ID()");
            long ret_id;
            if (res.next()) {
                ret_id = res.getLong(1);
                System.out.print(ret_id);
            }

            stmt = con.createStatement();

            //新增一条数据
            stmt.executeUpdate("INSERT INTO user (uid) VALUES (100)");
            res = stmt.executeQuery("select LAST_INSERT_ID()");
            if (res.next()) {
                ret_id = res.getLong(1);
                System.out.print(ret_id);
            }

        } catch (Exception e) {
            System.out.print("MYSQL ERROR:" + e.getMessage());
        }
    }

}
