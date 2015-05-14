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

import org.jfaster.mango.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author ash
 */
public class Transaction {

    private final boolean newTransaction;

    private final DataSource dataSource;

    private boolean completed;

    public Transaction(boolean newTransaction, DataSource dataSource) {
        this.newTransaction = newTransaction;
        this.dataSource = dataSource;
    }

    public void commit() {
        if (isCompleted()) {
            throw new RuntimeException(); // TODO
        }

        ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(dataSource);
        if (connHolder == null) {
            throw new IllegalStateException(); // TODO
        }

        if (!isNewTransaction()) { // 嵌套的事务，不真正提交
            return;
        }

        if (connHolder.isRollbackOnly()) { // 嵌套的事务出现回滚则回滚
            processRollback(connHolder.getConnection(), dataSource);
        }

        // 提交事务
        processCommit(connHolder.getConnection(), dataSource);
    }

    private void processCommit(Connection conn, DataSource dataSource) {
        try {
            doCommit(conn);
        } catch (Exception e) {
            doRollbackOnCommitException(conn, e);
        } finally {
            TransactionSynchronizationManager.unbindConnectionHolder(dataSource);
            DataSourceUtils.resetConnectionAfterTransaction(conn, dataSource, null); // TODO
            DataSourceUtils.releaseConnection(conn, dataSource);
            setCompleted(true);
        }
    }

    private void doCommit(Connection conn) {
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(); // TODO
        }
    }

    private void doRollbackOnCommitException(Connection conn, Exception ce) {
        try {
            doRollback(conn);
        } catch (Exception re) {
            throw new RuntimeException(); // TODO
        }
    }

    public void rollback() {
        if (isCompleted()) {
            throw new RuntimeException(); // TODO
        }

        ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(dataSource);
        if (connHolder == null) {
            throw new IllegalStateException(); // TODO
        }

        if (!isNewTransaction()) {
            connHolder.setRollbackOnly(true); // 促发顶层事务回滚
            return;
        }

        processRollback(connHolder.getConnection(), dataSource);
    }

    private void processRollback(Connection conn, DataSource dataSource) {
        try {
            doRollback(conn);
        } catch (Exception e) {
            throw new RuntimeException(); // TODO
        } finally {
            TransactionSynchronizationManager.unbindConnectionHolder(dataSource);
            DataSourceUtils.resetConnectionAfterTransaction(conn, dataSource, null); // TODO
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void doRollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(); // TODO
        }
    }

    public boolean isNewTransaction() {
        return newTransaction;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private boolean isCompleted() {
        return completed;
    }

    private void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
