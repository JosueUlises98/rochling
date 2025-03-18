@echo off
setlocal EnableDelayedExpansion

REM ====================================
REM Script de Configuración de Entorno para Microservicio Logging
REM Versión: 1.0.0
REM ====================================

REM ====================================
REM Configuración de Rutas Base
REM ====================================
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%..\"
set "RESOURCES_DIR=%PROJECT_ROOT%src\main\resources"
set "CONFIG_DIR=%RESOURCES_DIR%\config"
set "CERTS_DIR=%PROJECT_ROOT%certs"

REM ====================================
REM Validación de Directorios y Recursos
REM ====================================
if not exist "%RESOURCES_DIR%" (
    echo [ERROR] No se encuentra el directorio resources: %RESOURCES_DIR%
    exit /b 1
)

if not exist "%RESOURCES_DIR%\ilm.json" (
    echo [ERROR] No se encuentra el archivo ilm.json
    exit /b 1
)

if not exist "%RESOURCES_DIR%\index-template.json" (
    echo [ERROR] No se encuentra el archivo index-template.json
    exit /b 1
)

REM ====================================
REM Validación de Prerequisitos
REM ====================================
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java no está instalado o no está en el PATH
    exit /b 1
)

where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven no está instalado o no está en el PATH
    exit /b 1
)

REM ====================================
REM Configuración de Versión
REM ====================================
if exist "%PROJECT_ROOT%pom.xml" (
    for /f "tokens=*" %%a in ('mvn help:evaluate -Dexpression^=project.version -q -DforceStdout') do (
        set "SERVICE_VERSION=%%a"
    )
) else (
    set "SERVICE_VERSION=1.0.0"
)

REM ====================================
REM Configuración del Servicio
REM ====================================
set "SERVICE_NAME=logging"
set "SERVICE_DESCRIPTION=Microservicio de Logging Empresarial"
set "SERVICE_GROUP=org.kopingenieria.logging"

REM ====================================
REM Configuración de Perfiles
REM ====================================
if "%1"=="" (
    set "SPRING_PROFILES_ACTIVE=dev"
) else (
    set "SPRING_PROFILES_ACTIVE=%1"
)

REM ====================================
REM Configuración de Java
REM ====================================
if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME no está definido
    exit /b 1
)

REM ====================================
REM Configuración específica por ambiente
REM ====================================
if "%SPRING_PROFILES_ACTIVE%"=="dev" (
    set "JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC"
    set "JAVA_DEBUG_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
    set "LOG_LEVEL=DEBUG"
    set "LOG_MAX_FILE_SIZE=10MB"
    set "LOG_MAX_HISTORY=3"
    set "LOG_TOTAL_SIZE_CAP=100MB"
    set "ELASTICSEARCH_HOSTS=http://localhost:9200"
    set "ELASTICSEARCH_PASSWORD=elastic"
    set "ELASTICSEARCH_SHARDS=1"
    set "ELASTICSEARCH_REPLICAS=0"
    set "CORS_ALLOWED_ORIGINS=*"
    set "SSL_ENABLED=false"
    set "REDIS_HOST=localhost"
    set "REDIS_PORT=6379"
    set "CDN_BASE_URL=http://localhost:9000"
) else if "%SPRING_PROFILES_ACTIVE%"=="prod" (
    set "JAVA_OPTS=-Xms1024m -Xmx2048m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"
    set "LOG_LEVEL=WARN"
    set "LOG_MAX_FILE_SIZE=100MB"
    set "LOG_MAX_HISTORY=30"
    set "LOG_TOTAL_SIZE_CAP=3GB"
    set "ELASTICSEARCH_HOSTS=https://elastic-prod.kopingenieria.com:9200"
    set "ELASTICSEARCH_PASSWORD=prod-elastic-pass-2024"
    set "ELASTICSEARCH_SHARDS=3"
    set "ELASTICSEARCH_REPLICAS=2"
    set "CORS_ALLOWED_ORIGINS=https://kopingenieria.com,https://admin.kopingenieria.com"
    set "SSL_ENABLED=true"
    set "REDIS_HOST=redis-prod.kopingenieria.com"
    set "REDIS_PORT=6379"
    set "CDN_BASE_URL=https://cdn.kopingenieria.com"
)

REM ====================================
REM Configuración de Logging
REM ====================================
set "LOG_BASE_DIR=%LOCALAPPDATA%\logs\kopingenieria\%SPRING_PROFILES_ACTIVE%"
set "LOG_PATTERN=%%d{yyyy-MM-dd HH:mm:ss.SSS} [%%thread] [%%X{traceId}] %-5level %%logger{36} - %%msg%%n"

REM ====================================
REM Configuración de Elasticsearch
REM ====================================
set "ELASTICSEARCH_INDEX_PREFIX=kop-logs-%SPRING_PROFILES_ACTIVE%"
set "ELASTICSEARCH_TEMPLATE_NAME=kop-logs-template"
set "ELASTICSEARCH_ILM_POLICY=kop-logs-policy"
set "ELASTICSEARCH_REFRESH_INTERVAL=1s"

REM ====================================
REM Configuración de API
REM ====================================
set "SERVER_PORT=8081"
set "SERVER_SERVLET_CONTEXT_PATH=/api/v1"
set "MAX_HTTP_HEADER_SIZE=8KB"
set "SERVER_TOMCAT_MAX_THREADS=200"
set "SERVER_TOMCAT_MIN_SPARE_THREADS=20"
set LOGGING_API_KEYS=key1,key2,key3
set LOG_BASE_DIR=/var/log/rochling/logging-service
set SERVICE_NAME=logging-service


