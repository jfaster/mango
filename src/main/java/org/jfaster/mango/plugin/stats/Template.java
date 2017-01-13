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

package org.jfaster.mango.plugin.stats;

import org.jfaster.mango.stat.OperatorStat;

import java.util.Map;

/**
 * @author ash
 */
public class Template {

  public static String render(String beginTime, String endTime, Map<String, OperatorStat> osMap, Map<String, ExtendStat> esMap, boolean isFetchAll) {
    StringBuilder sb = new StringBuilder();
    for (String c1 : c1s) {
      sb.append(c1).append("\n");
    }
    for (String sKey : osMap.keySet()) {
      OperatorStat os = osMap.get(sKey);
      ExtendStat es = esMap.get(sKey);
      if (isFetchAll || os.getDatabaseExecuteCount() > 0 || os.getHitCount() > 0) {
        sb.append("    <tr>").append("\n");
        buildTD(sb, os.isCacheable() ? "yes" : "no");
        buildTD(sb, es.getSimpleClassName());
        buildTD(sb, es.getSimpleMethodName());
        buildTD(sb, div(os.getDatabaseExecuteAveragePenalty(), 1000000));
        buildTD(sb, os.getDatabaseExecuteCount());
        buildTD(sb, os.getDatabaseExecuteExceptionCount());
        buildTD(sb, mul(os.getDatabaseExecuteExceptionRate(), 100));
        buildTD(sb, os.getInitCount());
        buildTD(sb, os.getHitCount());
        buildTD(sb, os.getMissCount());
        buildTD(sb, mul(os.getHitRate(), 100));
        buildTD(sb, div(os.getCacheGetAveragePenalty(), 1000000));
        buildTD(sb, os.getCacheGetCount());
        buildTD(sb, os.getCacheGetExceptionCount());
        buildTD(sb, mul(os.getCacheGetExceptionRate(), 100));
        buildTD(sb, div(os.getCacheGetBulkAveragePenalty(), 1000000));
        buildTD(sb, os.getCacheGetBulkCount());
        buildTD(sb, os.getCacheGetBulkExceptionCount());
        buildTD(sb, mul(os.getCacheGetBulkExceptionRate(), 100));
        buildTD(sb, div(os.getCacheSetAveragePenalty(), 1000000));
        buildTD(sb, os.getCacheSetCount());
        buildTD(sb, os.getCacheSetExceptionCount());
        buildTD(sb, mul(os.getCacheSetExceptionRate(), 100));
        buildTD(sb, div(os.getCacheAddAveragePenalty(), 1000000));
        buildTD(sb, os.getCacheAddCount());
        buildTD(sb, os.getCacheAddExceptionCount());
        buildTD(sb, mul(os.getCacheAddExceptionRate(), 100));
        buildTD(sb, div(os.getCacheDeleteAveragePenalty(), 1000000));
        buildTD(sb, os.getCacheDeleteCount());
        buildTD(sb, os.getCacheDeleteExceptionCount());
        buildTD(sb, mul(os.getCacheDeleteExceptionRate(), 100));
        buildTD(sb, div(os.getCacheBatchDeleteAveragePenalty(), 1000000));
        buildTD(sb, os.getCacheBatchDeleteCount());
        buildTD(sb, os.getCacheBatchDeleteExceptionCount());
        buildTD(sb, mul(os.getCacheBatchDeleteExceptionRate(), 100));
        buildTD(sb, es.getSql());
        StringBuilder types = new StringBuilder();
        for (String type : es.getStrParameterTypes()) {
          types.append(type.replaceAll("<", "&lt;").replaceAll(">", "&gt;")).append("  ");
        }
        buildTD(sb, types);
        buildTD(sb, es.getType());
        buildTD(sb, os.isUseMultipleKeys() ? "yes" : "no");
        buildTD(sb, os.isCacheNullObject() ? "yes" : "no");
        sb.append("    </tr>").append("\n");
      }
    }
    for (String c2 : c2s) {
      sb.append(c2).append("\n");
    }
    String data = sb.toString();
    data = data.replace("${beginTime}", beginTime).replace("${endTime}", endTime);
    return data;
  }

  private static void buildTD(StringBuilder sb, Object data) {
    sb.append("      <td>" + data + "</td>").append("\n");
  }

  private static String div(long a, long b) {
    double c = ((double) a / (double) b);
    double d = (double) (Math.round(c * 10)) / 10;
    return String.valueOf(d);
  }

