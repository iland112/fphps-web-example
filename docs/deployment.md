# FastPass Web Application - 배포 가이드

## 목차
1. [시스템 요구사항](#시스템-요구사항)
2. [빌드 방법](#빌드-방법)
3. [실행 방법](#실행-방법)
4. [배포 방법](#배포-방법)
5. [설정 옵션](#설정-옵션)
6. [문제 해결](#문제-해결)

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

### 2. Windows 서비스 등록

[NSSM](https://nssm.cc/) 또는 [WinSW](https://github.com/winsw/winsw)를 사용하여 Windows 서비스로 등록할 수 있습니다.

**WinSW 예시:**

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

## 설정 옵션

### application.properties

```properties
# 서버 설정
server.port=8080
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.force=true

# 로깅 설정
logging.level.com.smartcoreinc=DEBUG
logging.file.name=log/application.log
logging.logback.rollingpolicy.max-file-size=1MB
logging.logback.rollingpolicy.max-history=7
logging.charset.file=UTF-8
logging.charset.console=UTF-8

# PA API 설정 (Passive Authentication)
pa-api.base-url=http://192.168.100.11:8081
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

### 5. 로그 파일 위치

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

---

## 지원

- **이슈 리포트**: GitHub Issues
- **기술 지원**: support@smartcoreinc.com
- **문서**: [CLAUDE.md](../CLAUDE.md)
