# FPHPS Web Example

SMARTCORE FastPass SDK (E-Passport Reader) Java API Library usage example web application.

## Overview

This project demonstrates how to integrate the SMARTCORE FastPass SDK with a Spring Boot web application. It provides a comprehensive web interface for reading e-passports, ID cards, and barcodes, with support for Passive Authentication (PA) verification.

## Features

- **E-Passport Reading**: Manual and automatic reading with RFID support
- **Passive Authentication (PA)**: Verify e-passport authenticity via external PA API
- **ID Card Reading**: Support for electronic ID cards
- **Barcode/Label Reading**: 1D/2D barcode scanning
- **Page Scanning**: Multi-spectrum document scanning (White, IR, UV light)
- **Progressive Web App (PWA)**: Offline support and installable web app
- **Real-time Updates**: WebSocket-based progress notifications
- **Dark Mode**: System-aware dark/light theme toggle
- **Responsive UI**: Mobile-friendly design with Tailwind CSS

## Technology Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language (toolchain) |
| Spring Boot | 3.5.0 | Web application framework |
| JNA | 5.17.0 | Java-Native library bridge |
| Apache HttpClient 5 | 5.4.4 | HTTP communication (PA API) |
| Bouncy Castle | 1.78 | PKI cryptography |
| Gson | 2.10.1 | JSON serialization |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Tailwind CSS | 4.1.5 | Utility-first CSS |
| Preline | 3.0.1 | UI component library |
| DaisyUI | 5.0.35 | Tailwind components |
| HTMX | - | Dynamic content loading |

## Requirements

- **Java**: 21 or higher
- **OS**: Windows (FastPass SDK DLL required)
- **FastPass SDK**: Installed with bin directory in PATH
- **Node.js**: For Tailwind CSS build (optional)

## Quick Start

### 1. Install FastPass SDK

Add the FastPass SDK bin directory to Windows PATH:
```
C:\SMARTCORE\FASTpass\Bin64
```

### 2. Build and Run

**Using build scripts (recommended):**

```batch
# Build the application
build.bat

# Run the application
start.bat
```

**Using Gradle directly:**

```bash
# Build
gradlew clean bootJar -x test

# Run
gradlew bootRun
```

### 3. Access the Application

Open your browser and navigate to:
```
http://localhost:8080
```

## Build Script Options

### build.bat

```batch
build.bat              # Build JAR without tests
build.bat --clean      # Clean and rebuild
build.bat --css        # Build Tailwind CSS and JAR
build.bat --test       # Build with tests
build.bat --help       # Show help
```

### start.bat

Automatically:
- Checks Java installation
- Finds or builds JAR file
- Starts the application with optimized JVM settings

## Project Structure

```
FPHPS_WEB_Example/
├── src/main/java/.../
│   ├── Controllers/         # REST controllers
│   ├── Services/            # Business logic
│   ├── strategies/          # Document reading strategies
│   ├── config/              # Spring configurations
│   ├── dto/                 # Data transfer objects
│   └── advice/              # Exception handlers
├── src/main/resources/
│   ├── templates/           # Thymeleaf templates
│   ├── static/              # CSS, JS, images
│   └── application.properties
├── src/main/frontend/       # Tailwind CSS source
├── docs/                    # Documentation
├── libs/                    # Local JAR dependencies
├── build.bat                # Build script
├── start.bat                # Startup script
└── README.md
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/fphps` | Main homepage |
| GET | `/fphps/device` | Device information |
| GET | `/fphps/passport/manual-read` | Manual passport reading |
| POST | `/fphps/passport/run-auto-read` | Automatic passport reading |
| POST | `/fphps/passport/verify-pa` | PA verification |
| GET | `/fphps/idcard/manual-read` | ID card reading |
| GET | `/fphps/barcode/manual-read` | Barcode reading |
| GET/POST | `/fphps/scan-page` | Page scanning |
| GET/POST | `/fphps/device-setting` | Device settings |

## WebSocket

- **Endpoint**: `/fastpass`
- **Protocol**: Raw WebSocket + STOMP messaging
- **Purpose**: Real-time reading progress and events

## PA (Passive Authentication) API

The application supports external PA API integration for e-passport verification:

- **PA V2 API**: API Gateway-based verification
- **Certificate Chain Validation**: CSCA/DSC certificate verification
- **CRL Status Check**: Certificate revocation list validation
- **Data Group Hash Verification**: DG1-DG16 integrity check

Configure PA API URL in `application.properties`:
```properties
pa.api.url=https://your-pa-api-gateway/verify
```

## Documentation

- [CLAUDE.md](CLAUDE.md) - Detailed project analysis
- [docs/api_documentation.md](docs/api_documentation.md) - API reference
- [docs/user_manual.md](docs/user_manual.md) - User manual
- [docs/pa_api_v2_integration.md](docs/pa_api_v2_integration.md) - PA API integration guide

## License

Copyright (c) SMARTCORE Inc. All rights reserved.

## Support

For SDK support and documentation, contact SMARTCORE Inc.
