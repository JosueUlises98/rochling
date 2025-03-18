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
set "SERVICE_NAME=audit"
set "SERVICE_DESCRIPTION=Microservicio de Auditoria Empresarial"
set "SERVICE_GROUP=org.kopingenieria.audit"

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
    set "CORS_ALLOWED_ORIGINS=*"
    set "SSL_ENABLED=false"
    set "REDIS_HOST=localhost"
    set "REDIS_PORT=6379"
    set "CDN_BASE_URL=http://localhost:9000"
) else if "%SPRING_PROFILES_ACTIVE%"=="prod" (
    set "JAVA_OPTS=-Xms1024m -Xmx2048m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"
    set "CORS_ALLOWED_ORIGINS=https://kopingenieria.com,https://admin.kopingenieria.com"
    set "SSL_ENABLED=true"
    set "REDIS_HOST=redis-prod.kopingenieria.com"
    set "REDIS_PORT=6379"
    set "CDN_BASE_URL=https://cdn.kopingenieria.com"
)

REM ====================================
REM Configuración de API
REM ====================================
set "SERVER_PORT=8082"
set "SERVER_SERVLET_CONTEXT_PATH=/api/v1"
set "MAX_HTTP_HEADER_SIZE=8KB"
set "SERVER_TOMCAT_MAX_THREADS=200"
set "SERVER_TOMCAT_MIN_SPARE_THREADS=20"
set LOGGING_SERVICE_URL=http://logging-service:8089
set LOGGING_API_KEY=key1
set LOG_BASE_DIR=/var/log/rochling/audit-service
set LOG_LEVEL=INFO

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
echo Perfil activo: %SPRING_PROFILES_ACTIVE%
echo Nombre del servicio: %SERVICE_NAME%
echo Descripción: %SERVICE_DESCRIPTION%
echo Versión: %SERVICE_VERSION%
echo Grupo: %SERVICE_GROUP%
echo.
echo ========== Configuración del Servidor ==========
echo Puerto: %SERVER_PORT%
echo Context Path: %SERVER_SERVLET_CONTEXT_PATH%
echo Max Threads: %SERVER_TOMCAT_MAX_THREADS%
echo SSL Habilitado: %SSL_ENABLED%
echo.
echo ========== Configuración Redis ==========
echo Host: %REDIS_HOST%
echo Puerto: %REDIS_PORT%
echo.
echo ========== Configuración Java ==========
echo JAVA_OPTS: %JAVA_OPTS%
if "%SPRING_PROFILES_ACTIVE%"=="dev" (
    echo JAVA_DEBUG_OPTS: %JAVA_DEBUG_OPTS%
)
echo.
echo ========== Rutas ==========
echo Project Root: %PROJECT_ROOT%
echo Resources: %RESOURCES_DIR%
echo Certificados: %CERTS_DIR%
echo.
echo ========== Configuración CDN ==========
echo Base URL: %CDN_BASE_URL%
echo.
echo ========== Seguridad ==========
echo CORS Origins: %CORS_ALLOWED_ORIGINS%
echo SSL Enabled: %SSL_ENABLED%
echo.
echo ===========================================

REM ====================================
REM Exportar Variables de Entorno
REM ====================================
endlocal & (
    REM Variables del Servicio
    set "SERVICE_NAME=%SERVICE_NAME%"
    set "SERVICE_VERSION=%SERVICE_VERSION%"
    set "SERVICE_DESCRIPTION=%SERVICE_DESCRIPTION%"
    set "SERVICE_GROUP=%SERVICE_GROUP%"

    REM Configuración Spring y Java
    set "SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%"
    set "JAVA_OPTS=%JAVA_OPTS%"
    if "%SPRING_PROFILES_ACTIVE%"=="dev" (
        set "JAVA_DEBUG_OPTS=%JAVA_DEBUG_OPTS%"
    )

    REM Configuración del Servidor
    set "SERVER_PORT=%SERVER_PORT%"
    set "SERVER_SERVLET_CONTEXT_PATH=%SERVER_SERVLET_CONTEXT_PATH%"
    set "MAX_HTTP_HEADER_SIZE=%MAX_HTTP_HEADER_SIZE%"
    set "SERVER_TOMCAT_MAX_THREADS=%SERVER_TOMCAT_MAX_THREADS%"
    set "SERVER_TOMCAT_MIN_SPARE_THREADS=%SERVER_TOMCAT_MIN_SPARE_THREADS%"

    REM Configuración de Seguridad
    set "SSL_ENABLED=%SSL_ENABLED%"
    set "SSL_KEYSTORE_PATH=%SSL_KEYSTORE_PATH%"
    set "SSL_KEYSTORE_PASSWORD=%SSL_KEYSTORE_PASSWORD%"
    set "SSL_KEY_ALIAS=%SSL_KEY_ALIAS%"
    set "JWT_SECRET=%JWT_SECRET%"
    set "JWT_EXPIRATION=%JWT_EXPIRATION%"
    set "JWT_REFRESH_EXPIRATION=%JWT_REFRESH_EXPIRATION%"
    set "CORS_ALLOWED_ORIGINS=%CORS_ALLOWED_ORIGINS%"

    REM Configuración de Monitoreo
    set "NEW_RELIC_LICENSE_KEY=%NEW_RELIC_LICENSE_KEY%"
    set "DATADOG_API_KEY=%DATADOG_API_KEY%"

    REM Configuración CDN
    set "CDN_BASE_URL=%CDN_BASE_URL%"
    set "CDN_ACCESS_KEY=%CDN_ACCESS_KEY%"
    set "CDN_SECRET_KEY=%CDN_SECRET_KEY%"

    REM Configuración Redis
    set "REDIS_HOST=%REDIS_HOST%"
    set "REDIS_PORT=%REDIS_PORT%"

    REM Configuración de Rutas
    set "SCRIPT_DIR=%SCRIPT_DIR%"
    set "PROJECT_ROOT=%PROJECT_ROOT%"
    set "RESOURCES_DIR=%RESOURCES_DIR%"
    set "CONFIG_DIR=%CONFIG_DIR%"
    set "CERTS_DIR=%CERTS_DIR%"
)
