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
import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.operator.cache.Day;
import org.jfaster.mango.operator.cache.LocalCacheHandler;
import org.jfaster.mango.support.DataSourceConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * 测试{@link IncorrectAnnotationException}
 *
 * @author ash
 */
public class IllegalStateExceptionTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("dao interface expected one @DB annotation but not found");
    Mango mango = Mango.newInstance(DataSourceConfig.getDataSource());
    mango.setLazyInit(true);
    mango.create(Dao.class);
  }

  @Test
  public void test2() {
    thrown.expect(DescriptionException.class);
    thrown.expectMessage("each method expected one of @SQL or @UseSqlGenerator annotation but not found");
    Mango mango = Mango.newInstance(DataSourceConfig.getDataSource());
    mango.setLazyInit(true);
    mango.setCacheHandler(new LocalCacheHandler());
    Dao2 dao = mango.create(Dao2.class);
    dao.add();
  }

  @Test
  public void test3() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("if use cache, each method expected one or more " +
        "@CacheBy annotation on parameter but found 0");
    Mango mango = Mango.newInstance(DataSourceConfig.getDataSource());
    mango.setLazyInit(true);
    mango.setCacheHandler(new LocalCacheHandler());
    Dao3 dao = mango.create(Dao3.class);
    dao.add();
  }

  static interface Dao {
  }

  @DB
  static interface Dao2 {
    public int add();
  }

  @DB
  @Cache(prefix = "dao3_", expire = Day.class)
  static interface Dao3 {
    @SQL("insert into ...")
    public int add();
  }

}
