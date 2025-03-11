REM scripts/deploy/setup-elasticsearch.bat
@echo off
echo Configurando Elasticsearch...
curl -X PUT "localhost:9200/_ilm/policy/logs-policy" -H "Content-Type: application/json" -d @ilm.json
curl -X PUT "localhost:9200/_template/logs-template" -H "Content-Type: application/json" -d @index-template.json
echo Configuración de Elasticsearch completada.