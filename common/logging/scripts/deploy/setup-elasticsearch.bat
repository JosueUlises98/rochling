@echo off
setlocal EnableDelayedExpansion

REM ====================================
REM Script de Configuración Elasticsearch para Microservicio Logging
REM Versión: 2.1.0
REM ====================================

REM Configuración de rutas con validación adicional
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%..\.."
set "COMMON_LOGGING_DIR=%PROJECT_ROOT%\common\logging"
set "RESOURCES_DIR=%COMMON_LOGGING_DIR%\src\main\resources"
set "LOG_DIR=%SCRIPT_DIR%logs"
set "LOG_FILE=%LOG_DIR%\elastic-setup_%date:~-4,4%%date:~-7,2%%date:~-10,2%.log"

REM Configuración de Elasticsearch
set "ES_HOST=localhost"
set "ES_PORT=9200"
set "ES_URL=http://%ES_HOST%:%ES_PORT%"
set "RETRY_ATTEMPTS=3"
set "WAIT_TIME=5"

REM Configuración de nombres de archivos y políticas
set "ILM_FILE=ilm.json"
set "TEMPLATE_FILE=index-template.json"
set "ILM_POLICY_NAME=logs-policy"
set "TEMPLATE_NAME=logs-template"
set "INDEX_PATTERN=logs-*"

REM ====================================
REM Funciones Mejoradas
REM ====================================
:logMessage
echo [%date% %time%] %~1 >> "%LOG_FILE%"
echo %~1
exit /b

:validatePaths
REM Validación mejorada de rutas
if not exist "%PROJECT_ROOT%" (
    call :logMessage "[ERROR] No se encuentra el directorio raíz del proyecto"
    exit /b 1
)

if not exist "%COMMON_LOGGING_DIR%" (
    call :logMessage "[ERROR] No se encuentra el módulo common/logging"
    exit /b 1
)

if not exist "%RESOURCES_DIR%" (
    call :logMessage "[ERROR] No se encuentra el directorio de recursos"
    exit /b 1
)

REM Validación específica de archivos de configuración
if not exist "%RESOURCES_DIR%\%ILM_FILE%" (
    call :logMessage "[ERROR] No se encuentra el archivo %ILM_FILE% en %RESOURCES_DIR%"
    exit /b 1
)

if not exist "%RESOURCES_DIR%\%TEMPLATE_FILE%" (
    call :logMessage "[ERROR] No se encuentra el archivo %TEMPLATE_FILE% en %RESOURCES_DIR%"
    exit /b 1
)
exit /b 0

:checkPrerequisites
REM Verificar curl
where curl >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] curl no está instalado o no está en el PATH"
    exit /b 1
)

REM Verificar conexión a Elasticsearch con timeout
curl -s -f --connect-timeout 5 "%ES_URL%/_cluster/health" >nul
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] No se puede conectar a Elasticsearch en %ES_URL%"
    call :logMessage "[INFO] Verifique que Elasticsearch esté ejecutándose en %ES_HOST%:%ES_PORT%"
    exit /b 1
)

exit /b 0

:createDirectories
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
exit /b 0

:backupCurrentConfig
set "BACKUP_DIR=%SCRIPT_DIR%backups\elasticsearch\%date:~-4,4%%date:~-7,2%%date:~-10,2%"
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

call :logMessage "[INFO] Realizando backup de configuración actual..."
curl -s "%ES_URL%/_ilm/policy/%ILM_POLICY_NAME%" > "%BACKUP_DIR%\%ILM_FILE%.backup" 2>nul
curl -s "%ES_URL%/_template/%TEMPLATE_NAME%" > "%BACKUP_DIR%\%TEMPLATE_FILE%.backup" 2>nul
exit /b 0

:validateJsonContent
call :logMessage "[INFO] Validando contenido de archivos JSON..."
set "TEMP_JSON=%TEMP%\temp_json_validate.txt"

REM Validación detallada de ILM
type "%RESOURCES_DIR%\%ILM_FILE%" > "%TEMP_JSON%"
findstr /r /c:"\"policy\"" "%TEMP_JSON%" >nul
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] El archivo %ILM_FILE% no tiene la estructura correcta de política ILM"
    exit /b 1
)

REM Validación detallada de Template
type "%RESOURCES_DIR%\%TEMPLATE_FILE%" > "%TEMP_JSON%"
findstr /r /c:"\"index_patterns\"" "%TEMP_JSON%" >nul
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] El archivo %TEMPLATE_FILE% no tiene la estructura correcta de template"
    exit /b 1
)
exit /b 0

