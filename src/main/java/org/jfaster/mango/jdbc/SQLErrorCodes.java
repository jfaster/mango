package org.jfaster.mango.jdbc;

/**
 * @author ash
 */
public enum SQLErrorCodes {

    EMPTY {
        @Override
        void init() {
        }
    },

    HSQL {
        @Override
        void init() {
            setDatabaseProductName("HSQL Database Engine");
            setBadSqlGrammarCodes(parse("-22,-28"));
            setDuplicateKeyCodes(parse("-104"));
            setDataIntegrityViolationCodes(parse("-9"));
            setDataAccessResourceFailureCodes(parse("-80"));
        }
    },

    MySQL {
        @Override
        void init() {
            setBadSqlGrammarCodes(parse("1054,1064,1146"));
            setDuplicateKeyCodes(parse("1062"));
            setDataIntegrityViolationCodes(parse("630,839,840,893,1169,1215,1216,1217,1364,1451,1452,1557"));
            setDataAccessResourceFailureCodes(parse("1"));
            setCannotAcquireLockCodes(parse("1205"));
            setDeadlockLoserCodes(parse("1213"));
        }
    }
    ;

    private String[] databaseProductNames = new String[0];

    private boolean useSqlStateForTranslation = false;

    private String[] badSqlGrammarCodes = new String[0];

    private String[] invalidResultSetAccessCodes = new String[0];

    private String[] duplicateKeyCodes = new String[0];

    private String[] dataIntegrityViolationCodes = new String[0];

    private String[] permissionDeniedCodes = new String[0];

    private String[] dataAccessResourceFailureCodes = new String[0];

    private String[] transientDataAccessResourceCodes = new String[0];

    private String[] cannotAcquireLockCodes = new String[0];

    private String[] deadlockLoserCodes = new String[0];

    private String[] cannotSerializeTransactionCodes = new String[0];

    public String getDatabaseProductName() {
        return (databaseProductNames != null && databaseProductNames.length > 0 ?
                databaseProductNames[0] : null);
    }

    public void setDatabaseProductName(String databaseProductName) {
        this.databaseProductNames = new String[] {databaseProductName};
    }

    public String[] getDatabaseProductNames() {
        return databaseProductNames;
    }

    public void setDatabaseProductNames(String[] databaseProductNames) {
        this.databaseProductNames = databaseProductNames;
    }

    public boolean isUseSqlStateForTranslation() {
        return useSqlStateForTranslation;
    }

    public void setUseSqlStateForTranslation(boolean useSqlStateForTranslation) {
        this.useSqlStateForTranslation = useSqlStateForTranslation;
    }

    public String[] getBadSqlGrammarCodes() {
        return badSqlGrammarCodes;
    }

    public void setBadSqlGrammarCodes(String[] badSqlGrammarCodes) {
        this.badSqlGrammarCodes = badSqlGrammarCodes;
    }

    public String[] getInvalidResultSetAccessCodes() {
        return invalidResultSetAccessCodes;
    }

    public void setInvalidResultSetAccessCodes(String[] invalidResultSetAccessCodes) {
        this.invalidResultSetAccessCodes = invalidResultSetAccessCodes;
    }

    public String[] getDuplicateKeyCodes() {
        return duplicateKeyCodes;
    }

    public void setDuplicateKeyCodes(String[] duplicateKeyCodes) {
        this.duplicateKeyCodes = duplicateKeyCodes;
    }

    public String[] getDataIntegrityViolationCodes() {
        return dataIntegrityViolationCodes;
    }

    public void setDataIntegrityViolationCodes(String[] dataIntegrityViolationCodes) {
        this.dataIntegrityViolationCodes = dataIntegrityViolationCodes;
    }

    public String[] getPermissionDeniedCodes() {
        return permissionDeniedCodes;
    }

    public void setPermissionDeniedCodes(String[] permissionDeniedCodes) {
        this.permissionDeniedCodes = permissionDeniedCodes;
    }

    public String[] getDataAccessResourceFailureCodes() {
        return dataAccessResourceFailureCodes;
    }

    public void setDataAccessResourceFailureCodes(String[] dataAccessResourceFailureCodes) {
        this.dataAccessResourceFailureCodes = dataAccessResourceFailureCodes;
    }

    public String[] getTransientDataAccessResourceCodes() {
        return transientDataAccessResourceCodes;
    }

    public void setTransientDataAccessResourceCodes(String[] transientDataAccessResourceCodes) {
        this.transientDataAccessResourceCodes = transientDataAccessResourceCodes;
    }

    public String[] getCannotAcquireLockCodes() {
        return cannotAcquireLockCodes;
    }

    public void setCannotAcquireLockCodes(String[] cannotAcquireLockCodes) {
        this.cannotAcquireLockCodes = cannotAcquireLockCodes;
    }

    public String[] getDeadlockLoserCodes() {
        return deadlockLoserCodes;
    }

    public void setDeadlockLoserCodes(String[] deadlockLoserCodes) {
        this.deadlockLoserCodes = deadlockLoserCodes;
    }

    public String[] getCannotSerializeTransactionCodes() {
        return cannotSerializeTransactionCodes;
    }

    public void setCannotSerializeTransactionCodes(String[] cannotSerializeTransactionCodes) {
        this.cannotSerializeTransactionCodes = cannotSerializeTransactionCodes;
    }

    abstract void init();

    private static String[] parse(String codes) {
        return codes.split(",");
    }

}
