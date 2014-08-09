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

import org.jfaster.mango.DriverManagerDataSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import javax.sql.DataSource;

/**
 * @author ash
 */
public class Config {

    private static String DIR = "hsqldb";
    private static Configuration[] CONFIGS;

    static {
        try {
            CONFIGS = new Configuration[4];
            CONFIGS[0] = new PropertiesConfiguration(DIR + "/database.properties");
            CONFIGS[1] = new PropertiesConfiguration(DIR + "/database1.properties");
            CONFIGS[2] = new PropertiesConfiguration(DIR + "/database2.properties");
            CONFIGS[3] = new PropertiesConfiguration(DIR + "/database3.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static String getDir() {
        return DIR;
    }

    public static DataSource getDataSource() {
        return getDataSource(0);
    }

    public static DataSource getDataSource(int i) {
        return new DriverManagerDataSource(getDriverClassName(i), getUrl(i), getUsername(i), getPassword(i));
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