:setupElasticsearch
call :logMessage "[INFO] Configurando política ILM %ILM_POLICY_NAME%..."
set "ATTEMPT=0"
:retryIlm
set /a ATTEMPT+=1
curl -s -X PUT "%ES_URL%/_ilm/policy/%ILM_POLICY_NAME%" ^
     -H "Content-Type: application/json" ^
     --data-binary @"%RESOURCES_DIR%\%ILM_FILE%" > "%TEMP%\es_response.txt" 2>&1

type "%TEMP%\es_response.txt" | findstr "acknowledged.:.*true" >nul
if %ERRORLEVEL% neq 0 (
    if %ATTEMPT% lss %RETRY_ATTEMPTS% (
        call :logMessage "[ADVERTENCIA] Reintentando configuración ILM... (Intento !ATTEMPT!)"
        timeout /t %WAIT_TIME% /nobreak >nul
        goto retryIlm
    )
    call :logMessage "[ERROR] Error al configurar política ILM"
    type "%TEMP%\es_response.txt"
    exit /b 1
)

call :logMessage "[INFO] Configurando plantilla de índice %TEMPLATE_NAME%..."
set "ATTEMPT=0"
:retryTemplate
set /a ATTEMPT+=1
curl -s -X PUT "%ES_URL%/_template/%TEMPLATE_NAME%" ^
     -H "Content-Type: application/json" ^
     --data-binary @"%RESOURCES_DIR%\%TEMPLATE_FILE%" > "%TEMP%\es_response.txt" 2>&1

type "%TEMP%\es_response.txt" | findstr "acknowledged.:.*true" >nul
if %ERRORLEVEL% neq 0 (
    if %ATTEMPT% lss %RETRY_ATTEMPTS% (
        call :logMessage "[ADVERTENCIA] Reintentando configuración de plantilla... (Intento !ATTEMPT!)"
        timeout /t %WAIT_TIME% /nobreak >nul
        goto retryTemplate
    )
    call :logMessage "[ERROR] Error al configurar plantilla de índice"
    type "%TEMP%\es_response.txt"
    exit /b 1
)
exit /b 0

:verifySetup
call :logMessage "[INFO] Verificando configuración..."

REM Verificación detallada de la política ILM
curl -s "%ES_URL%/_ilm/policy/%ILM_POLICY_NAME%?pretty" > "%TEMP%\verify_ilm.json"
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] No se pudo verificar la política ILM"
    exit /b 1
)

REM Verificación detallada de la plantilla
curl -s "%ES_URL%/_template/%TEMPLATE_NAME%?pretty" > "%TEMP%\verify_template.json"
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] No se pudo verificar la plantilla"
    exit /b 1
)

REM Verificar contenido específico
findstr /r /c:"policy" "%TEMP%\verify_ilm.json" >nul
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] La política ILM no se configuró correctamente"
    exit /b 1
)

findstr /r /c:"index_patterns" "%TEMP%\verify_template.json" >nul
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] La plantilla no se configuró correctamente"
    exit /b 1
)

call :logMessage "[INFO] Configuración verificada correctamente"
exit /b 0

REM ====================================
REM Inicio del Script
REM ====================================
echo.
echo ================================================
echo   Configuración de Elasticsearch para Logging
echo ================================================
echo.

REM Crear directorios necesarios
call :createDirectories

REM Iniciar log
call :logMessage "=== Iniciando configuración de Elasticsearch para Logging ==="
call :logMessage "[INFO] Usando configuración del directorio: %RESOURCES_DIR%"

REM Validación de rutas
call :validatePaths
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] Error en la validación de rutas"
    goto error
)

REM Verificar prerequisitos
call :checkPrerequisites
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] Error en prerequisitos"
    goto error
)

REM Validar contenido JSON
call :validateJsonContent
if %ERRORLEVEL% neq 0 (
    call :logMessage "[ERROR] Error en la validación de JSON"
    goto error
)

REM Realizar backup
call :backupCurrentConfig

REM Configurar Elasticsearch
call :setupElasticsearch
if %ERRORLEVEL% neq 0 goto error

REM Verificar configuración
call :verifySetup
if %ERRORLEVEL% neq 0 goto error

call :logMessage "[INFO] Configuración completada exitosamente"
echo.
echo Configuración completada exitosamente
echo Ver detalles en: %LOG_FILE%
exit /b 0

:error
call :logMessage "[ERROR] La configuración falló"
echo.
echo Error durante la configuración
echo Ver detalles en: %LOG_FILE%
exit /b 1