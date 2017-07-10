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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class CustomCrudDaoTest {

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

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test2() throws Throwable {
    thrown.expect(CrudException.class);
    thrown.expectMessage("the type of 1th parameters of method [getById] expected 'class java.lang.String', but 'int'");
    try {
      mango.create(CrudOrder2Dao.class);
    } catch (Exception e) {
      throw e.getCause();
    }
  }

  @Test
  public void test3() throws Throwable {
    thrown.expect(CrudException.class);
    thrown.expectMessage("can't convert method [abc] to SQL");
    try {
      mango.create(CrudOrder3Dao.class);
    } catch (Exception e) {
      throw e.getCause();
    }

  }

  @Test
  public void test4() throws Throwable {
    thrown.expect(CrudException.class);
    thrown.expectMessage("the type of 1th parameters of method [getByIdIn] expected iterable, but 'int'");
    try {
      mango.create(CrudOrder4Dao.class);
    } catch (Exception e) {
      throw e.getCause();
    }

  }

  @Test
  public void test5() throws Throwable {
    thrown.expect(CrudException.class);
    thrown.expectMessage("the type of 1th parameters of method [getByIdIn] error");
    try {
      mango.create(CrudOrder5Dao.class);
    } catch (Exception e) {
      throw e.getCause();
    }

  }

  @DB(table = "t_order")
  interface CrudOrderDao extends CrudDao<CrudOrder, String> {

    CrudOrder getById(String id);

    List<CrudOrder> getByIdIn(List<String> ids);

    CrudOrder getByUserIdAndId(int userId, String id);

    int countByUserId(int userId);

    int deleteByUserId(int userId);

  }

  @DB(table = "t_order")
  interface CrudOrder2Dao extends CrudDao<CrudOrder, String> {

    CrudOrder getById(int id);

  }

  @DB(table = "t_order")
  interface CrudOrder3Dao extends CrudDao<CrudOrder, String> {

    CrudOrder abc(int id);

  }

  @DB(table = "t_order")
  interface CrudOrder4Dao extends CrudDao<CrudOrder, String> {

    CrudOrder getByIdIn(int id);

  }

  @DB(table = "t_order")
  interface CrudOrder5Dao extends CrudDao<CrudOrder, String> {

    CrudOrder getByIdIn(List<Integer> ids);

  }

}
