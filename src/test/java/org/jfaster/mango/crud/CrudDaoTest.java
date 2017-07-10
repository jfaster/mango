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
public class CrudDaoTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private final static Mango mango = Mango.newInstance(ds);

  @Before
  public void before() throws Exception {
    Table.ORDER.load(ds);
  }

  @Test
  public void test() throws Exception {
    int userId = 1;
    CrudOrderDao dao = mango.create(CrudOrderDao.class);
    CrudOrder co = CrudOrder.createRandomCrudOrder(userId);
    dao.add(co);
    assertThat(dao.getById(co.getId()), equalTo(co));
    assertThat(dao.getOne(co.getId()), equalTo(co));
    assertThat(dao.delete(co.getId()), equalTo(1));
    assertThat(dao.getAll().size(), equalTo(0));
    assertThat(dao.count(), equalTo(0L));
  }



  @DB(table = "t_order")
  interface CrudOrderDao extends CrudDao<CrudOrder, String> {

    CrudOrder getById(String id);

  }

}
