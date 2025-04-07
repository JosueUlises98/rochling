package org.kopingenieria.util.helper;

public class ConfigurationHelper {

    private static long version = 0;

    public static long getNextVersion() {
        ConfigurationHelper.version++;
        return ConfigurationHelper.version;
    }
}
