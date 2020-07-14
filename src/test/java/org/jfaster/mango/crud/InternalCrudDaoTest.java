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
import org.jfaster.mango.page.Direction;
import org.jfaster.mango.page.Page;
import org.jfaster.mango.page.PageResult;
import org.jfaster.mango.page.Sort;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Msg;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ash
 */
public class InternalCrudDaoTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private final static Mango mango = Mango.newInstance(ds);
  private final static OrderDao dao = mango.create(OrderDao.class);
  private final static MsgDao msgDao = mango.create(MsgDao.class);

  @Before
  public void before() throws Exception {
    Table.ORDER.load(ds);
    Table.MSG.load(ds);
  }

  @Test
  public void testAdd() {
    CrudOrder co = CrudOrder.createRandomCrudOrder();
    dao.add(co);
    assertThat(dao.getOne(co.getId()), equalTo(co));
  }

  @Test
  public void testAddAndReturnGeneratedId() {
    Msg msg = Msg.createRandomMsg();
    int id = (int) msgDao.addAndReturnGeneratedId(msg);
    Msg dbMsg = msgDao.getOne(id);
    msg.setId(dbMsg.getId());
    assertThat(dbMsg, equalTo(msg));
  }

  @Test
  public void testBatchAdd() {
    List<CrudOrder> cos = CrudOrder.createRandomCrudOrders(5);
    dao.add(cos);
    assertThat(Sets.newHashSet(dao.findAll()), equalTo(Sets.newHashSet(cos)));

    List<Msg> msgs = Msg.createRandomMsgs(5);
    msgDao.add(msgs);
    assertThat(msgDao.findAll(), hasSize(5));
  }

  @Test
  public void testGetById() {
    CrudOrder co = CrudOrder.createRandomCrudOrder();
    dao.add(co);
    assertThat(dao.getOne(co.getId()), equalTo(co));
    assertThat(dao.getOne(co.getId() + "abc"), nullValue());
  }

  @Test
  public void testfindOne() {
    CrudOrder co = CrudOrder.createRandomCrudOrder();
    dao.add(co);
    Optional<CrudOrder> op = dao.findOne(co.getId());
    assertThat(op.isPresent(), equalTo(true));
    assertThat(op.get(), equalTo(co));
    assertThat(dao.findOne(co.getId() + "abc").isPresent(), equalTo(false));
  }

  @Test
  public void testfindMany() {
    List<CrudOrder> cos = CrudOrder.createRandomCrudOrders(5);
    dao.add(cos);
    List<String> ids = cos.stream().map(CrudOrder::getId).collect(Collectors.toList());
    assertThat(Sets.newHashSet(dao.findMany(ids)), equalTo(Sets.newHashSet(cos)));

    ids.clear();
    assertThat(dao.findMany(ids), hasSize(0));
  }

  @Test
  public void testCount() {
    assertThat(dao.count(), equalTo(0L));
    List<CrudOrder> cos = CrudOrder.createRandomCrudOrders(5);
    dao.add(cos);
    assertThat(dao.count(), equalTo(5L));
  }

  @Test
  public void testUpdate() {
    CrudOrder co = CrudOrder.createRandomCrudOrder();
    dao.add(co);
    co.setUserId(0);
    co.setPrice(100);
    dao.update(co);
    assertThat(dao.getOne(co.getId()), equalTo(co));

    co.setUserId(1);
    co.setPrice(null); // 设置成null不更新
    dao.update(co);
    co.setPrice(100);
    assertThat(dao.getOne(co.getId()), equalTo(co));
  }

  @Test
  public void testBatchUpdate() {
    CrudOrder co1 = CrudOrder.createRandomCrudOrder();
    CrudOrder co2 = CrudOrder.createRandomCrudOrder();
    CrudOrder co3 = CrudOrder.createRandomCrudOrder();
    dao.add(Lists.newArrayList(co1, co2, co3));
    co1.setUserId(10000000);
    co2.setUserId(10000000);
    co3.setUserId(10000000);
    int[] r = dao.update(Lists.newArrayList(co1, co2, co3));
    assertThat(r.length, equalTo(3));
    assertThat(Sets.newHashSet(dao.findAll()), equalTo(Sets.newHashSet(co1, co2, co3)));

    assertThat(dao.update(Lists.newArrayList()).length, equalTo(0));
  }

  @Test
  public void testDelete() {
    CrudOrder co = CrudOrder.createRandomCrudOrder();
    dao.add(co);
    assertThat(dao.getOne(co.getId()), equalTo(co));
    assertThat(dao.delete(co.getId()), equalTo(1));
    assertThat(dao.getOne(co.getId()), equalTo(null));
    assertThat(dao.delete(co.getId()), equalTo(0));
  }

  @Test
  public void testFindAll() {
    assertThat(dao.findAll(), hasSize(0));
    List<CrudOrder> cos = CrudOrder.createRandomCrudOrders(5);
    dao.add(cos);
    assertThat(Sets.newHashSet(dao.findAll()), equalTo(Sets.newHashSet(cos)));
  }

  @Test
  public void testFindAllSort() {
    CrudOrder co1 = CrudOrder.createCrudOrder("1", 2, 1);
    CrudOrder co2 = CrudOrder.createCrudOrder("2", 2, 10);
    CrudOrder co3 = CrudOrder.createCrudOrder("3", 3, 5);
    CrudOrder co4 = CrudOrder.createCrudOrder("4", 3, 7);
    CrudOrder co5 = CrudOrder.createCrudOrder("5", 3, 3);
    dao.add(Lists.newArrayList(co1, co2, co3, co4, co5));

    assertThat(dao.findAll(Sort.by(Direction.DESC, "uid", "price")),
        equalTo(Lists.newArrayList(co4, co3, co5, co2, co1)));
  }

  @Test
  public void testFindPage() {
    CrudOrder co1 = CrudOrder.createCrudOrder("1", 2, 1);
    CrudOrder co2 = CrudOrder.createCrudOrder("2", 2, 10);
    CrudOrder co3 = CrudOrder.createCrudOrder("3", 3, 5);
    CrudOrder co4 = CrudOrder.createCrudOrder("4", 3, 7);
    CrudOrder co5 = CrudOrder.createCrudOrder("5", 3, 3);
    dao.add(Lists.newArrayList(co1, co2, co3, co4, co5));

    PageResult<CrudOrder> pr = dao.findAll(Page.of(0, 2, Direction.DESC, "uid", "price"));
    assertThat(pr.getData(), equalTo(Lists.newArrayList(co4, co3)));
    assertThat(pr.getTotal(), equalTo(5L));

    pr = dao.findAll(Page.of(1, 2, Direction.DESC, "uid", "price"));
    assertThat(pr.getData(), equalTo(Lists.newArrayList(co5, co2)));
    assertThat(pr.getTotal(), equalTo(5L));

    pr = dao.findAll(Page.of(2, 2, Direction.DESC, "uid", "price"));
    assertThat(pr.getData(), equalTo(Lists.newArrayList(co1)));
    assertThat(pr.getTotal(), equalTo(5L));


    pr = dao.findAll(Page.of(0, 2));
    assertThat(pr.getData(), hasSize(2));
    assertThat(pr.getTotal(), equalTo(5L));

    pr = dao.findAll(Page.of(1, 2));
    assertThat(pr.getData(), hasSize(2));
    assertThat(pr.getTotal(), equalTo(5L));

    pr = dao.findAll(Page.of(2, 2));
    assertThat(pr.getData(), hasSize(1));
    assertThat(pr.getTotal(), equalTo(5L));
  }

  @DB(table = "t_order")
  interface OrderDao extends CrudDao<CrudOrder, String> {
  }

  @DB(table = "msg")
  interface MsgDao extends CrudDao<Msg, Integer> {
  }

}