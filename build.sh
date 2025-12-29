#!/bin/bash

# FastPass Web Application Build Script
# =====================================

set -e

echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║       FastPass E-Passport Reader - Build Script           ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Default options
BUILD_TYPE="bootJar"
SKIP_TEST="-x test"
BUILD_CSS=false

# Parse arguments
show_help() {
    echo "Usage: ./build.sh [options]"
    echo ""
    echo "Options:"
    echo "  --clean    Clean build (removes previous build artifacts)"
    echo "  --test     Run tests during build"
    echo "  --css      Build Tailwind CSS before Java build"
    echo "  --help     Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./build.sh              Build JAR without tests"
    echo "  ./build.sh --clean      Clean and rebuild"
    echo "  ./build.sh --css        Build CSS and JAR"
    echo "  ./build.sh --test       Build with tests"
    echo ""
    exit 0
}

while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            BUILD_TYPE="clean bootJar"
            shift
            ;;
        --test)
            SKIP_TEST=""
            shift
            ;;
        --css)
            BUILD_CSS=true
            shift
            ;;
        --help)
            show_help
            ;;
        *)
            echo -e "${RED}[ERROR]${NC} Unknown option: $1"
            show_help
            ;;
    esac
done

# Build CSS if requested
if [ "$BUILD_CSS" = true ]; then
    echo -e "${BLUE}[STEP 1/2]${NC} Building Tailwind CSS..."
    cd src/main/frontend
    if [ ! -d "node_modules" ]; then
        echo -e "${GREEN}[INFO]${NC} Installing npm dependencies..."
        npm install
    fi
    npm run build
    cd ../../..
    echo -e "${GREEN}[INFO]${NC} CSS build complete."
    echo ""
fi

# Build Java application
echo -e "${BLUE}[STEP]${NC} Building Java application..."
echo -e "${GREEN}[INFO]${NC} Build type: ${BUILD_TYPE}"
if [ -n "$SKIP_TEST" ]; then
    echo -e "${GREEN}[INFO]${NC} Skipping tests"
fi
echo ""

./gradlew $BUILD_TYPE $SKIP_TEST

echo ""
echo "══════════════════════════════════════════════════════════════"
echo -e "  ${GREEN}BUILD SUCCESSFUL${NC}"
echo "══════════════════════════════════════════════════════════════"
echo ""
echo "  JAR file location: build/libs/"
ls -la build/libs/*.jar 2>/dev/null || true
echo ""
echo "  To run the application:"
echo "    ./start.sh"
echo ""
