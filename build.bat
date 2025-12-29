@echo off
chcp 65001 > nul
setlocal

:: FastPass Web Application Build Script
:: =====================================

echo.
echo  ╔═══════════════════════════════════════════════════════════╗
echo  ║       FastPass E-Passport Reader - Build Script           ║
echo  ╚═══════════════════════════════════════════════════════════╝
echo.

:: Parse arguments
set BUILD_TYPE=bootJar
set SKIP_TEST=-x test
set BUILD_CSS=false

:parse_args
if "%~1"=="" goto :end_parse
if /i "%~1"=="--test" set SKIP_TEST=
if /i "%~1"=="--css" set BUILD_CSS=true
if /i "%~1"=="--clean" set BUILD_TYPE=clean bootJar
if /i "%~1"=="--help" goto :show_help
shift
goto :parse_args
:end_parse

:: Build CSS if requested
if "%BUILD_CSS%"=="true" (
    echo [STEP 1/2] Building Tailwind CSS...
    cd src\main\frontend
    if not exist node_modules (
        echo [INFO] Installing npm dependencies...
        call npm install
    )
    call npm run build
    cd ..\..\..
    echo [INFO] CSS build complete.
    echo.
)

:: Build Java application
echo [STEP] Building Java application...
echo [INFO] Build type: %BUILD_TYPE%
if defined SKIP_TEST (
    echo [INFO] Skipping tests
)
echo.

call gradlew.bat %BUILD_TYPE% %SKIP_TEST%

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo.
echo ══════════════════════════════════════════════════════════════
echo   BUILD SUCCESSFUL
echo ══════════════════════════════════════════════════════════════
echo.
echo   JAR file location: build\libs\
dir /b build\libs\*.jar 2>nul
echo.
echo   To run the application:
echo     start.bat
echo.

endlocal
exit /b 0

:show_help
echo Usage: build.bat [options]
echo.
echo Options:
echo   --clean    Clean build (removes previous build artifacts)
echo   --test     Run tests during build
echo   --css      Build Tailwind CSS before Java build
echo   --help     Show this help message
echo.
echo Examples:
echo   build.bat              Build JAR without tests
echo   build.bat --clean      Clean and rebuild
echo   build.bat --css        Build CSS and JAR
echo   build.bat --test       Build with tests
echo.
exit /b 0