  private static String mul(double a, long b) {
    double c = a * b;
    double d = (double) (Math.round(c * 10)) / 10;
    return String.valueOf(d) + "%";
  }

  private final static String[] c1s = new String[] {
      "<html>",
      "",
      "<head>",
      "  <title>mango统计信息</title>",
      "  <script src='http://static.jfaster.org/jquery/1.11.1/jquery.min.js' type='text/javascript' charset='utf-8'></script>",
      "  <link href='http://static.jfaster.org/bootstrap/3.2.0/css/bootstrap.min.css' rel='stylesheet' >",
      "  <script src='http://static.jfaster.org/bootstrap/3.2.0/js/bootstrap.min.js' type='text/javascript' charset='utf-8'></script>",
      "  <link href='http://static.jfaster.org/bootstrap-table/1.8.1/bootstrap-table.min.css' rel='stylesheet' >",
      "<script src='http://static.jfaster.org/bootstrap-table/1.8.1/bootstrap-table.min.js' type='text/javascript' charset='utf-8'></script>",
      "</head>",
      "",
      "<body>",
      "",
      "<div id='custom-toolbar'>",
      "  <span style='font-size: 16px;margin-left: 10px;color:#666;'>",
      "    <span style='color:#000'>${beginTime}</span> 到",
      "    <span style='color:#000'>${endTime}</span>",
      "  </span>",
      "</div>",
      "",
      "<table id='mango-stats-table'",
      "    data-toggle='table'",
      "    data-show-columns='true'",
      "    data-toolbar='#custom-toolbar'",
      "    data-sort-name='databaseExecuteCount'",
      "    data-sort-order='desc'",
      "    data-detail-view='true'",
      "    data-detail-formatter='detailFormatter'>",
      "  <thead>",
      "    <tr>",
      "      <th data-field='cacheable' data-sortable='true' data-switchable='false'>缓存</th>",
      "      <th data-field='simpleClassName' data-sortable='true' data-switchable='false'>类</th>",
      "      <th data-field='simpleMethodName' data-sortable='true' data-switchable='false'>方法</th>",
      "      <th data-field='averageDatabaseExecutePenalty' data-sortable='true' data-sorter='floatSorter' data-switchable='false'>db平均速率(毫秒)</th>",
      "      <th data-field='databaseExecuteCount' data-sortable='true' data-sorter='intSorter' data-switchable='false'>db总次数</th>",
      "      <th data-field='databaseExecuteExceptionCount' data-sortable='true' data-sorter='intSorter' data-switchable='false'>db失败次数</th>",
      "      <th data-field='databaseExecuteExceptionRate' data-sortable='true' data-sorter='rateSorter' data-switchable='false'>db失败率</th>",
      "      <th data-field='initCount' data-sortable='true' data-sorter='intSorter'>对象数</th>",
      "      <th data-field='hitCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache命中数</th>",
      "      <th data-field='missCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache丢失数</th>",
      "      <th data-field='hitRate' data-sortable='true' data-sorter='rateSorter' data-visible='false'>cache命中率</th>",
      "      <th data-field='averageCacheGetPenalty' data-sortable='true' data-sorter='floatSorter' data-visible='false'>cache[get]平均速率(毫秒)</th>",
      "      <th data-field='cacheGetCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[get]总次数</th>",
      "      <th data-field='cacheGetExceptionCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[get]失败次数</th>",
      "      <th data-field='cacheGetExceptionRate' data-sortable='true' data-sorter='rateSorter' data-visible='false'>cache[get]失败率</th>",
      "      <th data-field='averageCacheGetBulkPenalty' data-sortable='true' data-sorter='floatSorter' data-visible='false'>cache[getBulk]平均速率(毫秒)</th>",
      "      <th data-field='cacheGetBulkCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[getBulk]总次数</th>",
      "      <th data-field='cacheGetBulkExceptionCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[getBulk]失败次数</th>",
      "      <th data-field='cacheGetBulkExceptionRate' data-sortable='true' data-sorter='rateSorter' data-visible='false'>cache[getBulk]失败率</th>",
      "      <th data-field='averageCacheSetPenalty' data-sortable='true' data-sorter='floatSorter' data-visible='false'>cache[set]平均速率(毫秒)</th>",
      "      <th data-field='cacheSetCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[set]总次数</th>",
      "      <th data-field='cacheSetExceptionCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[set]失败次数</th>",
      "      <th data-field='cacheSetExceptionRate' data-sortable='true' data-sorter='rateSorter' data-visible='false'>cache[set]失败率</th>",
      "      <th data-field='averageCacheAddPenalty' data-sortable='true' data-sorter='floatSorter' data-visible='false'>cache[add]平均速率(毫秒)</th>",
      "      <th data-field='cacheAddCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[add]总次数</th>",
      "      <th data-field='cacheAddExceptionCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[add]失败次数</th>",
      "      <th data-field='cacheAddExceptionRate' data-sortable='true' data-sorter='rateSorter' data-visible='false'>cache[add]失败率</th>",
      "      <th data-field='averageCacheDeletePenalty' data-sortable='true' data-sorter='floatSorter' data-visible='false'>cache[delete]平均速率(毫秒)</th>",
      "      <th data-field='cacheDeleteCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[delete]总次数</th>",
      "      <th data-field='cacheDeleteExceptionCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[delete]失败次数</th>",
      "      <th data-field='cacheDeleteExceptionRate' data-sortable='true' data-sorter='rateSorter' data-visible='false'>cache[delete]失败率</th>",
      "      <th data-field='averageCacheBatchDeletePenalty' data-sortable='true' data-sorter='floatSorter' data-visible='false'>cache[batchDelete]平均速率(毫秒)</th>",
      "      <th data-field='cacheBatchDeleteCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[batchDelete]总次数</th>",
      "      <th data-field='cacheBatchDeleteExceptionCount' data-sortable='true' data-sorter='intSorter' data-visible='false'>cache[batchDelete]失败次数</th>",
      "      <th data-field='cacheBatchDeleteExceptionRate' data-sortable='true' data-sorter='rateSorter' data-visible='false'>cache[batchDelete]失败率</th>",
      "      <th data-field='sql' data-visible='false' data-switchable='false'>sql</th>",
      "      <th data-field='strParameterTypes' data-visible='false' data-switchable='false'>参数类型</th>",
      "      <th data-field='type' data-visible='false' data-switchable='false'>操作类型</th>",
      "      <th data-field='useMultipleKeys' data-visible='false' data-switchable='false'>缓存是否操作多个key</th>",
      "      <th data-field='cacheNullObject' data-visible='false' data-switchable='false'>是否缓存null对象</th>",
      "    </tr>",
      "  </thead>",
      "  <tbody>",
  };

