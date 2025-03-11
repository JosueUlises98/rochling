@echo off
REM scripts/common/env.bat

REM ====================================
REM Configuración Dinámica de Versión
REM ====================================
REM Lee la versión del pom.xml si existe
if exist "pom.xml" (
    for /f "tokens=*" %%a in ('mvn help:evaluate -Dexpression^=project.version -q -DforceStdout') do (
        set SERVICE_VERSION=%%a
    )
) else (
    set SERVICE_VERSION=1.0.0
)

REM ====================================
REM Configuración del Servicio
REM ====================================
set SERVICE_NAME=LoggingService
set SERVICE_DESCRIPTION=Microservicio de Logging Empresarial
set SERVICE_GROUP=com.empresa.logging

REM ====================================
REM Configuración de Entorno
REM ====================================
if not defined SPRING_PROFILES_ACTIVE (
    set SPRING_PROFILES_ACTIVE=dev
)

REM Determinar ambiente y cargar configuración específica
if "%SPRING_PROFILES_ACTIVE%"=="prod" (
    set CONFIG_SUFFIX=prod
) else if "%SPRING_PROFILES_ACTIVE%"=="qa" (
    set CONFIG_SUFFIX=qa
) else (
    set CONFIG_SUFFIX=dev
)

REM ====================================
REM Configuración de Java
REM ====================================
if not defined JAVA_HOME (
    set JAVA_HOME=C:\Program Files\Java\jdk-23
)
set JAVA_OPTS=-Xms512m -Xmx2048m -XX:MaxMetaspaceSize=512m
set JAVA_DEBUG_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

REM ====================================
REM Configuración de Logging
REM ====================================
set LOG_PATH=C:/logs/rochling/%SERVICE_NAME%
set LOG_LEVEL=INFO
set LOG_PATTERN=%%d{yyyy-MM-dd HH:mm:ss.SSS} [%%thread] %-5level %%logger{36} - %%msg%%n
set LOG_MAX_HISTORY=30
set LOG_MAX_FILE_SIZE=100MB
set LOG_TOTAL_SIZE_CAP=20GB

REM ====================================
REM Configuración de Elasticsearch
REM ====================================
set ELASTICSEARCH_HOSTS=localhost:9200
set ELASTICSEARCH_USERNAME=elastic
set ELASTICSEARCH_PASSWORD=changeme
set ELASTICSEARCH_INDEX_PREFIX=logs
set ELASTICSEARCH_SHARDS=2
set ELASTICSEARCH_REPLICAS=1
set ELASTICSEARCH_REFRESH_INTERVAL=5s

REM ====================================
REM Configuración de Monitoreo
REM ====================================
set ENABLE_METRICS=true
set METRICS_PATH=/metrics
set HEALTH_PATH=/health
set PROMETHEUS_ENDPOINT=/prometheus

REM ====================================
REM Configuración de API
REM ====================================
set SERVER_PORT=8080
set SERVER_SERVLET_CONTEXT_PATH=/api/v1/logging
set API_DOCS_PATH=/api-docs
set SWAGGER_UI_PATH=/swagger-ui
set MAX_HTTP_HEADER_SIZE=8KB
set SERVER_TOMCAT_MAX_THREADS=200
set SERVER_TOMCAT_MIN_SPARE_THREADS=20

REM ====================================
REM Configuración de Seguridad
REM ====================================
set SECURITY_ENABLED=true
set JWT_SECRET_KEY=your-secret-key-here
set JWT_EXPIRATION=86400
set ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
set RATE_LIMIT_ENABLED=true
set RATE_LIMIT_REQUESTS_PER_SECOND=100

REM ====================================
REM Configuración de Cache
REM ====================================
set CACHE_TYPE=caffeine
set CACHE_MAXIMUM_SIZE=1000
set CACHE_EXPIRE_AFTER_WRITE=3600

REM ====================================
REM Configuración de Resiliencia
REM ====================================
set CIRCUIT_BREAKER_ENABLED=true
set RETRY_MAX_ATTEMPTS=3
set RETRY_DELAY=1000
set TIMEOUT_DURATION=5000

REM ====================================
REM Configuración de Mensajería
REM ====================================
set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
set KAFKA_GROUP_ID=%SERVICE_NAME%
set KAFKA_TOPIC_LOGS=app-logs
set KAFKA_TOPIC_ALERTS=log-alerts

REM ====================================
REM Configuración de Auditoría
REM ====================================
set AUDIT_ENABLED=true
set AUDIT_TRAIL_TOPIC=audit-trail
set AUDIT_LOG_DIRECTORY=%LOG_PATH%/audit

REM ====================================
REM Configuración de Backup
REM ====================================
set BACKUP_ENABLED=true
set BACKUP_SCHEDULE=0 0 1 * * ?
set BACKUP_RETENTION_DAYS=90
set BACKUP_PATH=C:/backup/logs

REM ====================================
REM Configuración de Alertas
REM ====================================
set ALERT_EMAIL_ENABLED=true
set SMTP_HOST=smtp.empresa.com
set SMTP_PORT=587
set SMTP_USERNAME=alerts@empresa.com
set ALERT_RECIPIENTS=admin@empresa.com,soporte@empresa.com

REM ====================================
REM Mostrar Configuración
REM ====================================
if "%DEBUG_MODE%"=="true" (
    echo [CONFIG] Versión del Servicio: %SERVICE_VERSION%
    echo [CONFIG] Perfil Activo: %SPRING_PROFILES_ACTIVE%
    echo [CONFIG] Puerto del Servidor: %SERVER_PORT%
    echo [CONFIG] Ruta de Logs: %LOG_PATH%
    echo [CONFIG] Nivel de Log: %LOG_LEVEL%
)