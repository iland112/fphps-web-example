@echo off
chcp 65001 > nul
setlocal EnableDelayedExpansion

REM ================================================================
REM  FastPass Web - Custom JRE Builder (jlink)
REM  JDK 21 기반 최소 JRE 생성
REM ================================================================

echo.
echo ================================================================
echo   FastPass Web - Custom JRE Builder (jlink)
echo ================================================================
echo.

REM Check JAVA_HOME
if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME is not set.
    echo         Please set JAVA_HOME to your JDK 21 installation.
    exit /b 1
)

set JLINK=%JAVA_HOME%\bin\jlink.exe
if not exist "%JLINK%" (
    echo [ERROR] jlink not found at: %JLINK%
    echo         Make sure JAVA_HOME points to a JDK (not JRE).
    exit /b 1
)

REM Output directory
set OUTPUT_DIR=%~dp0jre
set JLINK_OPTS=--strip-debug --no-man-pages --no-header-files --compress zip-6

REM Remove existing JRE
if exist "%OUTPUT_DIR%" (
    echo [INFO] Removing existing JRE directory...
    rmdir /s /q "%OUTPUT_DIR%"
)

REM Required modules for Spring Boot web application
REM  - java.base: Core (always included)
REM  - java.sql: JDBC/JPA (SQLite)
REM  - java.naming: JNDI (Spring)
REM  - java.management: JMX (Spring Boot Actuator)
REM  - java.desktop: AWT/Image processing
REM  - java.logging: JUL logging
REM  - java.xml: XML processing (Spring, Thymeleaf)
REM  - java.instrument: Java agent (Spring Boot DevTools)
REM  - java.net.http: HTTP Client
REM  - java.security.jgss: Kerberos/GSSAPI
REM  - java.security.sasl: SASL authentication
REM  - java.compiler: javax.tools (annotation processing)
REM  - java.datatransfer: Data transfer (clipboard)
REM  - java.prefs: Preferences API
REM  - java.scripting: Scripting engine
REM  - java.rmi: RMI (JMX dependency)
REM  - java.transaction.xa: XA transactions
REM  - jdk.crypto.ec: Elliptic curve crypto (HTTPS)
REM  - jdk.crypto.cryptoki: PKCS#11 crypto
REM  - jdk.unsupported: sun.misc.Unsafe (Reflection, Netty)
REM  - jdk.net: Extended socket options
REM  - jdk.zipfs: ZIP filesystem (Spring Boot JAR)
REM  - jdk.localedata: Locale data
REM  - jdk.charsets: Additional charsets (UTF-8 etc.)

set MODULES=java.base,java.sql,java.naming,java.management,java.desktop,java.logging,java.xml,java.instrument,java.net.http,java.security.jgss,java.security.sasl,java.compiler,java.datatransfer,java.prefs,java.scripting,java.rmi,java.transaction.xa,jdk.crypto.ec,jdk.crypto.cryptoki,jdk.unsupported,jdk.net,jdk.zipfs,jdk.localedata,jdk.charsets

echo [INFO] JAVA_HOME: %JAVA_HOME%
echo [INFO] Output: %OUTPUT_DIR%
echo [INFO] Modules: %MODULES%
echo.

echo [INFO] Creating custom JRE with jlink...
"%JLINK%" %JLINK_OPTS% --add-modules %MODULES% --output "%OUTPUT_DIR%"

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] jlink failed!
    exit /b 1
)

REM Show JRE size
echo.
echo [INFO] Custom JRE created successfully!
echo [INFO] Location: %OUTPUT_DIR%

REM Calculate size
set SIZE=0
for /f "tokens=3" %%a in ('dir /s "%OUTPUT_DIR%" 2^>nul ^| findstr /c:"File(s)" ^| findstr /v /c:"Dir(s)"') do (
    set SIZE=%%a
)
echo [INFO] Size: %SIZE% bytes

REM Import custom CA certificates into JRE cacerts
set CERTS_DIR=%~dp0certs
set KEYTOOL=%OUTPUT_DIR%\bin\keytool.exe
set CACERTS=%OUTPUT_DIR%\lib\security\cacerts

if exist "%CERTS_DIR%\pkd-private-ca.crt" (
    echo [INFO] Importing PKD Private CA certificate...
    "%KEYTOOL%" -importcert -keystore "%CACERTS%" -storepass changeit -alias pkd-ca -file "%CERTS_DIR%\pkd-private-ca.crt" -noprompt
    if !ERRORLEVEL! equ 0 (
        echo [INFO] PKD Private CA certificate imported successfully.
    ) else (
        echo [WARN] Failed to import PKD Private CA certificate.
    )
) else (
    echo [WARN] PKD Private CA certificate not found at: %CERTS_DIR%\pkd-private-ca.crt
    echo         PA API HTTPS connections may fail.
)

echo.

REM Verify java.exe
"%OUTPUT_DIR%\bin\java.exe" -version 2>&1
echo.

endlocal
exit /b 0
