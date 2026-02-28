@echo off
chcp 65001 > nul
setlocal EnableDelayedExpansion

REM ================================================================
REM  FastPass Web - Installer Build Script
REM  전체 빌드 자동화: JAR → JRE → Staging → Inno Setup
REM ================================================================

echo.
echo ================================================================
echo   FastPass Web - Installer Builder
echo   SMARTCORE Inc.
echo ================================================================
echo.

set PROJECT_ROOT=%~dp0..
set INSTALLER_DIR=%~dp0
set STAGING_DIR=%INSTALLER_DIR%staging
set JAR_NAME=fphps_web_example-0.0.1-SNAPSHOT.jar
set WINSW_EXE=%INSTALLER_DIR%winsw\WinSW-x64.exe

REM ----------------------------------------------------------------
REM  Step 0: Prerequisites check
REM ----------------------------------------------------------------
echo [STEP 0] Checking prerequisites...

REM Check JAVA_HOME
if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME is not set.
    echo         Please set JAVA_HOME to your JDK 21 installation.
    goto :error
)
echo   [OK] JAVA_HOME: %JAVA_HOME%

REM Check Gradle wrapper
if not exist "%PROJECT_ROOT%\gradlew.bat" (
    echo [ERROR] gradlew.bat not found at %PROJECT_ROOT%
    goto :error
)
echo   [OK] Gradle wrapper found

REM Check WinSW executable
if not exist "%WINSW_EXE%" (
    echo [ERROR] WinSW executable not found at: %WINSW_EXE%
    echo.
    echo   Please download WinSW-x64.exe from:
    echo   https://github.com/winsw/winsw/releases
    echo.
    echo   Place it at: %WINSW_EXE%
    goto :error
)
echo   [OK] WinSW executable found

REM Check Inno Setup
set ISCC=
if exist "%ProgramFiles(x86)%\Inno Setup 6\ISCC.exe" (
    set "ISCC=%ProgramFiles(x86)%\Inno Setup 6\ISCC.exe"
) else if exist "%ProgramFiles%\Inno Setup 6\ISCC.exe" (
    set "ISCC=%ProgramFiles%\Inno Setup 6\ISCC.exe"
) else (
    where ISCC.exe >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        set ISCC=ISCC.exe
    ) else (
        echo [ERROR] Inno Setup 6 not found.
        echo         Please install from: https://jrsoftware.org/isdl.php
        goto :error
    )
)
echo   [OK] Inno Setup: %ISCC%
echo.

REM ----------------------------------------------------------------
REM  Step 1: Build Spring Boot JAR
REM ----------------------------------------------------------------
echo [STEP 1] Building Spring Boot JAR...
echo.

pushd "%PROJECT_ROOT%"
call gradlew.bat clean bootJar -x test
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Gradle build failed!
    popd
    goto :error
)
popd

if not exist "%PROJECT_ROOT%\build\libs\%JAR_NAME%" (
    echo [ERROR] JAR file not found: %PROJECT_ROOT%\build\libs\%JAR_NAME%
    goto :error
)
echo [OK] JAR build successful.
echo.

REM ----------------------------------------------------------------
REM  Step 2: Create custom JRE with jlink
REM ----------------------------------------------------------------
echo [STEP 2] Creating custom JRE with jlink...
echo.

call "%INSTALLER_DIR%create-jre.bat"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] JRE creation failed!
    goto :error
)
echo [OK] Custom JRE created.
echo.

REM ----------------------------------------------------------------
REM  Step 3: Prepare staging directory
REM ----------------------------------------------------------------
echo [STEP 3] Preparing staging directory...

REM Clean staging
if exist "%STAGING_DIR%" (
    rmdir /s /q "%STAGING_DIR%"
)
mkdir "%STAGING_DIR%"

REM Copy JAR
echo   Copying JAR file...
copy /y "%PROJECT_ROOT%\build\libs\%JAR_NAME%" "%STAGING_DIR%\fphps_web_example.jar" >nul

REM Copy application.properties
echo   Copying application.properties...
copy /y "%PROJECT_ROOT%\src\main\resources\application.properties" "%STAGING_DIR%\application.properties" >nul

REM Copy JRE
echo   Copying JRE...
xcopy /s /e /q /y "%INSTALLER_DIR%jre\*" "%STAGING_DIR%\jre\" >nul

REM Copy WinSW
echo   Copying WinSW...
copy /y "%WINSW_EXE%" "%STAGING_DIR%\fastpass-service.exe" >nul
copy /y "%INSTALLER_DIR%winsw\fastpass-service.xml" "%STAGING_DIR%\fastpass-service.xml" >nul

echo [OK] Staging directory prepared.
echo.

REM ----------------------------------------------------------------
REM  Step 4: Build installer with Inno Setup
REM ----------------------------------------------------------------
echo [STEP 4] Building installer with Inno Setup...
echo.

REM Create output directory
if not exist "%INSTALLER_DIR%output" (
    mkdir "%INSTALLER_DIR%output"
)

"%ISCC%" "%INSTALLER_DIR%fastpass-setup.iss"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Inno Setup compilation failed!
    goto :error
)

echo.
echo ================================================================
echo   BUILD SUCCESSFUL!
echo ================================================================
echo.
echo   Installer: %INSTALLER_DIR%output\
dir /b "%INSTALLER_DIR%output\*.exe" 2>nul
echo.
echo   This installer includes:
echo     - Spring Boot JAR (FastPass Web Application)
echo     - Bundled JRE (Java 21, jlink)
echo     - WinSW (Windows Service wrapper)
echo     - Automatic service registration
echo     - Firewall rule configuration
echo.

endlocal
exit /b 0

:error
echo.
echo [BUILD FAILED]
endlocal
exit /b 1
