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

package org.jfaster.mango;

import org.jfaster.mango.annotation.*;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.operator.cache.Day;
import org.jfaster.mango.operator.cache.LocalCacheHandler;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Position;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Random;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class MultiCacheByTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();

  @Before
  public void before() throws Exception {
    Connection conn = ds.getConnection();
    Table.POSITION.load(conn);
    conn.close();
  }


  @Test
  public void test() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    PositionDao dao = mango.create(PositionDao.class);
    Position p = createRandomPosition();
    dao.insert(p);
    String key = getKey(p);
    assertThat(cacheHandler.get(key), nullValue());
    assertThat(dao.get(p.getX(), p.getY()), equalTo(p));
    assertThat((Position) cacheHandler.get(key), equalTo(p));
    assertThat(dao.get(p.getX(), p.getY()), equalTo(p));

    p.setV(9527);
    assertThat(dao.update(p), is(1));
    assertThat(cacheHandler.get(key), nullValue());
    assertThat(dao.get(p.getX(), p.getY()), equalTo(p));
    assertThat((Position) cacheHandler.get(key), equalTo(p));

    dao.delete(p.getX(), p.getY());
    assertThat(cacheHandler.get(key), nullValue());
    assertThat(dao.get(p.getX(), p.getY()), nullValue());
  }

  @DB(table = "pos")
  @Cache(prefix = "pos", expire = Day.class)
  interface PositionDao {

    @CacheIgnored
    @SQL("insert into #table(x, y, v) values(:x, :y, :v)")
    void insert(@CacheBy("x, y") Position p);

    @SQL("delete from #table where x = :1 and y = :2")
    boolean delete(@CacheBy int x, @CacheBy int y);

    @SQL("update #table set v = :v where x = :x and y = :y")
    int update(@CacheBy("x,y") Position p);

    @SQL("select x, y, v from #table where x = :1 and y = :2")
    Position get(@CacheBy int x, @CacheBy int y);

  }

  private Position createRandomPosition() {
    Random r = new Random();
    int x = r.nextInt(100000);
    int y = r.nextInt(100000);
    int v = r.nextInt(100000);
    return new Position(x, y, v);
  }

  private String getKey(Position p) {
    return "pos_" + p.getX() + "_" + p.getY();
  }

}













