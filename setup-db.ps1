param(
    [Parameter(Mandatory=$false)]
    [string]$Password
)

if (-not $Password) {
    $secPass = Read-Host "Enter your MySQL root password (input will be hidden)" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secPass)
    $Password = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
}

Write-Host "Testing MySQL connection and preparing student_collab_db..." -ForegroundColor Cyan

# Test connection and create database
$res = & "mysql.exe" -u root "-p$Password" -e "CREATE DATABASE IF NOT EXISTS student_collab_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; SELECT 'Database student_collab_db ready!' AS Status;" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "MySQL connection verified successfully!" -ForegroundColor Green
    $configPath = Join-Path $PSScriptRoot "application-local.properties"
    $content = "spring.datasource.username=root`nspring.datasource.password=$Password`n"
    Set-Content -Path $configPath -Value $content -NoNewline
    Write-Host "Saved local configuration to: $configPath" -ForegroundColor Green
    Write-Host "Done! You can now let the assistant know to start the backend." -ForegroundColor Cyan
} else {
    Write-Host "Failed to connect to MySQL: $res" -ForegroundColor Red
    Write-Host "Please ensure the password is correct and try again." -ForegroundColor Yellow
}
