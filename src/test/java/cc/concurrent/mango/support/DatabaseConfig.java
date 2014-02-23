package cc.concurrent.mango.support;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @author ash
 */
public class DatabaseConfig {

    public static Configuration config;
    static {
        try {
            config = new PropertiesConfiguration("database.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static String getDriverClassName() {
        return config.getString("jdbc.driver");
    }

    public static String getUrl() {
        return config.getString("jdbc.url");
    }

    public static String getUsername() {
        return config.getString("jdbc.username");
    }

    public static String getPassword() {
        return config.getString("jdbc.password");
    }

}
