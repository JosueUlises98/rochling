REM scripts/maintenance/check-status.bat
@echo off
echo Verificando estado del servicio...
curl -X GET "localhost:9200/_cat/health"
echo Estado de Elasticsearch verificado.