REM ====================================
REM Configuración de Seguridad
REM ====================================
set "SSL_KEYSTORE_PATH=%CERTS_DIR%\%SPRING_PROFILES_ACTIVE%\keystore.p12"
set "SSL_KEYSTORE_PASSWORD=kop2024SecurePass"
set "SSL_KEY_ALIAS=kopservicelog"
set "JWT_SECRET=k0p1ng3n13r142024S3cur3T0k3nK3y"
set "JWT_EXPIRATION=86400000"
set "JWT_REFRESH_EXPIRATION=604800000"

REM ====================================
REM Configuración de Monitoreo
REM ====================================
set "NEW_RELIC_LICENSE_KEY=nr-license-key-2024"
set "DATADOG_API_KEY=dd-api-key-2024"

REM ====================================
REM Configuración CDN
REM ====================================
set "CDN_ACCESS_KEY=cdn-access-key-2024"
set "CDN_SECRET_KEY=cdn-secret-key-2024"

REM ====================================
REM Configuración Redis
REM ====================================
set "REDIS_PASSWORD=redisKop2024Pass"

REM ====================================
REM Creación de directorios necesarios
REM ====================================
if not exist "%LOG_BASE_DIR%" mkdir "%LOG_BASE_DIR%" 2>nul
if not exist "%LOG_BASE_DIR%\audit" mkdir "%LOG_BASE_DIR%\audit" 2>nul
if not exist "%CERTS_DIR%\%SPRING_PROFILES_ACTIVE%" mkdir "%CERTS_DIR%\%SPRING_PROFILES_ACTIVE%" 2>nul

REM ====================================
REM Resumen de la configuración
REM ====================================
echo.
echo ========== Configuración del Servicio ==========
echo Nombre: %SERVICE_NAME%
echo Versión: %SERVICE_VERSION%
echo Descripción: %SERVICE_DESCRIPTION%
echo Perfil Activo: %SPRING_PROFILES_ACTIVE%
echo.
echo ========== Configuración de Logging ==========
echo Directorio Base: %LOG_BASE_DIR%
echo Nivel: %LOG_LEVEL%
echo Tamaño Máximo: %LOG_MAX_FILE_SIZE%
echo Retención: %LOG_MAX_HISTORY% días
echo Capacidad Total: %LOG_TOTAL_SIZE_CAP%
echo.
echo ========== Configuración de Elasticsearch ==========
echo Hosts: %ELASTICSEARCH_HOSTS%
echo Shards: %ELASTICSEARCH_SHARDS%
echo Réplicas: %ELASTICSEARCH_REPLICAS%
echo.
echo ========== Configuración Java ==========
echo JAVA_OPTS: %JAVA_OPTS%
if "%SPRING_PROFILES_ACTIVE%"=="dev" (
    echo JAVA_DEBUG_OPTS: %JAVA_DEBUG_OPTS%
)
echo.
echo ========== Redis ==========
echo Host: %REDIS_HOST%
echo Puerto: %REDIS_PORT%
echo.
echo ========== Seguridad ==========
echo SSL Habilitado: %SSL_ENABLED%
echo CORS Origins: %CORS_ALLOWED_ORIGINS%
echo.
echo ========== CDN ==========
echo URL Base: %CDN_BASE_URL%
echo.
echo ========== Rutas ==========
echo Recursos: %RESOURCES_DIR%
echo Configuración: %CONFIG_DIR%
echo Certificados: %CERTS_DIR%
echo.
echo ===========================================

REM ====================================
REM Exportar Variables de Entorno
REM ====================================
endlocal & (
    REM Servicio y Ambiente
    set "SERVICE_NAME=%SERVICE_NAME%"
    set "SERVICE_VERSION=%SERVICE_VERSION%"
    set "SERVICE_DESCRIPTION=%SERVICE_DESCRIPTION%"
    set "SERVICE_GROUP=%SERVICE_GROUP%"
    set "SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%"

    REM Configuración Java
    set "JAVA_OPTS=%JAVA_OPTS%"
    if "%SPRING_PROFILES_ACTIVE%"=="dev" (
        set "JAVA_DEBUG_OPTS=%JAVA_DEBUG_OPTS%"
    )

    REM Logging
    set "LOG_BASE_DIR=%LOG_BASE_DIR%"
    set "LOG_LEVEL=%LOG_LEVEL%"
    set "LOG_MAX_FILE_SIZE=%LOG_MAX_FILE_SIZE%"
    set "LOG_MAX_HISTORY=%LOG_MAX_HISTORY%"
    set "LOG_TOTAL_SIZE_CAP=%LOG_TOTAL_SIZE_CAP%"

    REM Elasticsearch
    set "ELASTICSEARCH_HOSTS=%ELASTICSEARCH_HOSTS%"
    set "ELASTICSEARCH_PASSWORD=%ELASTICSEARCH_PASSWORD%"
    set "ELASTICSEARCH_SHARDS=%ELASTICSEARCH_SHARDS%"
    set "ELASTICSEARCH_REPLICAS=%ELASTICSEARCH_REPLICAS%"

    REM Redis
    set "REDIS_HOST=%REDIS_HOST%"
    set "REDIS_PORT=%REDIS_PORT%"

    REM Seguridad
    set "SSL_ENABLED=%SSL_ENABLED%"
    set "CORS_ALLOWED_ORIGINS=%CORS_ALLOWED_ORIGINS%"

    REM CDN
    set "CDN_BASE_URL=%CDN_BASE_URL%"

    REM Rutas
    set "SCRIPT_DIR=%SCRIPT_DIR%"
    set "PROJECT_ROOT=%PROJECT_ROOT%"
    set "RESOURCES_DIR=%RESOURCES_DIR%"
    set "CONFIG_DIR=%CONFIG_DIR%"
    set "CERTS_DIR=%CERTS_DIR%"
)