package org.kopingenieria.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for loading configuration properties from a file.
 * This class provides a method to load key-value pairs from a properties file
 * and returns them as a Properties object.
 */
public class ConfigurationLoader {
    /**
     * Logger instance for recording log messages related to configuration loading.
     * Used to log error messages and other relevant information during the
     * process of loading configuration properties.
     */
    private static final Logger logger = LogManager.getLogger(ConfigurationLoader.class);
    /**
     * Loads properties from a specified file.
     * This method reads a properties file and returns its contents as a Properties object.
     *
     * @param fileName the name of the properties file to be loaded
     * @return a Properties object containing the key-value pairs read from the file
     */
    public static Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        try {
            properties.load(ConfigurationLoader.class.getClassLoader().getResourceAsStream(fileName));
        } catch (IOException | NullPointerException e) {
            logger.error("Error al cargar archivo {}: {}", fileName, e.getMessage(), e);
        }
        return properties;
    }
}
