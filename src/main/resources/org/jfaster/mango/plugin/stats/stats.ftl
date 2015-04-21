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

<ul id="myTab" class="nav nav-tabs">
    <#list mangos as mango>
        <li <#if (mango_index == 0)>class="active"</#if>>
            <a href="#mango-${mango_index}" data-toggle="tab">mango-${mango_index}</a>
        </li>
    </#list>
</ul>


<div id="myTabContent" class="tab-content">
    <#list mangos as mango>
        <div class="tab-pane fade <#if (mango_index == 0)>in active</#if>" id="mango-${mango_index}">
            <table data-toggle="table" data-show-columns="true"
                   data-sort-name="executeCount" data-sort-order="desc">
                <thead>
                <tr>
                    <th data-sortable="true">类</th>
                    <th data-sortable="true">方法</th>
                    <th data-visible="false" data-sortable="true" data-sorter="intSorter">cache命中数</th>
                    <th data-visible="false" data-sortable="true" data-sorter="intSorter">cache丢失数</th>
                    <th data-visible="false" data-sortable="true" data-sorter="rateSorter">cache命中率</th>
                    <th data-visible="false" data-sortable="true" data-sorter="intSorter">cache剔除数量</th>
                    <th data-switchable="false" data-sortable="true" data-sorter="intSorter">db平均速率(毫秒)</th>
                    <th data-field="executeCount" data-sortable="true" data-sorter="intSorter" data-switchable="false">db总次数</th>
                    <th data-switchable="false" data-sortable="true" data-sorter="intSorter">db失败次数</th>
                    <th data-switchable="false" data-sortable="true" data-sorter="rateSorter">db失败率</th>
                    <th data-visible="false" data-sortable="true" data-sorter="intSorter">init速率(毫秒)</th>
                    <th data-visible="false" data-sortable="true" data-sorter="intSorter">init次数</th>
                </tr>
                </thead>
                <tbody>

                <#list mango.allStats as stats>
                    <#if (isFetchAll || stats.executeCount > 0)>
                        <tr>
                            <td>${stats.classSimpleName}</td>
                            <td>${stats.methodName}</td>
                            <td>${stats.hitCount}</td>
                            <td>${stats.missCount}</td>
                            <td>${(stats.hitRate * 100)?string('0.0')}%</td>
                            <td>${stats.evictionCount}</td>
                            <td>${(stats.averageExecutePenalty / 1000000)?string('0.0')}</td>
                            <td>${stats.executeCount}</td>
                            <td>${stats.executeExceptionCount}</td>
                            <td>${(stats.executeExceptionRate * 100)?string('0.0')}%</td>
                            <td>${(stats.averageInitPenalty / 1000000)?string('0.0')}</td>
                            <td>${stats.initCount}</td>
                        </tr>
                    </#if>
                </#list>
                </tbody>
            </table>
        </div>
    </#list>
</div>
<script>
    function intSorter(s1, s2) {
        var n1 = parseInt(s1);
        var n2 = parseInt(s2);
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
</script>
</body>
</html>