package org.kopingenieria.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LoggingUtils {

    public static String sanitizeParams(Object... params) {
        if (params == null) return "null";

        return Arrays.stream(params)
                .map(LoggingUtils::sanitizeValue)
                .collect(Collectors.joining(", "));
    }

    private static String sanitizeValue(Object value) {
        if (value == null) return "null";
        // Implementar lógica de sanitización según necesidades
        return value.toString();
    }
}
