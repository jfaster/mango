package cc.concurrent.mango;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

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

    public static String getDriverClassName() {
        return CONFIG.getString("jdbc.driver");
    }

    public static String getUrl() {
        return CONFIG.getString("jdbc.url");
    }

    public static String getUsername() {
        return CONFIG.getString("jdbc.username");
    }

    public static String getPassword() {
        return CONFIG.getString("jdbc.password");
    }

}
