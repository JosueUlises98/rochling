REM scripts/maintenance/backup-logs.bat
@echo off
echo Realizando backup de logs...
set BACKUP_PATH=C:\backup\logs\%date:~-4,4%%date:~-7,2%%date:~-10,2%
mkdir "%BACKUP_PATH%"
xcopy /s /i "%LOG_PATH%\*.*" "%BACKUP_PATH%"
echo Backup completado.