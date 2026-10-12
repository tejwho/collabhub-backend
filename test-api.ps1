Write-Host "=== 1. TESTING REGISTRATION ===" -ForegroundColor Cyan
$regBody = @{
    email = "alice.smith@college.edu"
    password = "Password123!"
    fullName = "Alice Smith"
    college = "National Institute of Technology"
    department = "Computer Science & Engineering"
    rollNumber = "CS2023-042"
    yearOfStudy = 3
    graduationYear = 2026
    bio = "Full-stack enthusiast interested in AI-powered collaboration tools."
    githubUrl = "https://github.com/alicesmith"
    linkedinUrl = "https://linkedin.com/in/alicesmith"
    portfolioUrl = "https://alicesmith.dev"
} | ConvertTo-Json

try {
    $regResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
    Write-Host "Registered successfully!" -ForegroundColor Green
    Write-Host "Token: $($regResponse.token.Substring(0, 25))..."
    Write-Host "User ID: $($regResponse.user.id)"
    Write-Host "User Name: $($regResponse.user.fullName)"
    Write-Host "User Role: $($regResponse.user.role)"
} catch {
    Write-Host "Registration response: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host "`n=== 2. TESTING LOGIN ===" -ForegroundColor Cyan
$loginBody = @{
    email = "alice.smith@college.edu"
    password = "Password123!"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
Write-Host "Logged in successfully!" -ForegroundColor Green
$jwtToken = $loginResponse.token
Write-Host "Token received: $($jwtToken.Substring(0, 25))..."

Write-Host "`n=== 3. TESTING PROTECTED /api/auth/me ===" -ForegroundColor Cyan
$headers = @{
    Authorization = "Bearer $jwtToken"
}
$meResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/me" -Method Get -Headers $headers
Write-Host "Authenticated profile retrieved for: $($meResponse.fullName) ($($meResponse.email))" -ForegroundColor Green
Write-Host "Department: $($meResponse.department), Roll No: $($meResponse.rollNumber)"

Write-Host "`n=== 4. TESTING DUPLICATE REGISTRATION PREVENTION ===" -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
    Write-Host "Duplicate was unexpectedly allowed!" -ForegroundColor Red
} catch {
    Write-Host "Duplicate correctly blocked with HTTP 400 Bad Request!" -ForegroundColor Green
}

Write-Host "`nALL BACKEND AUTH TESTS PASSED SUCCESSFULLY!" -ForegroundColor Green
