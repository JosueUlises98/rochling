@echo off
REM master-script.bat

setlocal enabledelayedexpansion
call scripts\common\env.bat

:MENU
cls
echo ======================================
echo    Servicio de Logging - Panel Admin
echo ======================================
echo.
echo 1. Entorno de Desarrollo
echo 2. Pruebas
echo 3. Despliegue
echo 4. Mantenimiento
echo 5. Salir
echo.
set /p OPCION="Seleccione una opción: "

if "%OPCION%"=="1" goto DESARROLLO
if "%OPCION%"=="2" goto PRUEBAS
if "%OPCION%"=="3" goto DESPLIEGUE
if "%OPCION%"=="4" goto MANTENIMIENTO
if "%OPCION%"=="5" goto FIN

:DESARROLLO
call scripts\development\dev-menu.bat
goto MENU

:PRUEBAS
call scripts\test\test-menu.bat
goto MENU

:DESPLIEGUE
call scripts\deploy\deploy-menu.bat
goto MENU

:MANTENIMIENTO
call scripts\maintenance\maintenance-menu.bat
goto MENU

:FIN
echo Saliendo...
exit /b 0