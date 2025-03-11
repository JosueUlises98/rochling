REM scripts/development/dev-menu.bat
@echo off
:MENU_DEV
cls
echo === Menú de Desarrollo ===
echo 1. Configurar entorno
echo 2. Ejecutar en modo desarrollo
echo 3. Volver
set /p DEV_OP="Seleccione opción: "

if "%DEV_OP%"=="1" call dev-setup.bat
if "%DEV_OP%"=="2" call dev-run.bat
if "%DEV_OP%"=="3" exit /b
goto MENU_DEV