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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    CrudOrderDao dao = mango.create(CrudOrderDao.class);
    int userId = 1;
    HashSet<CrudOrder> cos = Sets.newHashSet();
    List<String> ids = Lists.newArrayList();
    CrudOrder co = CrudOrder.createRandomCrudOrder(userId);
    dao.add(co);
    ids.add(co.getId());
    cos.add(co);
    co = CrudOrder.createRandomCrudOrder(userId);
    ids.add(co.getId());
    dao.add(co);
    cos.add(co);

    assertThat(dao.getById(co.getId()), equalTo(co));
    assertThat(dao.getByUserIdAndId(co.getUserId(), co.getId()), equalTo(co));
    HashSet<CrudOrder> actualCos = Sets.newHashSet(dao.getByIdIn(ids));
    assertThat(actualCos, equalTo(cos));
    assertThat(dao.countByUserId(userId), equalTo(2));
    assertThat(dao.deleteByUserId(userId), equalTo(2));
    assertThat(dao.countByUserId(userId), equalTo(0));
  }

  @DB(table = "t_order")
  interface CrudOrderDao extends CrudDao<CrudOrder, String> {

    CrudOrder getById(String id);

    List<CrudOrder> getByIdIn(List<String> ids);

    CrudOrder getByUserIdAndId(int userId, String id);

    int countByUserId(int userId);

    int deleteByUserId(int userId);

  }

}
