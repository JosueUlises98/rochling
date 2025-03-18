@echo off
setlocal EnableDelayedExpansion

:: Configuración de rutas y logging
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%..\..\"
set "LOG_PATH=%ROOT_DIR%logs"
set "CONFIG_FILE=%ROOT_DIR%resources\maintenance-config.properties"
set "MENU_LOG=%LOG_PATH%\maintenance-menu.log"

:: Crear directorio de logs si no existe
if not exist "%LOG_PATH%" mkdir "%LOG_PATH%"

:: Función para logging
:log
echo [%date% %time%] %* >> "%MENU_LOG%"
goto :eof

:: Configuración de colores
color 0F

:menu
cls
call :log "INFO: Menú de mantenimiento iniciado"
title Menú de Mantenimiento del Sistema

echo ================================================
echo           MENÚ DE MANTENIMIENTO
echo ================================================
echo.
echo  [1] Verificar Estado de Elasticsearch
echo  [2] Limpieza de Logs
echo  [3] Respaldar Logs
echo  [4] Monitoreo de Recursos
echo  [5] Ver Logs del Sistema
echo  [6] Configuración
echo  [7] Salir
echo.
echo ================================================

set /p "opcion=Seleccione una opción (1-7): "
call :log "INFO: Opción seleccionada: %opcion%"

if "%opcion%"=="1" (
    call :verificarElasticsearch
) else if "%opcion%"=="2" (
    call :limpiezaLogs
) else if "%opcion%"=="3" (
    call :respaldoLogs
) else if "%opcion%"=="4" (
    call :monitoreoRecursos
) else if "%opcion%"=="5" (
    call :verLogs
) else if "%opcion%"=="6" (
    call :configuracion
) else if "%opcion%"=="7" (
    goto :salir
) else (
    echo.
    echo Opción no válida. Por favor, intente nuevamente.
    timeout /t 2 >nul
    goto :menu
)
goto :menu

:verificarElasticsearch
echo.
echo Verificando estado de Elasticsearch...
call :log "INFO: Iniciando verificación de Elasticsearch"
call "%SCRIPT_DIR%check-status-elasticsearch.bat"
pause
goto :eof

:limpiezaLogs
echo.
echo Iniciando limpieza de logs...
call :log "INFO: Iniciando proceso de limpieza"
call "%SCRIPT_DIR%cleanup-logs.bat"
pause
goto :eof

:respaldoLogs
echo.
echo Iniciando respaldo de logs...
call :log "INFO: Iniciando proceso de respaldo"
call "%SCRIPT_DIR%backup-logs.bat"
pause
goto :eof

:monitoreoRecursos
echo.
echo === Monitor de Recursos ===
call :log "INFO: Iniciando monitoreo de recursos"
echo Uso de CPU:
wmic cpu get loadpercentage
echo.
echo Memoria disponible:
wmic OS get FreePhysicalMemory /Value
echo.
pause
goto :eof

:verLogs
echo.
echo === Últimas entradas de log ===
call :log "INFO: Visualizando logs del sistema"
if exist "%MENU_LOG%" (
    type "%MENU_LOG%" | tail -n 20
) else (
    echo No hay logs disponibles
)
echo.
pause
goto :eof

:configuracion
echo.
echo === Configuración del Sistema ===
call :log "INFO: Accediendo a configuración"
echo 1. Período de retención de logs: %RETENTION_DAYS% días
echo 2. Directorio de logs: %LOG_PATH%
echo 3. Directorio de respaldos: %ROOT_DIR%backups
echo.
pause
goto :eof

:salir
echo.
echo Cerrando menú de mantenimiento...
call :log "INFO: Cerrando menú de mantenimiento"
endlocal
exit /b 0

:error
echo.
echo Ha ocurrido un error en la operación
call :log "ERROR: Operación fallida"
pause
goto :menu