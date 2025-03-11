REM scripts/maintenance/cleanup-logs.bat
@echo off
echo Limpiando logs antiguos...
forfiles /p "%LOG_PATH%" /s /m *.log /d -30 /c "cmd /c del @path"
echo Limpieza completada.