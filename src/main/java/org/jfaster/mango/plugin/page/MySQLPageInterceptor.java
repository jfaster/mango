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
import org.jfaster.mango.interceptor.Parameter;
import org.jfaster.mango.interceptor.QueryInterceptor;
import org.jfaster.mango.mapper.SingleColumnRowMapper;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author ash
 */
public class MySQLPageInterceptor extends QueryInterceptor {

  @Override
  public void interceptQuery(BoundSql boundSql, List<Parameter> parameters, DataSource dataSource) {
    for (Parameter parameter : parameters) {
      Object val = parameter.getValue();
      if (val instanceof Page) {
        Page page = (Page) val;
        if (page.isFetchTotal()) { // 需要获取总数
          BoundSql totalBoundSql = boundSql.copy();
          String sql = totalBoundSql.getSql();
          String totalSql = "SELECT COUNT(1) FROM (" + sql + ") aliasForPage";
          totalBoundSql.setSql(totalSql);
          SingleColumnRowMapper<Integer> mapper = new SingleColumnRowMapper<Integer>(int.class);
          int total = getJdbcOperations().queryForObject(dataSource, totalBoundSql, mapper);
          page.setTotal(total);
        }

        // 分页取数据
        int startRow = (page.getPageNum() - 1) * page.getPageSize();
        int pageSize = page.getPageSize();

        if (startRow < 0) {
          throw new PageException("startRow need >= 0, but startRow is " + startRow);
        }
        if (pageSize <= 0) {
          throw new PageException("pageSize need > 1, but pageSize is " + pageSize);
        }
        String sql = boundSql.getSql();
        sql = sql + " limit ?, ?";
        boundSql.setSql(sql);
        boundSql.addArg(startRow);
        boundSql.addArg(pageSize);
      }
    }
  }

}
