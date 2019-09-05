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

import org.jfaster.mango.binding.BoundSql;

/**
 * @author ash
 */
public class OraclePageHandler implements PageHandler {

  @Override
  public void handlePage(BoundSql boundSql, Page page) {
    int startRow = page.getPageNum() * page.getPageSize();
    int endRow = (page.getPageNum() + 1) * page.getPageSize();

    boundSql.prepend("SELECT * FROM ( SELECT B.* , ROWNUM RN FROM (")
        .append(") B WHERE ROWNUM <= ").append(endRow).append(" ) WHERE RN > ").append(startRow);

  }

  @Override
  public void handleSort(BoundSql boundSql, Sort sort) {
    boundSql.append(sort.toString());
  }

  @Override
  public void handleCount(BoundSql boundSql) {
    boundSql.prepend("SELECT COUNT(1) FROM (").append(") aliasForPage");
  }

}
