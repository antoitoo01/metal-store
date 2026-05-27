$token = "eyJhbGciOiJFUzI1NiIsImtpZCI6IjgyMmYxOTk4LTRjYjAtNGQzNi1hNjI3LWE1ZmRjMGYwMjFlZSIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2Fqc3JlcXZkdmhmZ2RmcGd3ZWNtLnN1cGFiYXNlLmNvL2F1dGgvdjEiLCJzdWIiOiIxMmNiYzY2OS03ODJkLTQwOTUtOGI5MC1iMDA4Y2Y3MDMzYmUiLCJhdWQiOiJhdXRoZW50aWNhdGVkIiwiZXhwIjoxNzc5ODM4NjI0LCJpYXQiOjE3Nzk4MzUwMjQsImVtYWlsIjoidGFsbGVyMkBlamVtcGxvLmNvbSIsInBob25lIjoiIiwiYXBwX21ldGFkYXRhIjp7InByb3ZpZGVyIjoiZW1haWwiLCJwcm92aWRlcnMiOlsiZW1haWwiXX0sInVzZXJfbWV0YWRhdGEiOnsiZW1haWxfdmVyaWZpZWQiOnRydWUsInVzZXJuYW1lIjoiYWRtaW4yIn0sInJvbGUiOiJhdXRoZW50aWNhdGVkIiwiYWFsIjoiYWFsMSIsImFtciI6W3sibWV0aG9kIjoicGFzc3dvcmQiLCJ0aW1lc3RhbXAiOjE3Nzk4MzUwMjR9XSwic2Vzc2lvbl9pZCI6IjlmZTc4Y2NhLTM4MzQtNGQ3Ni04MWI3LTljMTcxZGQ4N2Q5OCIsImlzX2Fub255bW91cyI6ZmFsc2V9.wfBbKIq0ikleWDuWoR3gvgIuDlrzlhwlCjFtghuHxp4AKKQajO5FpS9DWcg-rTqMi7AjVkhP0w0NoSUewyDRBw"
$tenantId = "eb700f2f-429a-4e23-a090-c1e58f7c3884"
$h = @{Authorization="Bearer $token"; "X-Tenant-Id"="$tenantId"}
$base = "http://localhost:8080"

function Get-Api([string]$path) {
    $url = "$base$path"
    Invoke-RestMethod -Uri $url -Method GET -Headers $h
}

function Post-Api([string]$path, $body) {
    $url = "$base$path"
    Invoke-RestMethod -Uri $url -Method POST -ContentType "application/json" -Headers $h -Body ($body | ConvertTo-Json -Depth 10 -Compress)
}

Write-Host "============================================"
Write-Host "  Metal Store - Endpoint Test Suite"
Write-Host "============================================"

# Catalog
Write-Host "`n=== Catalog Profiles (paginated) ==="
$r = Get-Api "/api/catalog/profiles?page=0&size=3"
Write-Host "  Total: $($r.page.totalElements), Showing: $($r.content.Count)"

Write-Host "`n=== Catalog Profiles (EURO) ==="
try { $r = Get-Api "/api/catalog/profiles?standard=EURO&page=0&size=2"; Write-Host "  Total: $($r.page.totalElements), Sample: $($r.content[0].designation)" } catch { Write-Host "  ERROR" }

Write-Host "`n=== Catalog Profiles (AISC) ==="
try { $r = Get-Api "/api/catalog/profiles?standard=AISC&page=0&size=2"; Write-Host "  Total: $($r.page.totalElements), Sample: $($r.content[0].designation)" } catch { Write-Host "  ERROR" }

Write-Host "`n=== Catalog Families ==="
$r = Get-Api "/api/catalog/families"
Write-Host "  Families: $($r.Count)"

Write-Host "`n=== Catalog Items ==="
$r = Get-Api "/api/catalog/items?page=0&size=3"
Write-Host "  Items: $($r.page.totalElements)"

# Inventory
Write-Host "`n=== Inventory - Create ==="
$profileId = (Get-Api "/api/catalog/profiles?page=0&size=1").content[0].id
try {
    $r = Post-Api "/api/inventory" @{profileId=$profileId; quantity=10; location="Estante A1"; costPriceEur=85.50; supplier="Aceros S.A."}
    Write-Host "  Created: $($r.id), Location: $($r.location), Qty: $($r.quantity)"
} catch { $stream = $_.Exception.Response.GetResponseStream(); $reader = New-Object System.IO.StreamReader($stream); Write-Host "  ERROR: $($reader.ReadToEnd())" }

Write-Host "`n=== Inventory - List ==="
$r = Get-Api "/api/inventory"
Write-Host "  Items: $($r.page.totalElements)"

# Item Types
Write-Host "`n=== Item Types - Create ==="
try {
    $r = Post-Api "/api/catalog/item-types" @{name="Vidrio Templado"; schemaDefinition=@{thickness=@("6mm","8mm","10mm"); color=@("transparente","bronce")}}
    Write-Host "  Created: $($r.name)"
} catch { $stream = $_.Exception.Response.GetResponseStream(); $reader = New-Object System.IO.StreamReader($stream); Write-Host "  ERROR: $($reader.ReadToEnd())" }

# Billing
Write-Host "`n=== Price List - Create ==="
try {
    $r = Post-Api "/api/billing/prices" @{profileId=$profileId; unitPrice=250.00; notes="Precio estandar perfil"}
    Write-Host "  Price: $($r.unitPrice)"
} catch { $stream = $_.Exception.Response.GetResponseStream(); $reader = New-Object System.IO.StreamReader($stream); Write-Host "  ERROR: $($reader.ReadToEnd())" }

Write-Host "`n=== Invoice - Create Draft ==="
$inv = Post-Api "/api/billing/invoices" @{customerName="Taller Mecanico Perez"; customerVat="B12345678"}
Write-Host "  Invoice: $($inv.invoiceNumber), Status: $($inv.status)"

Write-Host "`n=== Invoice - Add Line ==="
try {
    $r = Post-Api "/api/billing/invoices/$($inv.id)/lines" @{profileId=$profileId; quantity=5; unitPrice=250.00}
    Write-Host "  Subtotal: $($r.subtotal), Total: $($r.total)"
} catch { $stream = $_.Exception.Response.GetResponseStream(); $reader = New-Object System.IO.StreamReader($stream); Write-Host "  ERROR: $($reader.ReadToEnd())" }

Write-Host "`n=== Invoice - Issue ==="
try {
    $r = Post-Api "/api/billing/invoices/$($inv.id)/issue" @{}
    Write-Host "  Status: $($r.status), Total: $($r.total)"
} catch { $stream = $_.Exception.Response.GetResponseStream(); $reader = New-Object System.IO.StreamReader($stream); Write-Host "  ERROR: $($reader.ReadToEnd())" }

Write-Host "`n=== Invoice - List ==="
$r = Get-Api "/api/billing/invoices"
Write-Host "  Invoices: $($r.page.totalElements)"

Write-Host "`n============================================"
Write-Host "  Tests Complete"
Write-Host "============================================"