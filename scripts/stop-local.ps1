$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

docker compose -f docker/docker-compose.yml down

Write-Host "Local infrastructure stopped."
