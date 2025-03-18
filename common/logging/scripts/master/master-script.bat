@echo off
setlocal EnableDelayedExpansion

:: Configuración de rutas y variables
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%"
set "LOG_PATH=%ROOT_DIR%logs"
set "MASTER_LOG=%LOG_PATH%\master.log"
set "CONFIG_PATH=%ROOT_DIR%resources"
set "ENV_CONFIG=%CONFIG_PATH%\environment.properties"
set "SESSION_LOG=%LOG_PATH%\session_%date:~-4,4%%date:~-7,2%%date:~-10,2%.log"

:: Crear estructura de directorios
if not exist "%LOG_PATH%" mkdir "%LOG_PATH%"
if not exist "%CONFIG_PATH%" mkdir "%CONFIG_PATH%"

:: Función para logging
:log
echo [%date% %time%] %* >> "%MASTER_LOG%"
echo [%date% %time%] %* >> "%SESSION_LOG%"
goto :eof

:: Cargar configuración del entorno
if exist "%ENV_CONFIG%" (
    for /f "tokens=1,2 delims==" %%a in (%ENV_CONFIG%) do (
        set "%%a=%%b"
    )
)

:: Inicialización
title Panel de Administración - Servicio de Logging
color 0F
call :log "INFO: Iniciando Panel de Administración"

:: Verificar scripts necesarios
set "SCRIPTS_OK=true"
for %%i in (development\dev-menu.bat test\test-menu.bat deploy\deploy-menu.bat maintenance\maintenance-menu.bat) do (
    if not exist "scripts\%%i" (
        echo ERROR: No se encuentra el script: %%i
        call :log "ERROR: Script no encontrado: %%i"
        set "SCRIPTS_OK=false"
    )
)

if "%SCRIPTS_OK%"=="false" (
    echo.
    echo ERROR: Faltan componentes necesarios.
    echo Contacte al administrador del sistema.
    pause
    exit /b 1
)

:menu
cls
call :log "INFO: Mostrando menú principal"

echo ================================================
echo         PANEL DE ADMINISTRACIÓN PRINCIPAL
echo ================================================
echo.
echo  [1] Entorno de Desarrollo
echo  [2] Gestión de Pruebas
echo  [3] Sistema de Despliegue
echo  [4] Herramientas de Mantenimiento
echo  [5] Estado del Sistema
echo  [6] Configuración
echo  [7] Salir
echo.
echo ================================================
echo.

:: Mostrar información del sistema
echo Estado actual:
echo - Fecha: %date%
echo - Hora: %time%
echo - Usuario: %USERNAME%
echo - Sesión: %SESSION_LOG%
echo.

set /p "OPCION=Seleccione una opción (1-7): "
call :log "INFO: Opción seleccionada: %OPCION%"

if "%OPCION%"=="1" (
    call :desarrollo
) else if "%OPCION%"=="2" (
    call :pruebas
) else if "%OPCION%"=="3" (
    call :despliegue
) else if "%OPCION%"=="4" (
    call :mantenimiento
) else if "%OPCION%"=="5" (
    call :estado_sistema
) else if "%OPCION%"=="6" (
    call :configuracion
) else if "%OPCION%"=="7" (
    goto :salir
) else (
    echo.
    echo Opción no válida. Por favor, intente nuevamente.
    timeout /t 2 >nul
    goto :menu
)
goto :menu

:desarrollo
call :log "INFO: Accediendo al menú de desarrollo"
call "scripts\development\dev-menu.bat"
if errorlevel 1 call :log "WARN: Menú de desarrollo terminó con errores"
goto :eof

:pruebas
call :log "INFO: Accediendo al menú de pruebas"
call "scripts\test\test-menu.bat"
if errorlevel 1 call :log "WARN: Menú de pruebas terminó con errores"
goto :eof

:despliegue
call :log "INFO: Accediendo al menú de despliegue"
call "scripts\deploy\deploy-menu.bat"
if errorlevel 1 call :log "WARN: Menú de despliegue terminó con errores"
goto :eof

:mantenimiento
call :log "INFO: Accediendo al menú de mantenimiento"
call "scripts\maintenance\maintenance-menu.bat"
if errorlevel 1 call :log "WARN: Menú de mantenimiento terminó con errores"
goto :eof

:estado_sistema
echo.
echo === Estado del Sistema ===
call :log "INFO: Consultando estado del sistema"
echo.
echo Espacio en disco:
wmic logicaldisk get deviceid,size,freespace
echo.
echo Memoria:
wmic OS get FreePhysicalMemory,TotalVisibleMemorySize /Value
echo.
echo Servicios:
sc query "ElasticSearch"
pause
goto :eof

:configuracion
echo.
echo === Configuración del Sistema ===
call :log "INFO: Accediendo a configuración"
echo 1. Ruta de logs: %LOG_PATH%
echo 2. Configuración: %CONFIG_PATH%
echo 3. Sesión actual: %SESSION_LOG%
pause
goto :eof

:salir
echo.
echo Cerrando Panel de Administración...
call :log "INFO: Cerrando Panel de Administración"
timeout /t 2 >nul
endlocal
exit /b 0

:error
echo.
echo Ha ocurrido un error en la operación
call :log "ERROR: Operación fallida"
pause
goto :menu
