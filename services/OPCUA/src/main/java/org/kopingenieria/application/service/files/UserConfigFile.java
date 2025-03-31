package org.kopingenieria.application.service.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.application.validators.user.UserConfigurationValidator;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.monitoring.MonitoringMode;
import org.kopingenieria.domain.enums.security.IdentityProvider;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
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
public class UserConfigFile {

    private static final String CONFIG_PATH = "src/main/resources";
    private static final String BACKUP_PATH = CONFIG_PATH + "backup/";
    private final ObjectMapper objectMapper;
    private Properties props;
    private UserConfiguration config;

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
    public UserConfiguration loadConfiguration(String filename) throws ConfigurationException {
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
    public void saveConfiguration(UserConfiguration config, String filename) throws ConfigurationException {
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
    public void updateConfiguration(UserConfiguration config, String filename) throws ConfigurationException {
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
    private boolean validateConfiguration(UserConfiguration config) throws ConfigurationException {
        Objects.requireNonNull(config, "La configuracion no puede ser nula");
        UserConfigurationValidator validator = new UserConfigurationValidator();
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

    private UserConfiguration loadYamlConfiguration(String filePath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.readValue(new File(filePath), UserConfiguration.class);
    }

    private UserConfiguration loadJsonConfiguration(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), UserConfiguration.class);
    }

    private UserConfiguration loadPropertiesConfiguration(String filePath) throws IOException {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(filePath)) {
            props.load(is);
            return mapPropertiesToConfig(props);
        }
    }

    private void saveYamlConfiguration(UserConfiguration config, String filePath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.writeValue(new File(filePath), config);
    }

    private void saveJsonConfiguration(UserConfiguration config, String filePath) throws IOException {
        objectMapper.writeValue(new File(filePath), config);
    }

    private void savePropertiesConfiguration(UserConfiguration config, String filePath) throws IOException {
        Properties props = mapConfigToProperties(config);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            props.store(fos, "OPC UA Configuration");
        }
    }

