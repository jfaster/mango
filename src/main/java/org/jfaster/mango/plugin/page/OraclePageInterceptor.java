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

package org.jfaster.mango.plugin.page;

import org.jfaster.mango.binding.BoundSql;

/**
 * @author ash
 */
public class OraclePageInterceptor extends AbstractPageInterceptor {

  @Override
  void handleTotal(BoundSql boundSql) {
    String sql = boundSql.getSql();
    sql = "SELECT COUNT(1) FROM (" + sql + ") aliasForPage";
    boundSql.setSql(sql);
  }

  @Override
  void handlePage(int pageNum, int pageSize, BoundSql boundSql) {
    int startRow = (pageNum - 1) * pageSize;
    int endRow = pageNum * pageSize;
    String sql = boundSql.getSql();
    sql = "SELECT * FROM ( SELECT B.* , ROWNUM RN FROM (" + sql + ") B WHERE ROWNUM <= "
        + endRow + " ) WHERE RN > " + startRow;
    boundSql.setSql(sql);
  }

}
