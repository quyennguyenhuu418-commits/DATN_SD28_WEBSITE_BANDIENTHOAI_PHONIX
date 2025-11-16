@echo off
echo ========================================
echo    STOPPING FRONTEND PROCESSES
echo ========================================
echo.

echo [1/3] Stopping Node.js processes...
taskkill /F /IM "node.exe" 2>nul
if %errorlevel% equ 0 (
    echo ✓ Node.js processes stopped successfully
) else (
    echo ! No Node.js processes found to stop
)

echo.
echo [2/3] Stopping npm processes...
taskkill /F /IM "npm.exe" 2>nul
if %errorlevel% equ 0 (
    echo ✓ npm processes stopped successfully
) else (
    echo ! No npm processes found to stop
)

echo.
echo [3/3] Stopping Vite processes...
taskkill /F /IM "vite.exe" 2>nul
if %errorlevel% equ 0 (
    echo ✓ Vite processes stopped successfully
) else (
    echo ! No Vite processes found to stop
)

echo.
echo ========================================
echo    FRONTEND PROCESSES STOPPED
echo ========================================
pause







