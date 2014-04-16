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
