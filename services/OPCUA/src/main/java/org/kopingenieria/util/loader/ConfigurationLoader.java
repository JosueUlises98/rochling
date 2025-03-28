package org.kopingenieria.util.loader;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

@Component("configurationLoader")
public class ConfigurationLoader {

    public static Properties loadProperties(String fileName) throws IOException {
        Properties properties = new Properties();
        properties.load(ConfigurationLoader.class.getClassLoader().getResourceAsStream(fileName));
        return properties;
    }
}
