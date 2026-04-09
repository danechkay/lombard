$base = "http://localhost:8080"

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginBody = @{
  username = "admin@lombard.ru"
  password = "password"
}

Write-Host "Logging in..."
try {
  Invoke-WebRequest -Uri ($base + "/login") -Method Post -Body $loginBody -ContentType "application/x-www-form-urlencoded" -WebSession $session -MaximumRedirection 5 -ErrorAction Stop | Out-Null
  Write-Host "Login request sent."
} catch {
  Write-Host "Login error (continuing). $($_.Exception.Message)"
}

$payload = @{
  categoryId = $null
  modelName = "TestModel"
  description = "desc"
  condition = "NEW"
  year = 1991
}
$json = $payload | ConvertTo-Json -Depth 6

Write-Host "Posting valuation request..."
try {
  $resp = Invoke-WebRequest -Uri ($base + "/api/valuation/requests") -Method Post -WebSession $session -ContentType "application/json" -Body $json -MaximumRedirection 0 -ErrorAction Stop
  Write-Host ("STATUS=" + $resp.StatusCode)
  Write-Host $resp.Content
} catch {
  try {
    $code = $_.Exception.Response.StatusCode.value__
  } catch {
    $code = "unknown"
  }
  Write-Host ("STATUS=" + $code)
  try {
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    Write-Host $reader.ReadToEnd()
  } catch {
    Write-Host "NO_BODY"
  }
}

