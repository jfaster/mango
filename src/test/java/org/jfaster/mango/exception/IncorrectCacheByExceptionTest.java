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

package org.jfaster.mango.exception;

import org.jfaster.mango.annotation.Cache;
import org.jfaster.mango.annotation.CacheBy;
import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.operator.cache.Day;
import org.jfaster.mango.operator.cache.IncorrectCacheByException;
import org.jfaster.mango.operator.cache.LocalCacheHandler;
import org.jfaster.mango.support.DataSourceConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试{@link org.jfaster.mango.operator.cache.IncorrectCacheByException}
 *
 * @author ash
 */
public class IncorrectCacheByExceptionTest {

  private final static Mango mango = Mango.newInstance(DataSourceConfig.getDataSource());

  static {
    mango.setLazyInit(true);
    mango.setCacheHandler(new LocalCacheHandler());
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() {
    thrown.expect(IncorrectCacheByException.class);
    thrown.expectMessage("CacheBy :2 can't match any db parameter");
    Dao dao = mango.create(Dao.class);
    dao.add(1, 2);
  }

  @Test
  public void test2() {
    thrown.expect(IncorrectCacheByException.class);
    thrown.expectMessage("CacheBy :1 can't match any db parameter");
    Dao dao = mango.create(Dao.class);
    dao.batchAdd(new ArrayList<Integer>());
  }

  @DB
  @Cache(prefix = "dao_", expire = Day.class)
  static interface Dao {
    @SQL("insert into ${1 + :1} ...")
    public int add(int a, @CacheBy int b);

    @SQL("insert into ...")
    public int[] batchAdd(@CacheBy List<Integer> ids);
  }

}
