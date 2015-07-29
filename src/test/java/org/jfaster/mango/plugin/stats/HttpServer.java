package org.jfaster.mango.plugin.stats;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.Config;
import org.jfaster.mango.support.Table;

import javax.sql.DataSource;
import java.sql.Connection;
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

        context.addServlet(new ServletHolder(new MangoStatsServlet()), "/mango-stats");

        server.start();
        server.join();
    }

    private static void init() throws Exception {
        DataSource ds = Config.getDataSource();
        Connection conn = ds.getConnection();
        Table.USER.load(conn);
        conn.close();
        Mango mango = Mango.newInstance(ds);
        final UserDao dao = mango.create(UserDao.class, true);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int id = 1;
                dao.getIntegerId(id);
                dao.getName(id);
                dao.getBoolObjGender(id);
                for (int i = 0; i < 1500; i++) {
                    dao.getLongObjMoney(id);
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    @DB()
    static interface UserDao {

        @SQL("select id from user where id = :1")
        public Integer getIntegerId(int id);

        @SQL("select name from user where id = :1")
        public String getName(int id);

        @SQL("select gender from user where id = :1")
        public Boolean getBoolObjGender(int id);

        @SQL("select money from user where id = :1")
        public Long getLongObjMoney(int id);

    }

}
