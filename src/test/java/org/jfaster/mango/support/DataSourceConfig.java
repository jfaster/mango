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

package org.jfaster.mango.support;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

/**
 * @author ash
 */
public class DataSourceConfig {

  private static String DIR = "hsqldb";
  private static Configuration[] CONFIGS;

  static {
    try {
      CONFIGS = new Configuration[5];
      CONFIGS[0] = new PropertiesConfiguration(DIR + "/database.properties");
      CONFIGS[1] = new PropertiesConfiguration(DIR + "/database1.properties");
      CONFIGS[2] = new PropertiesConfiguration(DIR + "/database2.properties");
      CONFIGS[3] = new PropertiesConfiguration(DIR + "/database3.properties");
      CONFIGS[4] = new PropertiesConfiguration(DIR + "/database4.properties");
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
  }

  public static boolean isUseMySQL() {
    return "mysql".equals(DIR);
  }

  public static String getDir() {
    return DIR;
  }

  public static DataSource getDataSource() {
    return getDataSource(0, true, 1);
  }

  public static DataSource getDataSource(int i) {
    return getDataSource(i, true, 1);
  }

  public static DataSource getDataSource(int i, boolean autoCommit, int maxActive) {
    String driverClassName = getDriverClassName(i);
    String url = getUrl(i);
    String username = getUsername(i);
    String password = getPassword(i);

    BasicDataSource ds = new BasicDataSource();
    ds.setUrl(url);
    ds.setUsername(username);
    ds.setPassword(password);
    ds.setInitialSize(1);
    ds.setMaxActive(maxActive);
    ds.setDriverClassName(driverClassName);
    ds.setDefaultAutoCommit(autoCommit);
    return ds;
  }


  private static String getDriverClassName(int i) {
    return CONFIGS[i].getString("jdbc.driver");
  }

  private static String getUrl(int i) {
    return CONFIGS[i].getString("jdbc.url");
  }

  private static String getUsername(int i) {
    return CONFIGS[i].getString("jdbc.username");
  }

  private static String getPassword(int i) {
    return CONFIGS[i].getString("jdbc.password");
  }

}
