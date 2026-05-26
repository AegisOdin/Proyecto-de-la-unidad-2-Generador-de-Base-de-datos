$root = $PSScriptRoot

Start-Process powershell -ArgumentList "-NoExit","-Command","cd '$root\backend'; mvn spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit","-Command","cd '$root\frontend'; npm run dev"

Write-Host "Backend y frontend lanzados en ventanas separadas." -ForegroundColor Green
Write-Host "Backend: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Frontend: http://localhost:3000" -ForegroundColor Cyan
