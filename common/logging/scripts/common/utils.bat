@echo off
REM scripts/common/utils.bat

REM ====================================
REM Funciones de Logging y Visualización
REM ====================================

:LOG_INFO
echo [%date% %time%] [INFO] %~1
goto :EOF

:LOG_WARN
echo [%date% %time%] [WARN] %~1
goto :EOF

:LOG_ERROR
echo [%date% %time%] [ERROR] %~1
goto :EOF

:LOG_DEBUG
if "%DEBUG_MODE%"=="true" (
    echo [%date% %time%] [DEBUG] %~1
)
goto :EOF

REM ====================================
REM Funciones de Validación
REM ====================================

:VALIDATE_ENVIRONMENT
call :LOG_INFO "Validando ambiente..."
if not defined JAVA_HOME (
    call :LOG_ERROR "JAVA_HOME no está configurado"
    exit /b 1
)
if not defined ELASTICSEARCH_HOSTS (
    call :LOG_ERROR "ELASTICSEARCH_HOSTS no está configurado"
    exit /b 1
)
if not defined LOG_PATH (
    call :LOG_ERROR "LOG_PATH no está configurado"
    exit /b 1
)
call :LOG_INFO "Validación de ambiente completada"
goto :EOF

:CHECK_DEPENDENCIES
call :LOG_INFO "Verificando dependencias..."
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :LOG_ERROR "Java no está instalado"
    exit /b 1
)
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :LOG_ERROR "Maven no está instalado"
    exit /b 1
)
where curl >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :LOG_ERROR "Curl no está instalado"
    exit /b 1
)
call :LOG_INFO "Verificación de dependencias completada"
goto :EOF

REM ====================================
REM Funciones de Sistema
REM ====================================

:CREATE_DIRECTORY
if not exist "%~1" (
    mkdir "%~1"
    call :LOG_INFO "Directorio creado: %~1"
)
goto :EOF

:DELETE_DIRECTORY
if exist "%~1" (
    rmdir /s /q "%~1"
    call :LOG_INFO "Directorio eliminado: %~1"
)
goto :EOF

:BACKUP_FILES
set "SOURCE=%~1"
set "DEST=%~2"
if not exist "%DEST%" mkdir "%DEST%"
xcopy /s /e /i "%SOURCE%" "%DEST%"
if %ERRORLEVEL% equ 0 (
    call :LOG_INFO "Backup completado: %SOURCE% -> %DEST%"
) else (
    call :LOG_ERROR "Error en backup: %SOURCE%"
    exit /b 1
)
goto :EOF

REM ====================================
REM Funciones de Red
REM ====================================

:CHECK_PORT_AVAILABLE
netstat -an | find ":%~1 " > nul
if %ERRORLEVEL% equ 0 (
    call :LOG_ERROR "Puerto %~1 está en uso"
    exit /b 1
)
call :LOG_INFO "Puerto %~1 está disponible"
goto :EOF

:WAIT_FOR_SERVICE
set "URL=%~1"
set "TIMEOUT=%~2"
set "RETRY_COUNT=0"

:RETRY_CONNECTION
curl -s -f %URL% > nul
if %ERRORLEVEL% equ 0 (
    call :LOG_INFO "Servicio disponible: %URL%"
    goto :EOF
)
set /a RETRY_COUNT+=1
if %RETRY_COUNT% geq %TIMEOUT% (
    call :LOG_ERROR "Tiempo de espera agotado para: %URL%"
    exit /b 1
)
timeout /t 1 > nul
goto :RETRY_CONNECTION

REM ====================================
REM Funciones de Elasticsearch
REM ====================================

:CHECK_ELASTICSEARCH_CONNECTION
call :LOG_INFO "Verificando conexión con Elasticsearch..."
curl -s %ELASTICSEARCH_HOSTS%/_cluster/health > nul
if %ERRORLEVEL% neq 0 (
    call :LOG_ERROR "No se puede conectar a Elasticsearch"
    exit /b 1
)
call :LOG_INFO "Conexión con Elasticsearch establecida"
goto :EOF

:CREATE_INDEX_TEMPLATE
set "TEMPLATE_FILE=%~1"
set "TEMPLATE_NAME=%~2"
if not exist "%TEMPLATE_FILE%" (
    call :LOG_ERROR "Archivo de template no encontrado: %TEMPLATE_FILE%"
    exit /b 1
)
curl -X PUT "%ELASTICSEARCH_HOSTS%/_template/%TEMPLATE_NAME%" -H "Content-Type: application/json" -d @%TEMPLATE_FILE%
if %ERRORLEVEL% neq 0 (
    call :LOG_ERROR "Error al crear template de índice"
    exit /b 1
)
call :LOG_INFO "Template de índice creado: %TEMPLATE_NAME%"
goto :EOF

REM ====================================
REM Funciones de Utilidad
REM ====================================

:GET_TIMESTAMP
set "TIMESTAMP=%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "TIMESTAMP=%TIMESTAMP: =0%"
goto :EOF

:PAUSE_SCRIPT
echo.
echo Presione cualquier tecla para continuar...
pause > nul
goto :EOF

:SHOW_SPINNER
set "SPINNER_CHARS=/-\|"
set /a SPINNER_INDEX=(%~1 %% 4)
<nul set /p ".=%SPINNER_CHARS:~%SPINNER_INDEX%,1%" <nul
goto :EOF

REM ====================================
REM Funciones de Maven
REM ====================================

:MVN_BUILD
call :LOG_INFO "Iniciando build de Maven..."
call mvn clean package -DskipTests %*
if %ERRORLEVEL% neq 0 (
    call :LOG_ERROR "Error en build de Maven"
    exit /b 1
)
call :LOG_INFO "Build de Maven completado"
goto :EOF

:MVN_TEST
call :LOG_INFO "Ejecutando pruebas Maven..."
call mvn test %*
if %ERRORLEVEL% neq 0 (
    call :LOG_ERROR "Error en pruebas de Maven"
    exit /b 1
)
call :LOG_INFO "Pruebas de Maven completadas"
goto :EOF

REM ====================================
REM Funciones de Menu
REM ====================================

:DISPLAY_MENU_HEADER
echo ============================================
echo %~1
echo ============================================
echo.
goto :EOF

:GET_USER_INPUT
set /p "%~1=%~2"
goto :EOF

:CLEAR_SCREEN
cls
goto :EOF