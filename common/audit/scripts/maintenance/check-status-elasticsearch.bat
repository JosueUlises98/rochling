@echo off
setlocal EnableDelayedExpansion

:: Configuración de rutas y logging
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%..\..\"
set "LOG_PATH=%ROOT_DIR%logs"
set "LOGFILE=%LOG_PATH%\elasticsearch-status.log"
set "CONFIG_FILE=%ROOT_DIR%resources\elasticsearch-config.properties"

:: Configuración de Elasticsearch
set "ES_HOST=localhost"
set "ES_PORT=9200"
set "ES_TIMEOUT=10"

:: Crear directorio de logs si no existe
if not exist "%LOG_PATH%" mkdir "%LOG_PATH%"

:: Función para logging
:log
echo [%date% %time%] %* >> "%LOGFILE%"
goto :eof

:: Título y banner
title Monitor de Estado Elasticsearch
echo ============================================
echo      MONITOR DE ESTADO ELASTICSEARCH
echo ============================================
echo.

:: Logging del inicio
call :log "INFO: Iniciando verificación de estado de Elasticsearch"

:: Verificar si curl está disponible
where curl >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: curl no está instalado o no está en el PATH
    call :log "ERROR: curl no encontrado en el sistema"
    goto :error
)

:: Crear archivo temporal para la respuesta
set "TEMP_FILE=%TEMP%\es_health_%RANDOM%.tmp"

echo Verificando estado de Elasticsearch...
echo Host: %ES_HOST%:%ES_PORT%
echo.

:: Intentar conexión con Elasticsearch
curl -s -m %ES_TIMEOUT% -o "%TEMP_FILE%" -w "%%{http_code}" "http://%ES_HOST%:%ES_PORT%/_cat/health" > "%TEMP_FILE%_status"
set /p STATUS=<"%TEMP_FILE%_status"

if "%STATUS%"=="200" (
    :: Mostrar información de estado
    echo Estado del cluster:
    echo ----------------------------------------
    type "%TEMP_FILE%"
    call :log "INFO: Elasticsearch funcionando correctamente"

    :: Obtener información adicional
    echo.
    echo Información del nodo:
    curl -s "http://%ES_HOST%:%ES_PORT%/_nodes/stats/os,jvm" > "%TEMP_FILE%_nodes"
    type "%TEMP_FILE%_nodes" | findstr "heap_used_percent cpu"
) else (
    echo ERROR: No se puede conectar a Elasticsearch
    echo Código de estado: %STATUS%
    call :log "ERROR: Fallo al conectar con Elasticsearch. Estado: %STATUS%"
    goto :error
)

:: Limpiar archivos temporales
del "%TEMP_FILE%*" 2>nul

echo.
echo ============================================
echo Verificación completada
call :log "INFO: Verificación completada exitosamente"
goto :end

:error
echo.
echo Verificación fallida
call :log "ERROR: Proceso terminado con errores"
exit /b 1

:end
:: Pausa opcional para ver resultados
timeout /t 5 >nul
endlocal
exit /b 0