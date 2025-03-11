REM scripts/deploy/deploy-menu.bat
@echo off
:MENU_DEPLOY
cls
echo === Menú de Despliegue ===
echo 1. Desplegar en desarrollo
echo 2. Desplegar en producción
echo 3. Configurar Elasticsearch
echo 4. Volver
set /p DEPLOY_OP="Seleccione opción: "

if "%DEPLOY_OP%"=="1" call deploy-dev.bat
if "%DEPLOY_OP%"=="2" call deploy-prod.bat
if "%DEPLOY_OP%"=="3" call setup-elasticsearch.bat
if "%DEPLOY_OP%"=="4" exit /b
goto MENU_DEPLOY