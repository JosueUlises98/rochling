@echo off
setlocal EnableDelayedExpansion

:: Configuración de rutas y variables
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%..\..\"
set "LOG_PATH=%ROOT_DIR%logs\tests"
set "TEST_LOG=%LOG_PATH%\integration-tests.log"
set "CONFIG_FILE=%ROOT_DIR%resources\test-config.properties"
set "REPORT_DIR=%ROOT_DIR%target\integration-reports"
set "ERROR_LOG=%LOG_PATH%\integration-errors.log"

:: Crear estructura de directorios
if not exist "%LOG_PATH%" mkdir "%LOG_PATH%"
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"

:: Función para logging
:log
echo [%date% %time%] %* >> "%TEST_LOG%"
goto :eof

:: Banner de inicio
echo ================================================
echo        EJECUCIÓN DE PRUEBAS DE INTEGRACIÓN
echo ================================================
echo.

:: Logging del inicio
call :log "INFO: Iniciando ejecución de pruebas de integración"

:: Verificar requisitos previos
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven no está instalado o no está en el PATH
    call :log "ERROR: Maven no encontrado en el sistema"
    goto :error
)

:: Verificar JAVA_HOME
if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME no está configurado
    call :log "ERROR: JAVA_HOME no definido"
    goto :error
)

:: Limpieza previa
echo Limpiando reportes anteriores...
if exist "%REPORT_DIR%" (
    rmdir /s /q "%REPORT_DIR%"
    mkdir "%REPORT_DIR%"
)

:: Timestamp para el reporte
set "TIMESTAMP=%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "TIMESTAMP=%TIMESTAMP: =0%"

echo.
echo Iniciando pruebas de integración...
echo Timestamp: %TIMESTAMP%
echo Log: %TEST_LOG%
echo.

:: Ejecutar pruebas
call :log "INFO: Ejecutando mvn verify con perfil de integración"
call mvn verify -P integration-test -Dtest.timestamp=%TIMESTAMP% ^
    -Dtest.output.dir="%REPORT_DIR%" ^
    -Dlog.file="%TEST_LOG%" ^
    -Dfailsafe.reportsDirectory="%REPORT_DIR%" ^
    --log-file "%TEST_LOG%" 2>> "%ERROR_LOG%"

set TEST_RESULT=%ERRORLEVEL%

:: Verificar resultado
if %TEST_RESULT% equ 0 (
    echo.
    echo Pruebas de integración completadas exitosamente.
    call :log "INFO: Pruebas de integración completadas con éxito"
) else (
    echo.
    echo ERROR: Las pruebas de integración han fallado.
    echo Revise el log de errores en: %ERROR_LOG%
    call :log "ERROR: Pruebas de integración fallidas con código %TEST_RESULT%"
    goto :error
)

:: Generar resumen
echo.
echo === Resumen de Ejecución ===
echo Fecha: %date%
echo Hora: %time%
echo Reportes: %REPORT_DIR%
echo Log completo: %TEST_LOG%

:: Mostrar estadísticas básicas si existen
if exist "%REPORT_DIR%\failsafe-summary.xml" (
    echo.
    echo === Estadísticas ===
    findstr "completed failures errors skipped" "%REPORT_DIR%\failsafe-summary.xml"
)

goto :end

:error
echo.
echo El proceso de pruebas ha fallado
call :log "ERROR: Proceso terminado con errores"
exit /b 1

:end
echo.
echo ================================================
call :log "INFO: Proceso de pruebas finalizado"

:: Pausa opcional
timeout /t 5
endlocal
exit /b %TEST_RESULT%