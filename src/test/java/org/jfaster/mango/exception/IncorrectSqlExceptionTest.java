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

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.parser.SqlParserException;
import org.jfaster.mango.support.DataSourceConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author ash
 */
public class IncorrectSqlExceptionTest {

  private final static Mango mango = Mango.newInstance(DataSourceConfig.getDataSource());

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test2() {
    thrown.expect(DescriptionException.class);
    thrown.expectMessage("sql is null or empty");
    Dao dao = mango.create(Dao.class);
    dao.add2();
  }

  @Test
  public void test4() {
    thrown.expect(SqlParserException.class);
    Dao dao = mango.create(Dao.class);
    dao.add3();
  }

  @DB
  interface Dao {

    @SQL("")
    int add2();

    @SQL("test")
    int add3();

  }

}
