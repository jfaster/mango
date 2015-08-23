package org.jfaster.mango.plugin.stats;

import com.google.common.collect.Lists;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jfaster.mango.annotation.*;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.operator.cache.Day;
import org.jfaster.mango.support.CacheHandlerImpl;
import org.jfaster.mango.support.Config;
import org.jfaster.mango.support.Randoms;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ash
 */
public class HttpServer {

    private final static int PORT = 8080;

    public static void main(String[] args) throws Exception {
        init();

        Server server = new Server(PORT);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder servlet = new ServletHolder(new MangoStatsServlet());
        //servlet.setInitParameter("key", "9527");
        context.addServlet(servlet, "/mango-stats");
        server.start();
        server.join();
    }

    private static void init() throws Exception {
        DataSource ds = Config.getDataSource();
        Connection conn = ds.getConnection();
        Table.USER.load(conn);
        conn.close();
        Mango mango = Mango.newInstance(ds);
        mango.setDefaultCacheHandler(new CacheHandlerImpl() {
            void sleep() {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
            @Override
            public Object get(String key) {
                sleep();
                return super.get(key);
            }

            @Override
            public Map<String, Object> getBulk(Set<String> keys) {
                sleep();
                return super.getBulk(keys);
            }

            @Override
            public void set(String key, Object value, int expires) {
                sleep();
                super.set(key, value, expires);
            }

            @Override
            public void add(String key, Object value, int expires) {
                sleep();
                super.add(key, value, expires);
            }

            @Override
            public void batchDelete(Set<String> keys) {
                sleep();
                super.batchDelete(keys);
            }

            @Override
            public void delete(String key) {
                sleep();
                super.delete(key);
            }
        });
        final UserDao dao = mango.create(UserDao.class, true);

        int id = 1;
        dao.getIntegerId(id);
        dao.getName(id);
        dao.getBoolObjGender(id);

        int id1 = dao.insertUser(createRandomUser());
        int id2 = dao.insertUser(createRandomUser());
        List<Integer> ids = Lists.newArrayList(id1, id2);
        dao.getUser(id1);
        dao.getUser(id1);
        dao.getUsers(ids);
        dao.getUsers(ids);
        dao.delete(id1);
        dao.getUser(id1);
        dao.delete(id1);
        dao.getUsers(ids);
        dao.deletes(ids);
        dao.deletes2(ids);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int id = 1;
                dao.getIntegerId(id);
                dao.getName(id);
                dao.getBoolObjGender(id);
                for (int i = 0; i < 1500; i++) {
                    dao.getLongObjMoney(id, null, null);
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    @DB(table = "user")
    @Cache(prefix = "user", expire = Day.class, cacheNullObject = true)
    static interface UserDao {

        @CacheIgnored
        @SQL("select id from #table where id = :1")
        public Integer getIntegerId(int id);

        @CacheIgnored
        @SQL("select name from #table where id = :1")
        public String getName(int id);

        @CacheIgnored
        @SQL("select gender from #table where id = :1")
        public Boolean getBoolObjGender(int id);

        @CacheIgnored
        @SQL("select money from #table where id = :1")
        public Long getLongObjMoney(int id, String str, List<User> users);

        @SQL("delete from #table where id = :1")
        public boolean delete(@CacheBy int id);

        @SQL("delete from #table where id = :1")
        public void deletes(@CacheBy List<Integer> ids);

        @SQL("delete from #table where id in (:1)")
        public void deletes2(@CacheBy List<Integer> ids);

        @SQL("select id, name, age, gender, money, update_time from #table where id = :1")
        public User getUser(@CacheBy int id);

        @SQL("select id, name, age, gender, money, update_time from #table where id in (:1)")
        public List<User> getUsers(@CacheBy List<Integer> ids);

        @ReturnGeneratedId
        @CacheIgnored
        @SQL("insert into user(name, age, gender, money, update_time) " +
                "values(:1.name, :1.age, :1.gender, :1.money, :1.updateTime)")
        public int insertUser(User user);

    }

    private static User createRandomUser() {
        Random r = new Random();
        String name = Randoms.randomString(20);
        int age = r.nextInt(200);
        boolean gender = r.nextBoolean();
        long money = r.nextInt(1000000);
        Date date = new Date();
        User user = new User(name, age, gender, money, date);
        return user;
    }

}
