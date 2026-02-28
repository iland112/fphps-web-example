@echo off
chcp 65001 > nul
setlocal

REM ================================================================
REM  FastPass Web - Service Installer
REM  Windows 서비스 등록 및 시작
REM ================================================================

set SERVICE_EXE=%~dp0..\fastpass-service.exe
set SERVICE_NAME=FastPassWeb

echo [INFO] Installing FastPass Web service...

REM Check if service already exists
sc query %SERVICE_NAME% >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [WARN] Service already exists. Stopping and removing...
    net stop %SERVICE_NAME% >nul 2>&1
    "%SERVICE_EXE%" uninstall >nul 2>&1
    timeout /t 3 /nobreak >nul
)

REM Install service
"%SERVICE_EXE%" install
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to install service.
    exit /b 1
)

echo [INFO] Service installed successfully.

REM Start service
echo [INFO] Starting service...
net start %SERVICE_NAME%
if %ERRORLEVEL% neq 0 (
    echo [WARN] Failed to start service immediately.
    echo        The service will start automatically on next boot.
    exit /b 0
)

echo [INFO] Service started successfully.
echo [INFO] Access the application at: http://localhost:50000

endlocal
exit /b 0
