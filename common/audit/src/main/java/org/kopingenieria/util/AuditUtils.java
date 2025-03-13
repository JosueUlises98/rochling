package org.kopingenieria.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Optional;

public class AuditUtils {

    private AuditUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String obtenerIpCliente() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(AuditUtils::extractIpAddress)
                .orElse("UNKNOWN");
    }

    public static String extractIpAddress(HttpServletRequest request) {
        String[] headersToCheck = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headersToCheck) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
