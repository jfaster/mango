///*
// * Copyright 2014 mango.jfaster.org
// *
// * The Mango Project licenses this file to you under the Apache License,
// * version 2.0 (the "License"); you may not use this file except in compliance
// * with the License. You may obtain a copy of the License at:
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations
// * under the License.
// */
//
//package org.jfaster.mango.sharding;
//
//import org.jfaster.mango.annotation.DB;
//import org.jfaster.mango.annotation.SQL;
//import org.jfaster.mango.annotation.ShardingBy;
//import org.jfaster.mango.datasource.DataSourceFactory;
//import org.jfaster.mango.datasource.MultipleDataSourceFactory;
//import org.jfaster.mango.datasource.SimpleDataSourceFactory;
//import org.jfaster.mango.operator.Mango;
//import org.jfaster.mango.support.DataSourceConfig;
//import org.jfaster.mango.support.Table;
//import org.jfaster.mango.support.model4table.Product;
//import org.jfaster.mango.util.HashUtil;
//import org.junit.Before;
//import org.junit.Test;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.equalTo;
//
///**
// * @author ash
// */
//public class ShardStrategyAndTypeTest {
//
//    private static String[] dsns = new String[] {"ds1", "ds2", "ds3", "ds4"};
//    private static ProductDao productDao;
//
//    @Before
//    public void before() throws Exception {
//        Map<String, DataSourceFactory> factories = new HashMap<String, DataSourceFactory>();
//        for (int i = 0; i < 4; i++) {
//            DataSource ds = DataSourceConfig.getDataSource(i + 1);
//            Connection conn = ds.getConnection();
//            Table.PRODUCT_PARTITION.load(conn);
//            conn.close();
//            factories.put(dsns[i], new SimpleDataSourceFactory(ds));
//        }
//        DataSourceFactory dsf = new MultipleDataSourceFactory(factories);
//        Mango mango = Mango.newInstance(dsf);
//        productDao = mango.create(ProductDao.class);
//    }
//
//    @Test
//    public void test() throws Exception {
//        int t = 111111;
//        for (int uid = 1; uid < 500; uid++) {
//            t++;
//            String id = "" + (HashUtil.fnv1_31(uid) % 4 + 1) + (uid % 10) + t;
//            Product p = new Product();
//            p.setId(id);
//            p.setUid(String.valueOf(uid));
//            p.setPrice(t);
//            productDao.insert(p);
//            assertThat(productDao.getProductById(id), equalTo(p));
//            assertThat(productDao.getProductByUid(String.valueOf(uid)), equalTo(p));
//        }
//    }
//
//    @DB(
//            table = "product",
//            dataSourceRouter = ProductShardStrategy.class,
//            tablePartition = ProductShardStrategy.class
//    )
//    interface ProductDao {
//
//        @SQL("insert into #table(id, uid, price) values(:id, :uid, :price)")
//        int insert(@ShardingBy(value = "id", type = TYPE_ID) Product product);
//
//        @SQL("select id, uid, price from #table where id = :1")
//        public Product getProductById(@ShardingBy(type = TYPE_ID) String id);
//
//        @SQL("select id, uid, price from #table where uid = :1")
//        public Product getProductByUid(@ShardingBy(type = TYPE_UID) String uid);
//
//    }
//
//    private static final int TYPE_ID = 1;
//    private static final int TYPE_UID = 2;
//
//    static class ProductShardStrategy implements ShardingStrategy<String> {
//
//        @Override
//        public String getDatabase(String shardParam, int type) {
//            int key;
//            if (type == TYPE_ID) {
//                key = getDataSourceKeyById(shardParam);
//            } else if (type == TYPE_UID) {
//                key = getDataSourceKeyByUid(shardParam);
//            } else {
//                throw new IllegalStateException();
//            }
//            return "ds" + key;
//        }
//
//        private int getDataSourceKeyById(String id) {
//            return Integer.valueOf(id.substring(0, 1));
//        }
//
//        private int getDataSourceKeyByUid(String uid) {
//            return HashUtil.fnv1_31(uid) % 4 + 1;
//        }
//
//        @Override
//        public String getTargetTable(String table, String shardParam, int type) {
//            int key;
//            if (type == TYPE_ID) {
//                key = getTableKeyById(shardParam);
//            } else if (type == TYPE_UID) {
//                key = getTableKeyByUid(shardParam);
//            } else {
//                throw new IllegalStateException();
//            }
//            return "product_" + key;
//        }
//
//        private int getTableKeyById(String id) {
//            return Integer.valueOf(id.substring(1, 2));
//        }
//
//        private int getTableKeyByUid(String uid) {
//            return Integer.valueOf(uid) % 10;
//        }
//
//    }
//
//}
