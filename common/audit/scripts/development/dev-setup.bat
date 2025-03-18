@echo off
setlocal EnableDelayedExpansion

:: Configuración de logging
set "LOGFILE=%~dp0..\..\logs\dev-setup.log"
if not exist "%~dp0..\..\logs" mkdir "%~dp0..\..\logs"

:: Función para logging
:log
echo [%date% %time%] %* >> "%LOGFILE%"
goto :eof

:: Título de la ventana
title Configuración del Entorno de Desarrollo

:: Banner informativo
echo ============================================
echo    CONFIGURACIÓN ENTORNO DE DESARROLLO
echo ============================================
echo.

:: Logging del inicio
call :log "INFO: Iniciando configuración del entorno de desarrollo"

:: Verificación de Maven
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven no está instalado o no está en el PATH
    call :log "ERROR: Maven no encontrado en el sistema"
    pause
    exit /b 1
)

:: Verificación del archivo pom.xml
if not exist "%~dp0..\..\pom.xml" (
    echo ERROR: No se encuentra el archivo pom.xml
    call :log "ERROR: Archivo pom.xml no encontrado"
    pause
    exit /b 1
)

:: Limpieza y construcción del proyecto
echo Limpiando y construyendo el proyecto...
call :log "INFO: Iniciando limpieza y construcción del proyecto"

:: Ejecución de Maven con manejo de errores
call mvn clean install -DskipTests
if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: La construcción del proyecto falló
    call :log "ERROR: Fallo en la construcción del proyecto. Código: %ERRORLEVEL%"
    pause
    exit /b 1
)

:: Mensaje de éxito
echo.
echo Configuración completada exitosamente.
call :log "INFO: Configuración del entorno completada con éxito"

:: Pausa opcional para ver resultados
timeout /t 3 >nul

endlocal
exit /b 0