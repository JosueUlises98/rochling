@echo off
setlocal EnableDelayedExpansion

:: Configuración de rutas y variables
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%..\..\"
set "LOG_PATH=%ROOT_DIR%logs"
set "METADATA_FILE=%ROOT_DIR%resources\backup-config.properties"
set "TIMESTAMP=%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "TIMESTAMP=%TIMESTAMP: =0%"
set "BACKUP_ROOT=%ROOT_DIR%backups\logs"
set "BACKUP_PATH=%BACKUP_ROOT%\%TIMESTAMP%"
set "LOGFILE=%LOG_PATH%\backup-operations.log"

:: Función para logging
:log
echo [%date% %time%] %* >> "%LOGFILE%"
goto :eof

:: Crear directorios necesarios
if not exist "%LOG_PATH%" mkdir "%LOG_PATH%"
if not exist "%BACKUP_ROOT%" mkdir "%BACKUP_ROOT%"

:: Banner informativo
echo ============================================
echo          RESPALDO DE ARCHIVOS LOG
echo ============================================
echo.

:: Logging del inicio
call :log "INFO: Iniciando proceso de respaldo de logs"

:: Verificar existencia de directorio de logs
if not exist "%LOG_PATH%" (
    echo ERROR: Directorio de logs no encontrado
    call :log "ERROR: Directorio de logs no existe en: %LOG_PATH%"
    exit /b 1
)

:: Crear directorio de respaldo
echo Creando directorio de respaldo...
call :log "INFO: Creando directorio de respaldo en: %BACKUP_PATH%"
mkdir "%BACKUP_PATH%" 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: No se pudo crear el directorio de respaldo
    call :log "ERROR: Fallo al crear directorio de respaldo"
    exit /b 1
)

:: Realizar el respaldo
echo Copiando archivos de log...
call :log "INFO: Iniciando copia de archivos"

xcopy /s /i /y "%LOG_PATH%\*.*" "%BACKUP_PATH%" >nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo en la copia de archivos
    call :log "ERROR: Fallo en la operación de copia"
    exit /b 1
)

:: Crear archivo de metadatos del respaldo
echo Creando metadatos del respaldo...
(
    echo backup.timestamp=%TIMESTAMP%
    echo backup.source=%LOG_PATH%
    echo backup.destination=%BACKUP_PATH%
    echo backup.files=%BACKUP_PATH%\files.txt
) > "%BACKUP_PATH%\backup-info.properties"

:: Crear lista de archivos respaldados
dir /b /s "%BACKUP_PATH%\*.*" > "%BACKUP_PATH%\files.txt"

:: Limpiar respaldos antiguos (mantener últimos 7 días)
echo Limpiando respaldos antiguos...
forfiles /p "%BACKUP_ROOT%" /d -7 /c "cmd /c if @isdir==TRUE rmdir /s /q @path" 2>nul

:: Mensaje de éxito
echo.
echo Respaldo completado exitosamente en: %BACKUP_PATH%
call :log "INFO: Proceso de respaldo completado exitosamente"
echo ============================================

:: Generar resumen
echo.
echo Resumen del respaldo:
echo - Fecha y hora: %TIMESTAMP%
echo - Ubicación: %BACKUP_PATH%
echo - Log detallado: %LOGFILE%

endlocal
exit /b 0