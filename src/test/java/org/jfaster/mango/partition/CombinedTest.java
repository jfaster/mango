/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.partition;

import org.jfaster.mango.annotation.*;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.MultipleDataSourceFactory;
import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Order;
import org.jfaster.mango.util.HashUtil;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class CombinedTest {

    private static String[] dsns = new String[] {"ds1", "ds2", "ds3", "ds4"};
    private static Order1Dao order1Dao;
    private static Order2Dao order2Dao;
    private static Order3Dao order3Dao;
    private static Order4Dao order4Dao;
    private static OrderDao orderDao;

    @Before
    public void before() throws Exception {
        Map<String, DataSourceFactory> factories = new HashMap<String, DataSourceFactory>();
        for (int i = 0; i < 4; i++) {
            DataSource ds = DataSourceConfig.getDataSource(i + 1);
            Connection conn = ds.getConnection();
            Table.ORDER_PARTITION.load(conn);
            conn.close();
            factories.put(dsns[i], new SimpleDataSourceFactory(ds));
        }
        DataSourceFactory dsf = new MultipleDataSourceFactory(factories);
        Mango mango = Mango.newInstance(dsf);
        order1Dao = mango.create(Order1Dao.class);
        order2Dao = mango.create(Order2Dao.class);
        order3Dao = mango.create(Order3Dao.class);
        order4Dao = mango.create(Order4Dao.class);
        orderDao = mango.create(OrderDao.class);
    }

    @Test
    public void test() throws Exception {
        int price = 0;
        for (int cid = 0; cid < 10; cid++) {
            for (int intUid = 10; intUid < 20; intUid++) {
                String uid = String.valueOf(intUid);
                price++;
                Order o = new Order();
                o.setCid(cid);
                o.setUid(uid);
                o.setPrice(price);
                orderDao.insert(o);
                assertThat(orderDao.getOrder(cid, uid), equalTo(o));
                assertThat(getDaoByCid(cid).getOrder(getTableByUid(uid), cid, uid), equalTo(o));
            }
        }
        for (int cid = 100; cid < 110; cid++) {
            for (int intUid = 110; intUid < 120; intUid++) {
                String uid = String.valueOf(intUid);
                price++;
                Order o = new Order();
                o.setCid(cid);
                o.setUid(String.valueOf(uid));
                o.setPrice(price);
                getDaoByCid(cid).insert(getTableByUid(uid), o);
                assertThat(orderDao.getOrder(cid, uid), equalTo(o));
                assertThat(getDaoByCid(cid).getOrder(getTableByUid(uid), cid, uid), equalTo(o));
            }
        }
    }

    interface IOrderDao {
        int insert(String table, Order order);

        public Order getOrder(String table, int cid, String uid);
    }

    @DB(dataSource = "ds1")
    interface Order1Dao extends IOrderDao {
        @SQL("insert into #{:table}(cid, uid, price) values(:cid, :uid, :price)")
        int insert(@Rename("table") String table, Order order);

        @SQL("select cid, uid, price from #{:1} where cid = :2 and uid = :3")
        public Order getOrder(String table, int cid, String uid);
    }

    @DB(dataSource = "ds2")
    interface Order2Dao extends IOrderDao {
        @SQL("insert into #{:table}(cid, uid, price) values(:cid, :uid, :price)")
        int insert(@Rename("table") String table, Order order);

        @SQL("select cid, uid, price from #{:1} where cid = :2 and uid = :3")
        public Order getOrder(String table, int cid, String uid);
    }

    @DB(dataSource = "ds3")
    interface Order3Dao extends IOrderDao {
        @SQL("insert into #{:table}(cid, uid, price) values(:cid, :uid, :price)")
        int insert(@Rename("table") String table, Order order);

        @SQL("select cid, uid, price from #{:1} where cid = :2 and uid = :3")
        public Order getOrder(String table, int cid, String uid);
    }

    @DB(dataSource = "ds4")
    interface Order4Dao extends IOrderDao {
        @SQL("insert into #{:table}(cid, uid, price) values(:cid, :uid, :price)")
        int insert(@Rename("table") String table, Order order);

        @SQL("select cid, uid, price from #{:1} where cid = :2 and uid = :3")
        public Order getOrder(String table, int cid, String uid);
    }

    @DB(table = "order", dataSourceRouter = OrderDataSourceRouter.class, tablePartition = OrderTablePartition.class)
    interface OrderDao {

        @SQL("insert into #table(cid, uid, price) values(:cid, :uid, :price)")
        int insert(@DataSourceShardBy("cid") @TableShardBy("uid") Order order);

        @SQL("select cid, uid, price from #table where cid = :1 and uid = :2")
        public Order getOrder(@DataSourceShardBy int cid, @TableShardBy String uid);

    }

    static class OrderDataSourceRouter implements DataSourceRouter<Integer> {

        @Override
        public String getDataSourceName(Integer cid, int type) {
            return getDataSourceNameByCid(cid);
        }

    }

    static class OrderTablePartition implements TablePartition<String> {

        @Override
        public String getPartitionedTable(String table, String uid, int type) {
            return table + "_" + Integer.valueOf(uid) % 10;
        }

    }

    private static String getDataSourceNameByCid(int cid) {
        long hash = HashUtil.fnv1_31(cid);
        return "ds" + (hash % 4 + 1);
    }

    private static String getTableByUid(String uid) {
        return "order_" + Integer.valueOf(uid) % 10;
    }

    private static IOrderDao getDaoByCid(int cid) {
        String dataSourceName = getDataSourceNameByCid(cid);
        if ("ds1".equals(dataSourceName)) {
            return order1Dao;
        } else if ("ds2".equals(dataSourceName)) {
            return order2Dao;
        } else if ("ds3".equals(dataSourceName)) {
            return order3Dao;
        } else if ("ds4".equals(dataSourceName)) {
            return order4Dao;
        }
        return null;
    }

}














