REM scripts/development/dev-setup.bat
@echo off
echo Configurando entorno de desarrollo...
call mvn clean install -DskipTests
echo Configuración completada.