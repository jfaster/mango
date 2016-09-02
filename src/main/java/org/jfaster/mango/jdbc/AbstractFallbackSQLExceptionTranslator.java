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
