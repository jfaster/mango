<html>
<head>
    <title>统计信息</title>
    <link href="http://static.jfaster.org/bootstrap-2.3.1/css/bootstrap.min.css" rel="stylesheet" >
    <link href="http://static.jfaster.org/bootstrap-table/bootstrap-table.min.css" rel="stylesheet" >
    <script src="http://static.jfaster.org/jquery.min.js" type="text/javascript" charset="utf-8"></script>
    <script src="http://static.jfaster.org/bootstrap-2.3.1/js/bootstrap.min.js" type="text/javascript" charset="utf-8"></script>
    <script src="http://static.jfaster.org/bootstrap-table/bootstrap-table.min.js" type="text/javascript" charset="utf-8"></script>
</head>
<body>

<div id="custom-toolbar">
    <div class="form-inline" role="form">
        <button id="clear-stats" type="button" class="btn btn-default">清空状态</button>
    </div>
</div>

<table data-toggle="table" data-toolbar="#custom-toolbar"
       data-sort-name="databaseExecuteCount" data-sort-order="desc">
    <thead>
    <tr>
        <th data-sortable="true">类</th>
        <th data-sortable="true">方法</th>

        <th data-sortable="true" data-sorter="floatSorter">db平均速率(毫秒)</th>
        <th data-sortable="true" data-sorter="intSorter" data-field="databaseExecuteCount">db总次数</th>
        <th data-sortable="true" data-sorter="intSorter">db失败次数</th>
        <th data-sortable="true" data-sorter="rateSorter">db失败率</th>

        <th data-sortable="true" data-sorter="intSorter">cache命中数</th>
        <th data-sortable="true" data-sorter="intSorter">cache丢失数</th>
        <th data-sortable="true" data-sorter="rateSorter">cache命中率</th>

        <th data-sortable="true" data-sorter="floatSorter">cache[get]平均速率(毫秒)</th>
        <th data-sortable="true" data-sorter="intSorter">cache[get]总次数</th>
        <th data-sortable="true" data-sorter="intSorter">cache[get]失败次数</th>
        <th data-sortable="true" data-sorter="rateSorter">cache[get]失败率</th>

        <th data-sortable="true" data-sorter="floatSorter">cache[getBulk]平均速率(毫秒)</th>
        <th data-sortable="true" data-sorter="intSorter">cache[getBulk]总次数</th>
        <th data-sortable="true" data-sorter="intSorter">cache[getBulk]失败次数</th>
        <th data-sortable="true" data-sorter="rateSorter">cache[getBulk]失败率</th>

        <th data-sortable="true" data-sorter="floatSorter">cache[set]平均速率(毫秒)</th>
        <th data-sortable="true" data-sorter="intSorter">cache[set]总次数</th>
        <th data-sortable="true" data-sorter="intSorter">cache[set]失败次数</th>
        <th data-sortable="true" data-sorter="rateSorter">cache[set]失败率</th>

        <th data-sortable="true" data-sorter="floatSorter">cache[add]平均速率(毫秒)</th>
        <th data-sortable="true" data-sorter="intSorter">cache[add]总次数</th>
        <th data-sortable="true" data-sorter="intSorter">cache[add]失败次数</th>
        <th data-sortable="true" data-sorter="rateSorter">cache[add]失败率</th>

        <th data-sortable="true" data-sorter="floatSorter">cache[delete]平均速率(毫秒)</th>
        <th data-sortable="true" data-sorter="intSorter">cache[delete]总次数</th>
        <th data-sortable="true" data-sorter="intSorter">cache[delete]失败次数</th>
        <th data-sortable="true" data-sorter="rateSorter">cache[delete]失败率</th>

        <th data-sortable="true" data-sorter="floatSorter">cache[batchDelete]平均速率(毫秒)</th>
        <th data-sortable="true" data-sorter="intSorter">cache[batchDelete]总次数</th>
        <th data-sortable="true" data-sorter="intSorter">cache[batchDelete]失败次数</th>
        <th data-sortable="true" data-sorter="rateSorter">cache[batchDelete]失败率</th>

        <th data-sortable="true" data-sorter="floatSorter">init速率(毫秒)</th>
        <th data-sortable="true" data-sorter="intSorter">init次数</th>
    </tr>
    </thead>
    <tbody>

    <#list mango.allStats as stats>
        <#if (isFetchAll || stats.databaseExecuteCount > 0 || stats.hitCount > 0)>
            <tr>
                <td>${stats.classSimpleName}</td>
                <td>${stats.methodNameWithParameterNum}</td>

                <td>${(stats.averageDatabaseExecutePenalty / 1000000)?string('0.0')}</td>
                <td>${stats.databaseExecuteCount?c}</td>
                <td>${stats.databaseExecuteExceptionCount?c}</td>
                <td>${(stats.databaseExecuteExceptionRate * 100)?string('0.0')}%</td>

                <td>${stats.hitCount?c}</td>
                <td>${stats.missCount?c}</td>
                <td>${(stats.hitRate * 100)?string('0.0')}%</td>

                <td>${(stats.averageCacheGetPenalty / 1000000)?string('0.0')}</td>
                <td>${stats.cacheGetCount?c}</td>
                <td>${stats.cacheGetExceptionCount?c}</td>
                <td>${(stats.cacheGetExceptionRate * 100)?string('0.0')}%</td>

                <td>${(stats.averageCacheGetBulkPenalty / 1000000)?string('0.0')}</td>
                <td>${stats.cacheGetBulkCount?c}</td>
                <td>${stats.cacheGetBulkExceptionCount?c}</td>
                <td>${(stats.cacheGetBulkExceptionRate * 100)?string('0.0')}%</td>

                <td>${(stats.averageCacheSetPenalty / 1000000)?string('0.0')}</td>
                <td>${stats.cacheSetCount?c}</td>
                <td>${stats.cacheSetExceptionCount?c}</td>
                <td>${(stats.cacheSetExceptionRate * 100)?string('0.0')}%</td>

                <td>${(stats.averageCacheAddPenalty / 1000000)?string('0.0')}</td>
                <td>${stats.cacheAddCount?c}</td>
                <td>${stats.cacheAddExceptionCount?c}</td>
                <td>${(stats.cacheAddExceptionRate * 100)?string('0.0')}%</td>

                <td>${(stats.averageCacheDeletePenalty / 1000000)?string('0.0')}</td>
                <td>${stats.cacheDeleteCount?c}</td>
                <td>${stats.cacheDeleteExceptionCount?c}</td>
                <td>${(stats.cacheDeleteExceptionRate * 100)?string('0.0')}%</td>

                <td>${(stats.averageCacheBatchDeletePenalty / 1000000)?string('0.0')}</td>
                <td>${stats.cacheBatchDeleteCount?c}</td>
                <td>${stats.cacheBatchDeleteExceptionCount?c}</td>
                <td>${(stats.cacheBatchDeleteExceptionRate * 100)?string('0.0')}%</td>

                <td>${(stats.averageInitPenalty / 1000000)?string('0.0')}</td>
                <td>${stats.initCount?c}</td>
            </tr>
        </#if>
    </#list>
    </tbody>
</table>

<script>
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
    $(function() {
        $("#clear-stats").click(function(){
            var action = window.location.pathname + "?type=reset<#if key?exists>&key=${key}</#if>";
            window.location.href = action;
        });
    });
</script>
</body>
</html>