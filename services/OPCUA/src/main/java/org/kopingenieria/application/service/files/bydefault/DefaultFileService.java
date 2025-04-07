package org.kopingenieria.application.service.files.bydefault;

import lombok.AllArgsConstructor;
import org.kopingenieria.application.service.files.component.DefaultConfigFile;
import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.kopingenieria.exception.exceptions.ConfigurationException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class DefaultFileService {

    private final DefaultConfigFile configFile;

    public void initializeConfiguration() {
        try {
            configFile.init();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public DefaultConfiguration getConfiguration(String fileName) throws ConfigurationException {
        return configFile.loadConfiguration(fileName);
    }

    public void saveConfiguration(DefaultConfiguration configuration, String fileName)
            throws ConfigurationException {
        configFile.saveConfiguration(configuration, fileName);
    }

    public void deleteConfiguration(String fileName) throws ConfigurationException {
        configFile.deleteConfiguration(fileName);
    }

    public List<String> listConfigurations() throws ConfigurationException {
        return configFile.listConfigurations();
    }
}
