REM scripts/deploy/deploy-prod.bat
@echo off
echo Desplegando en producción...
set SPRING_PROFILES_ACTIVE=prod
call mvn clean package -DskipTests
java -jar target/logging-1.0.0.jar