package org.jfaster.mango.jdbc;

import org.jfaster.mango.jdbc.exception.DataAccessException;
import org.jfaster.mango.jdbc.exception.UncategorizedSQLException;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import java.sql.SQLException;

/**
 * @author ash
 */
public abstract class AbstractFallbackSQLExceptionTranslator implements SQLExceptionTranslator {

    protected final static InternalLogger logger = InternalLoggerFactory.getInstance(AbstractFallbackSQLExceptionTranslator.class);

    private SQLExceptionTranslator fallbackTranslator;

    @Override
    public DataAccessException translate(String sql, SQLException ex) {
        DataAccessException dex = doTranslate(sql, ex);
        if (dex != null) {
            return dex;
        }
        SQLExceptionTranslator fallback = getFallbackTranslator();
        if (fallback != null) {
            return fallback.translate(sql, ex);
        }
        return new UncategorizedSQLException(sql, ex);
    }

    protected abstract DataAccessException doTranslate(String sql, SQLException ex);

    protected String buildMessage(String sql, SQLException ex) {
        return "SQL [" + sql + "]; " + ex.getMessage();
    }

    public SQLExceptionTranslator getFallbackTranslator() {
        return fallbackTranslator;
    }

    public void setFallbackTranslator(SQLExceptionTranslator fallbackTranslator) {
        this.fallbackTranslator = fallbackTranslator;
    }

}
