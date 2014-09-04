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

package org.jfaster.mango.transaction;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author ash
 */
public class TransactionContext {

    private final TransactionIsolationLevel level;
    private DataSource dataSource;
    private Connection connection;
    private Integer previousLevel; // nul表示level不用变

    public TransactionContext(TransactionIsolationLevel level) {
        this.level = level;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setPreviousLevel(Integer previousLevel) {
        this.previousLevel = previousLevel;
    }

    public TransactionIsolationLevel getLevel() {
        return level;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() {
        return connection;
    }

    public Integer getPreviousLevel() {
        return previousLevel;
    }

}
