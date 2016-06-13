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

package org.jfaster.mango.sharding;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.DatabaseShardingBy;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.annotation.TableShardingBy;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.MultipleDatabaseDataSourceFactory;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class TupleShardStrategyTest {

    private static String[] dsns = new String[] {"ds1", "ds2", "ds3", "ds4"};
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
        DataSourceFactory dsf = new MultipleDatabaseDataSourceFactory(factories);
        Mango mango = Mango.newInstance(dsf);
        orderDao = mango.create(OrderDao.class);
    }

    @Test
    public void test() throws Exception {
        int price = 0;
        List<Order> orders = new ArrayList<Order>();
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
                o.setPrice(9527);
                orders.add(o);
            }
        }
        orderDao.batchUpdate(orders);
        for (Order o : orders) {
            assertThat(orderDao.getOrder(o.getCid(), o.getUid()), equalTo(o));
        }
    }

    @DB(table = "order", dataSourceRouter = OrderShardingStrategy.class, tablePartition = OrderShardingStrategy.class)
    interface OrderDao {

        @SQL("insert into #table(cid, uid, price) values(:cid, :uid, :price)")
        int insert(@DatabaseShardingBy("cid") @TableShardingBy("uid") Order order);

        @SQL("select cid, uid, price from #table where cid = :1 and uid = :2")
        public Order getOrder(@DatabaseShardingBy int cid, @TableShardingBy String uid);

        @SQL("update #table set price = :price where cid = :cid and uid = :uid")
        public int batchUpdate(@DatabaseShardingBy("cid") @TableShardingBy("uid") List<Order> orders);

    }

    static class OrderShardingStrategy implements ShardingStrategy<Integer, String> {

        @Override
        public String getDatabase(Integer cid) {
            long hash = HashUtil.fnv1_31(cid);
            return "ds" + (hash % 4 + 1);
        }

        @Override
        public String getTargetTable(String table, String uid) {
            return table + "_" + Integer.valueOf(uid) % 10;
        }

    }

}
