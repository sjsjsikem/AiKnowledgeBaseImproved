$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

docker compose -f docker/docker-compose.yml up -d

Write-Host "Local infrastructure is starting."
Write-Host "MySQL: localhost:3307 / ai_knowledge_base / root / root123456"
Write-Host "Redis: localhost:6380"
