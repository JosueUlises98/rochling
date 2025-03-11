REM scripts/deploy/deploy-dev.bat
@echo off
echo Desplegando en desarrollo...
set SPRING_PROFILES_ACTIVE=dev
call mvn clean package -DskipTests
java -jar target/logging-1.0.0.jar