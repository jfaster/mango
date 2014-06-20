/*
 * Copyright 2014 mango.concurrent.cc
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

package cc.concurrent.mango;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import javax.sql.DataSource;

/**
 * @author ash
 */
public class Config {

    private static String DIR = "hsqldb";
    private static Configuration CONFIG;

    static {
        try {
            CONFIG = new PropertiesConfiguration(DIR + "/database.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static String getDir() {
        return DIR;
    }

    public static DataSource getDataSource() {
        return new DriverManagerDataSource(Config.getDriverClassName(), Config.getUrl(),
                Config.getUsername(), Config.getPassword());
    }


    private static String getDriverClassName() {
        return CONFIG.getString("jdbc.driver");
    }

    private static String getUrl() {
        return CONFIG.getString("jdbc.url");
    }

    private static String getUsername() {
        return CONFIG.getString("jdbc.username");
    }

    private static String getPassword() {
        return CONFIG.getString("jdbc.password");
    }

}
