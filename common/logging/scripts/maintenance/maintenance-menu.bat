REM scripts/maintenance/maintenance-menu.bat
@echo off
:MENU_MAINT
cls
echo === Menú de Mantenimiento ===
echo 1. Limpiar logs
echo 2. Backup de logs
echo 3. Verificar estado
echo 4. Volver
set /p MAINT_OP="Seleccione opción: "

if "%MAINT_OP%"=="1" call cleanup-logs.bat
if "%MAINT_OP%"=="2" call backup-logs.bat
if "%MAINT_OP%"=="3" call check-status.bat
if "%MAINT_OP%"=="4" exit /b
goto MENU_MAINT