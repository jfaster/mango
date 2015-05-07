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

        // Fallback to Spring's own SQL state translation...
        return null;
    }

}
