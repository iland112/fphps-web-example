# FastPass Web Application - 배포 가이드

## 목차
1. [시스템 요구사항](#시스템-요구사항)
2. [빌드 방법](#빌드-방법)
3. [실행 방법](#실행-방법)
4. [배포 방법](#배포-방법)
5. [Windows 설치 프로그램](#windows-설치-프로그램)
6. [설정 옵션](#설정-옵션)
7. [문제 해결](#문제-해결)

---

## 시스템 요구사항

### 필수 요구사항

| 항목 | 요구사항 |
|------|----------|
| **운영체제** | Windows 10/11 (64-bit) |
| **Java** | JDK 21 이상 |
| **메모리** | 최소 512MB RAM |
| **디스크** | 최소 200MB 여유 공간 |
| **FastPass SDK** | FPHPS.dll이 시스템 PATH에 등록되어야 함 |

### 선택 요구사항 (개발용)

| 항목 | 버전 | 용도 |
|------|------|------|
| **Node.js** | 18+ | Tailwind CSS 빌드 |
| **npm** | 9+ | 프론트엔드 의존성 관리 |

### FastPass SDK 설치

1. SMARTCORE FastPass SDK 설치
2. 설치 경로의 `bin` 폴더를 시스템 PATH에 추가
3. `FPHPS.dll`이 접근 가능한지 확인

```cmd
:: PATH 확인 (FPHPS.dll 위치가 포함되어야 함)
echo %PATH%
```

---

## 빌드 방법

### Windows

```cmd
:: 기본 빌드 (테스트 제외)
build.bat

:: 클린 빌드
build.bat --clean

:: CSS 빌드 포함
build.bat --css

:: 테스트 포함
build.bat --test

:: 전체 빌드 (CSS + 클린 + 테스트)
build.bat --css --clean --test
```

### Linux/Mac

```bash
# 스크립트 실행 권한 부여
chmod +x build.sh start.sh

# 기본 빌드
./build.sh

# 클린 빌드
./build.sh --clean

# CSS 빌드 포함
./build.sh --css
```

### Gradle 직접 사용

```bash
# Windows
gradlew.bat clean bootJar -x test

# Linux/Mac
./gradlew clean bootJar -x test
```

---

## 실행 방법

### Windows

```cmd
:: 스크립트 사용
start.bat

:: 직접 실행
java -jar build\libs\fphps_web_example-0.0.1-SNAPSHOT.jar
```

### Linux/Mac

```bash
# 스크립트 사용
./start.sh

# 직접 실행
java -jar build/libs/fphps_web_example-0.0.1-SNAPSHOT.jar
```

### 개발 모드 (Hot Reload)

```bash
# Gradle bootRun 사용 (devtools 활성화)
gradlew.bat bootRun
```

### 포트 변경

```bash
# 환경 변수로 포트 지정
set PORT=9090
start.bat

# 또는 명령줄 옵션
java -jar app.jar --server.port=9090
```

---

## 배포 방법

### 1. 로컬 배포 (권장)

FastPass SDK(FPHPS.dll)가 Windows 네이티브 라이브러리이므로, Windows 환경에서만 동작합니다.

```
배포 폴더 구조:
fastpass-web/
├── fphps_web_example-0.0.1-SNAPSHOT.jar
├── start.bat
├── application.properties (선택)
└── log/ (자동 생성)
```

**배포 단계:**

1. JAR 파일 복사
   ```cmd
   copy build\libs\fphps_web_example-*.jar C:\fastpass-web\
   ```

2. 실행 스크립트 복사
   ```cmd
   copy start.bat C:\fastpass-web\
   ```

3. (선택) 설정 파일 복사 및 수정
   ```cmd
   copy src\main\resources\application.properties C:\fastpass-web\
   ```

4. 실행
   ```cmd
   cd C:\fastpass-web
   start.bat
   ```

### 2. Windows 서비스 등록 (수동)

[WinSW](https://github.com/winsw/winsw)를 사용하여 수동으로 Windows 서비스를 등록할 수 있습니다.

> **Note**: [Windows 설치 프로그램](#windows-설치-프로그램)을 사용하면 JRE 번들링과 서비스 등록이 자동으로 처리됩니다.

`fastpass-service.xml`:
```xml
<service>
  <id>FastPassWeb</id>
  <name>FastPass Web Application</name>
  <description>SMARTCORE FastPass E-Passport Reader Web Service</description>
  <executable>java</executable>
  <arguments>-Xms256m -Xmx512m -jar fphps_web_example-0.0.1-SNAPSHOT.jar</arguments>
  <log mode="roll-by-size">
    <sizeThreshold>10240</sizeThreshold>
    <keepFiles>8</keepFiles>
  </log>
</service>
```

```cmd
:: 서비스 설치
winsw install fastpass-service.xml

:: 서비스 시작
winsw start fastpass-service.xml
```

### 3. Docker 배포 (제한적)

> ⚠️ **주의**: FPHPS.dll은 Windows 네이티브 라이브러리이므로 Linux 컨테이너에서는 동작하지 않습니다.
> Windows Container를 사용해야 하며, USB 패스스루가 필요합니다.

---

## Windows 설치 프로그램

Java가 설치되지 않은 PC에서도 동작하는 Windows 설치 프로그램(.exe)을 생성합니다.
설치 시 JRE 번들링, Windows 서비스 자동 등록, 방화벽 규칙 추가가 자동으로 처리됩니다.

### 구성 요소

| 도구 | 용도 | 버전 |
|------|------|------|
| **jlink** | JDK 21에서 최소 JRE 생성 (~62MB) | JDK 21 내장 |
| **WinSW** | Spring Boot JAR을 Windows 서비스로 등록 | v3.0.0-alpha.11 |
| **Inno Setup** | 전문 Windows 설치 프로그램 생성 | 6.7+ |

### 빌드 PC 사전 조건

1. **JDK 21** 설치 및 `JAVA_HOME` 환경변수 설정
2. **Inno Setup 6** 설치 ([다운로드](https://jrsoftware.org/isdl.php))
3. **WinSW-x64.exe** 다운로드 후 `installer/winsw/` 에 배치
   - [GitHub Release](https://github.com/winsw/winsw/releases) 에서 `WinSW-x64.exe` 다운로드

### 인스톨러 빌드

```cmd
cd installer
build-installer.bat
```

빌드 과정:
1. `gradlew.bat clean bootJar -x test` → Spring Boot JAR 빌드
2. `create-jre.bat` → jlink 커스텀 JRE 생성 (24개 모듈, ~62MB)
3. Staging 디렉토리 구성 (JAR + JRE + WinSW + 설정파일)
4. Inno Setup 컴파일 → `installer/output/FastPassSetup-x.x.x.exe` 생성

### 빌드 결과

```
installer/output/FastPassSetup-1.1.0.exe    (~131MB)
```

포함 내용:
- Spring Boot JAR (~93MB)
- 번들 JRE - Java 21 jlink (~62MB)
- WinSW 서비스 래퍼 (~17MB)
- application.properties
- 서비스 관리 스크립트

### 설치 프로그램 기능

#### 설치 (Install)
1. 환영 화면 → 설치 경로 선택 (기본: `C:\Program Files\SMARTCORE\FastPass Web`)
2. 파일 복사: JAR, JRE, WinSW, 설정파일
3. Windows 서비스 자동 등록 (`FastPassWeb`)
4. 방화벽 인바운드 규칙 추가 (포트 50000)
5. 바탕화면/시작메뉴 바로가기 생성 (선택)
6. 서비스 시작 → 브라우저 열기 옵션

#### 제거 (Uninstall)
1. 서비스 중지 및 제거
2. 방화벽 규칙 삭제
3. 파일 삭제

### 설치 후 디렉토리 구조

```
C:\Program Files\SMARTCORE\FastPass Web\
├── fastpass-service.exe          # WinSW (서비스 래퍼)
├── fastpass-service.xml          # WinSW 설정
├── fphps_web_example.jar         # Spring Boot JAR
├── application.properties        # 앱 설정 (수정 가능)
├── jre/                          # 번들 JRE (Java 21)
│   ├── bin/java.exe
│   └── ...
├── data/                         # SQLite DB (자동 생성)
├── log/                          # 서비스 로그 (자동 생성)
├── scripts/
│   ├── install-service.bat       # 서비스 재설치 시 사용
│   └── uninstall-service.bat     # 서비스 수동 제거 시 사용
└── unins000.exe                  # 언인스톨러
```

### 서비스 관리

```cmd
:: 서비스 상태 확인
sc query FastPassWeb

:: 서비스 중지
net stop FastPassWeb

:: 서비스 시작
net start FastPassWeb

:: 서비스 재시작
net stop FastPassWeb && net start FastPassWeb
```

Windows `services.msc`에서 "FastPass Web Application" 서비스를 GUI로 관리할 수도 있습니다.

### 설정 변경

설치 후 `application.properties`를 직접 편집하여 설정을 변경할 수 있습니다:

```cmd
:: 설정 파일 편집
notepad "C:\Program Files\SMARTCORE\FastPass Web\application.properties"

:: 변경사항 적용을 위해 서비스 재시작
net stop FastPassWeb && net start FastPassWeb
```

### Private CA 인증서 관리

PA API 서버(`pkd.smartcoreinc.com`)는 Private CA 인증서를 사용합니다. jlink JRE의 cacerts에는 이 인증서가 포함되어 있지 않으므로, 빌드 과정에서 자동으로 임포트합니다.

**인증서 파일 위치**: `installer/certs/pkd-private-ca.crt`

- `create-jre.bat` 실행 시 jlink JRE 생성 후 `keytool -importcert`로 자동 임포트
- 인증서 발급자: `CN=ICAO Local PKD Private CA, O=SmartCore Inc.`
- 유효기간: 2026-02-27 ~ 2036-02-25

**인증서 갱신 시**:
1. 새 CA 인증서를 `installer/certs/pkd-private-ca.crt`에 덮어쓰기
2. `build-installer.bat` 재실행 (JRE 재생성 + 인증서 재임포트)

**개발 JDK에 인증서 추가** (로컬 개발 환경):
```cmd
keytool -importcert -keystore "%JAVA_HOME%\lib\security\cacerts" -storepass changeit -alias pkd-ca -file installer\certs\pkd-private-ca.crt -noprompt
```

### 인스톨러 파일 구조

```
installer/
├── build-installer.bat              # 전체 빌드 자동화 스크립트
├── create-jre.bat                   # jlink JRE 생성 스크립트
├── fastpass-setup.iss               # Inno Setup 스크립트
├── certs/
│   └── pkd-private-ca.crt           # PA API Private CA 인증서
├── winsw/
│   ├── fastpass-service.xml         # WinSW 서비스 설정
│   └── WinSW-x64.exe               # WinSW 실행 파일 (.gitignore)
├── scripts/
│   ├── install-service.bat          # 서비스 설치 (설치 시 자동 호출)
│   └── uninstall-service.bat        # 서비스 제거 (제거 시 자동 호출)
├── staging/                         # 빌드 임시 디렉토리 (.gitignore)
├── jre/                             # jlink JRE 출력 (.gitignore)
└── output/                          # 설치 프로그램 출력 (.gitignore)
```

### 버전 업데이트

설치 프로그램 버전을 변경하려면 `installer/fastpass-setup.iss`의 상단을 수정합니다:

```iss
#define MyAppVersion "1.1.0"
```

> **Note**: FastPass SDK (FPHPS.dll)는 설치 프로그램에 포함되지 않습니다.
> SDK는 별도의 SMARTCORE FastPass SDK 설치 프로그램으로 설치해야 하며, 시스템 PATH에 등록되어야 합니다.

---

## 설정 옵션

### application.properties

```properties
# 서버 설정
server.port=50000
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.force=true

# 로깅 설정
logging.level.com.smartcoreinc=INFO
logging.file.name=log/application.log
logging.logback.rollingpolicy.max-file-size=5MB
logging.logback.rollingpolicy.max-history=3
logging.logback.rollingpolicy.total-size-cap=15MB
logging.charset.file=UTF-8
logging.charset.console=UTF-8

# PA API 설정 (Passive Authentication)
pa-api.base-url=https://pkd.smartcoreinc.com
pa-api.api-key=icao_XXXXXXXX_YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY

# Face Verification API 설정
face-api.base-url=http://localhost:10100

# 데이터 내보내기 설정
document-export.base-dir=D:/passport_exports
```

### 외부 설정 파일 사용

JAR 파일과 같은 디렉터리에 `application.properties`를 두면 자동으로 적용됩니다.

```cmd
fastpass-web/
├── fphps_web_example-0.0.1-SNAPSHOT.jar
└── application.properties  ← 이 파일의 설정이 우선 적용됨
```

### JVM 옵션

```cmd
:: 메모리 설정
set JAVA_OPTS=-Xms512m -Xmx1024m

:: 인코딩 설정
set JAVA_OPTS=-Dfile.encoding=UTF-8

:: 프로파일 설정
java -jar app.jar --spring.profiles.active=production
```

---

## 문제 해결

### 1. Java를 찾을 수 없음

```
[ERROR] Java is not installed or not in PATH.
```

**해결:**
- JDK 21 이상 설치
- `JAVA_HOME` 환경변수 설정
- `%JAVA_HOME%\bin`을 PATH에 추가

### 2. FPHPS.dll 로딩 실패

```
java.lang.UnsatisfiedLinkError: Unable to load library 'FPHPS'
```

**해결:**
- FastPass SDK 설치 확인
- FPHPS.dll 경로가 시스템 PATH에 포함되어 있는지 확인
- 64-bit DLL과 64-bit Java 사용 확인

### 3. 포트 충돌

```
Web server failed to start. Port 8080 was already in use.
```

**해결:**
```cmd
:: 포트 사용 프로세스 확인
netstat -ano | findstr :8080

:: 다른 포트로 실행
java -jar app.jar --server.port=9090
```

### 4. 메모리 부족

```
java.lang.OutOfMemoryError: Java heap space
```

**해결:**
```cmd
set JAVA_OPTS=-Xms512m -Xmx1024m
start.bat
```

### 5. PA API 인증서 오류 (PKIX path building failed)

```
PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
unable to find valid certification path to requested target
```

**해결:**
PA API 서버의 Private CA 인증서가 JRE cacerts에 등록되지 않은 경우 발생합니다.

- **Installer 환경**: `build-installer.bat`를 재실행하여 최신 인증서가 포함된 JRE를 번들링
- **개발 환경**: 개발 JDK cacerts에 인증서 임포트
  ```cmd
  keytool -importcert -keystore "%JAVA_HOME%\lib\security\cacerts" -storepass changeit -alias pkd-ca -file installer\certs\pkd-private-ca.crt -noprompt
  ```

### 6. 로그 파일 위치

로그 파일은 `log/application.log`에 생성됩니다.

```cmd
:: 실시간 로그 확인
type log\application.log

:: PowerShell에서 tail 형태로 확인
Get-Content log\application.log -Wait -Tail 50
```

---

## 버전 정보

| 버전 | 날짜 | 변경사항 |
|------|------|----------|
| 0.0.1-SNAPSHOT | 2025-12 | 초기 버전, PWA 지원 추가 |
| 1.0.0 | 2026-02 | Windows 설치 프로그램, jlink JRE 번들링, WinSW 서비스 래퍼 |
| 1.1.0 | 2026-02 | 다크 모드 콘텐츠 영역 전체 적용, Tailwind CSS v4 @source 디렉티브 |
| 1.1.0 | 2026-03 | MRZ diff 시각화, Installer 안정성 개선, Private CA 인증서 번들링 |

---

## 지원

- **이슈 리포트**: GitHub Issues
- **기술 지원**: support@smartcoreinc.com
- **문서**: [CLAUDE.md](../CLAUDE.md)
