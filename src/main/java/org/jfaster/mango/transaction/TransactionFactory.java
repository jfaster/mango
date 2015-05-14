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
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author ash
 */
public abstract class TransactionFactory {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(TransactionFactory.class);

    public static Transaction doNewTransaction(DataSource dataSource, TransactionIsolationLevel level) {
        if (level == null) {
            new IllegalArgumentException("TransactionIsolationLevel can't be null");
        }
        ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(dataSource);
        return connHolder != null ?
                usingExistingTransaction(dataSource) :
                createNewTransaction(dataSource);
    }

    private static Transaction usingExistingTransaction(DataSource dataSource) {
        if (logger.isDebugEnabled()) {
            logger.debug("Using existing transaction");
        }
        Transaction transaction = new Transaction(false, dataSource);
        return transaction;
    }

    private static Transaction createNewTransaction(DataSource dataSource) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating new transaction");
        }
        Transaction transaction = new Transaction(true, dataSource);
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            ConnectionHolder connHolder = new ConnectionHolder(conn);
            TransactionSynchronizationManager.bindConnectionHolder(dataSource, connHolder);
            return transaction;
        } catch (Throwable e) {
            DataSourceUtils.releaseConnection(conn, dataSource);
            throw new RuntimeException(); // TODO
        }
    }

}
