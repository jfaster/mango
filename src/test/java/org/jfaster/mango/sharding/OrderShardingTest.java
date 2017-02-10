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
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.annotation.Sharding;
import org.jfaster.mango.annotation.ShardingBy;
import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Order;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class OrderShardingTest {

  private static String[] dsns = new String[]{"db1", "db2", "db3", "db4"};
  private static OrderDao orderDao;

  @Before
  public void before() throws Exception {
    Mango mango = Mango.newInstance();
    for (int i = 0; i < 4; i++) {
      DataSource ds = DataSourceConfig.getDataSource(i + 1);
      Connection conn = ds.getConnection();
      Table.ORDER_PARTITION.load(conn);
      conn.close();
      mango.addDataSourceFactory(new SimpleDataSourceFactory(dsns[i], ds));
    }
    orderDao = mango.create(OrderDao.class);
  }

  @Test
  public void test() throws Exception {
    int price = 0;
    for (int uid = 1; uid < 100; uid++) {
      String id = getOrderIdByUid(uid);
      Order o = new Order();
      o.setId(id);
      o.setUid(uid);
      o.setPrice(price);
      orderDao.insert(o);
      assertThat(orderDao.getOrderById(id), equalTo(o));
      assertThat(orderDao.getOrdersByUid(uid).get(0), equalTo(o));
    }
  }

  @DB(table = "order")
  interface OrderDao {

    @SQL("insert into #table(id, uid, price) values(:id, :uid, :price)")
    @Sharding(shardingStrategy = OrderIdShardingStrategy.class)
    int insert(@ShardingBy("id") Order order);

    @SQL("select id, uid, price from #table where id = :1")
    @Sharding(shardingStrategy = OrderIdShardingStrategy.class)
    public Order getOrderById(@ShardingBy String id);

    @SQL("select id, uid, price from #table where uid = :1")
    @Sharding(shardingStrategy = OrderUidShardingStrategy.class)
    public List<Order> getOrdersByUid(@ShardingBy int uid);

  }

  static class OrderIdShardingStrategy implements ShardingStrategy<String, String> {

    @Override
    public String getDataSourceFactoryName(String id) {
      return "db" + id.substring(0, 1);
    }

    @Override
    public String getTargetTable(String table, String id) {
      return table + "_" + id.substring(1, 2);
    }

  }

  static class OrderUidShardingStrategy implements ShardingStrategy<Integer, Integer> {

    @Override
    public String getDataSourceFactoryName(Integer uid) {
      return "db" + String.valueOf((uid / 10) % 4 + 1);
    }

    @Override
    public String getTargetTable(String table, Integer uid) {
      return table + "_" + String.valueOf(uid % 10);
    }

  }

  private static final AtomicInteger num = new AtomicInteger(1);

  private static String getOrderIdByUid(int uid) {
    String dbInfo = String.valueOf((uid / 10) % 4 + 1);
    String tableInfo = String.valueOf(uid % 10);
    return dbInfo + tableInfo + num.getAndIncrement();
  }

}
