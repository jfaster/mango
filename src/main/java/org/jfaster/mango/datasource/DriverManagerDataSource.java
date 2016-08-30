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

package org.jfaster.mango.datasource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 用于测试的数据库连接池，每次getConnection()都会返回一个新的连接
 *
 * @author ash
 */
public class DriverManagerDataSource implements DataSource {

  private String driverClassName;
  private String url;
  private String username;
  private String password;

  public DriverManagerDataSource() {
  }

  public DriverManagerDataSource(String driverClassName, String url, String username, String password) {
    String driverClassNameToUse = driverClassName.trim();
    try {
      Class.forName(driverClassNameToUse);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Could not load JDBC driver class [" + driverClassNameToUse + "]", e);
    }
    this.driverClassName = driverClassNameToUse;
    this.url = url;
    this.username = username;
    this.password = password;
  }

  @Override
  public Connection getConnection() throws SQLException {
    Properties props = new Properties();
    props.setProperty("user", username);
    props.setProperty("password", password);
    return DriverManager.getConnection(url, props);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    throw new UnsupportedOperationException();
  }

  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public void setDriverClassName(String driverClassName) {
    String driverClassNameToUse = driverClassName.trim();
    try {
      Class.forName(driverClassNameToUse);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Could not load JDBC driver class [" + driverClassNameToUse + "]", e);
    }
    this.driverClassName = driverClassNameToUse;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
