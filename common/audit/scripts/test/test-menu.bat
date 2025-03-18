@echo off
setlocal EnableDelayedExpansion

:: Configuración de rutas y variables
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%..\..\"
set "LOG_PATH=%ROOT_DIR%logs\tests"
set "MENU_LOG=%LOG_PATH%\test-menu.log"
set "CONFIG_FILE=%ROOT_DIR%resources\test-config.properties"
set "LAST_RUN_FILE=%LOG_PATH%\last_test_run.txt"

:: Crear directorio de logs si no existe
if not exist "%LOG_PATH%" mkdir "%LOG_PATH%"

:: Función para logging
:log
echo [%date% %time%] %* >> "%MENU_LOG%"
goto :eof

:: Configuración de colores y título
color 0F
title Panel de Control de Pruebas

:menu_test
cls
call :log "INFO: Menú de pruebas iniciado/actualizado"

echo ================================================
echo             PANEL DE CONTROL DE PRUEBAS
echo ================================================
echo.
echo  [1] Ejecutar Pruebas Unitarias
echo  [2] Ejecutar Pruebas de Integración
echo  [3] Ejecutar Todas las Pruebas
echo  [4] Ver Resultados Anteriores
echo  [5] Limpiar Reportes
echo  [6] Configuración de Pruebas
echo  [7] Volver al Menú Principal
echo.
echo ================================================

:: Mostrar último test ejecutado si existe
if exist "%LAST_RUN_FILE%" (
    echo.
    echo Última prueba ejecutada:
    type "%LAST_RUN_FILE%"
    echo.
)

set /p "TEST_OP=Seleccione una opción (1-7): "
call :log "INFO: Opción seleccionada: %TEST_OP%"

if "%TEST_OP%"=="1" (
    call :ejecutar_unitarias
) else if "%TEST_OP%"=="2" (
    call :ejecutar_integracion
) else if "%TEST_OP%"=="3" (
    call :ejecutar_todas
) else if "%TEST_OP%"=="4" (
    call :ver_resultados
) else if "%TEST_OP%"=="5" (
    call :limpiar_reportes
) else if "%TEST_OP%"=="6" (
    call :configuracion_pruebas
) else if "%TEST_OP%"=="7" (
    goto :salir
) else (
    echo.
    echo Opción no válida. Por favor, intente nuevamente.
    timeout /t 2 >nul
)
goto :menu_test

:ejecutar_unitarias
echo.
echo Ejecutando pruebas unitarias...
call :log "INFO: Iniciando ejecución de pruebas unitarias"
echo [%date% %time%] Pruebas Unitarias > "%LAST_RUN_FILE%"
call "%SCRIPT_DIR%run-unit-tests.bat"
if %ERRORLEVEL% equ 0 (
    echo Pruebas unitarias completadas exitosamente.
) else (
    echo ERROR: Las pruebas unitarias fallaron.
)
pause
goto :eof

:ejecutar_integracion
echo.
echo Ejecutando pruebas de integración...
call :log "INFO: Iniciando ejecución de pruebas de integración"
echo [%date% %time%] Pruebas de Integración > "%LAST_RUN_FILE%"
call "%SCRIPT_DIR%run-integration-tests.bat"
if %ERRORLEVEL% equ 0 (
    echo Pruebas de integración completadas exitosamente.
) else (
    echo ERROR: Las pruebas de integración fallaron.
)
pause
goto :eof

:ejecutar_todas
echo.
echo Ejecutando todas las pruebas...
call :log "INFO: Iniciando ejecución completa de pruebas"
echo [%date% %time%] Suite Completa de Pruebas > "%LAST_RUN_FILE%"
call :ejecutar_unitarias
call :ejecutar_integracion
echo.
echo Ejecución completa finalizada.
pause
goto :eof

:ver_resultados
echo.
echo === Resultados de Pruebas Recientes ===
call :log "INFO: Consultando resultados de pruebas"
echo.
if exist "%ROOT_DIR%target\surefire-reports" (
    dir /b "%ROOT_DIR%target\surefire-reports\TEST-*.xml"
    echo.
    echo Los reportes detallados están en: %ROOT_DIR%target\surefire-reports
) else (
    echo No se encontraron reportes de pruebas recientes.
)
pause
goto :eof

:limpiar_reportes
echo.
echo Limpiando reportes anteriores...
call :log "INFO: Limpiando reportes antiguos"
if exist "%ROOT_DIR%target\surefire-reports" rmdir /s /q "%ROOT_DIR%target\surefire-reports"
if exist "%ROOT_DIR%target\failsafe-reports" rmdir /s /q "%ROOT_DIR%target\failsafe-reports"
echo Reportes eliminados exitosamente.
pause
goto :eof

:configuracion_pruebas
echo.
echo === Configuración de Pruebas ===
call :log "INFO: Accediendo a configuración de pruebas"
echo 1. Directorio de logs: %LOG_PATH%
echo 2. Directorio de reportes: %ROOT_DIR%target
echo 3. Archivo de configuración: %CONFIG_FILE%
pause
goto :eof

:salir
call :log "INFO: Cerrando menú de pruebas"
endlocal
exit /b 0

:error
echo.
echo Ha ocurrido un error en la operación
call :log "ERROR: Operación fallida"
pause
goto :menu_test