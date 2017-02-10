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

import org.jfaster.mango.annotation.*;
import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Bill;
import org.jfaster.mango.util.HashUtil;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class BillShardingTest {

  private static String[] dsns = new String[]{"db1", "db2", "db3", "db4"};
  private static Bill1Dao bill1Dao;
  private static Bill2Dao bill2Dao;
  private static Bill3Dao bill3Dao;
  private static Bill4Dao bill4Dao;
  private static BillDao billDao;
  private static SimpleBillDao simpleBillDao;

  @Before
  public void before() throws Exception {
    Mango mango = Mango.newInstance();
    for (int i = 0; i < 4; i++) {
      DataSource ds = DataSourceConfig.getDataSource(i + 1);
      Connection conn = ds.getConnection();
      Table.BILL_PARTITION.load(conn);
      conn.close();
      mango.addDataSourceFactory(new SimpleDataSourceFactory(dsns[i], ds));
    }
    bill1Dao = mango.create(Bill1Dao.class);
    bill2Dao = mango.create(Bill2Dao.class);
    bill3Dao = mango.create(Bill3Dao.class);
    bill4Dao = mango.create(Bill4Dao.class);
    billDao = mango.create(BillDao.class);
    simpleBillDao = mango.create(SimpleBillDao.class);
  }

  @Test
  public void test() throws Exception {
    int price = 0;
    for (int cid = 0; cid < 10; cid++) {
      for (int intUid = 10; intUid < 20; intUid++) {
        String uid = String.valueOf(intUid);
        price++;
        Bill o = new Bill();
        o.setCid(cid);
        o.setUid(uid);
        o.setPrice(price);
        billDao.insert(o);
        assertThat(billDao.getBill(cid, uid), equalTo(o));
        assertThat(getDaoByCid(cid).getBill(getTableByUid(uid), cid, uid), equalTo(o));
      }
    }
    for (int cid = 100; cid < 110; cid++) {
      for (int intUid = 110; intUid < 120; intUid++) {
        String uid = String.valueOf(intUid);
        price++;
        Bill o = new Bill();
        o.setCid(cid);
        o.setUid(String.valueOf(uid));
        o.setPrice(price);
        getDaoByCid(cid).insert(getTableByUid(uid), o);
        assertThat(billDao.getBill(cid, uid), equalTo(o));
        assertThat(getDaoByCid(cid).getBill(getTableByUid(uid), cid, uid), equalTo(o));
      }
    }
  }

  @Test
  public void test2() throws Exception {
    int price = 0;
    for (int cid = 0; cid < 10; cid++) {
      for (int intUid = 10; intUid < 20; intUid++) {
        String uid = String.valueOf(intUid);
        price++;
        Bill o = new Bill();
        o.setCid(cid);
        o.setUid(uid);
        o.setPrice(price);
        simpleBillDao.insert(o);
        assertThat(simpleBillDao.getBill(cid, uid), equalTo(o));
        assertThat(getDaoByCid(cid).getBill(getTableByUid(uid), cid, uid), equalTo(o));
      }
    }
    for (int cid = 100; cid < 110; cid++) {
      for (int intUid = 110; intUid < 120; intUid++) {
        String uid = String.valueOf(intUid);
        price++;
        Bill o = new Bill();
        o.setCid(cid);
        o.setUid(String.valueOf(uid));
        o.setPrice(price);
        getDaoByCid(cid).insert(getTableByUid(uid), o);
        assertThat(simpleBillDao.getBill(cid, uid), equalTo(o));
        assertThat(getDaoByCid(cid).getBill(getTableByUid(uid), cid, uid), equalTo(o));
      }
    }
  }

  interface IBillDao {
    int insert(String table, Bill bill);

    public Bill getBill(String table, int cid, String uid);
  }

  @DB(name = "db1")
  interface Bill1Dao extends IBillDao {
    @SQL("insert into #{:table}(cid, uid, price) values(:cid, :uid, :price)")
    int insert(@Rename("table") String table, Bill bill);

    @SQL("select cid, uid, price from #{:1} where cid = :2 and uid = :3")
    public Bill getBill(String table, int cid, String uid);
  }

  @DB(name = "db2")
  interface Bill2Dao extends IBillDao {
    @SQL("insert into #{:table}(cid, uid, price) values(:cid, :uid, :price)")
    int insert(@Rename("table") String table, Bill bill);

    @SQL("select cid, uid, price from #{:1} where cid = :2 and uid = :3")
    public Bill getBill(String table, int cid, String uid);
  }

  @DB(name = "db3")
  interface Bill3Dao extends IBillDao {
    @SQL("insert into #{:table}(cid, uid, price) values(:cid, :uid, :price)")
    int insert(@Rename("table") String table, Bill bill);

    @SQL("select cid, uid, price from #{:1} where cid = :2 and uid = :3")
    public Bill getBill(String table, int cid, String uid);
  }

  @DB(name = "db4")
  interface Bill4Dao extends IBillDao {
    @SQL("insert into #{:table}(cid, uid, price) values(:cid, :uid, :price)")
    int insert(@Rename("table") String table, Bill bill);

    @SQL("select cid, uid, price from #{:1} where cid = :2 and uid = :3")
    public Bill getBill(String table, int cid, String uid);
  }

  @DB(table = "bill")
  @Sharding(
      databaseShardingStrategy = BillDatabaseShardingStrategy.class,
      tableShardingStrategy = BillTableShardingStrategy.class
  )
  interface BillDao {

    @SQL("insert into #table(cid, uid, price) values(:cid, :uid, :price)")
    int insert(@DatabaseShardingBy("cid") @TableShardingBy("uid") Bill bill);

    @SQL("select cid, uid, price from #table where cid = :1 and uid = :2")
    public Bill getBill(@DatabaseShardingBy int cid, @TableShardingBy String uid);

  }

  static class BillDatabaseShardingStrategy implements DatabaseShardingStrategy<Integer> {

    @Override
    public String getDataSourceFactoryName(Integer cid) {
      return getDatabaseByCid(cid);
    }
  }

  static class BillTableShardingStrategy implements TableShardingStrategy<String> {

    @Override
    public String getTargetTable(String table, String uid) {
      return table + "_" + Integer.valueOf(uid) % 10;
    }

  }

  @DB(table = "bill")
  @Sharding(shardingStrategy = BillShardingStrategy.class)
  interface SimpleBillDao {

    @SQL("insert into #table(cid, uid, price) values(:cid, :uid, :price)")
    int insert(@DatabaseShardingBy("cid") @TableShardingBy("uid") Bill bill);

    @SQL("select cid, uid, price from #table where cid = :1 and uid = :2")
    public Bill getBill(@DatabaseShardingBy int cid, @TableShardingBy String uid);

  }

  static class BillShardingStrategy implements ShardingStrategy<Integer, String> {

    @Override
    public String getDataSourceFactoryName(Integer cid) {
      return getDatabaseByCid(cid);
    }

    @Override
    public String getTargetTable(String table, String uid) {
      return table + "_" + Integer.valueOf(uid) % 10;
    }
  }

  private static String getDatabaseByCid(int cid) {
    long hash = HashUtil.fnv1_31(cid);
    return "db" + (hash % 4 + 1);
  }

  private static String getTableByUid(String uid) {
    return "bill_" + Integer.valueOf(uid) % 10;
  }

  private static IBillDao getDaoByCid(int cid) {
    String database = getDatabaseByCid(cid);
    if ("db1".equals(database)) {
      return bill1Dao;
    } else if ("db2".equals(database)) {
      return bill2Dao;
    } else if ("db3".equals(database)) {
      return bill3Dao;
    } else if ("db4".equals(database)) {
      return bill4Dao;
    }
    return null;
  }

}














