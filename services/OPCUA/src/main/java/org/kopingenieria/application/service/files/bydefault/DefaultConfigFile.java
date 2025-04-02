package org.kopingenieria.application.service.files.bydefault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.kopingenieria.exception.exceptions.ConfigurationException;
import org.kopingenieria.logging.model.LogException;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogSystemEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class DefaultConfigFile {

    private static final String CONFIG_PATH = "src/main/resources";
    private static final String BACKUP_PATH = CONFIG_PATH + "backup/";
    private final ObjectMapper objectMapper;
    private Properties props;

    @Auditable(value = "Evento de inicializacion de directorios", type = AuditEntryType.CREATE, description = "Inicializacion de directorios en ruta definida")
    @LogSystemEvent(event = "Inicializacion de directorios",
            description = "Inicializando directorios en ruta definida",
            level = LogLevel.INFO)
    @LogException(message = "Error en la inicializacion de directorios", method = "init", component = "ConfigurationService")
    public void init() throws ConfigurationException {
        try {
            Files.createDirectories(Paths.get(CONFIG_PATH));
            Files.createDirectories(Paths.get(BACKUP_PATH));
        } catch (IOException e) {
            throw new ConfigurationException("Error en inicialización de configuración", e);
        }
    }

    @LogSystemEvent(
            event = "Carga de configuración",
            description = "Cargando archivo de configuración: ${filename}",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error al cargar la configuración",
            method = "loadConfiguration",
            component = "ConfigurationService"
    )
    @Auditable(
            value = "Lectura de configuración",
            type = AuditEntryType.READ,
            description = "Lectura del archivo de configuración"
    )
    public DefaultConfiguration loadConfiguration(String filename) throws ConfigurationException {
        validateFilename(filename);
        try {
            String filePath = CONFIG_PATH + filename;
            if (!Files.exists(Paths.get(filePath))) {
                throw new ConfigurationException("Archivo de configuración no encontrado: " + filename);
            }
            return switch (getFileExtension(filename).toLowerCase()) {
                case "yml", "yaml" -> loadYamlConfiguration(filePath);
                case "json" -> loadJsonConfiguration(filePath);
                case "properties" -> loadPropertiesConfiguration();
                default -> throw new ConfigurationException("Formato de archivo no soportado");
            };
        } catch (IOException e) {
            throw new ConfigurationException("Error al cargar la configuración", e);
        }
    }

    @LogSystemEvent(
            event = "Guardado de configuración",
            description = "Guardando nueva configuración: ${filename}",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error al guardar la configuración",
            method = "saveConfiguration",
            component = "ConfigurationService"
    )
    @Auditable(
            value = "Creación de configuración",
            type = AuditEntryType.CREATE,
            description = "Creación de nuevo archivo de configuración"
    )
    public void saveConfiguration(DefaultConfiguration config, String filename) throws ConfigurationException {
        validateConfiguration(config);
        validateFilename(filename);
        try {
            backupExistingConfiguration(filename);
            String filePath = CONFIG_PATH + filename;
            switch (getFileExtension(filename).toLowerCase()) {
                case "yml", "yaml" -> saveYamlConfiguration(config, filePath);
                case "json" -> saveJsonConfiguration(config, filePath);
                case "properties" -> savePropertiesConfiguration(config, filePath);
                default -> throw new ConfigurationException("Formato de archivo no soportado");
            }
        } catch (IOException e) {
            throw new ConfigurationException("Error al guardar la configuración", e);
        }
    }

    @LogSystemEvent(
            event = "Eliminación de configuración",
            description = "Eliminando archivo de configuración: ${filename}",
            level = LogLevel.WARN
    )
    @LogException(
            message = "Error al eliminar la configuración",
            method = "deleteConfiguration",
            component = "ConfigurationService"
    )
    @Auditable(
            value = "Eliminación de configuración",
            type = AuditEntryType.DELETE,
            description = "Eliminación de archivo de configuración"
    )
    public void deleteConfiguration(String filename) throws ConfigurationException {
        validateFilename(filename);
        try {
            String filePath = CONFIG_PATH + filename;
            if (!Files.deleteIfExists(Paths.get(filePath))) {
                throw new ConfigurationException("No se pudo eliminar el archivo: " + filename);
            }
        } catch (IOException e) {
            throw new ConfigurationException("Error al eliminar la configuración", e);
        }
    }

    @LogSystemEvent(
            event = "Listado de configuraciones",
            description = "Listando archivos de configuraciones",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error al listar las configuraciones",
            method = "listConfigurations",
            component = "ConfigurationService"
    )
    @Auditable(
            value = "Listado de configuraciones",
            type = AuditEntryType.READ,
            description = "Listado de configuraciones del archivo de configuracion"
    )
    public List<String> listConfigurations() throws ConfigurationException {
        try {
            try (var stream = Files.list(Paths.get(CONFIG_PATH))) {
                return stream.filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new ConfigurationException("Error al listar configuraciones", e);
        }
    }

    @LogSystemEvent(
            event = "Validando configuracion del cliente opcua",
            description = "Validacion de configuracion",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error al validar la configuracion",
            method = "validateConfiguration",
            component = "ConfigurationService"
    )
    @Auditable(
            value = "Validacion de configuracion del cliente opcua",
            type = AuditEntryType.OPERATION,
            description = "Validacion de los campos de configuracion del cliente opcua"
    )
    @Transactional
    private boolean validateConfiguration(DefaultConfiguration config) throws ConfigurationException {
        Objects.requireNonNull(config, "La configuracion no puede ser nula");
        DefaultConfigurationValidatorImpl validator = new DefaultConfigurationValidatorImpl();
        //Validacion de conexion
        validator.validateConnection(config);
        //Validacion de autenticacion
        validator.validateAuthentication(config);
        //Validacion de encriptacion
        validator.validateEncryption(config);
        //Validacion de session
        validator.validateSession(config);
        //Validacion de suscripcion
        validator.validateSubscription(config);
        //Validacion de configuracion industrial
        validator.validateIndustrialConfiguration(config);
        return true;
    }

    @LogSystemEvent(
            event = "Backup de configuración",
            description = "Creando backup de configuración: ${filename}",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error al crear backup de configuración",
            method = "backupConfiguration",
            component = "ConfigurationService"
    )
    @Auditable(
            value = "Backup de configuración",
            type = AuditEntryType.CREATE,
            description = "Creación de backup de configuración"
    )
    public void backupConfiguration(String filename) throws ConfigurationException {
        validateFilename(filename);
        try {
            String sourcePath = CONFIG_PATH + filename;
            String backupFileName = generateBackupFileName(filename);
            String backupPath = BACKUP_PATH + backupFileName;
            Files.copy(Paths.get(sourcePath), Paths.get(backupPath),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ConfigurationException("Error al crear backup", e);
        }
    }

    @LogSystemEvent(
            event = "Backup de configuración",
            description = "Creando backup de configuración: ${filename}",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error al crear backup de configuración",
            method = "backupConfiguration",
            component = "ConfigurationService"
    )
    @Auditable(
            value = "Backup de configuración",
            type = AuditEntryType.CREATE,
            description = "Creación de backup de configuración"
    )
    public void restoreConfiguration(String backupFilename) throws ConfigurationException {
        validateFilename(backupFilename);
        try {
            String backupPath = BACKUP_PATH + backupFilename;
            String originalFilename = extractOriginalFilename(backupFilename);
            String restorePath = CONFIG_PATH + originalFilename;
            Files.copy(Paths.get(backupPath), Paths.get(restorePath),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ConfigurationException("Error al restaurar backup", e);
        }
    }

    private DefaultConfiguration loadYamlConfiguration(String filePath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.readValue(new File(filePath), DefaultConfiguration.class);
    }

    private DefaultConfiguration loadJsonConfiguration(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), DefaultConfiguration.class);
    }

    private DefaultConfiguration loadPropertiesConfiguration() throws IOException {
        DefaultConfiguration config = new DefaultConfiguration();
        return config.getInmutableDefaultConfiguration();
    }

    private void saveYamlConfiguration(DefaultConfiguration config, String filePath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.writeValue(new File(filePath), config);
    }

    private void saveJsonConfiguration(DefaultConfiguration config, String filePath) throws IOException {
        objectMapper.writeValue(new File(filePath), config);
    }

    private void savePropertiesConfiguration(DefaultConfiguration config, String filePath) throws IOException {
        Properties props = mapConfigToProperties(config);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            props.store(fos, "OPC UA Default Configuration");
        }
    }

    @Transactional
    private Properties mapConfigToProperties(DefaultConfiguration config) {
        Objects.requireNonNull(config, "La configuracion no puede ser nula");
        // Propiedades principales
        props.setProperty("opcua.default.filename", config.getFilename());
        props.setProperty("opcua.default.description", config.getDescription());
        props.setProperty("opcua.default.enabled", String.valueOf(config.getEnabled()));
        props.setProperty("opcua.default.version", String.valueOf(config.getVersion()));
        //Propiedades de conexion
        connectionToProperties(config);
        //Propiedades de autenticacion
        authenticationToProperties(config);
        //Propiedades de encriptacion
        encryptionToProperties(config);
        //Propiedades de session
        sessionToProperties(config);
        //Propiedades de suscripciones
        subscriptionsToProperties(config);
        //Propiedades de configuracion industrial
        industrialConfigurationToProperties(config);
        return props;
    }

    private void connectionToProperties(DefaultConfiguration config) {
        // Connection
        DefaultConfiguration.Connection conn = config.getConnection();
        Objects.requireNonNull(conn, "La conexion no puede ser nula");
        props.setProperty("opcua.default.connection.endpointUrl", conn.getEndpointUrl());
        props.setProperty("opcua.default.connection.applicationName", conn.getApplicationName());
        props.setProperty("opcua.default.connection.applicationUri", conn.getApplicationUri());
        props.setProperty("opcua.default.connection.productUri", conn.getProductUri());
        props.setProperty("opcua.default.connection.type",conn.getType().name());
        props.setProperty("opcua.default.connection.timeout", String.valueOf(conn.getTimeout()));
        props.setProperty("opcua.default.connection.status",conn.getStatus().name());
    }

    private void authenticationToProperties(DefaultConfiguration config) {
        // Authentication
        DefaultConfiguration.Authentication auth = config.getAuthentication();
        Objects.requireNonNull(auth, "La autenticacion no puede ser nula");
        props.setProperty("opcua.default.authentication.identityProvider",auth.getIdentityProvider().name());
        props.setProperty("opcua.default.authentication.username", auth.getUserName());
        props.setProperty("opcua.default.authentication.password", auth.getPassword());
        props.setProperty("opcua.default.authentication.securityPolicy", String.valueOf(auth.getSecurityPolicy()));
        props.setProperty("opcua.default.authentication.messageSecurityMode", String.valueOf(auth.getMessageSecurityMode()));
        props.setProperty("opcua.default.authentication.certificatePath", auth.getCertificatePath());
        props.setProperty("opcua.default.authentication.privateKeyPath", auth.getPrivateKeyPath());
        props.setProperty("opcua.default.authentication.trustListPath", auth.getTrustListPath());
        props.setProperty("opcua.default.authentication.issuerListPath", auth.getIssuerListPath());
        props.setProperty("opcua.default.authentication.revocationListPath", auth.getRevocationListPath());
        props.setProperty("opcua.default.authentication.securityPolicyUri", String.valueOf(auth.getSecurityPolicyUri()));
        props.setProperty("opcua.default.authentication.expirationWarningDays", String.valueOf(auth.getExpirationWarningDays()));
    }

    private void encryptionToProperties(DefaultConfiguration config) {
        // Encryption
        DefaultConfiguration.Encryption enc = config.getEncryption();
        Objects.requireNonNull(enc, "La encriptacion no puede ser nula");
        props.setProperty("opcua.default.encryption.securityPolicy", String.valueOf(enc.getSecurityPolicy()));
        props.setProperty("opcua.default.encryption.messageSecurityMode", String.valueOf(enc.getMessageSecurityMode()));
        props.setProperty("opcua.default.encryption.clientCertificate", Arrays.toString(enc.getClientCertificate()));
        props.setProperty("opcua.default.encryption.privateKey", Arrays.toString(enc.getPrivateKey()));
        props.setProperty("opcua.default.encryption.trustedCertificates", String.valueOf(enc.getTrustedCertificates()));
        props.setProperty("opcua.default.encryption.keyLength", String.valueOf(enc.getKeyLength()));
        props.setProperty("opcua.default.encryption.algorithmName", String.valueOf(enc.getAlgorithmName()));
        props.setProperty("opcua.default.encryption.protocolVersion",enc.getProtocolVersion());
    }

    private void sessionToProperties(DefaultConfiguration config) {
        // Session
        DefaultConfiguration.Session session = config.getSession();
        Objects.requireNonNull(session, "La sesion no puede ser nula");
        props.setProperty("opcua.default.session.sessionName", session.getSessionName());
        props.setProperty("opcua.default.session.serverUri", session.getServerUri());
        props.setProperty("opcua.default.session.maxResponseMessageSize", String.valueOf(session.getMaxResponseMessageSize()));
        props.setProperty("opcua.default.session.securityMode",session.getSecurityMode());
        props.setProperty("opcua.default.session.securityPolicyUri", String.valueOf(session.getSecurityPolicyUri()));
        props.setProperty("opcua.default.session.clientCertificate", session.getClientCertificate());
        props.setProperty("opcua.default.session.serverCertificate", session.getServerCertificate());
        props.setProperty("opcua.default.session.localeIds", String.valueOf(session.getLocaleIds()));
        props.setProperty("opcua.default.session.maxChunkCount", String.valueOf(session.getMaxChunkCount()));
        props.setProperty("opcua.default.session.timeout", String.valueOf(session.getTimeout()));
    }

    private void subscriptionsToProperties(DefaultConfiguration config) {
        // Subscriptions
        Objects.requireNonNull(config.getSubscriptions(), "La lista de suscripciones no puede ser nula");
        for (int i = 0; i < config.getSubscriptions().size(); i++) {
            DefaultConfiguration.Subscription sub = config.getSubscriptions().get(i);
            String prefix = "opcua.default.subscriptions[" + i + "].";
            props.setProperty(prefix + "nodeId", sub.getNodeId());
            props.setProperty(prefix + "publishingInterval", String.valueOf(sub.getPublishingInterval()));
            props.setProperty(prefix + "lifetimeCount", String.valueOf(sub.getLifetimeCount()));
            props.setProperty(prefix + "maxKeepAliveCount", String.valueOf(sub.getMaxKeepAliveCount()));
            props.setProperty(prefix + "publishingEnabled", String.valueOf(sub.getPublishingEnabled()));
            props.setProperty(prefix + "maxNotificationsPerPublish", String.valueOf(sub.getMaxNotificationsPerPublish()));
            props.setProperty(prefix + "priority", String.valueOf(sub.getPriority()));
            props.setProperty(prefix + "samplingInterval", String.valueOf(sub.getSamplingInterval()));
            props.setProperty(prefix + "queueSize", String.valueOf(sub.getQueueSize()));
            props.setProperty(prefix + "discardOldest", String.valueOf(sub.getDiscardOldest()));
            props.setProperty(prefix + "monitoringMode", String.valueOf(sub.getMonitoringMode()));
            props.setProperty(prefix + "timestampsToReturn", String.valueOf(sub.getTimestampsToReturn()));
        }
    }

    private void industrialConfigurationToProperties(DefaultConfiguration config) {
        // IndustrialConfiguration
        DefaultConfiguration.IndustrialConfiguration ind = config.getIndustrialConfiguration();
        Objects.requireNonNull(ind, "La configuracion industrial no puede ser nula");
        props.setProperty("opcua.default.industrialConfiguration.industrialZone", ind.getIndustrialZone());
        props.setProperty("opcua.default.industrialConfiguration.equipmentId", ind.getEquipmentId());
        props.setProperty("opcua.default.industrialConfiguration.areaId", ind.getAreaId());
        props.setProperty("opcua.default.industrialConfiguration.processId", ind.getProcessId());
        props.setProperty("opcua.default.industrialConfiguration.operatorName", ind.getOperatorName());
        props.setProperty("opcua.default.industrialConfiguration.operatorId", ind.getOperatorId());
    }

    private void validateFilename(String filename) throws ConfigurationException {
        if (!StringUtils.hasText(filename)) {
            throw new ConfigurationException("El nombre del archivo no puede estar vacío");
        }
        if (filename.contains("..")) {
            throw new ConfigurationException("Nombre de archivo inválido");
        }
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private boolean exists(String filename) {
        return Files.exists(Paths.get(CONFIG_PATH + filename));
    }

    private void backupExistingConfiguration(String filename) throws IOException, ConfigurationException {
        if (exists(filename)) {
            backupConfiguration(filename);
        }
    }

    private String generateBackupFileName(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = getFileExtension(originalFilename);
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        return baseName + "_" + timestamp + "." + extension;
    }

    private String extractOriginalFilename(String backupFilename) {
        // Asume formato: nombreoriginal_timestamp.extension
        return backupFilename.replaceAll("_\\d+\\.", ".");
    }


    @LogSystemEvent(
            event = "Extracción de archivo de configuración",
            description = "Extrayendo el nombre de archivo existente en la ruta de configuración",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error al extraer el archivo de configuración",
            method = "extractExistingFilename",
            component = "ConfigurationService"
    )
    @Auditable(
            value = "Extracción de archivo de configuración",
            type = AuditEntryType.READ,
            description = "Extracción de archivo existente en la ruta de configuración"
    )
    public String extractExistingFilename() throws ConfigurationException {
        try {
            try (var stream = Files.list(Paths.get(CONFIG_PATH))) {
                return stream.filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .findFirst()
                        .orElseThrow(() -> new ConfigurationException("No se encontró ningún archivo en la ruta de configuración."));
            }
        } catch (IOException e) {
            throw new ConfigurationException("Error al extraer el archivo de configuración existente", e);
        }
    }
}
