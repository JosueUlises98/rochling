@echo off
setlocal EnableDelayedExpansion

:: Configuración de rutas y variables
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%..\..\"
set "LOG_PATH=%ROOT_DIR%logs"
set "CONFIG_FILE=%ROOT_DIR%resources\cleanup-config.properties"
set "CLEANUP_LOG=%LOG_PATH%\cleanup-operations.log"
set "RETENTION_DAYS=30"
set "ARCHIVE_DIR=%ROOT_DIR%archive\logs"

:: Función para logging
:log
echo [%date% %time%] %* >> "%CLEANUP_LOG%"
goto :eof

:: Crear directorios necesarios
if not exist "%LOG_PATH%" mkdir "%LOG_PATH%"
if not exist "%ARCHIVE_DIR%" mkdir "%ARCHIVE_DIR%"

:: Título y banner
title Limpieza de Archivos Log
echo ============================================
echo         LIMPIEZA DE ARCHIVOS LOG
echo ============================================
echo.

:: Logging del inicio
call :log "INFO: Iniciando proceso de limpieza de logs"

:: Verificar existencia del directorio de logs
if not exist "%LOG_PATH%" (
    echo ERROR: Directorio de logs no encontrado
    call :log "ERROR: Directorio de logs no existe en: %LOG_PATH%"
    goto :error
)

:: Crear archivo de respaldo con la lista de archivos a eliminar
set "DELETION_LIST=%TEMP%\logs_to_delete_%RANDOM%.txt"
echo Analizando archivos antiguos...
call :log "INFO: Identificando archivos más antiguos de %RETENTION_DAYS% días"

:: Listar archivos a eliminar
forfiles /p "%LOG_PATH%" /s /m *.log /d -%RETENTION_DAYS% /c "cmd /c echo @path" > "%DELETION_LIST%" 2>nul

:: Verificar si hay archivos para eliminar
for %%I in ("%DELETION_LIST%") do if %%~zI==0 (
    echo No se encontraron archivos para limpiar.
    call :log "INFO: No se encontraron archivos que excedan el período de retención"
    del "%DELETION_LIST%" 2>nul
    goto :success
)

:: Crear archivo de respaldo comprimido antes de eliminar
set "ARCHIVE_NAME=%ARCHIVE_DIR%\logs_backup_%date:~-4,4%%date:~-7,2%%date:~-10,2%.zip"
echo Creando respaldo de seguridad...
call :log "INFO: Creando archivo de respaldo: %ARCHIVE_NAME%"

:: Comprimir archivos antiguos antes de eliminarlos
powershell Compress-Archive -Path (Get-Content '%DELETION_LIST%') -DestinationPath '%ARCHIVE_NAME%' -Force

if %ERRORLEVEL% neq 0 (
    echo ERROR: No se pudo crear el archivo de respaldo
    call :log "ERROR: Fallo al crear archivo de respaldo"
    goto :error
)

:: Eliminar archivos antiguos
echo Eliminando archivos antiguos...
for /f "tokens=*" %%F in (%DELETION_LIST%) do (
    del "%%F"
    if !ERRORLEVEL! equ 0 (
        call :log "INFO: Eliminado: %%F"
    ) else (
        call :log "ERROR: No se pudo eliminar: %%F"
    )
)

:: Limpiar archivo temporal
del "%DELETION_LIST%" 2>nul

:success
echo.
echo Proceso de limpieza completado exitosamente.
echo Archivos respaldados en: %ARCHIVE_NAME%
call :log "INFO: Proceso de limpieza completado exitosamente"
goto :end

:error
echo.
echo El proceso de limpieza falló
call :log "ERROR: Proceso terminado con errores"
if exist "%DELETION_LIST%" del "%DELETION_LIST%" 2>nul
exit /b 1

:end
:: Mostrar resumen
echo.
echo Resumen de la operación:
echo - Fecha: %date%
echo - Logs procesados: Ver %CLEANUP_LOG%
echo - Respaldo: %ARCHIVE_NAME%
echo ============================================

:: Pausa opcional
timeout /t 3 >nul
endlocal
exit /b 0