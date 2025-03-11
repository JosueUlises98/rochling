REM scripts/test/test-menu.bat
@echo off
:MENU_TEST
cls
echo === Menú de Pruebas ===
echo 1. Ejecutar pruebas unitarias
echo 2. Ejecutar pruebas de integración
echo 3. Volver
set /p TEST_OP="Seleccione opción: "

if "%TEST_OP%"=="1" call run-unit-tests.bat
if "%TEST_OP%"=="2" call run-integration-tests.bat
if "%TEST_OP%"=="3" exit /b
goto MENU_TEST