  private final static String[] c2s = new String[] {
      "  </tbody>",
      "</table>",
      "",
      "<script>",
      "  var $table = $('#mango-stats-table');",
      "  $table.on('expand-row.bs.table', function (e, index, row, $detail) {",
      "  });",
      "  function intSorter(s1, s2) {",
      "    var n1 = parseInt(s1);",
      "    var n2 = parseInt(s2);",
      "    if (n1 > n2) return 1;",
      "    if (n1 < n2) return -1;",
      "    return 0;",
      "  }",
      "  function floatSorter(s1, s2) {",
      "    var n1 = parseFloat(s1);",
      "    var n2 = parseFloat(s2);",
      "    if (n1 > n2) return 1;",
      "    if (n1 < n2) return -1;",
      "    return 0;",
      "  }",
      "  function rateSorter(s1, s2) {",
      "    s1 = s1.substring(0, s1.length - 1);",
      "    s2 = s2.substring(0, s2.length - 1);",
      "    var n1 = parseFloat(s1);",
      "    var n2 = parseFloat(s2);",
      "    if (n1 > n2) return 1;",
      "    if (n1 < n2) return -1;",
      "    return 0;",
      "  }",
      "  function detailFormatter(index, row) {",
      "    var html = [];",
      "    html.push('<p><b>SQL:</b>  ' + row['sql'] + '</p>');",
      "    html.push('<p><b>参数类型:</b>  ' + row['strParameterTypes'] + '</p>');",
      "    var isCacheable = row['cacheable'] == 'yes';",
      "    var isUseMultipleKeys = row['useMultipleKeys'] == 'yes';",
      "    var isCacheNullObject = row['cacheNullObject'] == 'yes';",
      "    var isQuery = row['type'] == 'query';",
      "    var isUpdate = row['type'] == 'update';",
      "    var isBatchUpdate = row['type'] == 'batchupdate';",
      "    if (isCacheable) {",
      "      html.push('<br>');",
      "      if (isQuery) {",
      "        html.push('<p><b>cache命中数:</b>  ' + row['hitCount'] + '</p>');",
      "        html.push('<p><b>cache丢失数:</b>  ' + row['missCount'] + '</p>');",
      "        html.push('<p><b>cache命中率:</b>  ' + row['hitRate'] + '</p>');",
      "        html.push('<br>');",
      "      }",
      "      if (isQuery && !isUseMultipleKeys) {",
      "        html.push('<p><b>cache[get]平均速率(毫秒):</b>  ' + row['averageCacheGetPenalty'] + '</p>');",
      "        html.push('<p><b>cache[get]总次数:</b>  ' + row['cacheGetCount'] + '</p>');",
      "        html.push('<p><b>cache[get]失败次数:</b>  ' + row['cacheGetExceptionCount'] + '</p>');",
      "        html.push('<p><b>cache[get]失败率:</b>  ' + row['cacheGetExceptionRate'] + '</p>');",
      "        html.push('<br>');",
      "      }",
      "      if (isQuery && isUseMultipleKeys) {",
      "        html.push('<p><b>cache[getBulk]平均速率(毫秒):</b>  ' + row['averageCacheGetBulkPenalty'] + '</p>');",
      "        html.push('<p><b>cache[getBulk]总次数:</b>  ' + row['cacheGetBulkCount'] + '</p>');",
      "        html.push('<p><b>cache[getBulk]失败次数:</b>  ' + row['cacheGetBulkExceptionCount'] + '</p>');",
      "        html.push('<p><b>cache[getBulk]失败率:</b>  ' + row['cacheGetBulkExceptionRate'] + '</p>');",
      "        html.push('<br>');",
      "      }",
      "      if (isQuery) {",
      "        html.push('<p><b>cache[set]平均速率(毫秒):</b>  ' + row['averageCacheSetPenalty'] + '</p>');",
      "        html.push('<p><b>cache[set]总次数:</b>  ' + row['cacheSetCount'] + '</p>');",
      "        html.push('<p><b>cache[set]失败次数:</b>  ' + row['cacheSetExceptionCount'] + '</p>');",
      "        html.push('<p><b>cache[set]失败率:</b>  ' + row['cacheSetExceptionRate'] + '</p>');",
      "        html.push('<br>');",
      "      }",
      "      if (isQuery && isCacheNullObject) {",
      "        html.push('<p><b>cache[add]平均速率(毫秒):</b>  ' + row['averageCacheAddPenalty'] + '</p>');",
      "        html.push('<p><b>cache[add]总次数:</b>  ' + row['cacheAddCount'] + '</p>');",
      "        html.push('<p><b>cache[add]失败次数:</b>  ' + row['cacheAddExceptionCount'] + '</p>');",
      "        html.push('<p><b>cache[add]失败率:</b>  ' + row['cacheAddExceptionRate'] + '</p>');",
      "        html.push('<br>');",
      "      }",
      "      if (isUpdate && !isUseMultipleKeys) {",
      "        html.push('<p><b>cache[delete]平均速率(毫秒):</b>  ' + row['averageCacheDeletePenalty'] + '</p>');",
      "        html.push('<p><b>cache[delete]总次数:</b>  ' + row['cacheDeleteCount'] + '</p>');",
      "        html.push('<p><b>cache[delete]失败次数:</b>  ' + row['cacheDeleteExceptionCount'] + '</p>');",
      "        html.push('<p><b>cache[delete]失败率:</b>  ' + row['cacheDeleteExceptionRate'] + '</p>');",
      "        html.push('<br>');",
      "      }",
      "      if ((isUpdate && isUseMultipleKeys) || isBatchUpdate) {",
      "        html.push('<p><b>cache[batchDelete]平均速率(毫秒):</b>  ' + row['averageCacheBatchDeletePenalty'] + '</p>');",
      "        html.push('<p><b>cache[batchDelete]总次数:</b>  ' + row['cacheBatchDeleteCount'] + '</p>');",
      "        html.push('<p><b>cache[batchDelete]失败次数:</b>  ' + row['cacheBatchDeleteExceptionCount'] + '</p>');",
      "        html.push('<p><b>cache[batchDelete]失败率:</b>  ' + row['cacheBatchDeleteExceptionRate'] + '</p>');",
      "        html.push('<br>');",
      "      }",
      "    }",
      "    return html.join('');",
      "  }",
      "</script>",
      "</body>",
      "</html>"
  };

}









