package org.kopingenieria.application.service.files.user;

import lombok.AllArgsConstructor;
import org.kopingenieria.application.service.files.component.UserConfigFile;
import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.kopingenieria.exception.exceptions.ConfigurationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserFileService {

    private final UserConfigFile userConfigFile;

    public void initializeSystem() {
        try {
            userConfigFile.init();
        } catch (ConfigurationException e) {
            throw new RuntimeException("Initialization error", e);
        }
    }

    public UserConfiguration getConfiguration(String fileName) {
        try {
            return userConfigFile.loadConfiguration(fileName);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    public void createConfiguration(UserConfiguration configuration, String fileName) {
        try {
            userConfigFile.saveConfiguration(configuration, fileName);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Error creating configuration", e);
        }
    }

    public void updateConfiguration(UserConfiguration configuration, String fileName) {
        try {
            userConfigFile.updateConfiguration(configuration, fileName);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Error updating configuration", e);
        }
    }

    public void deleteConfiguration(String fileName) {
        try {
            userConfigFile.deleteConfiguration(fileName);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Error deleting configuration", e);
        }
    }

    public List<String> listConfigurations() {
        try {
            return userConfigFile.listConfigurations();
        } catch (ConfigurationException e) {
            throw new RuntimeException("Error listing configurations", e);
        }
    }

    public boolean configurationExists(String fileName) {
        return listConfigurations().contains(fileName);
    }

    public void saveConfiguration(UserConfiguration configuration, String fileName) {
        try {
            if (configurationExists(fileName)) {
                updateConfiguration(configuration, fileName);
            } else {
                createConfiguration(configuration, fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving configuration", e);
        }
    }
}
