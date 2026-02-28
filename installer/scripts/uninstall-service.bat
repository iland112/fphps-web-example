@echo off
chcp 65001 > nul
setlocal

REM ================================================================
REM  FastPass Web - Service Uninstaller
REM  Windows 서비스 중지 및 제거
REM ================================================================

set SERVICE_EXE=%~dp0..\fastpass-service.exe
set SERVICE_NAME=FastPassWeb

echo [INFO] Uninstalling FastPass Web service...

REM Stop service if running
sc query %SERVICE_NAME% >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [INFO] Stopping service...
    net stop %SERVICE_NAME% >nul 2>&1
    timeout /t 5 /nobreak >nul
)

REM Uninstall service
"%SERVICE_EXE%" uninstall >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [WARN] Service uninstall returned non-zero. It may have already been removed.
)

echo [INFO] Service removed successfully.

endlocal
exit /b 0
