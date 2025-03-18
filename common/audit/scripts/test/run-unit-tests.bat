@echo off
setlocal EnableDelayedExpansion

:: Configuración de rutas y variables
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%..\..\"
set "LOG_PATH=%ROOT_DIR%logs\tests"
set "UNIT_TEST_LOG=%LOG_PATH%\unit-tests.log"
set "CONFIG_FILE=%ROOT_DIR%resources\test-config.properties"
set "REPORT_DIR=%ROOT_DIR%target\surefire-reports"
set "COVERAGE_DIR=%ROOT_DIR%target\site\jacoco"
set "ERROR_LOG=%LOG_PATH%\unit-test-errors.log"

:: Crear estructura de directorios
if not exist "%LOG_PATH%" mkdir "%LOG_PATH%"
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"
if not exist "%COVERAGE_DIR%" mkdir "%COVERAGE_DIR%"

:: Función para logging
:log
echo [%date% %time%] %* >> "%UNIT_TEST_LOG%"
goto :eof

:: Banner de inicio
echo ================================================
echo           EJECUCIÓN DE PRUEBAS UNITARIAS
echo ================================================
echo.

:: Logging del inicio
call :log "INFO: Iniciando ejecución de pruebas unitarias"

:: Verificar entorno
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven no está instalado o no está en el PATH
    call :log "ERROR: Maven no encontrado en el sistema"
    goto :error
)

if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME no está configurado
    call :log "ERROR: Variable JAVA_HOME no definida"
    goto :error
)

:: Limpiar reportes anteriores
echo Limpiando reportes anteriores...
if exist "%REPORT_DIR%" (
    rmdir /s /q "%REPORT_DIR%"
    mkdir "%REPORT_DIR%"
)

:: Timestamp para el reporte
set "TIMESTAMP=%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "TIMESTAMP=%TIMESTAMP: =0%"

echo.
echo Configuración del entorno de pruebas:
echo - Java: %JAVA_HOME%
echo - Timestamp: %TIMESTAMP%
echo - Log: %UNIT_TEST_LOG%
echo.

:: Ejecutar pruebas unitarias
echo Ejecutando pruebas unitarias...
call :log "INFO: Iniciando ejecución de pruebas con Maven"

call mvn test ^
    -Dtest.timestamp=%TIMESTAMP% ^
    -Dsurefire.reportsDirectory="%REPORT_DIR%" ^
    -Djacoco.destFile="%COVERAGE_DIR%\jacoco.exec" ^
    -Dlog.file="%UNIT_TEST_LOG%" ^
    --log-file "%UNIT_TEST_LOG%" 2>> "%ERROR_LOG%"

set TEST_RESULT=%ERRORLEVEL%

:: Verificar resultado
if %TEST_RESULT% equ 0 (
    echo.
    echo Pruebas unitarias completadas exitosamente.
    call :log "INFO: Pruebas unitarias completadas con éxito"

    :: Generar reporte de cobertura
    echo Generando reporte de cobertura...
    call mvn jacoco:report >> "%UNIT_TEST_LOG%" 2>> "%ERROR_LOG%"
) else (
    echo.
    echo ERROR: Las pruebas unitarias han fallado.
    echo Revise el log de errores en: %ERROR_LOG%
    call :log "ERROR: Pruebas unitarias fallidas con código %TEST_RESULT%"
    goto :error
)

:: Generar resumen
echo.
echo === Resumen de Ejecución ===
echo Fecha: %date%
echo Hora: %time%
echo.
echo Resultados disponibles en:
echo - Reportes: %REPORT_DIR%
echo - Cobertura: %COVERAGE_DIR%
echo - Log: %UNIT_TEST_LOG%

:: Mostrar estadísticas de pruebas
if exist "%REPORT_DIR%\TEST-*.xml" (
    echo.
    echo === Estadísticas de Pruebas ===
    findstr /R /C:"tests=\"[0-9]*\"" /C:"failures=\"[0-9]*\"" /C:"errors=\"[0-9]*\"" /C:"skipped=\"[0-9]*\"" "%REPORT_DIR%\TEST-*.xml"
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
call :log "INFO: Proceso de pruebas unitarias finalizado"

:: Pausa opcional
timeout /t 5
endlocal
exit /b %TEST_RESULT%