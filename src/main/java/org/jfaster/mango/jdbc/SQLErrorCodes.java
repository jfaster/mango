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

/**
 * @author ash
 */
public enum SQLErrorCodes {

  EMPTY {
    @Override
    void init() {
    }
  },

  DB2 {
    @Override
    void init() {
      setDatabaseProductName("DB2*");
      setBadSqlGrammarCodes(parse("-007,-029,-097,-104,-109,-115,-128,-199,-204,-206,-301,-408,-441,-491"));
      setDuplicateKeyCodes(parse("-803"));
      setDataIntegrityViolationCodes(parse("-407,-530,-531,-532,-543,-544,-545,-603,-667"));
      setDataAccessResourceFailureCodes(parse("-904,-971"));
      setTransientDataAccessResourceCodes(parse("-1035,-1218,-30080,-30081"));
      setDeadlockLoserCodes(parse("-911,-913"));
    }
  },

  Derby {
    @Override
    void init() {
      setDatabaseProductName("Apache Derby");
      setUseSqlStateForTranslation(true);
      setBadSqlGrammarCodes(parse("42802,42821,42X01,42X02,42X03,42X04,42X05,42X06,42X07,42X08"));
      setDuplicateKeyCodes(parse("23505"));
      setDataIntegrityViolationCodes(parse("22001,22005,23502,23503,23513,X0Y32"));
      setDataAccessResourceFailureCodes(parse("04501,08004,42Y07"));
      setCannotAcquireLockCodes(parse("40XL1"));
      setDeadlockLoserCodes(parse("40001"));
    }
  },

  H2 {
    @Override
    void init() {
      setBadSqlGrammarCodes(parse("42000,42001,42101,42102,42111,42112,42121,42122,42132"));
      setDuplicateKeyCodes(parse("23001,23505"));
      setDataIntegrityViolationCodes(parse("22001,22003,22012,22018,22025,23000,23002,23003,23502,23503,23506,23507,23513"));
      setDataAccessResourceFailureCodes(parse("90046,90100,90117,90121,90126"));
      setCannotAcquireLockCodes(parse("50200"));
    }
  },

  Informix {
    @Override
    void init() {
      setDatabaseProductName("Informix Dynamic Server");
      setBadSqlGrammarCodes(parse("-201,-217,-696"));
      setDuplicateKeyCodes(parse("-239,-268,-6017"));
      setDataIntegrityViolationCodes(parse("-692,-11030"));
    }
  },

  MSSQL {
    @Override
    void init() {
      setDatabaseProductNames(new String[]{
          "MS-SQL", "Microsoft SQL Server"
      });
      setBadSqlGrammarCodes(parse("156,170,207,208,209"));
      setPermissionDeniedCodes(parse("229"));
      setDuplicateKeyCodes(parse("2601,2627"));
      setDataIntegrityViolationCodes(parse("544,8114,8115"));
      setDataAccessResourceFailureCodes(parse("4060"));
      setCannotAcquireLockCodes(parse("1222"));
      setDeadlockLoserCodes(parse("1205"));
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
  },

  Oracle {
    @Override
    void init() {
      setBadSqlGrammarCodes(parse("900,903,904,917,936,942,17006,6550"));
      setInvalidResultSetAccessCodes(parse("17003"));
      setDuplicateKeyCodes(parse("1"));
      setDataIntegrityViolationCodes(parse("1400,1722,2291,2292"));
      setDataAccessResourceFailureCodes(parse("17002,17447"));
      setCannotAcquireLockCodes(parse("54,30006"));
      setCannotSerializeTransactionCodes(parse("8177"));
      setDeadlockLoserCodes(parse("60"));
    }
  },

  PostgreSQL {
    @Override
    void init() {
      setUseSqlStateForTranslation(true);
      setBadSqlGrammarCodes(parse("03000,42000,42601,42602,42622,42804,42P01"));
      setDuplicateKeyCodes(parse("23505"));
      setDataIntegrityViolationCodes(parse("23000,23502,23503,23514"));
      setDataAccessResourceFailureCodes(parse("53000,53100,53200,53300"));
      setCannotAcquireLockCodes(parse("55P03"));
      setCannotSerializeTransactionCodes(parse("40001"));
      setDeadlockLoserCodes(parse("40P01"));
    }
  },

  Sybase {
    @Override
    void init() {
      setDatabaseProductNames(new String[]{
          "Sybase SQL Server",
          "SQL Server",
          "Adaptive Server Enterprise",
          "ASE",
          "sql server"
      });
      setBadSqlGrammarCodes(parse("101,102,103,104,105,106,107,108,109,110,111,112,113,116,120,121,123,207,208,213,257,512"));
      setDuplicateKeyCodes(parse("2601,2615,2626"));
      setDataIntegrityViolationCodes(parse("233,511,515,530,546,547,2615,2714"));
      setTransientDataAccessResourceCodes(parse("921,1105"));
      setCannotAcquireLockCodes(parse("12205"));
      setDeadlockLoserCodes(parse("1205"));
    }
  },

  Hana {
    @Override
    void init() {
      setDatabaseProductName("SAP DB");
      setBadSqlGrammarCodes(parse("257,259,260,261,262,263,264,267,268,269,270,271,272,273,275,276,277,278,278,279,280,281,282,283,284,285,286,288,289,290,294,295,296,297,299,308,309,313,315,316,318,319,320,321,322,323,324,328,329,330,333,335,336,337,338,340,343,350,351,352,362,368"));
      setPermissionDeniedCodes(parse("10,258"));
      setDuplicateKeyCodes(parse("301"));
      setDataIntegrityViolationCodes(parse("461,462"));
      setDataAccessResourceFailureCodes(parse("-813,-709,-708,1024,1025,1026,1027,1029,1030,1031"));
      setInvalidResultSetAccessCodes(parse("-11210,582,587,588,594"));
      setCannotAcquireLockCodes(parse("131"));
      setCannotSerializeTransactionCodes(parse("138,143"));
      setDeadlockLoserCodes(parse("133"));
    }
  },;

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
    this.databaseProductNames = new String[]{databaseProductName};
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
