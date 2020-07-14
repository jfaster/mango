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

package org.jfaster.mango.crud;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class InternalCrudDaoInheritTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private final static Mango mango = Mango.newInstance(ds);
  private final static OrderDao dao = mango.create(OrderDao.class);
  private final static SubOrderDao subDao = mango.create(SubOrderDao.class);

  @Before
  public void before() throws Exception {
    Table.ORDER.load(ds);
    Table.MSG.load(ds);
  }

  @Test
  public void testAdd() {
    CrudOrder co = CrudOrder.createRandomCrudOrder();
    subDao.add(co);
    assertThat(dao.getOne(co.getId()), equalTo(co));
  }

  @DB(table = "t_order")
  interface OrderDao extends CrudDao<CrudOrder, String> {

    @Override
    CrudOrder getOne(String primaryKey);
  }

  @DB(table = "t_order")
  interface SubOrderDao extends OrderDao {

    @Override
    void add(CrudOrder entity);

    @Override
    CrudOrder getOne(String primaryKey);
  }

}