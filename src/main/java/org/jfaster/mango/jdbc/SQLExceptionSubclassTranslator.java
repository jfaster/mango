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

package org.jfaster.mango.jdbc;

import org.jfaster.mango.jdbc.exception.*;

import java.sql.*;

/**
 * @author ash
 */
public class SQLExceptionSubclassTranslator extends AbstractFallbackSQLExceptionTranslator {

  public SQLExceptionSubclassTranslator() {
    setFallbackTranslator(new SQLStateSQLExceptionTranslator());
  }

  @Override
  protected DataAccessException doTranslate(String sql, SQLException ex) {
    if (ex instanceof SQLTransientException) {
      if (ex instanceof SQLTransactionRollbackException) {
        return new ConcurrencyFailureException(buildMessage(sql, ex), ex);
      }
      if (ex instanceof SQLTransientConnectionException) {
        return new TransientDataAccessResourceException(buildMessage(sql, ex), ex);
      }
      if (ex instanceof SQLTimeoutException) {
        return new QueryTimeoutException(buildMessage(sql, ex), ex);
      }
    } else if (ex instanceof SQLNonTransientException) {
      if (ex instanceof SQLDataException) {
        return new DataIntegrityViolationException(buildMessage(sql, ex), ex);
      } else if (ex instanceof SQLFeatureNotSupportedException) {
        return new InvalidDataAccessApiUsageException(buildMessage(sql, ex), ex);
      } else if (ex instanceof SQLIntegrityConstraintViolationException) {
        return new DataIntegrityViolationException(buildMessage(sql, ex), ex);
      } else if (ex instanceof SQLInvalidAuthorizationSpecException) {
        return new PermissionDeniedDataAccessException(buildMessage(sql, ex), ex);
      } else if (ex instanceof SQLNonTransientConnectionException) {
        return new DataAccessResourceFailureException(buildMessage(sql, ex), ex);
      } else if (ex instanceof SQLSyntaxErrorException) {
        return new BadSqlGrammarException(sql, ex);
      }
    } else if (ex instanceof SQLRecoverableException) {
      return new RecoverableDataAccessException(buildMessage(sql, ex), ex);
    }

    return null;
  }

}
