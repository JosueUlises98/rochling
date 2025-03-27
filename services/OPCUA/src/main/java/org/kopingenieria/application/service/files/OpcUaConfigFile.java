package org.kopingenieria.application.service.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.application.validators.OpcUaConfigurationValidator;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.config.OpcUaConfiguration;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.monitoring.MonitoringMode;
import org.kopingenieria.domain.enums.security.IdentityProvider;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import org.kopingenieria.exception.ConfigurationException;
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
public class OpcUaConfigFile {

    private static final String CONFIG_PATH = "src/main/resources";
    private static final String BACKUP_PATH = CONFIG_PATH + "backup/";
    private static final String DEFAULT_CONFIG = "default-config";
    private final ObjectMapper objectMapper;
    private Properties props;
    private OpcUaConfiguration config;

    @Auditable(value = "Evento de inicializacion de directorios", type = AuditEntryType.CREATE, description = "Inicializacion de directorios en ruta definida")
    @LogSystemEvent(event = "Inicializacion de directorios",
            description = "Inicializando directorios en ruta definida",
            level = LogLevel.INFO)
    @LogException(message = "Error en la inicializacion de directorios", method = "init", component = "ConfigurationService")
    public void init() throws ConfigurationException {
        try {
            Files.createDirectories(Paths.get(CONFIG_PATH));
            Files.createDirectories(Paths.get(BACKUP_PATH));
            createDefaultConfigIfNotExists();
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
    public OpcUaConfiguration loadConfiguration(String filename) throws ConfigurationException {
        validateFilename(filename);
        try {
            String filePath = CONFIG_PATH + filename;
            if (!Files.exists(Paths.get(filePath))) {
                throw new ConfigurationException("Archivo de configuración no encontrado: " + filename);
            }
            return switch (getFileExtension(filename).toLowerCase()) {
                case "yml", "yaml" -> loadYamlConfiguration(filePath);
                case "json" -> loadJsonConfiguration(filePath);
                case "properties" -> loadPropertiesConfiguration(filePath);
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
    public void saveConfiguration(OpcUaConfiguration config, String filename) throws ConfigurationException {
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
            event = "Actualización de configuración",
            description = "Actualizando configuración existente: ${filename}",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error al actualizar la configuración",
            method = "updateConfiguration",
            component = "ConfigurationService"
    )
    @Auditable(
            value = "Actualización de configuración",
            type = AuditEntryType.UPDATE,
            description = "Modificación de archivo de configuración existente"
    )
    public void updateConfiguration(OpcUaConfiguration config, String filename) throws ConfigurationException {
        if (!exists(filename)) {
            throw new ConfigurationException("Configuración no encontrada para actualizar: " + filename);
        }
        saveConfiguration(config, filename);
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
    private boolean validateConfiguration(OpcUaConfiguration config) throws ConfigurationException {
        Objects.requireNonNull(config, "La configuracion no puede ser nula");
        OpcUaConfigurationValidator validator = new OpcUaConfigurationValidator();
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

    // Métodos privados de utilidad

    private void createDefaultConfigIfNotExists() throws IOException {
        String defaultConfigPath = CONFIG_PATH + DEFAULT_CONFIG + ".yml";
        if (!Files.exists(Paths.get(defaultConfigPath))) {
            OpcUaConfiguration defaultConfig = createDefaultConfiguration();
            saveYamlConfiguration(defaultConfig, defaultConfigPath);
        }
    }

    private OpcUaConfiguration createDefaultConfiguration() {
        OpcUaConfiguration config = new OpcUaConfiguration();
        config.setFilename("Default Configuration for OPC UA client");
        config.setDescription("Default configuration for OPC UA client. This configuration is used when no configuration is found in the configuration directory.");
        config.setEnabled(false);
        config.setVersion(1L);
        return config;
    }

    private OpcUaConfiguration loadYamlConfiguration(String filePath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.readValue(new File(filePath), OpcUaConfiguration.class);
    }

    private OpcUaConfiguration loadJsonConfiguration(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), OpcUaConfiguration.class);
    }

    private OpcUaConfiguration loadPropertiesConfiguration(String filePath) throws IOException {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(filePath)) {
            props.load(is);
            return mapPropertiesToConfig(props);
        }
    }

    private void saveYamlConfiguration(OpcUaConfiguration config, String filePath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.writeValue(new File(filePath), config);
    }

    private void saveJsonConfiguration(OpcUaConfiguration config, String filePath) throws IOException {
        objectMapper.writeValue(new File(filePath), config);
    }

    private void savePropertiesConfiguration(OpcUaConfiguration config, String filePath) throws IOException {
        Properties props = mapConfigToProperties(config);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            props.store(fos, "OPC UA Configuration");
        }
    }

    @Transactional
    private Properties mapConfigToProperties(OpcUaConfiguration config) {
        Objects.requireNonNull(config, "La configuracion no puede ser nula");
        // Propiedades principales
        props.setProperty("opcua.filename", config.getFilename());
        props.setProperty("opcua.description", config.getDescription());
        props.setProperty("opcua.enabled", String.valueOf(config.getEnabled()));
        props.setProperty("opcua.version", String.valueOf(config.getVersion()));
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

    private void connectionToProperties(OpcUaConfiguration config) {
        // Connection
        OpcUaConfiguration.Connection conn = config.getConnection();
        Objects.requireNonNull(conn, "La conexion no puede ser nula");
        props.setProperty("opcua.connection.endpointUrl", conn.getEndpointUrl());
        props.setProperty("opcua.connection.applicationName", conn.getApplicationName());
        props.setProperty("opcua.connection.applicationUri", conn.getApplicationUri());
        props.setProperty("opcua.connection.productUri", conn.getProductUri());
        props.setProperty("opcua.connection.type", String.valueOf(conn.getType()));
        props.setProperty("opcua.connection.timeout", String.valueOf(conn.getTimeout()));
    }

    private void authenticationToProperties(OpcUaConfiguration config) {
        // Authentication
        OpcUaConfiguration.Authentication auth = config.getAuthentication();
        Objects.requireNonNull(auth, "La autenticacion no puede ser nula");
        props.setProperty("opcua.authentication.identityProvider",auth.getIdentityProvider().name());
        props.setProperty("opcua.authentication.username", auth.getUserName());
        props.setProperty("opcua.authentication.password", auth.getPassword());
        props.setProperty("opcua.authentication.securityPolicy", String.valueOf(auth.getSecurityPolicy()));
        props.setProperty("opcua.authentication.messageSecurityMode", String.valueOf(auth.getMessageSecurityMode()));
        props.setProperty("opcua.authentication.certificatePath", auth.getCertificatePath());
        props.setProperty("opcua.authentication.privateKeyPath", auth.getPrivateKeyPath());
        props.setProperty("opcua.authentication.trustListPath", auth.getTrustListPath());
        props.setProperty("opcua.authentication.issuerListPath", auth.getIssuerListPath());
        props.setProperty("opcua.authentication.revocationListPath", auth.getRevocationListPath());
    }

    private void encryptionToProperties(OpcUaConfiguration config) {
        // Encryption
        OpcUaConfiguration.Encryption enc = config.getEncryption();
        Objects.requireNonNull(enc, "La encriptacion no puede ser nula");
        props.setProperty("opcua.encryption.securityPolicy", enc.getSecurityPolicy());
        props.setProperty("opcua.encryption.messageSecurityMode", enc.getMessageSecurityMode());
        props.setProperty("opcua.encryption.clientCertificate", Arrays.toString(enc.getClientCertificate()));
        props.setProperty("opcua.encryption.privateKey", Arrays.toString(enc.getPrivateKey()));
        props.setProperty("opcua.encryption.trustedCertificates", String.valueOf(enc.getTrustedCertificates()));
        props.setProperty("opcua.encryption.keyLength", String.valueOf(enc.getKeyLength()));
        props.setProperty("opcua.encryption.algorithmName", enc.getAlgorithmName());
        props.setProperty("opcua.encryption.protocolVersion",enc.getProtocolVersion());
    }

    private void sessionToProperties(OpcUaConfiguration config) {
        // Session
        OpcUaConfiguration.Session session = config.getSession();
        Objects.requireNonNull(session, "La sesion no puede ser nula");
        props.setProperty("opcua.session.sessionName", session.getSessionName());
        props.setProperty("opcua.session.serverUri", session.getServerUri());
        props.setProperty("opcua.session.maxResponseMessageSize", String.valueOf(session.getMaxResponseMessageSize()));
        props.setProperty("opcua.session.securityMode",session.getSecurityMode());
        props.setProperty("opcua.session.securityPolicyUri", session.getSecurityPolicyUri());
        props.setProperty("opcua.session.clientCertificate", session.getClientCertificate());
        props.setProperty("opcua.session.serverCertificate", session.getServerCertificate());
        props.setProperty("opcua.session.localeIds", String.valueOf(session.getLocaleIds()));
        props.setProperty("opcua.session.maxChunkCount", String.valueOf(session.getMaxChunkCount()));
        props.setProperty("opcua.session.timeout", String.valueOf(session.getTimeout()));
    }

    private void subscriptionsToProperties(OpcUaConfiguration config) {
        // Subscriptions
        Objects.requireNonNull(config.getSubscriptions(), "La lista de suscripciones no puede ser nula");
        for (int i = 0; i < config.getSubscriptions().size(); i++) {
            OpcUaConfiguration.Subscription sub = config.getSubscriptions().get(i);
            String prefix = "opcua.subscriptions[" + i + "].";
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

    private void industrialConfigurationToProperties(OpcUaConfiguration config) {
        // IndustrialConfiguration
        OpcUaConfiguration.IndustrialConfiguration ind = config.getIndustrialConfiguration();
        Objects.requireNonNull(ind, "La configuracion industrial no puede ser nula");
        props.setProperty("opcua.industrialConfiguration.industrialZone", ind.getIndustrialZone());
        props.setProperty("opcua.industrialConfiguration.equipmentId", ind.getEquipmentId());
        props.setProperty("opcua.industrialConfiguration.areaId", ind.getAreaId());
        props.setProperty("opcua.industrialConfiguration.processId", ind.getProcessId());
        props.setProperty("opcua.industrialConfiguration.operatorName", ind.getOperatorName());
        props.setProperty("opcua.industrialConfiguration.operatorId", ind.getOperatorId());
    }

    @Transactional
    private OpcUaConfiguration mapPropertiesToConfig(Properties props) {
        Objects.requireNonNull(props, "Propiedades no pueden ser nulas");
        // Propiedades principales
        config.setFilename(props.getProperty("opcua.filename"));
        config.setDescription(props.getProperty("opcua.description"));
        config.setEnabled(Boolean.valueOf(props.getProperty("opcua.enabled")));
        config.setVersion(Long.valueOf(props.getProperty("opcua.version")));
        //Propiedades de conexion
        config.setConnection(connectionToConfig(props));
        //Propiedades de autenticacion
        config.setAuthentication(authenticationToConfig(props));
        //Propiedades de encriptacion
        config.setEncryption(encryptionToConfig(props));
        //Propiedades de session
        config.setSession(sessionToConfig(props));
        //Propiedades de suscripcion
        config.setSubscriptions(subscriptionToConfig(props));
        //Propiedades de configuracion industrial
        config.setIndustrialConfiguration(industrialConfigurationToConfig(props));
        return config;
    }

    private OpcUaConfiguration.Connection connectionToConfig(Properties props) {
        // Connection
        return OpcUaConfiguration.Connection.builder()
                .endpointUrl(props.getProperty("opcua.connection.endpointUrl"))
                .applicationName(props.getProperty("opcua.connection.applicationName"))
                .applicationUri(props.getProperty("opcua.connection.applicationUri"))
                .productUri(props.getProperty("opcua.connection.productUri"))
                .type(ConnectionType.valueOf(props.getProperty("opcua.connection.type")))
                .timeout(Timeouts.valueOf(props.getProperty("opcua.connection.timeout")))
                .build();
    }

    private OpcUaConfiguration.Authentication authenticationToConfig(Properties props) {
        // Authentication
        return OpcUaConfiguration.Authentication.builder()
                .identityProvider(IdentityProvider.valueOf(props.getProperty("opcua.authentication.identityProvider")))
                .userName(props.getProperty("opcua.authentication.userName"))
                .password(props.getProperty("opcua.authentication.password"))
                .securityPolicy(SecurityPolicy.valueOf(props.getProperty("opcua.authentication.securityPolicy")))
                .messageSecurityMode(MessageSecurityMode.valueOf(props.getProperty("opcua.authentication.messageSecurityMode")))
                .certificatePath(props.getProperty("opcua.authentication.certificatePath"))
                .privateKeyPath(props.getProperty("opcua.authentication.privateKeyPath"))
                .trustListPath(props.getProperty("opcua.authentication.trustListPath"))
                .issuerListPath(props.getProperty("opcua.authentication.issuerListPath"))
                .revocationListPath(props.getProperty("opcua.authentication.revocationListPath"))
                .build();
    }

    private OpcUaConfiguration.Encryption encryptionToConfig(Properties props) {
        // Encryption
        return OpcUaConfiguration.Encryption.builder()
                .securityPolicy(props.getProperty("opcua.encryption.securityPolicy"))
                .messageSecurityMode(props.getProperty("opcua.encryption.messageSecurityMode"))
                .clientCertificate(props.getProperty("opcua.encryption.clientCertificate").getBytes())
                .privateKey(props.getProperty("opcua.encryption.privateKey").getBytes())
                .trustedCertificates(Collections.singletonList(props.getProperty("opcua.encryption.trustedCertificates").getBytes()))
                .keyLength(Integer.valueOf(props.getProperty("opcua.encryption.keyLength")))
                .algorithmName(props.getProperty("opcua.encryption.algorithmName"))
                .protocolVersion(props.getProperty("opcua.encryption.protocolVersion"))
                .build();
    }

    private OpcUaConfiguration.Session sessionToConfig(Properties props) {
        // Session
        return OpcUaConfiguration.Session.builder()
                .sessionName(props.getProperty("opcua.session.sessionName"))
                .serverUri(props.getProperty("opcua.session.serverUri"))
                .maxResponseMessageSize(Long.valueOf(props.getProperty("opcua.session.maxResponseMessageSize")))
                .securityMode(props.getProperty("opcua.session.securityMode"))
                .securityPolicyUri(props.getProperty("opcua.session.securityPolicyUri"))
                .clientCertificate(props.getProperty("opcua.session.clientCertificate"))
                .serverCertificate(props.getProperty("opcua.session.serverCertificate"))
                .localeIds(Arrays.asList(props.getProperty("opcua.session.localeIds").split(",")))
                .maxChunkCount(Integer.valueOf(props.getProperty("opcua.session.maxChunkCount")))
                .timeout(Timeouts.valueOf(props.getProperty("opcua.session.timeout")))
                .build();
    }

    private List<OpcUaConfiguration.Subscription> subscriptionToConfig(Properties props) {
        // Subscriptions
        List<OpcUaConfiguration.Subscription> subscriptions = new ArrayList<>();
        int i = 0;
        while (props.containsKey("opcua.subscriptions[" + i + "].name")) {
            String prefix = "opcua.subscriptions[" + i + "].";
            OpcUaConfiguration.Subscription subscription = OpcUaConfiguration.Subscription.builder()
                    .nodeId(props.getProperty(prefix + "nodeId"))
                    .publishingInterval(getDoubleProperty(props, prefix + "publishingInterval"))
                    .lifetimeCount(getIntegerProperty(props, prefix + "lifetimeCount"))
                    .maxKeepAliveCount(getIntegerProperty(props, prefix + "maxKeepAliveCount"))
                    .maxNotificationsPerPublish(getIntegerProperty(props, prefix + "maxNotificationsPerPublish"))
                    .publishingEnabled(getBooleanProperty(props, prefix + "publishingEnabled"))
                    .priority(getUByteProperty(props, prefix + "priority"))
                    .samplingInterval(getDoubleProperty(props, prefix + "samplingInterval"))
                    .queueSize(getIntegerProperty(props, prefix + "queueSize"))
                    .discardOldest(getBooleanProperty(props, prefix + "discardOldest"))
                    .monitoringMode(MonitoringMode.valueOf(props.getProperty(prefix + "monitoringMode")))
                    .timestampsToReturn(TimestampsToReturn.valueOf(props.getProperty(prefix + "timestampsToReturn")))
                    .build();
            subscriptions.add(subscription);
            i++;
        }
        return subscriptions;
    }

    private OpcUaConfiguration.IndustrialConfiguration industrialConfigurationToConfig(Properties props) {
        // IndustrialConfiguration
       return OpcUaConfiguration.IndustrialConfiguration.builder()
                .industrialZone(props.getProperty("opcua.industrialConfiguration.industrialZone"))
                .equipmentId(props.getProperty("opcua.industrialConfiguration.equipmentId"))
                .areaId(props.getProperty("opcua.industrialConfiguration.areaId"))
                .processId(props.getProperty("opcua.industrialConfiguration.processId"))
               .operatorName(props.getProperty("opcua.industrialConfiguration.operatorName"))
               .operatorId(props.getProperty("opcua.industrialConfiguration.operatorId"))
                .build();
    }

    // Métodos auxiliares para conversión de tipos
    private UInteger getIntegerProperty(Properties props, String key) {
        String value = props.getProperty(key);
        return value != null ? UInteger.valueOf(value) : null;
    }

    private Double getDoubleProperty(Properties props, String key) {
        String value = props.getProperty(key);
        return value != null ? Double.valueOf(value) : null;
    }

    private Boolean getBooleanProperty(Properties props, String key) {
        String value = props.getProperty(key);
        return value != null ? Boolean.valueOf(value) : null;
    }

    private UByte getUByteProperty(Properties props, String key) {
        String value = props.getProperty(key);
        return value != null ? UByte.valueOf(value) : null;
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
