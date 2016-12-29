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
import org.jfaster.mango.support.DataSourceConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

/**
 * 测试{@link IncorrectParameterTypeException}
 *
 * @author ash
 */
public class IncorrectParameterTypeExceptionTest {

  private final static Mango mango = Mango.newInstance(DataSourceConfig.getDataSource());

  static {
    mango.setLazyInit(true);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test4() {
    thrown.expect(IncorrectParameterTypeException.class);
    thrown.expectMessage("invalid type of :1, " +
        "expected array or implementations of java.util.List or implementations of java.util.Set " +
        "but int");
    Dao dao = mango.create(Dao.class);
    dao.get(1);
  }

  @Test
  public void test6() {
    thrown.expect(IncorrectParameterTypeException.class);
    thrown.expectMessage("invalid component type of :1, component type of :1 " +
        "expected a class can be identified by jdbc " +
        "but class java.lang.Object");
    Dao dao = mango.create(Dao.class);
    dao.get3(new Object[]{});
  }

  @Test
  public void test7() {
    thrown.expect(IncorrectParameterTypeException.class);
    thrown.expectMessage("invalid type of :1, " +
        "expected a class can be identified by jdbc " +
        "but class java.lang.Object");
    Dao dao = mango.create(Dao.class);
    dao.get4(new Object());
  }

  @DB
  static interface Dao {

    @SQL("select ... where a in (:1)")
    public List<Integer> get(int a);

    @SQL("select ... where a in (:1)")
    public List<Integer> get2(List<List<Integer>> list);

    @SQL("select ... where a in (:1)")
    public List<Integer> get3(Object[] objs);

    @SQL("select ... where a=:1")
    public int get4(Object obj);
  }


}
