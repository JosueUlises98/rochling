@echo off
setlocal EnableDelayedExpansion

REM ====================================
REM Configuración del Sistema
REM ====================================
set "SCRIPT_DIR=%~dp0"
set "LOG_DIR=%SCRIPT_DIR%logs"
set "BACKUP_DIR=%SCRIPT_DIR%backups"
set "LOG_FILE=%LOG_DIR%\deploy-menu_%date:~-4,4%%date:~-7,2%%date:~-10,2%.log"
set "CONFIG_FILE=%SCRIPT_DIR%config\deploy-config.ini"
set "TITLE=Sistema de Despliegue v1.0.0"
set "MAX_LOG_DAYS=30"

REM ====================================
REM Funciones de Utilidad
REM ====================================
:createDirectories
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
if not exist "%SCRIPT_DIR%config" mkdir "%SCRIPT_DIR%config"
exit /b 0

:LOG
echo [%date% %time%] %~1 >> "%LOG_FILE%"
echo %~1
exit /b

:ROTATE_LOGS
forfiles /P "%LOG_DIR%" /M *.log /D -%MAX_LOG_DAYS% /C "cmd /c del @path" 2>nul
exit /b 0

:CHECK_PREREQS
call :LOG "[INFO] Verificando prerequisitos..."

REM Verificar Java
java -version >nul 2>&1 || (
    call :LOG "[ERROR] Java no está instalado o no está en el PATH"
    exit /b 1
)

REM Verificar Maven
mvn -version >nul 2>&1 || (
    call :LOG "[ERROR] Maven no está instalado o no está en el PATH"
    exit /b 1
)

REM Verificar Git
git --version >nul 2>&1 || (
    call :LOG "[ADVERTENCIA] Git no está instalado"
)

REM Verificar espacio en disco
for /f "tokens=3" %%a in ('dir /-c 2^>nul') do set SPACE=%%a
if %SPACE% LSS 5368709120 (
    call :LOG "[ADVERTENCIA] Espacio en disco bajo"
)

call :LOG "[INFO] Verificación de prerequisitos completada"
exit /b 0

:UPDATE_CONFIG
echo %~1=%~2> "%CONFIG_FILE%"
exit /b 0

:READ_CONFIG
if exist "%CONFIG_FILE%" (
    for /f "tokens=1,2 delims==" %%a in ('type "%CONFIG_FILE%"') do (
        set "%%a=%%b"
    )
)
exit /b 0

:SHOW_DIAGNOSTICS
cls
echo ================================================================
echo                    Herramientas de Diagnóstico
echo ================================================================
echo [1] Verificar estado de servicios
echo [2] Mostrar procesos Java
echo [3] Verificar puertos en uso
echo [4] Estado de memoria
echo [5] Verificar logs de errores
echo [6] Volver
echo ================================================================
set /p DIAG_OP="Seleccione una opción [1-6]: "

if "%DIAG_OP%"=="1" (
    call :LOG "[INFO] Verificando servicios..."
    sc query state= all | findstr "SERVICE_NAME STATE"
    pause
    goto SHOW_DIAGNOSTICS
)
if "%DIAG_OP%"=="2" (
    call :LOG "[INFO] Mostrando procesos Java..."
    tasklist | findstr "java.exe"
    pause
    goto SHOW_DIAGNOSTICS
)
if "%DIAG_OP%"=="3" (
    call :LOG "[INFO] Verificando puertos..."
    netstat -ano | findstr "LISTENING"
    pause
    goto SHOW_DIAGNOSTICS
)
if "%DIAG_OP%"=="4" (
    call :LOG "[INFO] Estado de memoria..."
    systeminfo | findstr "Memory"
    pause
    goto SHOW_DIAGNOSTICS
)
if "%DIAG_OP%"=="5" (
    call :LOG "[INFO] Mostrando logs de errores..."
    findstr /i "error exception failed" "%LOG_FILE%"
    pause
    goto SHOW_DIAGNOSTICS
)
if "%DIAG_OP%"=="6" goto MENU_DEPLOY
goto SHOW_DIAGNOSTICS

:SHOW_LOGS
cls
echo ================================================================
echo                         Visor de Logs
echo ================================================================
echo [1] Ver log de despliegue actual
echo [2] Ver logs de error
echo [3] Ver todos los logs
echo [4] Limpiar logs antiguos
echo [5] Volver
echo ================================================================
set /p LOG_OP="Seleccione una opción [1-5]: "

