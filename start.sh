#!/bin/bash

# FastPass Web Application Startup Script
# ========================================

set -e

echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║       FastPass E-Passport Reader Web Application          ║"
echo "║                   SMARTCORE Inc.                          ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# Configuration
APP_NAME="fphps_web_example"
APP_PORT="${PORT:-8080}"
JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx512m -Dfile.encoding=UTF-8}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check Java installation
if ! command -v java &> /dev/null; then
    echo -e "${RED}[ERROR]${NC} Java is not installed or not in PATH."
    echo "        Please install Java 21 or higher."
    exit 1
fi

# Check Java version
JAVA_VER=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
echo -e "${GREEN}[INFO]${NC} Java version: $JAVA_VER"

# Find JAR file
JAR_FILE=$(find build/libs -name "${APP_NAME}-*.jar" -type f 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo -e "${YELLOW}[WARN]${NC} JAR file not found. Building application..."
    echo ""
    ./gradlew clean bootJar -x test
    JAR_FILE=$(find build/libs -name "${APP_NAME}-*.jar" -type f 2>/dev/null | head -n 1)
fi

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}[ERROR]${NC} JAR file not found after build."
    exit 1
fi

echo -e "${GREEN}[INFO]${NC} Starting ${APP_NAME}..."
echo -e "${GREEN}[INFO]${NC} JAR: ${JAR_FILE}"
echo -e "${GREEN}[INFO]${NC} Port: ${APP_PORT}"
echo ""
echo "──────────────────────────────────────────────────────────────"
echo "  Access the application at: http://localhost:${APP_PORT}"
echo "  Press Ctrl+C to stop the server"
echo "──────────────────────────────────────────────────────────────"
echo ""

# Start application
exec java $JAVA_OPTS -jar "$JAR_FILE"
