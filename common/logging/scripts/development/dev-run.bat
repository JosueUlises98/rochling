REM scripts/development/dev-run.bat
@echo off
echo Ejecutando en modo desarrollo...
set SPRING_PROFILES_ACTIVE=dev
call mvn spring-boot:run