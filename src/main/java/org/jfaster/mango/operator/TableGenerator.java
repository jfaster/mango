package org.jfaster.mango.operator;

import org.jfaster.mango.invoker.GetterInvoker;
import org.jfaster.mango.partition.TablePartition;

/**
 * @author ash
 */
public class TableGenerator {

    private String table; // 原始表名称
    private String shardParameterName;
    private GetterInvoker invoker;
    private TablePartition tablePartition; // 分表

    public TableGenerator(String table, String shardParameterName,
                          GetterInvoker invoker, TablePartition tablePartition) {
        this.table = table;
        this.shardParameterName = shardParameterName;
        this.invoker = invoker;
        this.tablePartition = tablePartition;
    }

    public String getTable(InvocationContext context) {
        String realTable;
        if (!needTablePartition()) {
            realTable = table;
        } else {
            Object shardParam = context.getPropertyValue(shardParameterName, invoker);
            realTable = tablePartition.getPartitionedTable(table, shardParam);
        }
        return realTable;
    }

    boolean needTablePartition() {
        return tablePartition != null && shardParameterName != null;
    }

}
