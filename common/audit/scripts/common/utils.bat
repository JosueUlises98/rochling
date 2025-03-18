@echo off
setlocal EnableDelayedExpansion

REM ====================================
REM Utilidades para Microservicio Audit
REM Versión: 1.0.0
REM ====================================

:init
    set "SCRIPT_DIR=%~dp0"
    call "%SCRIPT_DIR%env.bat"
    if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
    goto :main

:main
    if "%1"=="" (
        call :mostrar_ayuda
        exit /b 0
    )

    if "%1"=="help" (
        call :mostrar_ayuda
        exit /b 0
    )

    if "%1"=="status" (
        call :verificar_estado
        exit /b %ERRORLEVEL%
    )

    if "%1"=="clean" (
        call :limpiar_proyecto
        exit /b %ERRORLEVEL%
    )

    if "%1"=="validate" (
        call :validar_configuracion
        exit /b %ERRORLEVEL%
    )

    if "%1"=="backup" (
        call :crear_backup
        exit /b %ERRORLEVEL%
    )

    echo [ERROR] Comando no reconocido: %1
    call :mostrar_ayuda
    exit /b 1

:mostrar_ayuda
    echo.
    echo Utilidades para Microservicio de Auditoria
    echo =========================================
    echo.
    echo Uso: utils.bat [comando] [opciones]
    echo.
    echo Comandos disponibles:
    echo   help      - Muestra esta ayuda
    echo   status    - Verifica el estado del servicio
    echo   clean     - Limpia archivos temporales
    echo   validate  - Valida la configuración
    echo   backup    - Crea respaldo de datos
    echo.
    goto :eof

:verificar_estado
    echo.
    echo Verificando estado del servicio...
    echo ==================================

    REM Verificar Java y Maven
    call :verificar_prerequisitos || exit /b 1

    REM Verificar directorios críticos
    call :verificar_directorios || exit /b 1

    REM Verificar conexiones
    call :verificar_conexiones || exit /b 1

    echo [OK] Todas las verificaciones completadas
    goto :eof

:verificar_prerequisitos
    echo Verificando prerequisitos...

    where java >nul 2>&1 || (
        echo [ERROR] Java no está instalado o no está en el PATH
        exit /b 1
    )

    where mvn >nul 2>&1 || (
        echo [ERROR] Maven no está instalado o no está en el PATH
        exit /b 1
    )

    echo [OK] Prerequisitos verificados
    goto :eof

:verificar_directorios
    echo Verificando directorios...

    if not exist "%RESOURCES_DIR%" (
        echo [ERROR] Directorio resources no encontrado: %RESOURCES_DIR%
        exit /b 1
    )

    if not exist "%CONFIG_DIR%" (
        echo [ERROR] Directorio config no encontrado: %CONFIG_DIR%
        exit /b 1
    )

    if not exist "%CERTS_DIR%" (
        echo [ERROR] Directorio certificados no encontrado: %CERTS_DIR%
        exit /b 1
    )

    echo [OK] Directorios verificados
    goto :eof

:verificar_conexiones
    echo Verificando conexiones...

    REM Verificar Redis
    ping %REDIS_HOST% -n 1 >nul || (
        echo [ADVERTENCIA] No se puede contactar Redis en %REDIS_HOST%
    )

    echo [OK] Conexiones verificadas
    goto :eof

:limpiar_proyecto
    echo.
    echo Limpiando proyecto...
    echo ====================

    REM Limpiar compilaciones previas
    call mvn clean -f "%PROJECT_ROOT%pom.xml" || (
        echo [ERROR] Error al limpiar proyecto Maven
        exit /b 1
    )

    REM Limpiar logs
    if exist "%LOG_BASE_DIR%" (
        echo Limpiando logs antiguos...
        del /F /Q "%LOG_BASE_DIR%\*.log" 2>nul
    )

    REM Limpiar temporales
    if exist "%PROJECT_ROOT%temp" (
        echo Limpiando archivos temporales...
        rmdir /S /Q "%PROJECT_ROOT%temp"
    )

    echo [OK] Limpieza completada
    goto :eof

:validar_configuracion
    echo.
    echo Validando configuración...
    echo =========================

    REM Validar perfil activo
    if "%SPRING_PROFILES_ACTIVE%"=="" (
        echo [ERROR] Perfil Spring no configurado
        exit /b 1
    )

    REM Validar configuración SSL
    if "%SSL_ENABLED%"=="true" (
        if not exist "%SSL_KEYSTORE_PATH%" (
            echo [ERROR] Keystore no encontrado: %SSL_KEYSTORE_PATH%
            exit /b 1
        )
    )

    REM Validar valores críticos
    call :validar_valores_criticos || exit /b 1

    echo [OK] Configuración válida
    goto :eof

:validar_valores_criticos
    if "%SERVICE_NAME%"=="" (
        echo [ERROR] SERVICE_NAME no configurado
        exit /b 1
    )

    if "%SERVICE_VERSION%"=="" (
        echo [ERROR] SERVICE_VERSION no configurado
        exit /b 1
    )

    if "%JWT_SECRET%"=="" (
        echo [ERROR] JWT_SECRET no configurado
        exit /b 1
    )

    echo [OK] Valores críticos validados
    goto :eof

:crear_backup
    echo.
    echo Creando backup...
    echo ================

    set "BACKUP_DIR=%PROJECT_ROOT%backups"
    set "BACKUP_NAME=audit_backup_%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%"
    set "BACKUP_NAME=%BACKUP_NAME: =0%"

    if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

    REM Backup configuración
    echo Respaldando configuración...
    xcopy "%CONFIG_DIR%" "%BACKUP_DIR%\%BACKUP_NAME%\config\" /E /I /H /Y >nul

    REM Backup certificados
    if exist "%CERTS_DIR%" (
        echo Respaldando certificados...
        xcopy "%CERTS_DIR%" "%BACKUP_DIR%\%BACKUP_NAME%\certs\" /E /I /H /Y >nul
    )

    echo [OK] Backup creado en: %BACKUP_DIR%\%BACKUP_NAME%
    goto :eof

endlocal