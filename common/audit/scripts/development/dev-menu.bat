@echo off
setlocal EnableDelayedExpansion

:: Configuración de logging
set "LOGFILE=%~dp0..\..\logs\dev-menu.log"
if not exist "%~dp0..\..\logs" mkdir "%~dp0..\..\logs"

:: Función para logging
:log
echo [%date% %time%] %* >> "%LOGFILE%"
goto :eof

:MENU_DEV
cls
echo ============================================
echo           MENU DE DESARROLLO
echo ============================================
echo.
echo  1. Configurar entorno
echo  2. Ejecutar en modo desarrollo
echo  3. Volver
echo.
echo ============================================
set /p DEV_OP="Seleccione una opción [1-3]: "

:: Validación de entrada
if not "%DEV_OP%" geq "1" if not "%DEV_OP%" leq "3" (
    call :log "ERROR: Opción inválida seleccionada: %DEV_OP%"
    echo.
    echo Error: Por favor, seleccione una opción válida.
    timeout /t 2 >nul
    goto MENU_DEV
)

:: Procesamiento de opciones
if "%DEV_OP%"=="1" (
    call :log "INFO: Iniciando configuración del entorno"
    call dev-setup.bat
    if errorlevel 1 (
        call :log "ERROR: Error al ejecutar dev-setup.bat"
    ) else (
        call :log "INFO: Configuración del entorno completada"
    )
)

if "%DEV_OP%"=="2" (
    call :log "INFO: Iniciando modo desarrollo"
    call dev-run.bat
    if errorlevel 1 (
        call :log "ERROR: Error al ejecutar dev-run.bat"
    ) else (
        call :log "INFO: Ejecución en modo desarrollo completada"
    )
)

if "%DEV_OP%"=="3" (
    call :log "INFO: Saliendo del menú de desarrollo"
    endlocal
    exit /b 0
)

goto MENU_DEV