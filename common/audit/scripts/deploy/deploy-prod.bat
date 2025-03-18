@echo off
setlocal EnableDelayedExpansion

REM ====================================
REM Script de Despliegue Producción
REM Versión: 1.0
REM ====================================

REM Configuración de la aplicación
set "APP_NAME=audit"
set "APP_VERSION=1.0.0"
set "SPRING_PROFILES_ACTIVE=prod"
set "LOG_DIR=logs"
set "DEPLOY_LOG=%LOG_DIR%\deploy-prod_%date:~-4,4%%date:~-7,2%%date:~-10,2%.log"
set "BACKUP_DIR=backups"
set "MAX_HEAP=4096m"
set "MIN_HEAP=2048m"
set "APP_PORT=8082"

REM Configuración JVM para producción
set "JAVA_OPTS=-Xms%MIN_HEAP% -Xmx%MAX_HEAP% -XX:+HeapDumpOnOutOfMemoryError"
set "JAVA_OPTS=%JAVA_OPTS% -XX:HeapDumpPath=%LOG_DIR%/heapdump.hprof"
set "JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
set "JAVA_OPTS=%JAVA_OPTS% -Dserver.port=%APP_PORT%"
set "JAVA_OPTS=%JAVA_OPTS% -Dspring.profiles.active=%SPRING_PROFILES_ACTIVE%"

REM ====================================
REM Funciones
REM ====================================
:logMessage
echo [%date% %time%] %~1 >> "%DEPLOY_LOG%"
echo %~1
exit /b

:checkPrerequisites
REM Verificar Java
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] Java no está instalado o no está en el PATH"
    exit /b 1
)

REM Verificar Maven
mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] Maven no está instalado o no está en el PATH"
    exit /b 1
)

REM Verificar espacio en disco (mínimo 5GB)
for /f "tokens=3" %%a in ('dir /-c 2^>nul') do set SPACE=%%a
if %SPACE% LSS 5368709120 (
    call :logMessage "[ERROR] Espacio insuficiente en disco"
    exit /b 1
)
exit /b 0

:createDirectories
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
exit /b 0

:backupCurrentVersion
if exist "target\%APP_NAME%-%APP_VERSION%.jar" (
    call :logMessage "[INFO] Realizando backup de la versión actual..."
    set "BACKUP_FILE=%BACKUP_DIR%\%APP_NAME%-%APP_VERSION%_%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%.jar"
    copy "target\%APP_NAME%-%APP_VERSION%.jar" "%BACKUP_FILE%" >nul
)
exit /b 0

REM ====================================
REM Inicio del Script
REM ====================================
echo.
echo ============================================
echo        Despliegue en PRODUCCION
echo        ¡¡¡ PRECAUCION !!!
echo ============================================
echo.

REM Solicitar confirmación
set /p CONFIRM="¿Está seguro de realizar el despliegue en PRODUCCION? (S/N): "
if /i "%CONFIRM%" neq "S" (
    call :logMessage "[INFO] Despliegue cancelado por el usuario"
    exit /b 0
)

REM Crear directorios necesarios
call :createDirectories

REM Iniciar log
call :logMessage "=== Iniciando despliegue en PRODUCCION ==="
call :logMessage "[INFO] Versión de aplicación: %APP_NAME%-%APP_VERSION%"

REM Verificar prerequisitos
call :checkPrerequisites
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] Fallo en verificación de prerequisitos"
    exit /b 1
)

REM Verificar que estamos en el directorio correcto
if not exist "pom.xml" (
    call :logMessage "[ERROR] No se encuentra pom.xml. Ejecute desde el directorio raíz"
    exit /b 1
)

REM Backup de la versión actual
call :backupCurrentVersion

REM Detener aplicación existente
for /f "tokens=5" %%a in ('netstat -ano ^| find ":%APP_PORT% "') do (
    call :logMessage "[INFO] Deteniendo proceso anterior (PID: %%a)..."
    taskkill /F /PID %%a 2>nul
)

REM Limpiar y construir
call :logMessage "[INFO] Iniciando build del proyecto..."
call mvn clean package -DskipTests -P prod
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] Error en la construcción del proyecto"
    exit /b 1
)

REM Verificar JAR generado
if not exist "target\%APP_NAME%-%APP_VERSION%.jar" (
    call :logMessage "[ERROR] No se encontró el archivo JAR generado"
    exit /b 1
)

REM Desplegar aplicación
call :logMessage "[INFO] Iniciando aplicación..."
start /B javaw %JAVA_OPTS% -jar "target\%APP_NAME%-%APP_VERSION%.jar" >> "%LOG_DIR%\app.log" 2>&1

REM Verificar inicio exitoso
set "RETRY_COUNT=0"
set "MAX_RETRIES=30"

:CHECK_APP
timeout /t 2 /nobreak >nul
curl -s http://localhost:%APP_PORT%/actuator/health >nul 2>&1
if %ERRORLEVEL% equ 0 (
    call :logMessage "[INFO] Aplicación desplegada exitosamente"
    goto :DEPLOY_SUCCESS
)

set /a RETRY_COUNT+=1
if %RETRY_COUNT% geq %MAX_RETRIES% (
    call :logMessage "[ERROR] Error al iniciar la aplicación"
    exit /b 1
)
goto :CHECK_APP

:DEPLOY_SUCCESS
call :logMessage "[INFO] Despliegue en PRODUCCION completado exitosamente"
call :logMessage "[INFO] URL: http://localhost:%APP_PORT%"
call :logMessage "[INFO] Logs disponibles en: %LOG_DIR%\app.log"
call :logMessage "=== Fin del despliegue ==="

echo.
echo Despliegue completado exitosamente
echo Para ver los logs: type "%LOG_DIR%\app.log"
echo.

endlocal