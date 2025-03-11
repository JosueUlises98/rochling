REM scripts/test/run-integration-tests.bat
@echo off
echo Ejecutando pruebas de integración...
call mvn verify -P integration-test
echo Pruebas de integración completadas.