Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   STOPPING FRONTEND PROCESSES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/3] Stopping Node.js processes..." -ForegroundColor Yellow
$nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue
if ($nodeProcesses) {
    $nodeProcesses | Stop-Process -Force
    Write-Host "✓ Node.js processes stopped successfully" -ForegroundColor Green
} else {
    Write-Host "! No Node.js processes found to stop" -ForegroundColor Gray
}

Write-Host ""
Write-Host "[2/3] Stopping npm processes..." -ForegroundColor Yellow
$npmProcesses = Get-Process -Name "npm" -ErrorAction SilentlyContinue
if ($npmProcesses) {
    $npmProcesses | Stop-Process -Force
    Write-Host "✓ npm processes stopped successfully" -ForegroundColor Green
} else {
    Write-Host "! No npm processes found to stop" -ForegroundColor Gray
}

Write-Host ""
Write-Host "[3/3] Stopping Vite processes..." -ForegroundColor Yellow
$viteProcesses = Get-Process -Name "vite" -ErrorAction SilentlyContinue
if ($viteProcesses) {
    $viteProcesses | Stop-Process -Force
    Write-Host "✓ Vite processes stopped successfully" -ForegroundColor Green
} else {
    Write-Host "! No Vite processes found to stop" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   FRONTEND PROCESSES STOPPED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")







