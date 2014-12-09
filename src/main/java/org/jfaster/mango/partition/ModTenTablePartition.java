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

package org.jfaster.mango.partition;

/**
 * 模十分表
 *
 * @author ash
 */
public class ModTenTablePartition implements TablePartition {

    @Override
    public String getPartitionedTable(String table, Object shardParam) {
        int mod;
        if (shardParam instanceof Integer) {
            mod = ((Integer) shardParam) % 10;
        } else if (shardParam instanceof Long) {
            mod = (int) (((Long) shardParam) % 10);
        } else if (shardParam instanceof String) {
            try {
                mod = (int) (Long.parseLong((String) shardParam) % 10);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("can't convert shard parameter [" + shardParam + "] to digital");
            }
        } else {
            throw new IllegalArgumentException("shard parameter need int or Integer or long or Long but "
                    + shardParam.getClass());
        }
        return table + "_" + mod;
    }

}
