@echo off
setlocal EnableDelayedExpansion

:: Configuración de logging
set "LOGFILE=%~dp0..\..\logs\dev-run.log"
if not exist "%~dp0..\..\logs" mkdir "%~dp0..\..\logs"

:: Función para logging
:log
echo [%date% %time%] %* >> "%LOGFILE%"
goto :eof

:: Título de la ventana
title Ejecución en Modo Desarrollo

:: Banner informativo
echo ============================================
echo       EJECUCIÓN EN MODO DESARROLLO
echo ============================================
echo.

:: Logging del inicio
call :log "INFO: Iniciando ejecución en modo desarrollo"

:: Configuración del entorno
set "SPRING_PROFILES_ACTIVE=dev"
call :log "INFO: Perfil Spring configurado: %SPRING_PROFILES_ACTIVE%"

:: Verificación de Maven
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven no está instalado o no está en el PATH
    call :log "ERROR: Maven no encontrado en el sistema"
    pause
    exit /b 1
)

:: Ejecución de la aplicación
echo Iniciando aplicación Spring Boot...
call :log "INFO: Iniciando Spring Boot con perfil dev"

:: Intento de ejecución con manejo de errores
mvn spring-boot:run
if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: La aplicación no se pudo iniciar correctamente
    call :log "ERROR: Fallo en la ejecución de Spring Boot. Código de error: %ERRORLEVEL%"
    pause
    exit /b 1
) else (
    call :log "INFO: Aplicación finalizada correctamente"
)

endlocal
exit /b 0