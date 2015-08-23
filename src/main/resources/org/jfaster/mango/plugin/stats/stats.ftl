<html>
<head>
    <title>mango统计信息</title>
    <link href="http://static.jfaster.org/bootstrap-3.2.0/css/bootstrap.min.css" rel="stylesheet" >
    <link href="http://static.jfaster.org/bootstrap-table-1.8.1/bootstrap-table.min.css" rel="stylesheet" >
    <script src="http://static.jfaster.org/jquery.min.js" type="text/javascript" charset="utf-8"></script>
    <script src="http://static.jfaster.org/bootstrap-3.2.0/js/bootstrap.min.js" type="text/javascript" charset="utf-8"></script>
    <script src="http://static.jfaster.org/bootstrap-table-1.8.1/bootstrap-table.min.js" type="text/javascript" charset="utf-8"></script>
</head>
<body>

<div id="custom-toolbar">
    <div class="form-inline" role="form">
        <button id="clear-stats" type="button" class="btn btn-default">清空状态</button>
    </div>
</div>

<table id="mango-stats-table"
       data-toggle="table"
       data-show-columns="true"
       data-toolbar="#custom-toolbar"
       data-sort-name="databaseExecuteCount"
       data-sort-order="desc"
       data-detail-view="true"
       data-detail-formatter="detailFormatter">
    <thead>
    <tr>
        <th data-field="cacheable" data-sortable="true">缓存</th>

        <th data-field="simpleClassName" data-sortable="true">类</th>
        <th data-field="simpleMethodName" data-sortable="true">方法</th>

        <th data-field="averageDatabaseExecutePenalty" data-sortable="true" data-sorter="floatSorter">db平均速率(毫秒)</th>
        <th data-field="databaseExecuteCount" data-sortable="true" data-sorter="intSorter">db总次数</th>
        <th data-field="databaseExecuteExceptionCount" data-sortable="true" data-sorter="intSorter">db失败次数</th>
        <th data-field="databaseExecuteExceptionRate" data-sortable="true" data-sorter="rateSorter">db失败率</th>

        <th data-field="hitCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache命中数</th>
        <th data-field="missCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache丢失数</th>
        <th data-field="hitRate" data-sortable="true" data-sorter="rateSorter" data-visible="false">cache命中率</th>

        <th data-field="averageCacheGetPenalty" data-sortable="true" data-sorter="floatSorter" data-visible="false">cache[get]平均速率(毫秒)</th>
        <th data-field="cacheGetCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[get]总次数</th>
        <th data-field="cacheGetExceptionCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[get]失败次数</th>
        <th data-field="cacheGetExceptionRate" data-sortable="true" data-sorter="rateSorter" data-visible="false">cache[get]失败率</th>

        <th data-field="averageCacheGetBulkPenalty" data-sortable="true" data-sorter="floatSorter" data-visible="false">cache[getBulk]平均速率(毫秒)</th>
        <th data-field="cacheGetBulkCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[getBulk]总次数</th>
        <th data-field="cacheGetBulkExceptionCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[getBulk]失败次数</th>
        <th data-field="cacheGetBulkExceptionRate" data-sortable="true" data-sorter="rateSorter" data-visible="false">cache[getBulk]失败率</th>

        <th data-field="averageCacheSetPenalty" data-sortable="true" data-sorter="floatSorter" data-visible="false">cache[set]平均速率(毫秒)</th>
        <th data-field="cacheSetCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[set]总次数</th>
        <th data-field="cacheSetExceptionCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[set]失败次数</th>
        <th data-field="cacheSetExceptionRate" data-sortable="true" data-sorter="rateSorter" data-visible="false">cache[set]失败率</th>

        <th data-field="averageCacheAddPenalty" data-sortable="true" data-sorter="floatSorter" data-visible="false">cache[add]平均速率(毫秒)</th>
        <th data-field="cacheAddCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[add]总次数</th>
        <th data-field="cacheAddExceptionCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[add]失败次数</th>
        <th data-field="cacheAddExceptionRate" data-sortable="true" data-sorter="rateSorter" data-visible="false">cache[add]失败率</th>

        <th data-field="averageCacheDeletePenalty" data-sortable="true" data-sorter="floatSorter" data-visible="false">cache[delete]平均速率(毫秒)</th>
        <th data-field="cacheDeleteCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[delete]总次数</th>
        <th data-field="cacheDeleteExceptionCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[delete]失败次数</th>
        <th data-field="cacheDeleteExceptionRate" data-sortable="true" data-sorter="rateSorter" data-visible="false">cache[delete]失败率</th>

        <th data-field="averageCacheBatchDeletePenalty" data-sortable="true" data-sorter="floatSorter" data-visible="false">cache[batchDelete]平均速率(毫秒)</th>
        <th data-field="cacheBatchDeleteCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[batchDelete]总次数</th>
        <th data-field="cacheBatchDeleteExceptionCount" data-sortable="true" data-sorter="intSorter" data-visible="false">cache[batchDelete]失败次数</th>
        <th data-field="cacheBatchDeleteExceptionRate" data-sortable="true" data-sorter="rateSorter" data-visible="false">cache[batchDelete]失败率</th>

        <th data-field="averageInitPenalty" data-sortable="true" data-sorter="floatSorter">init速率(毫秒)</th>
        <th data-field="initCount" data-sortable="true" data-sorter="intSorter">init次数</th>

        <th data-field="sql" data-visible="false">sql</th>
        <th data-field="strParameterTypes" data-visible="false">参数类型</th>

        <th data-field="type" data-visible="false">操作类型</th>
        <th data-field="useMultipleKeys" data-visible="false">缓存是否操作多个key</th>
        <th data-field="cacheNullObject" data-visible="false">是否缓存null对象</th>
    </tr>
    </thead>
    <tbody>

    <#list osMap?keys as key>
        <#assign os = osMap[key]>
        <#assign es = esMap[key]>
        <#if (isFetchAll || os.databaseExecuteCount > 0 || os.hitCount > 0)>
            <tr>
                <td>${os.cacheable?string("yes", "no")}</td>

                <td>${es.simpleClassName}</td>
                <td>${es.simpleMethodName}</td>

                <td>${(os.averageDatabaseExecutePenalty / 1000000)?string('0.0')}</td>
                <td>${os.databaseExecuteCount?c}</td>
                <td>${os.databaseExecuteExceptionCount?c}</td>
                <td>${(os.databaseExecuteExceptionRate * 100)?string('0.0')}%</td>

                <td>${os.hitCount?c}</td>
                <td>${os.missCount?c}</td>
                <td>${(os.hitRate * 100)?string('0.0')}%</td>

                <td>${(os.averageCacheGetPenalty / 1000000)?string('0.0')}</td>
                <td>${os.cacheGetCount?c}</td>
                <td>${os.cacheGetExceptionCount?c}</td>
                <td>${(os.cacheGetExceptionRate * 100)?string('0.0')}%</td>

                <td>${(os.averageCacheGetBulkPenalty / 1000000)?string('0.0')}</td>
                <td>${os.cacheGetBulkCount?c}</td>
                <td>${os.cacheGetBulkExceptionCount?c}</td>
                <td>${(os.cacheGetBulkExceptionRate * 100)?string('0.0')}%</td>

                <td>${(os.averageCacheSetPenalty / 1000000)?string('0.0')}</td>
                <td>${os.cacheSetCount?c}</td>
                <td>${os.cacheSetExceptionCount?c}</td>
                <td>${(os.cacheSetExceptionRate * 100)?string('0.0')}%</td>

                <td>${(os.averageCacheAddPenalty / 1000000)?string('0.0')}</td>
                <td>${os.cacheAddCount?c}</td>
                <td>${os.cacheAddExceptionCount?c}</td>
                <td>${(os.cacheAddExceptionRate * 100)?string('0.0')}%</td>

                <td>${(os.averageCacheDeletePenalty / 1000000)?string('0.0')}</td>
                <td>${os.cacheDeleteCount?c}</td>
                <td>${os.cacheDeleteExceptionCount?c}</td>
                <td>${(os.cacheDeleteExceptionRate * 100)?string('0.0')}%</td>

                <td>${(os.averageCacheBatchDeletePenalty / 1000000)?string('0.0')}</td>
                <td>${os.cacheBatchDeleteCount?c}</td>
                <td>${os.cacheBatchDeleteExceptionCount?c}</td>
                <td>${(os.cacheBatchDeleteExceptionRate * 100)?string('0.0')}%</td>

                <td>${(os.averageInitPenalty / 1000000)?string('0.0')}</td>
                <td>${os.initCount?c}</td>

                <td>${es.sql}</td>
                <td><#list es.strParameterTypes as type>${type?replace("<", "&lt;")?replace(">", "&gt;")}  </#list></td>

                <td>${es.type}</td>
                <td>${os.useMultipleKeys?string("yes", "no")}</td>
                <td>${os.cacheNullObject?string("yes", "no")}</td>
            </tr>
        </#if>
    </#list>
    </tbody>
