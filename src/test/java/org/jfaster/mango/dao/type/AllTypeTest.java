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

package org.jfaster.mango.dao.type;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.ReturnGeneratedId;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.TableIncludeAllTypes;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class AllTypeTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private final static Mango mango = Mango.newInstance(ds);
  private final static TableIncludeAllTypesDao dao = mango.create(TableIncludeAllTypesDao.class);

  @Before
  public void before() throws Exception {
    Connection conn = ds.getConnection();
    Table.TABLE_INCLUDE_ALL_TYPES.load(conn);
    conn.close();
  }

  @Test
  public void testNonNull() {
    TableIncludeAllTypes t = new TableIncludeAllTypes();
    t.setNavByte(Byte.MAX_VALUE);
    t.setNavShort(Short.MAX_VALUE);
    t.setNavInteger(Integer.MAX_VALUE);
    t.setNavLong(Long.MAX_VALUE);
    t.setNavFloat(3.14f);
    t.setNavDouble(128.12);
    t.setNavBollean(true);
    t.setNavChar('a');
    t.setObjByte(Byte.MAX_VALUE);
    t.setObjShort(Short.MAX_VALUE);
    t.setObjInteger(Integer.MAX_VALUE);
    t.setObjLong(Long.MAX_VALUE);
    t.setObjFloat(12.44f);
    t.setObjDouble(100000000.1);
    t.setObjBollean(true);
    t.setObjChar('b');
    t.setObjString("ash266");
    t.setObjBigDecimal(new BigDecimal("999999.88"));
    t.setObjBigInteger(new BigInteger("123456789987654321"));
    t.setNavBytes(new byte[] {1, 2, 3});
    t.setObjBytes(new Byte[] {2, 3, 4});
    t.setObjDate(new Date());
    int id = dao.add(t);
    t.setId(id);
    TableIncludeAllTypes t2 = dao.getById(id);
    assertThat(t2, equalTo(t));
  }

  @Test
  public void testNull() {
    TableIncludeAllTypes t = new TableIncludeAllTypes();
    int id = dao.add(t);
    t.setId(id);
    TableIncludeAllTypes t2 = dao.getById(id);
    assertThat(t2, equalTo(t));
  }

  @DB(table = "table_include_all_types")
  interface TableIncludeAllTypesDao {

    String COLUMNS = "nav_byte, nav_short, nav_integer, nav_long, nav_float, nav_double, nav_bollean, nav_char, obj_byte, obj_short, obj_integer, obj_long, obj_float, obj_double, obj_bollean, obj_char, obj_string, obj_big_decimal, obj_big_integer, nav_bytes, obj_bytes, obj_date";

    @ReturnGeneratedId
    @SQL("insert into #table(" + COLUMNS + ") values(:navByte, :navShort, :navInteger, :navLong, :navFloat, :navDouble, :navBollean, :navChar, :objByte, :objShort, :objInteger, :objLong, :objFloat, :objDouble, :objBollean, :objChar, :objString, :objBigDecimal, :objBigInteger, :navBytes, :objBytes, :objDate)")
    int add(TableIncludeAllTypes tableIncludeAllTypes);

    @SQL("select id, " + COLUMNS + " from #table where id = :1")
    TableIncludeAllTypes getById(int id);

  }


}
