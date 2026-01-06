@echo off
chcp 65001 > nul
setlocal EnableDelayedExpansion

REM FastPass Web Application Startup Script

echo.
echo ===============================================================
echo        FastPass E-Passport Reader Web Application
echo                      SMARTCORE Inc.
echo ===============================================================
echo.

REM Configuration
set APP_NAME=fphps_web_example
set APP_PORT=8080
set JAVA_OPTS=-Xms256m -Xmx512m -Dfile.encoding=UTF-8

REM Check Java installation
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo         Please install Java 21 or higher.
    pause
    exit /b 1
)

REM Check Java version
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VER=%%i
)
set JAVA_VER=%JAVA_VER:"=%
echo [INFO] Java version: %JAVA_VER%

REM Find JAR file
set JAR_FILE=
for %%f in (build\libs\%APP_NAME%-*.jar) do (
    set JAR_FILE=%%f
)

if not defined JAR_FILE (
    echo [WARN] JAR file not found. Building application...
    echo.
    call gradlew.bat clean bootJar -x test
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Build failed.
        pause
        exit /b 1
    )
    for %%f in (build\libs\%APP_NAME%-*.jar) do (
        set JAR_FILE=%%f
    )
)

if not defined JAR_FILE (
    echo [ERROR] JAR file not found after build.
    pause
    exit /b 1
)

echo [INFO] Starting %APP_NAME%...
echo [INFO] JAR: %JAR_FILE%
echo [INFO] Port: %APP_PORT%
echo.
echo ---------------------------------------------------------------
echo   Access the application at: http://localhost:%APP_PORT%
echo   Press Ctrl+C to stop the server
echo ---------------------------------------------------------------
echo.

REM Start application
java %JAVA_OPTS% -jar %JAR_FILE%

endlocal
