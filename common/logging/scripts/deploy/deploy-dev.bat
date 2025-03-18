@echo off
setlocal EnableDelayedExpansion

REM ====================================
REM Configuración de Despliegue
REM ====================================
set "APP_NAME=logging"
set "APP_VERSION=1.0.0"
set "SPRING_PROFILES_ACTIVE=dev"
set "JAVA_OPTS=-Xms512m -Xmx1024m -XX:MaxMetaspaceSize=256m"
set "APP_PORT=8081"
set "DEPLOY_LOG=deploy-dev.log"

REM ====================================
REM Validación Inicial
REM ====================================
echo [%date% %time%] Iniciando despliegue en ambiente DEV... | tee -a %DEPLOY_LOG%

REM Verificar Java
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java no está instalado o no está en el PATH | tee -a %DEPLOY_LOG%
    exit /b 1
)

REM Verificar Maven
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven no está instalado o no está en el PATH | tee -a %DEPLOY_LOG%
    exit /b 1
)

REM Verificar que estamos en el directorio correcto
if not exist "pom.xml" (
    echo [ERROR] No se encuentra el archivo pom.xml. Ejecute el script desde el directorio raíz del proyecto | tee -a %DEPLOY_LOG%
    exit /b 1
)

REM ====================================
REM Verificar puerto disponible
REM ====================================
netstat -ano | find ":%APP_PORT% " >nul
if %ERRORLEVEL% equ 0 (
    echo [WARN] El puerto %APP_PORT% está en uso. Verificando procesos... | tee -a %DEPLOY_LOG%
    for /f "tokens=5" %%a in ('netstat -ano ^| find ":%APP_PORT% "') do (
        echo [INFO] Terminando proceso %%a | tee -a %DEPLOY_LOG%
        taskkill /F /PID %%a 2>nul
        if !ERRORLEVEL! neq 0 (
            echo [ERROR] No se pudo liberar el puerto %APP_PORT% | tee -a %DEPLOY_LOG%
            exit /b 1
        )
    )
)

REM ====================================
REM Limpieza previa
REM ====================================
echo [INFO] Limpiando build anterior... | tee -a %DEPLOY_LOG%
if exist "target" rmdir /s /q "target"

REM ====================================
REM Construcción del proyecto
REM ====================================
echo [INFO] Iniciando build del proyecto... | tee -a %DEPLOY_LOG%
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Error durante la construcción del proyecto | tee -a %DEPLOY_LOG%
    exit /b 1
)

REM ====================================
REM Verificación del JAR
REM ====================================
set "JAR_FILE=target\%APP_NAME%-%APP_VERSION%.jar"
if not exist "%JAR_FILE%" (
    echo [ERROR] No se encontró el archivo JAR: %JAR_FILE% | tee -a %DEPLOY_LOG%
    exit /b 1
)

REM ====================================
REM Despliegue
REM ====================================
echo [INFO] Iniciando despliegue de la aplicación... | tee -a %DEPLOY_LOG%

REM Crear archivo de PID
set "PID_FILE=%APP_NAME%.pid"

REM Ejecutar la aplicación
echo [INFO] Ejecutando aplicación... | tee -a %DEPLOY_LOG%
start /B java %JAVA_OPTS% ^
    -Dspring.profiles.active=%SPRING_PROFILES_ACTIVE% ^
    -Dserver.port=%APP_PORT% ^
    -jar %JAR_FILE% ^
    > app.log 2>&1

REM Guardar PID
for /f "tokens=2" %%a in ('tasklist /fi "imagename eq java.exe" /fo list /v ^| find "PID:"') do (
    echo %%a > %PID_FILE%
    echo [INFO] Aplicación iniciada con PID: %%a | tee -a %DEPLOY_LOG%
)

REM ====================================
REM Verificación de Despliegue
REM ====================================
echo [INFO] Esperando que la aplicación inicie... | tee -a %DEPLOY_LOG%
set "RETRY_COUNT=0"
set "MAX_RETRIES=30"

:CHECK_APP
timeout /t 1 /nobreak >nul
curl -s http://localhost:%APP_PORT%/actuator/health >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [INFO] Aplicación desplegada exitosamente | tee -a %DEPLOY_LOG%
    echo [INFO] URL: http://localhost:%APP_PORT% | tee -a %DEPLOY_LOG%
    goto :END
)

set /a RETRY_COUNT+=1
if %RETRY_COUNT% geq %MAX_RETRIES% (
    echo [ERROR] Tiempo de espera agotado. La aplicación no respondió | tee -a %DEPLOY_LOG%
    if exist "%PID_FILE%" (
        for /f %%i in (%PID_FILE%) do (
            taskkill /F /PID %%i 2>nul
        )
        del %PID_FILE%
    )
    exit /b 1
)
goto :CHECK_APP

:END
echo [INFO] Despliegue completado exitosamente | tee -a %DEPLOY_LOG%
echo [INFO] Logs disponibles en app.log | tee -a %DEPLOY_LOG%
endlocal