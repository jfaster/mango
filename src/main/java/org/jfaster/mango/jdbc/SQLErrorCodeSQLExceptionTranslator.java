package org.jfaster.mango.jdbc;

import org.jfaster.mango.jdbc.exception.DataAccessException;
import org.jfaster.mango.jdbc.exception.DuplicateKeyException;

import javax.sql.DataSource;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author ash
 */
public class SQLErrorCodeSQLExceptionTranslator extends AbstractFallbackSQLExceptionTranslator {

    private final SQLErrorCodes sqlErrorCodes;

    public SQLErrorCodeSQLExceptionTranslator(DataSource dataSource) {
        setFallbackTranslator(null);
        this.sqlErrorCodes = SQLErrorCodesFactory.getInstance().getErrorCodes(dataSource);
    }

    @Override
    protected DataAccessException doTranslate(String sql, SQLException ex) {
        SQLException sqlEx = ex;
        if (sqlEx instanceof BatchUpdateException && sqlEx.getNextException() != null) {
            SQLException nestedSqlEx = sqlEx.getNextException();
            if (nestedSqlEx.getErrorCode() > 0 || nestedSqlEx.getSQLState() != null) {
                logger.debug("Using nested SQLException from the BatchUpdateException");
                sqlEx = nestedSqlEx;
            }
        }

        String errorCode;
        if (sqlErrorCodes.isUseSqlStateForTranslation()) {
            errorCode = sqlEx.getSQLState();
        } else {
            // Try to find SQLException with actual error code, looping through the causes.
            // E.g. applicable to java.sql.DataTruncation as of JDK 1.6.
            SQLException current = sqlEx;
            while (current.getErrorCode() == 0 && current.getCause() instanceof SQLException) {
                current = (SQLException) current.getCause();
            }
            errorCode = Integer.toString(current.getErrorCode());
        }

        if (errorCode != null) {
            if (Arrays.binarySearch(sqlErrorCodes.getDuplicateKeyCodes(), errorCode) >= 0) {
                logTranslation(sql, sqlEx);
                return new DuplicateKeyException(buildMessage(sql, sqlEx), sqlEx);
            }
        }

        // We couldn't identify it more precisely - let's hand it over to the SQLState fallback translator.
        if (logger.isDebugEnabled()) {
            String codes;
            if (sqlErrorCodes.isUseSqlStateForTranslation()) {
                codes = "SQL state '" + sqlEx.getSQLState() + "', error code '" + sqlEx.getErrorCode();
            } else {
                codes = "Error code '" + sqlEx.getErrorCode() + "'";
            }
            logger.debug("Unable to translate SQLException with " + codes + ", will now try the fallback translator");
        }

        return null;
    }

    private void logTranslation(String sql, SQLException sqlEx) {
        if (logger.isDebugEnabled()) {
            logger.debug("Translating SQLException with SQL state '" + sqlEx.getSQLState() +
                    "', error code '" + sqlEx.getErrorCode() + "', message [" + sqlEx.getMessage() +
                    "]; SQL was [" + sql + "]");
        }
    }

}
