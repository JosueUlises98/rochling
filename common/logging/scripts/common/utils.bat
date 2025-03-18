@echo off
setlocal EnableDelayedExpansion

REM ====================================
REM Configuración Global
REM ====================================
set "SCRIPT_VERSION=1.0.0"
set "SCRIPT_NAME=%~nx0"
set "RESOURCES_DIR=%~dp0..\src\main\resources"

REM ====================================
REM Validación de Recursos
REM ====================================
:VALIDATE_RESOURCES
call :LOG_INFO "Validando recursos del módulo logging..."
set "_RESOURCE_ERRORS=0"

REM Validar estructura de directorios
if not exist "%RESOURCES_DIR%" (
    call :LOG_ERROR "Directorio resources no encontrado: %RESOURCES_DIR%"
    set /a _RESOURCE_ERRORS+=1
)

REM Validar archivos de configuración necesarios
for %%f in (
    "ilm.json"
    "index-template.json"
) do (
    if not exist "%RESOURCES_DIR%\%%f" (
        call :LOG_ERROR "Archivo requerido no encontrado: %%f"
        set /a _RESOURCE_ERRORS+=1
    )
)

if %_RESOURCE_ERRORS% gtr 0 (
    call :LOG_ERROR "Faltan %_RESOURCE_ERRORS% archivos/directorios requeridos"
    exit /b 1
)

REM ====================================
REM Funciones de Logging y Visualización
REM ====================================
:LOG_INFO
echo [%date% %time%] [INFO] %~1
exit /b 0

:LOG_WARN
echo [%date% %time%] [WARN] %~1 1>&2
exit /b 0

:LOG_ERROR
echo [%date% %time%] [ERROR] %~1 1>&2
exit /b 0

:LOG_DEBUG
if /i "%DEBUG_MODE%"=="true" (
    echo [%date% %time%] [DEBUG] %~1
)
exit /b 0

REM ====================================
REM Funciones de Validación Elasticsearch
REM ====================================
:VALIDATE_ES_CONFIG
call :LOG_INFO "Validando configuración de Elasticsearch..."

REM Validar JSON files
for %%f in ("%RESOURCES_DIR%\*.json") do (
    call :VALIDATE_JSON "%%f" || (
        call :LOG_ERROR "JSON inválido: %%f"
        exit /b 1
    )
)
exit /b 0

:VALIDATE_JSON
if "%~1"=="" exit /b 1
type "%~1" | findstr /r /c:"^{.*}$" >nul 2>&1
exit /b %ERRORLEVEL%

REM ====================================
REM Funciones de Backup
REM ====================================
:BACKUP_CONFIG
set "_BACKUP_DIR=%~dp0backups\%date:~-4,4%%date:~-7,2%%date:~-10,2%"
call :CREATE_DIRECTORY "%_BACKUP_DIR%"

call :BACKUP_FILES "%RESOURCES_DIR%" "%_BACKUP_DIR%"
if !ERRORLEVEL! neq 0 (
    call :LOG_ERROR "Error al realizar backup de configuración"
    exit /b 1
)
exit /b 0

REM ====================================
REM Funciones de Sistema
REM ====================================
:CREATE_DIRECTORY
if "%~1"=="" (
    call :LOG_ERROR "CREATE_DIRECTORY: Ruta no especificada"
    exit /b 1
)

if not exist "%~1" (
    mkdir "%~1" 2>nul || (
        call :LOG_ERROR "No se pudo crear el directorio: %~1"
        exit /b 1
    )
    call :LOG_INFO "Directorio creado: %~1"
)
exit /b 0

:VALIDATE_ELASTICSEARCH
call :LOG_INFO "Verificando conexión con Elasticsearch..."
curl -s "http://%ELASTICSEARCH_HOSTS%/_cluster/health" >nul
if !ERRORLEVEL! neq 0 (
    call :LOG_ERROR "No se puede conectar a Elasticsearch"
    exit /b 1
)
call :LOG_INFO "Conexión a Elasticsearch verificada"
exit /b 0

REM ====================================
REM Función Principal
REM ====================================
:MAIN
call :VALIDATE_RESOURCES || exit /b 1
call :VALIDATE_ENVIRONMENT || exit /b 1
call :VALIDATE_ES_CONFIG || exit /b 1
call :BACKUP_CONFIG || exit /b 1

call :LOG_INFO "Utilidades inicializadas correctamente"
exit /b 0

REM Ejecutar función principal
call :MAIN