if "%LOG_OP%"=="1" (
    type "%LOG_FILE%"
    pause
    goto SHOW_LOGS
)
if "%LOG_OP%"=="2" (
    findstr /i "error warning" "%LOG_FILE%"
    pause
    goto SHOW_LOGS
)
if "%LOG_OP%"=="3" (
    dir /b /s "%LOG_DIR%\*.log"
    set /p LOG_SELECT="Seleccione archivo (nombre completo): "
    if exist "!LOG_SELECT!" type "!LOG_SELECT!"
    pause
    goto SHOW_LOGS
)
if "%LOG_OP%"=="4" (
    call :ROTATE_LOGS
    call :LOG "[INFO] Logs antiguos eliminados"
    pause
    goto SHOW_LOGS
)
if "%LOG_OP%"=="5" goto MENU_DEPLOY
goto SHOW_LOGS

REM ====================================
REM Inicialización
REM ====================================
call :createDirectories
title %TITLE%
call :CHECK_PREREQS
if %ERRORLEVEL% neq 0 (
    pause
    exit /b 1
)

REM Crear/leer configuración
if not exist "%CONFIG_FILE%" (
    call :UPDATE_CONFIG "last_environment" "dev"
    call :UPDATE_CONFIG "deploy_count" "0"
)
call :READ_CONFIG

REM ====================================
REM Menú Principal
REM ====================================
:MENU_DEPLOY
cls
echo ================================================================
echo                     %TITLE%
echo ================================================================
echo.
echo  [1] Desplegar en Desarrollo
echo  [2] Desplegar en Producción
echo  [3] Configurar Elasticsearch
echo  [4] Herramientas de Diagnóstico
echo  [5] Ver Logs
echo  [6] Configuración
echo  [7] Salir
echo.
echo ================================================================
echo  Último despliegue: %last_environment%
echo  Total despliegues: %deploy_count%
echo ================================================================

set "DEPLOY_OP="
set /p DEPLOY_OP="Seleccione una opción [1-7]: "

REM Validación de entrada
if not defined DEPLOY_OP goto INVALID_OPTION
if %DEPLOY_OP% LSS 1 goto INVALID_OPTION
if %DEPLOY_OP% GTR 7 goto INVALID_OPTION

REM ====================================
REM Procesamiento de Opciones
REM ====================================
if "%DEPLOY_OP%"=="1" (
    call :LOG "[INFO] Iniciando despliegue en desarrollo..."
    call :UPDATE_CONFIG "last_environment" "dev"
    set /a deploy_count+=1
    call :UPDATE_CONFIG "deploy_count" "%deploy_count%"
    call "%SCRIPT_DIR%deploy-dev.bat"
    if !ERRORLEVEL! neq 0 (
        call :LOG "[ERROR] Falló el despliegue en desarrollo"
    ) else (
        call :LOG "[INFO] Despliegue en desarrollo completado"
    )
    pause
)

if "%DEPLOY_OP%"=="2" (
    echo [ADVERTENCIA] ¿Está seguro de desplegar en PRODUCCIÓN? (S/N)
    set /p CONFIRM=""
    if /i "!CONFIRM!"=="S" (
        call :LOG "[INFO] Iniciando despliegue en producción..."
        call :UPDATE_CONFIG "last_environment" "prod"
        set /a deploy_count+=1
        call :UPDATE_CONFIG "deploy_count" "%deploy_count%"
        call "%SCRIPT_DIR%deploy-prod.bat"
        if !ERRORLEVEL! neq 0 (
            call :LOG "[ERROR] Falló el despliegue en producción"
        ) else (
            call :LOG "[INFO] Despliegue en producción completado"
        )
    ) else (
        call :LOG "[INFO] Despliegue en producción cancelado"
    )
    pause
)

if "%DEPLOY_OP%"=="3" (
    call :LOG "[INFO] Configurando Elasticsearch..."
    call "%SCRIPT_DIR%setup-elasticsearch.bat"
    pause
)

if "%DEPLOY_OP%"=="4" (
    goto SHOW_DIAGNOSTICS
)

if "%DEPLOY_OP%"=="5" (
    goto SHOW_LOGS
)

if "%DEPLOY_OP%"=="6" (
    start notepad "%CONFIG_FILE%"
)

if "%DEPLOY_OP%"=="7" (
    call :LOG "[INFO] Saliendo del sistema..."
    exit /b 0
)

goto MENU_DEPLOY

:INVALID_OPTION
call :LOG "[ERROR] Opción inválida"
timeout /t 2 >nul
goto MENU_DEPLOY