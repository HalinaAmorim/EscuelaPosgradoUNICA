#Requires -Version 5.1
<#
.SYNOPSIS
  Sobe SonarQube (Docker) e analisa o backend Java.
.NOTES
  Pré-requisito: Docker Desktop em execução.
#>
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

Write-Host "==> 1/5 Verificando Docker..." -ForegroundColor Cyan
docker info 1>$null 2>$null
if ($LASTEXITCODE -ne 0) {
  Write-Error "Docker Desktop nao esta em execucao. Abra o Docker Desktop e rode este script novamente."
}

Write-Host "==> 2/5 Subindo SonarQube Community (porta 9000)..." -ForegroundColor Cyan
$existing = docker ps -a --filter "name=sonarqube-epu" --format "{{.Names}}"
if (-not $existing) {
  docker run -d --name sonarqube-epu -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -p 9000:9000 sonarqube:community
} else {
  docker start sonarqube-epu | Out-Null
}

Write-Host "==> Aguardando SonarQube ficar pronto (pode levar 1-3 min)..." -ForegroundColor Yellow
$ready = $false
for ($i = 1; $i -le 60; $i++) {
  try {
    $r = Invoke-WebRequest -Uri "http://localhost:9000/api/system/status" -UseBasicParsing -TimeoutSec 5
    if ($r.Content -match '"status":"UP"') { $ready = $true; break }
  } catch { }
  Start-Sleep -Seconds 5
  Write-Host "  tentativa $i/60..."
}
if (-not $ready) { Write-Error "SonarQube nao subiu a tempo. Verifique: docker logs sonarqube-epu" }

Write-Host "==> 3/5 Compilando backend com Maven (Docker)..." -ForegroundColor Cyan
foreach ($mod in @("Autenticacion","Intranet","Matricula")) {
  Write-Host "  compile $mod..."
  docker run --rm -v "${Root}/backend/${mod}:/app" -w /app maven:3.9-eclipse-temurin-21 `
    mvn -q -DskipTests compile
  if ($LASTEXITCODE -ne 0) { Write-Error "Falha ao compilar $mod" }
}

Write-Host "==> 4/5 Criando token de analise (admin/admin)..." -ForegroundColor Cyan
# Login inicial: admin/admin — se ja mudou, ajuste SONAR_TOKEN abaixo
$auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
$tokenName = "epu-agent-$(Get-Date -Format 'yyyyMMddHHmmss')"
try {
  $tokenResp = Invoke-RestMethod -Method Post `
    -Uri "http://localhost:9000/api/user_tokens/generate?name=$tokenName" `
    -Headers @{ Authorization = "Basic $auth" }
  $sonarToken = $tokenResp.token
} catch {
  Write-Host "Nao foi possivel gerar token automaticamente (senha admin pode ter sido alterada)." -ForegroundColor Yellow
  Write-Host "Crie um token em http://localhost:9000 (My Account > Security) e defina:" -ForegroundColor Yellow
  Write-Host '  $env:SONAR_TOKEN = "seu_token"' -ForegroundColor Yellow
  if (-not $env:SONAR_TOKEN) { Write-Error "Defina SONAR_TOKEN e rode novamente a partir do passo 5." }
  $sonarToken = $env:SONAR_TOKEN
}

Write-Host "==> 5/5 Executando sonar-scanner..." -ForegroundColor Cyan
# No Docker Desktop (Windows), o container acessa o host via host.docker.internal
docker run --rm `
  -e SONAR_HOST_URL="http://host.docker.internal:9000" `
  -e SONAR_TOKEN="$sonarToken" `
  -v "${Root}:/usr/src" `
  sonarsource/sonar-scanner-cli

Write-Host ""
Write-Host "Analise concluida." -ForegroundColor Green
Write-Host "Dashboard: http://localhost:9000/dashboard?id=escuela-posgrado-unica-backend" -ForegroundColor Green
Write-Host "Login padrao: admin / admin (troque na 1a vez)" -ForegroundColor Green
Write-Host ""
Write-Host "Exporte metricas (cole a saida no chat):" -ForegroundColor Cyan
Write-Host @"
`$auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
Invoke-RestMethod -Headers @{Authorization="Basic `$auth"} -Uri "http://localhost:9000/api/measures/component?component=escuela-posgrado-unica-backend&metricKeys=bugs,vulnerabilities,code_smells,security_hotspots,duplicated_lines_density,sqale_index,sqale_rating,reliability_rating,security_rating,coverage,ncloc,sqale_debt_ratio,complexity,cognitive_complexity"
Invoke-RestMethod -Headers @{Authorization="Basic `$auth"} -Uri "http://localhost:9000/api/issues/search?componentKeys=escuela-posgrado-unica-backend&types=BUG,VULNERABILITY,CODE_SMELL&ps=100&facets=files"
"@
