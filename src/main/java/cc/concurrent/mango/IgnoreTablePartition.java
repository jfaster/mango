package cc.concurrent.mango;

import cc.concurrent.mango.exception.UnreachableCodeException;

/**
 * {@link DB#tablePartition()}的默认值，表示不使用分表
 *
 * @author ash
 */
public final class IgnoreTablePartition implements TablePartition {

    @Override
    public String getPartitionedTable(String table, Object shardByParam) {
        throw new UnreachableCodeException();
    }

}
