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

package org.jfaster.mango.page;

import org.jfaster.mango.crud.CrudOrder;
import org.jfaster.mango.crud.CustomCrudDaoTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jfaster.mango.crud.CustomCrudDaoTest.mango;

/**
 * @author ash
 */
public class PageTest {

  @Test
  public void create() throws Exception {

    Page page = Page.of(1, 100);
    assertThat(page.getPageNum(), is(1));
    assertThat(page.getPageSize(), is(100));

    page = Page.of(2, 200);
    assertThat(page.getPageNum(), is(2));
    assertThat(page.getPageSize(), is(200));

  }

  @Test
  public void testPage() throws Exception {
    CustomCrudDaoTest.CrudOrderDao dao = mango.create(CustomCrudDaoTest.CrudOrderDao.class);
    int userId = 2;
    for (int i = 0; i < 10; i++) {
      CrudOrder order = CrudOrder.createRandomCrudOrder(userId);
      dao.add(order);
    }
    assertThat(dao.getByUserId(userId, Page.of(0, 3)).size(), equalTo(3));

    assertThat(dao.getByUserId(userId, Page.of(1, 3)).size(), equalTo(3));

    assertThat(dao.getByUserId(userId, Page.of(2, 3)).size(), equalTo(3));

    PageResult<CrudOrder> pr = dao.findByUserId(userId, Page.of(3, 3));
    assertThat(pr.getData().size(), equalTo(1));
    assertThat(pr.getTotal(), equalTo(10L));
  }

}