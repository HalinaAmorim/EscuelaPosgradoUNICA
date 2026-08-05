$token = Get-Content "C:\Users\Halina\EscuelaPosgradoUNICA\.sonar-token.tmp" -Raw
$headers = @{ Authorization = "Bearer $token" }
$base = "http://localhost:9000"

# Wait CE
$taskId = "f1bc7cf8-c9b2-4527-897d-01e8344804a9"
for ($i = 1; $i -le 30; $i++) {
  $t = Invoke-RestMethod -Headers $headers -Uri "$base/api/ce/task?id=$taskId"
  Write-Host ("CE status={0}" -f $t.task.status)
  if ($t.task.status -eq "SUCCESS" -or $t.task.status -eq "FAILED") { break }
  Start-Sleep -Seconds 3
}

$metricKeys = "bugs,vulnerabilities,code_smells,security_hotspots,duplicated_lines_density,sqale_index,sqale_rating,reliability_rating,security_rating,coverage,ncloc,sqale_debt_ratio,complexity,cognitive_complexity,duplicated_blocks,duplicated_files"
$comp = Invoke-RestMethod -Headers $headers -Uri "$base/api/measures/component?component=escuela-posgrado-unica-backend&metricKeys=$metricKeys"
$lines = @()
foreach ($m in $comp.component.measures) {
  $lines += ("{0}={1}" -f $m.metric, $m.value)
}
$lines | Sort-Object | Set-Content "C:\Users\Halina\EscuelaPosgradoUNICA\sonar-project-measures.txt"
Write-Host "=== PROJECT MEASURES ==="
Get-Content "C:\Users\Halina\EscuelaPosgradoUNICA\sonar-project-measures.txt"

$issues = Invoke-RestMethod -Headers $headers -Uri "$base/api/issues/search?componentKeys=escuela-posgrado-unica-backend&types=BUG,VULNERABILITY,CODE_SMELL&ps=500&facets=files,severities,types,rules"
Write-Host ("TOTAL_ISSUES={0}" -f $issues.total)

$outIssues = @()
foreach ($iss in $issues.issues) {
  $file = $iss.component
  if ($file -match ":") { $file = $file.Substring($file.IndexOf(":") + 1) }
  $outIssues += [PSCustomObject]@{
    type = $iss.type
    severity = $iss.severity
    rule = $iss.rule
    effort = $iss.effort
    file = $file
    line = $iss.line
    message = $iss.message
  }
}
$outIssues | ConvertTo-Json -Depth 5 | Set-Content "C:\Users\Halina\EscuelaPosgradoUNICA\sonar-issues.json"

Write-Host "=== TYPES ==="
foreach ($f in $issues.facets) {
  if ($f.property -eq "types") {
    foreach ($v in $f.values) { Write-Host ("  {0}={1}" -f $v.val, $v.count) }
  }
}
Write-Host "=== SEVERITIES ==="
foreach ($f in $issues.facets) {
  if ($f.property -eq "severities") {
    foreach ($v in $f.values) { Write-Host ("  {0}={1}" -f $v.val, $v.count) }
  }
}
Write-Host "=== TOP FILES ==="
foreach ($f in $issues.facets) {
  if ($f.property -eq "files") {
    $sorted = $f.values | Sort-Object count -Descending | Select-Object -First 30
    foreach ($v in $sorted) { Write-Host ("  {0}`t{1}" -f $v.count, $v.val) }
  }
}

try {
  $hs = Invoke-RestMethod -Headers $headers -Uri "$base/api/hotspots/search?projectKey=escuela-posgrado-unica-backend&ps=100"
  Write-Host ("HOTSPOTS={0}" -f $hs.paging.total)
  $hs.hotspots | ConvertTo-Json -Depth 5 | Set-Content "C:\Users\Halina\EscuelaPosgradoUNICA\sonar-hotspots.json"
} catch {
  Write-Host ("Hotspots: {0}" -f $_.Exception.Message)
}

$treeKeys = "bugs,vulnerabilities,code_smells,ncloc,sqale_index,complexity,duplicated_lines_density,cognitive_complexity"
$tree = Invoke-RestMethod -Headers $headers -Uri "$base/api/measures/component_tree?component=escuela-posgrado-unica-backend&metricKeys=$treeKeys&strategy=leaves&ps=500"

$rows = @()
foreach ($c in $tree.components) {
  if ($c.path -notmatch "(service|controller|security)/") { continue }
  $map = @{}
  foreach ($m in $c.measures) { $map[$m.metric] = $m.value }
  $debt = 0
  if ($map.ContainsKey("sqale_index")) { $debt = [int]$map["sqale_index"] }
  $rows += [PSCustomObject]@{
    Path = $c.path
    Bugs = $(if ($map.ContainsKey("bugs")) { $map["bugs"] } else { "0" })
    Vuln = $(if ($map.ContainsKey("vulnerabilities")) { $map["vulnerabilities"] } else { "0" })
    Smells = $(if ($map.ContainsKey("code_smells")) { $map["code_smells"] } else { "0" })
    NLOC = $(if ($map.ContainsKey("ncloc")) { $map["ncloc"] } else { "0" })
    DebtMin = $debt
    Complexity = $(if ($map.ContainsKey("complexity")) { $map["complexity"] } else { "0" })
    Cog = $(if ($map.ContainsKey("cognitive_complexity")) { $map["cognitive_complexity"] } else { "0" })
    Dup = $(if ($map.ContainsKey("duplicated_lines_density")) { $map["duplicated_lines_density"] } else { "0.0" })
  }
}
$rows = $rows | Sort-Object DebtMin -Descending
$rows | Format-Table -AutoSize | Out-String -Width 250 | Tee-Object "C:\Users\Halina\EscuelaPosgradoUNICA\sonar-file-measures.txt"
$rows | ConvertTo-Json -Depth 4 | Set-Content "C:\Users\Halina\EscuelaPosgradoUNICA\sonar-file-measures.json"
Write-Host "DONE"