</table>

<script>
    var $table = $('#mango-stats-table');

    $table.on('expand-row.bs.table', function (e, index, row, $detail) {
    });

    function intSorter(s1, s2) {
        var n1 = parseInt(s1);
        var n2 = parseInt(s2);
        if (n1 > n2) return 1;
        if (n1 < n2) return -1;
        return 0;
    }
    function floatSorter(s1, s2) {
        var n1 = parseFloat(s1);
        var n2 = parseFloat(s2);
        if (n1 > n2) return 1;
        if (n1 < n2) return -1;
        return 0;
    }
    function rateSorter(s1, s2) {
        s1 = s1.substring(0, s1.length - 1);
        s2 = s2.substring(0, s2.length - 1);
        var n1 = parseFloat(s1);
        var n2 = parseFloat(s2);
        if (n1 > n2) return 1;
        if (n1 < n2) return -1;
        return 0;
    }
    function detailFormatter(index, row) {
        var html = [];
        html.push('<p><b>SQL:</b>  ' + row['sql'] + '</p>');
        html.push('<p><b>参数类型:</b>  ' + row['strParameterTypes'] + '</p>');

        var isCacheable = row['cacheable'] == 'yes';
        var isUseMultipleKeys = row['useMultipleKeys'] == 'yes';
        var isCacheNullObject = row['cacheNullObject'] == 'yes';
        var isQuery = row['type'] == 'query';
        var isUpdate = row['type'] == 'update';
        var isBatchUpdate = row['type'] == 'batchupdate';

        if (isCacheable) {
            html.push('<br>');

            if (isQuery) {
                html.push('<p><b>cache命中数:</b>  ' + row['hitCount'] + '</p>');
                html.push('<p><b>cache丢失数:</b>  ' + row['missCount'] + '</p>');
                html.push('<p><b>cache命中率:</b>  ' + row['hitRate'] + '</p>');
                html.push('<br>');
            }

            if (isQuery && !isUseMultipleKeys) {
                html.push('<p><b>cache[get]平均速率(毫秒):</b>  ' + row['averageCacheGetPenalty'] + '</p>');
                html.push('<p><b>cache[get]总次数:</b>  ' + row['cacheGetCount'] + '</p>');
                html.push('<p><b>cache[get]失败次数:</b>  ' + row['cacheGetExceptionCount'] + '</p>');
                html.push('<p><b>cache[get]失败率:</b>  ' + row['cacheGetExceptionRate'] + '</p>');
                html.push('<br>');
            }

            if (isQuery && isUseMultipleKeys) {
                html.push('<p><b>cache[getBulk]平均速率(毫秒):</b>  ' + row['averageCacheGetBulkPenalty'] + '</p>');
                html.push('<p><b>cache[getBulk]总次数:</b>  ' + row['cacheGetBulkCount'] + '</p>');
                html.push('<p><b>cache[getBulk]失败次数:</b>  ' + row['cacheGetBulkExceptionCount'] + '</p>');
                html.push('<p><b>cache[getBulk]失败率:</b>  ' + row['cacheGetBulkExceptionRate'] + '</p>');
                html.push('<br>');
            }

            if (isQuery) {
                html.push('<p><b>cache[set]平均速率(毫秒):</b>  ' + row['averageCacheSetPenalty'] + '</p>');
                html.push('<p><b>cache[set]总次数:</b>  ' + row['cacheSetCount'] + '</p>');
                html.push('<p><b>cache[set]失败次数:</b>  ' + row['cacheSetExceptionCount'] + '</p>');
                html.push('<p><b>cache[set]失败率:</b>  ' + row['cacheSetExceptionRate'] + '</p>');
                html.push('<br>');
            }

            if (isQuery && isCacheNullObject) {
                html.push('<p><b>cache[add]平均速率(毫秒):</b>  ' + row['averageCacheAddPenalty'] + '</p>');
                html.push('<p><b>cache[add]总次数:</b>  ' + row['cacheAddCount'] + '</p>');
                html.push('<p><b>cache[add]失败次数:</b>  ' + row['cacheAddExceptionCount'] + '</p>');
                html.push('<p><b>cache[add]失败率:</b>  ' + row['cacheAddExceptionRate'] + '</p>');
                html.push('<br>');
            }

            if (isUpdate && !isUseMultipleKeys) {
                html.push('<p><b>cache[delete]平均速率(毫秒):</b>  ' + row['averageCacheDeletePenalty'] + '</p>');
                html.push('<p><b>cache[delete]总次数:</b>  ' + row['cacheDeleteCount'] + '</p>');
                html.push('<p><b>cache[delete]失败次数:</b>  ' + row['cacheDeleteExceptionCount'] + '</p>');
                html.push('<p><b>cache[delete]失败率:</b>  ' + row['cacheDeleteExceptionRate'] + '</p>');
                html.push('<br>');
            }

            if ((isUpdate && isUseMultipleKeys) || isBatchUpdate) {
                html.push('<p><b>cache[batchDelete]平均速率(毫秒):</b>  ' + row['averageCacheBatchDeletePenalty'] + '</p>');
                html.push('<p><b>cache[batchDelete]总次数:</b>  ' + row['cacheBatchDeleteCount'] + '</p>');
                html.push('<p><b>cache[batchDelete]失败次数:</b>  ' + row['cacheBatchDeleteExceptionCount'] + '</p>');
                html.push('<p><b>cache[batchDelete]失败率:</b>  ' + row['cacheBatchDeleteExceptionRate'] + '</p>');
                html.push('<br>');
            }
        }
        return html.join('');
    }

    $(function() {
        $("#clear-stats").click(function(){
            var action = window.location.pathname + "?type=reset<#if key?exists>&key=${key}</#if>";
            window.location.href = action;
        });
    });
</script>
</body>
</html>