    @Transactional
    private Properties mapConfigToProperties(UserConfiguration config) {
        Objects.requireNonNull(config, "La configuracion no puede ser nula");
        // Propiedades principales
        props.setProperty("opcua.user.filename", config.getFilename());
        props.setProperty("opcua.user.description", config.getDescription());
        props.setProperty("opcua.user.enabled", String.valueOf(config.getEnabled()));
        props.setProperty("opcua.user.version", String.valueOf(config.getVersion()));
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

    private void connectionToProperties(UserConfiguration config) {
        // Connection
        UserConfiguration.Connection conn = config.getConnection();
        Objects.requireNonNull(conn, "La conexion no puede ser nula");
        props.setProperty("opcua.user.connection.endpointUrl", conn.getEndpointUrl());
        props.setProperty("opcua.user.connection.applicationName", conn.getApplicationName());
        props.setProperty("opcua.user.connection.applicationUri", conn.getApplicationUri());
        props.setProperty("opcua.user.connection.productUri", conn.getProductUri());
        props.setProperty("opcua.user.connection.type", String.valueOf(conn.getType()));
        props.setProperty("opcua.user.connection.timeout", String.valueOf(conn.getTimeout()));
    }

    private void authenticationToProperties(UserConfiguration config) {
        // Authentication
        UserConfiguration.Authentication auth = config.getAuthentication();
        Objects.requireNonNull(auth, "La autenticacion no puede ser nula");
        props.setProperty("opcua.user.authentication.identityProvider",auth.getIdentityProvider().name());
        props.setProperty("opcua.user.authentication.username", auth.getUserName());
        props.setProperty("opcua.user.authentication.password", auth.getPassword());
        props.setProperty("opcua.user.authentication.securityPolicy", String.valueOf(auth.getSecurityPolicy()));
        props.setProperty("opcua.user.authentication.messageSecurityMode", String.valueOf(auth.getMessageSecurityMode()));
        props.setProperty("opcua.user.authentication.certificatePath", auth.getCertificatePath());
        props.setProperty("opcua.user.authentication.privateKeyPath", auth.getPrivateKeyPath());
        props.setProperty("opcua.user.authentication.trustListPath", auth.getTrustListPath());
        props.setProperty("opcua.user.authentication.issuerListPath", auth.getIssuerListPath());
        props.setProperty("opcua.user.authentication.revocationListPath", auth.getRevocationListPath());
    }

    private void encryptionToProperties(UserConfiguration config) {
        // Encryption
        UserConfiguration.Encryption enc = config.getEncryption();
        Objects.requireNonNull(enc, "La encriptacion no puede ser nula");
        props.setProperty("opcua.user.encryption.securityPolicy", enc.getSecurityPolicy());
        props.setProperty("opcua.user.encryption.messageSecurityMode", enc.getMessageSecurityMode());
        props.setProperty("opcua.user.encryption.clientCertificate", Arrays.toString(enc.getClientCertificate()));
        props.setProperty("opcua.user.encryption.privateKey", Arrays.toString(enc.getPrivateKey()));
        props.setProperty("opcua.user.encryption.trustedCertificates", String.valueOf(enc.getTrustedCertificates()));
        props.setProperty("opcua.user.encryption.keyLength", String.valueOf(enc.getKeyLength()));
        props.setProperty("opcua.user.encryption.algorithmName", enc.getAlgorithmName());
        props.setProperty("opcua.user.encryption.protocolVersion",enc.getProtocolVersion());
    }

    private void sessionToProperties(UserConfiguration config) {
        // Session
        UserConfiguration.Session session = config.getSession();
        Objects.requireNonNull(session, "La sesion no puede ser nula");
        props.setProperty("opcua.user.session.sessionName", session.getSessionName());
        props.setProperty("opcua.user.session.serverUri", session.getServerUri());
        props.setProperty("opcua.user.session.maxResponseMessageSize", String.valueOf(session.getMaxResponseMessageSize()));
        props.setProperty("opcua.user.session.securityMode",session.getSecurityMode());
        props.setProperty("opcua.user.session.securityPolicyUri", session.getSecurityPolicyUri());
        props.setProperty("opcua.user.session.clientCertificate", session.getClientCertificate());
        props.setProperty("opcua.user.session.serverCertificate", session.getServerCertificate());
        props.setProperty("opcua.user.session.localeIds", String.valueOf(session.getLocaleIds()));
        props.setProperty("opcua.user.session.maxChunkCount", String.valueOf(session.getMaxChunkCount()));
        props.setProperty("opcua.user.session.timeout", String.valueOf(session.getTimeout()));
    }

    private void subscriptionsToProperties(UserConfiguration config) {
        // Subscriptions
        Objects.requireNonNull(config.getSubscriptions(), "La lista de suscripciones no puede ser nula");
        for (int i = 0; i < config.getSubscriptions().size(); i++) {
            UserConfiguration.Subscription sub = config.getSubscriptions().get(i);
            String prefix = "opcua.user.subscriptions[" + i + "].";
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

    private void industrialConfigurationToProperties(UserConfiguration config) {
        // IndustrialConfiguration
        UserConfiguration.IndustrialConfiguration ind = config.getIndustrialConfiguration();
        Objects.requireNonNull(ind, "La configuracion industrial no puede ser nula");
        props.setProperty("opcua.user.industrialConfiguration.industrialZone", ind.getIndustrialZone());
        props.setProperty("opcua.user.industrialConfiguration.equipmentId", ind.getEquipmentId());
        props.setProperty("opcua.user.industrialConfiguration.areaId", ind.getAreaId());
        props.setProperty("opcua.user.industrialConfiguration.processId", ind.getProcessId());
        props.setProperty("opcua.user.industrialConfiguration.operatorName", ind.getOperatorName());
        props.setProperty("opcua.user.industrialConfiguration.operatorId", ind.getOperatorId());
    }

    @Transactional
    private UserConfiguration mapPropertiesToConfig(Properties props) {
        Objects.requireNonNull(props, "Propiedades no pueden ser nulas");
        // Propiedades principales
        config.setFilename(props.getProperty("opcua.user.filename"));
        config.setDescription(props.getProperty("opcua.user.description"));
        config.setEnabled(Boolean.valueOf(props.getProperty("opcua.user.enabled")));
        config.setVersion(Long.valueOf(props.getProperty("opcua.user.version")));
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

    private UserConfiguration.Connection connectionToConfig(Properties props) {
        // Connection
        return UserConfiguration.Connection.builder()
                .endpointUrl(props.getProperty("opcua.user.connection.endpointUrl"))
                .applicationName(props.getProperty("opcua.user.connection.applicationName"))
                .applicationUri(props.getProperty("opcua.user.connection.applicationUri"))
                .productUri(props.getProperty("opcua.user.connection.productUri"))
                .type(ConnectionType.valueOf(props.getProperty("opcua.user.connection.type")))
                .name(props.getProperty("opcua.user.connection.name"))
                .build();
    }

    private UserConfiguration.Authentication authenticationToConfig(Properties props) {
        // Authentication
        return UserConfiguration.Authentication.builder()
                .identityProvider(IdentityProvider.valueOf(props.getProperty("opcua.user.authentication.identityProvider")))
                .userName(props.getProperty("opcua.user.authentication.userName"))
                .password(props.getProperty("opcua.user.authentication.password"))
                .securityPolicy(SecurityPolicy.valueOf(props.getProperty("opcua.user.authentication.securityPolicy")))
                .messageSecurityMode(MessageSecurityMode.valueOf(props.getProperty("opcua.user.authentication.messageSecurityMode")))
                .certificatePath(props.getProperty("opcua.user.authentication.certificatePath"))
                .privateKeyPath(props.getProperty("opcua.user.authentication.privateKeyPath"))
                .trustListPath(props.getProperty("opcua.user.authentication.trustListPath"))
                .issuerListPath(props.getProperty("opcua.user.authentication.issuerListPath"))
                .revocationListPath(props.getProperty("opcua.user.authentication.revocationListPath"))
                .build();
    }

    private UserConfiguration.Encryption encryptionToConfig(Properties props) {
        // Encryption
        return UserConfiguration.Encryption.builder()
                .securityPolicy(props.getProperty("opcua.user.encryption.securityPolicy"))
                .messageSecurityMode(props.getProperty("opcua.user.encryption.messageSecurityMode"))
                .clientCertificate(props.getProperty("opcua.user.encryption.clientCertificate").getBytes())
                .privateKey(props.getProperty("opcua.user.encryption.privateKey").getBytes())
                .trustedCertificates(Collections.singletonList(props.getProperty("opcua.user.encryption.trustedCertificates").getBytes()))
                .keyLength(Integer.valueOf(props.getProperty("opcua.user.encryption.keyLength")))
                .algorithmName(props.getProperty("opcua.user.encryption.algorithmName"))
                .protocolVersion(props.getProperty("opcua.user.encryption.protocolVersion"))
                .build();
    }

    private UserConfiguration.Session sessionToConfig(Properties props) {
        // Session
        return UserConfiguration.Session.builder()
                .sessionName(props.getProperty("opcua.user.session.sessionName"))
                .serverUri(props.getProperty("opcua.user.session.serverUri"))
                .maxResponseMessageSize(Long.valueOf(props.getProperty("opcua.user.session.maxResponseMessageSize")))
                .securityMode(props.getProperty("opcua.user.session.securityMode"))
                .securityPolicyUri(props.getProperty("opcua.user.session.securityPolicyUri"))
                .clientCertificate(props.getProperty("opcua.user.session.clientCertificate"))
                .serverCertificate(props.getProperty("opcua.user.session.serverCertificate"))
                .localeIds(Arrays.asList(props.getProperty("opcua.user.session.localeIds").split(",")))
                .maxChunkCount(Integer.valueOf(props.getProperty("opcua.user.session.maxChunkCount")))
                .build();
    }

    private List<UserConfiguration.Subscription> subscriptionToConfig(Properties props) {
        // Subscriptions
        List<UserConfiguration.Subscription> subscriptions = new ArrayList<>();
        int i = 0;
        while (props.containsKey("opcua.user.subscriptions[" + i + "].name")) {
            String prefix = "opcua.user.subscriptions[" + i + "].";
            UserConfiguration.Subscription subscription = UserConfiguration.Subscription.builder()
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

    private UserConfiguration.IndustrialConfiguration industrialConfigurationToConfig(Properties props) {
        // IndustrialConfiguration
       return UserConfiguration.IndustrialConfiguration.builder()
                .industrialZone(props.getProperty("opcua.user.industrialConfiguration.industrialZone"))
                .equipmentId(props.getProperty("opcua.user.industrialConfiguration.equipmentId"))
                .areaId(props.getProperty("opcua.user.industrialConfiguration.areaId"))
                .processId(props.getProperty("opcua.user.industrialConfiguration.processId"))
               .operatorName(props.getProperty("opcua.user.industrialConfiguration.operatorName"))
               .operatorId(props.getProperty("opcua.user.industrialConfiguration.operatorId"))
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
