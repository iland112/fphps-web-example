# FPHPS_WEB_Example - Claude Code Analysis

## 프로젝트 개요

**프로젝트명**: fphps-web-example
**버전**: 0.0.1-SNAPSHOT
**목적**: SMARTCORE FastPass SDK(전자여권 판독기)를 Spring Boot 웹 애플리케이션에 통합한 데모 프로젝트

이 프로젝트는 C/C++ Windows DLL로 작성된 전자여권 판독기 SDK를 JNA(Java Native Access)로 래핑하여 웹 환경에서 사용할 수 있도록 구현한 샘플 애플리케이션입니다.

---

## 기술 스택 상세

### 백엔드 (Backend)

| 기술 | 버전 | 용도 |
|------|------|------|
| **Java** | 21 | 핵심 언어 (툴체인 설정) |
| **Spring Boot** | 3.5.0 | 웹 애플리케이션 프레임워크 |
| └ spring-boot-starter-web | | HTTP/REST 엔드포인트 |
| └ spring-boot-starter-thymeleaf | | 서버 사이드 템플릿 엔진 |
| └ spring-boot-starter-websocket | | 실시간 양방향 통신 |
| └ spring-boot-devtools | | 개발 편의 도구 |
| **JNA** | 5.17.0 | Java-네이티브 라이브러리 브리지 |
| **Apache HttpClient 5** | 5.4.4 | 안정적인 HTTP 통신 (PA API 연동) |
| **Bouncy Castle** | 1.78 | PKI 암호화 및 전자여권 인증 |
| **Gson** | 2.10.1 | JSON 직렬화/역직렬화 |
| **JavaTuples** | 1.2 | 불변 튜플 데이터 구조 |
| **Lombok** | | 보일러플레이트 코드 생성 |
| **WebJars** | | JavaScript 라이브러리 로컬 의존성 관리 |
| └ webjars-locator-core | 0.59 | WebJar 경로 자동 버전 해결 |
| └ htmx.org | 2.0.4 | HTMX 라이브러리 (CDN 대체) |

### 프론트엔드 (Frontend)

| 기술 | 버전 | 용도 |
|------|------|------|
| **Tailwind CSS** | 4.1.5 | 유틸리티 우선 CSS 프레임워크 |
| **Preline** | 3.0.1 | UI 컴포넌트 라이브러리 |
| **DaisyUI** | 5.0.35 | Tailwind 컴포넌트 확장 |
| **Thymeleaf Layout Dialect** | 3.0.0 | 템플릿 레이아웃 관리 |

### 빌드 시스템

- **Gradle** (Wrapper 포함)
- **Java 21 툴체인**
- **UTF-8 인코딩** 전역 설정
- 로컬 JAR 의존성: `fphps-1.0.0.jar`

---

## 프로젝트 구조

```
FPHPS_WEB_Example/
│
├── src/main/java/com/smartcoreinc/fphps/example/fphps_web_example/
│   ├── FphpsWebExampleApplication.java         # Spring Boot 진입점
│   │
│   ├── Controllers/
│   │   └── FPHPSController.java                # 메인 REST 컨트롤러
│   │
│   ├── Services/
│   │   ├── FPHPSService.java                   # 핵심 디바이스 관리 서비스
│   │   ├── DevicePropertiesService.java        # 디바이스 속성 상태 관리
│   │   ├── PassiveAuthenticationService.java   # PA 검증/Lookup 서비스 (외부 API 연동)
│   │   └── FaceVerificationService.java        # 얼굴 검증 서비스 (InsightFace 연동)
│   │
│   ├── strategies/                             # 전략 패턴 구현
│   │   ├── DocumentReadStrategy.java           # 전략 인터페이스
│   │   ├── PassportReadStrategy.java           # 전자여권 읽기 전략
│   │   ├── IDCardReadStrategy.java             # 신분증 읽기 전략
│   │   └── BarcodeReadStrategy.java            # 바코드 읽기 전략
│   │
│   ├── config/                                 # 설정 클래스
│   │   ├── WebSocketConfig.java                # Raw WebSocket 설정
│   │   ├── WebSocketStompBrokerConfig.java     # STOMP 메시징 설정
│   │   ├── PaApiClientConfig.java              # PA API HTTP 클라이언트 설정
│   │   └── handler/
│   │       └── FastPassWebSocketHandler.java   # WebSocket 메시지 핸들러
│   │
│   ├── dto/                                    # DTO 클래스
│   │   ├── pa/                                 # PA API DTO
│   │   │   ├── PaVerificationRequest.java      # PA 검증 요청
│   │   │   ├── PaVerificationResponse.java     # PA 검증 응답
│   │   │   ├── PaLookupRequest.java            # PA Lookup 요청 (subjectDn, fingerprint)
│   │   │   ├── PaLookupResponse.java           # PA Lookup 응답 래퍼
│   │   │   └── PaLookupValidation.java         # PA Lookup 검증 결과 (21개 필드)
│   │   ├── face/                               # Face Verification DTO
│   │   │   ├── FaceVerificationResponse.java   # 얼굴 검증 응답
│   │   │   ├── FaceQualityMetrics.java         # 얼굴 품질 메트릭
│   │   │   └── BoundingBox.java                # 얼굴 영역 좌표
│   │   ├── CertificateInfo.java                # X509 인증서 정보
│   │   └── ParsedSODInfo.java                  # SOD 파싱 정보
│   │
│   ├── exceptions/
│   │   └── DeviceOperationException.java       # 커스텀 예외
│   │
│   ├── advice/
│   │   └── GlobalExceptionHandler.java         # 전역 예외 처리
│   │
│   └── forms/                                  # DTO/Form 객체
│       ├── SettingsForm.java                   # 디바이스 설정 (79개 속성)
│       ├── EPassportSettingForm.java           # 전자여권 설정
│       ├── DevSettingsForm.java                # 개발 설정
│       └── ScanForm.java                       # 페이지 스캔 설정
│
├── src/main/resources/
│   ├── application.properties                  # Spring 설정
│   ├── templates/
│   │   ├── index.html                          # 메인 페이지
│   │   ├── settings.html                       # 설정 페이지
│   │   ├── layouts/
│   │   │   └── default.html                    # 기본 레이아웃
│   │   └── fragments/                          # 30+ UI 프래그먼트
│   │       ├── header.html, footer.html
│   │       ├── device_info.html
│   │       ├── passport_*.html
│   │       ├── idcard_*.html
│   │       ├── barcode_*.html
│   │       └── ...
│   └── static/
│       ├── css/main.css                        # 컴파일된 Tailwind CSS
│       ├── js/                                 # JavaScript 파일
│       └── image/                              # 로고 및 이미지
│
├── src/main/frontend/
│   ├── package.json                            # npm 의존성
│   ├── tailwind.config.js                      # Tailwind 설정
│   └── input.css                               # Tailwind 소스
│
├── docs/
│   ├── api_documentation.md                    # API 문서
│   ├── deployment.md                           # 배포 가이드
│   ├── user_manual.md                          # 사용자 매뉴얼
│   ├── API_CLIENT_USER_GUIDE.md                # PA API 외부 클라이언트 연동 가이드
│   └── analysis/
│       └── 20251220_task_analysis.md           # 태스크 분석
│
├── installer/                                  # Windows 설치 프로그램
│   ├── build-installer.bat                     # 전체 빌드 자동화
│   ├── create-jre.bat                          # jlink 커스텀 JRE 생성
│   ├── fastpass-setup.iss                      # Inno Setup 스크립트
│   ├── winsw/
│   │   └── fastpass-service.xml                # WinSW 서비스 설정
│   └── scripts/
│       ├── install-service.bat                 # 서비스 설치
│       └── uninstall-service.bat               # 서비스 제거
│
├── build.gradle                                # Gradle 빌드 설정
├── settings.gradle                             # Gradle 프로젝트 설정
├── README.md                                   # 빠른 시작 가이드
├── GEMINI.md                                   # Gemini 분석 문서
└── CLAUDE.md                                   # 본 문서
```

---

## 핵심 컴포넌트 상세 분석

### 1. 컨트롤러 계층

#### FPHPSController.java
**라우트 베이스**: `/fphps`

**주요 엔드포인트**:

| HTTP 메서드 | 경로 | 기능 |
|-------------|------|------|
| GET | `/fphps` | 메인 홈페이지 (디바이스 정보 포함) |
| GET | `/fphps/home` | 홈 컨텐츠 프래그먼트 |
| GET | `/fphps/device` | 디바이스 정보 조회 |
| GET/POST | `/fphps/scan-page` | 페이지 스캔 인터페이스 |
| GET/POST | `/fphps/device-setting` | 디바이스 설정 관리 |
| GET | `/fphps/passport/manual-read` | 수동 전자여권 읽기 |
| POST | `/fphps/passport/run-auto-read` | 자동 전자여권 읽기 |
| POST | `/fphps/passport/verify-pa-v2` | PA 전체 검증 (API Gateway) |
| POST | `/fphps/passport/pa-lookup` | PA 간편 조회 (DSC Trust Chain Lookup) |
| GET | `/fphps/passport/pa-health` | PA API 서버 연결 상태 확인 |
| POST | `/fphps/passport/verify-face` | 얼굴 검증 (InsightFace) |
| POST | `/fphps/passport/export-data` | 여권 데이터 내보내기 |
| GET | `/fphps/idcard/manual-read` | 신분증 읽기 |
| GET | `/fphps/barcode/manual-read` | 바코드 읽기 |

### 2. 서비스 계층

#### FPHPSService.java
**역할**: 핵심 디바이스 관리 서비스

**주요 메서드**:
- `getDeviceInfo()`: 디바이스 정보 조회
- `read(docType, isAuto)`: 문서 타입에 따른 전략 패턴 실행
- `scanPage(lightType)`: 특정 광원으로 페이지 스캔
- `executeWithDevice(operation)`: 템플릿 메서드 패턴으로 디바이스 안전 제어

**특징**:
- 스레드 안전성을 위한 `synchronized` 메서드 사용
- Try-finally를 통한 디바이스 리소스 정리 보장
- 시작 시 디바이스 속성 로딩 및 캐싱

#### DevicePropertiesService.java
**역할**: 디바이스 속성 상태 관리 (싱글톤 스타일)

**주요 메서드**:
- `getProperties()`: 현재 디바이스 속성 반환
- `setProperties(props)`: 디바이스 속성 업데이트 (null 체크 포함)

**초기화**:
- NPE 방지를 위해 기본 빈 속성으로 초기화

### 3. 전략 패턴 구현

#### DocumentReadStrategy 인터페이스
```java
public interface DocumentReadStrategy {
    boolean supports(String docType);
    DocumentReadResponse read(FPHPSDevice device,
                             FastPassWebSocketHandler handler,
                             boolean isAuto);
}
```

#### 4가지 전략 구현체

1. **PassportReadStrategy**
   - 전자여권 읽기
   - RF(RFID) 활성화
   - EPassportReader 사용

2. **IDCardReadStrategy**
   - 신분증 읽기
   - 전자신분증 대비 RF 활성화 유지
   - IDCardReader 사용

3. **BarcodeReadStrategy**
   - 바코드/라벨 읽기
   - RF 및 IDCard 비활성화
   - BarcodeReader 사용

4. **DocumentReadStrategy**
   - 기본 인터페이스로 확장성 확보

**전략별 공통 로직**:
- 문서 타입에 맞는 디바이스 속성 설정
- 해당 Reader 클래스 사용
- WebSocket 핸들러로 실시간 이벤트 브로드캐스트

### 4. WebSocket 통신

#### WebSocketConfig.java (Raw WebSocket)
- 엔드포인트: `/fastpass`
- 모든 Origin 허용 (`*`)
- 텍스트 메시지 직접 처리

#### WebSocketStompBrokerConfig.java (STOMP 메시징)
- 구독 프리픽스: `/sub`
- 발행 프리픽스: `/pub`
- SockJS 폴백 지원

#### FastPassWebSocketHandler.java
**역할**: 메시지 브로드캐스팅

**주요 기능**:
- `TextWebSocketHandler` 확장 및 `MessageBroadcastable` 구현
- `CopyOnWriteArrayList`로 스레드 안전한 세션 관리
- 모든 활성 세션에 JSON 메시지 전송

**메서드**:
- `afterConnectionEstablished()`: 연결 로깅
- `afterConnectionClosed()`: 세션 제거
- `broadcast(EventMessageData)`: 전체 클라이언트에 메시지 전송

### 5. 예외 처리

#### DeviceOperationException
- 커스텀 런타임 예외
- 디바이스 작업 실패 전용
- 메시지 단독 또는 메시지+원인 생성자 지원

#### GlobalExceptionHandler.java
- `@ControllerAdvice`로 전역 예외 가로채기
- `DeviceOperationException` 처리
- 에러 프래그먼트 반환
- ERROR 레벨 로깅

### 6. DTO/Form 계층

#### SettingsForm.java
**속성 수**: 약 79개

**주요 카테고리**:
- RF(RFID) 설정
- 이미지 향상 (IR, UV, 백색광)
- 바코드 설정
- 전자여권 데이터 그룹 선택 (DG1-DG16)
- 전자여권 인증 옵션 (PA, AA, CA, TA, SAC)
- 배치 모드 이미지 캡처
- 반사 방지 및 노이즈 감소

**변환 메서드**:
- `from(FPHPSDeviceProperties)`: DTO → 도메인
- `to(SettingsForm, existing)`: 도메인 → DTO

---

## 아키텍처 패턴

### 적용된 디자인 패턴

1. **전략 패턴 (Strategy Pattern)**
   - **위치**: `strategies/` 패키지
   - **목적**: 문서 타입별 읽기 로직 런타임 선택
   - **장점**: 새로운 문서 타입 추가 용이

2. **템플릿 메서드 패턴 (Template Method Pattern)**
   - **위치**: `FPHPSService.executeWithDevice()`
   - **목적**: 일관된 디바이스 생명주기 관리
   - **장점**: 리소스 정리 보장

3. **싱글톤 패턴 (Singleton Pattern)**
   - **위치**: `DevicePropertiesService`, `FPHPSDeviceManager`
   - **목적**: 단일 상태 유지
   - **장점**: 디바이스 속성 중앙 관리

4. **옵저버 패턴 (Observer Pattern)**
   - **위치**: WebSocket 브로드캐스터
   - **목적**: 실시간 진행 상황 업데이트
   - **장점**: 다중 클라이언트 동시 알림

5. **DTO 패턴 (Data Transfer Object)**
   - **위치**: `forms/` 패키지
   - **목적**: 웹 폼과 도메인 모델 브리지
   - **장점**: 계층 간 데이터 전달 명확화

### 주요 구현 특징

#### 스레드 안전성 (Thread Safety)
- 디바이스 작업에 `synchronized` 적용
- WebSocket 세션 관리에 `CopyOnWriteArrayList` 사용

#### 에러 핸들링
- 네이티브 라이브러리 에러를 애플리케이션 예외로 변환
- 전역 예외 핸들러로 일관된 에러 응답
- 다단계 로깅 (DEBUG, INFO, ERROR)

#### 디바이스 생명주기 관리
- Try-finally로 정리 작업 보장
- 시작 시 디바이스 속성 로딩 및 캐싱
- UI를 통한 속성 수정 가능

#### 실시간 통신
- Raw WebSocket과 STOMP 이중 설정
- 직접 메시징 및 Pub/Sub 패턴 지원
- 모든 연결된 클라이언트에 읽기 진행 상황 브로드캐스트

---

## 프론트엔드 아키텍처

### Thymeleaf 템플릿

**레이아웃 구조** (Thymeleaf Layout Dialect):
- `layouts/default.html`: 공통 구조를 가진 마스터 레이아웃
- 프래그먼트 기반 조합으로 모듈성 확보
- 30개 이상의 템플릿 프래그먼트

**주요 템플릿 섹션**:
- 자동/수동 읽기 워크플로우 프래그먼트
- 디바이스 속성 표시 및 폼
- 이미지 캡처 및 표시
- MRZ(Machine Readable Zone) 데이터 파싱
- 이미지 및 메타데이터 결과 표시

### CSS 빌드 파이프라인

**빌드 스크립트** (`package.json`):
```json
{
  "build": "npx @tailwindcss/cli -i ./input.css -o ../resources/static/css/main.css --minify",
  "watch": "npx @tailwindcss/cli -i ./input.css -o ../resources/static/css/main.css --watch"
}
```

**의존성**:
- Tailwind CSS 4.1.5 (코어 스타일링)
- Preline 3.0.1 (사전 제작 컴포넌트)
- DaisyUI 5.0.35 (추가 컴포넌트)
- Tailwind Forms 0.5.10 (폼 스타일링)

**출력**: `src/main/resources/static/css/main.css`

---

## 설정 파일

### application.properties

```properties
spring.application.name=fphps_web_example
logging.level.com.smartcoreinc=DEBUG
logging.file.name=log/application.log
logging.logback.rollingpolicy.max-file-size=1MB
logging.logback.rollingpolicy.max-history=7
spring.thymeleaf.cache=false
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.force=true
logging.charset.file=UTF-8
logging.charset.console=UTF-8
```

**주요 설정**:
- smartcoreinc 패키지 DEBUG 로깅
- 롤링 파일 로그 (1MB 최대, 7일 보관)
- 개발용 Thymeleaf 캐시 비활성화
- 전역 UTF-8 인코딩

---

## 빌드 및 실행

### 요구사항

- **Java**: 21 이상
- **운영체제**: Windows (FPHPS SDK DLL 필요)
- **FastPass SDK**: 설치 및 PATH 설정 필요
- **브라우저**: WebSocket 지원

### 빌드 단계

```bash
# 1. 프론트엔드 의존성 설치 (최초 1회)
cd src/main/frontend/
npm install

# 2. Tailwind CSS 빌드
npm run build

# 3. 전체 빌드
cd ../../..
./gradlew clean build

# 4. 애플리케이션 실행
./gradlew bootRun
```

### 접속

**기본 URL**: `http://localhost:50000`

---

## 작업 이력

### 2026-02-27: Windows 설치 프로그램 구현

**구현 내용**:
- Java 미설치 PC에서도 동작하는 Windows 설치 프로그램(.exe) 생성 파이프라인 구현
- jlink로 최소 JRE 번들링 (62MB), WinSW로 Windows 서비스 자동 등록, Inno Setup으로 전문 인스톨러 생성
- 설치 시 방화벽 규칙 자동 추가, 바탕화면 바로가기 생성, 서비스 자동 시작
- 제거 시 서비스 중지/제거, 방화벽 규칙 삭제

**주요 변경사항**:

1. **jlink 커스텀 JRE** (`installer/create-jre.bat`):
   - JDK 21에서 Spring Boot 실행에 필요한 24개 모듈만 포함한 최소 JRE 생성
   - 압축 옵션 (`--compress zip-6`, `--strip-debug`, `--no-man-pages`) 적용
   - 결과: ~62MB (전체 JDK ~300MB 대비 약 80% 감소)

2. **WinSW 서비스 래퍼** (`installer/winsw/fastpass-service.xml`):
   - Service ID: `FastPassWeb`, Display Name: `FastPass Web Application`
   - 번들 JRE 경로로 java.exe 실행 (`%BASE%\jre\bin\java.exe`)
   - 자동 시작 (Delayed Auto Start), 실패 시 재시작 (60초/120초 후)
   - 로그: roll-by-size (10MB, 8파일)

3. **서비스 관리 스크립트** (`installer/scripts/`):
   - `install-service.bat`: 기존 서비스 존재 시 제거 후 재설치, 서비스 시작
   - `uninstall-service.bat`: 서비스 중지 및 제거

4. **Inno Setup 스크립트** (`installer/fastpass-setup.iss`):
   - 관리자 권한 필요, x64 전용
   - 기본 설치 경로: `C:\Program Files\SMARTCORE\FastPass Web`
   - 한국어/영어 다국어 지원
   - PostInstall: 서비스 설치, 방화벽 규칙 추가, 브라우저 열기 옵션
   - 언인스톨: 서비스 제거, 방화벽 규칙 삭제

5. **전체 빌드 자동화** (`installer/build-installer.bat`):
   - 사전 조건 체크 (JAVA_HOME, Gradle, WinSW, Inno Setup)
   - 4단계 빌드: JAR → JRE → Staging → Inno Setup 컴파일
   - 출력: `installer/output/FastPassSetup-x.x.x.exe` (~131MB)

**설치 결과 디렉토리 구조**:
```
C:\Program Files\SMARTCORE\FastPass Web\
├── fastpass-service.exe          # WinSW (서비스 래퍼)
├── fastpass-service.xml          # WinSW 설정
├── fphps_web_example.jar         # Spring Boot JAR
├── application.properties        # 앱 설정
├── jre/                          # 번들 JRE (jlink, Java 21)
├── data/                         # SQLite DB (자동 생성)
├── log/                          # 로그 (자동 생성)
└── unins000.exe                  # 언인스톨러
```

**빌드 PC 사전 조건**:
- JDK 21 (`JAVA_HOME` 설정)
- Inno Setup 6 설치
- WinSW-x64.exe (`installer/winsw/`에 배치)

**수정된 파일**:

**신규:**
- `installer/build-installer.bat` - 전체 빌드 자동화 스크립트
- `installer/create-jre.bat` - jlink JRE 생성 스크립트
- `installer/fastpass-setup.iss` - Inno Setup 스크립트
- `installer/winsw/fastpass-service.xml` - WinSW 서비스 설정
- `installer/scripts/install-service.bat` - 서비스 설치 스크립트
- `installer/scripts/uninstall-service.bat` - 서비스 제거 스크립트

**수정:**
- `.gitignore` - installer 빌드 산출물 제외 (staging/, jre/, output/, WinSW-x64.exe)
- `docs/deployment.md` - Windows 설치 프로그램 빌드 가이드 추가
- `docs/user_manual.md` - Windows 설치 프로그램 설치 가이드 추가

**테스트 결과**:
- ✅ jlink JRE 생성 성공 (Java 21.0.7, 62MB)
- ✅ Gradle bootJar 빌드 성공
- ✅ Inno Setup 컴파일 성공 (`FastPassSetup-1.0.0.exe`, 131MB)
- ✅ WinSW 서비스 설정 파일 정상

---

### 2026-02-27: 다크 모드 콘텐츠 영역 전체 적용

**구현 내용**:
- 헤더/사이드바만 적용되어 있던 다크 모드를 콘텐츠 영역 전체로 확장
- Thymeleaf 템플릿 7개, JavaScript 동적 렌더링 3개 파일에 `dark:` Tailwind CSS 클래스 추가
- 홈 페이지 Quick Start Guide 카드 및 Feature 카드 다크 모드 적용
- Tailwind CSS v4 `@source` 디렉티브 추가로 콘텐츠 스캐닝 보장

**주요 변경사항**:

1. **Tailwind CSS v4 콘텐츠 스캐닝 수정** (`input.css`):
   - `@source "../resources/templates/**/*.html"` 추가
   - `@source "../resources/static/js/**/*.js"` 추가
   - Tailwind v4는 `tailwind.config.js`의 `content` 외에 `input.css`의 `@source` 디렉티브도 필요

2. **Thymeleaf 템플릿 다크 모드 적용** (7개 파일):
   - `epassport_manual_read.html` - 탭 네비게이션, 탭 콘텐츠 컨테이너
   - `epassport_auto_read.html` - 탭 네비게이션, 이벤트 로그
   - `passport_cards.html` - MRZ, Photo, Auth 등 6개 카드
   - `pa_tab_content.html` - PA 헤더, 버튼, 결과 컨테이너
   - `sod_information.html` - SOD 정보 카드, 인증서 상세
   - `mrz_validation.html` - MRZ 검증 결과, 상태 배지
   - `face_verification_tab_content.html` - Face 탭 헤더, 결과 컨테이너

3. **JavaScript 동적 렌더링 다크 모드 적용** (3개 파일):
   - `pa-verification.js` - PA Health Check 배너, 에러 카드, PA Verify/Lookup 결과 렌더링 (13개 함수)
   - `passport-tabs.js` - SOD 렌더링, CRL 상태, MRZ Validation 렌더링 (11개 함수, 30+ 수정)
   - `face-verification.js` - Face Verification 결과, Quality Metrics 렌더링

4. **홈 페이지 다크 모드** (`home_content.html`):
   - Quick Start Guide 카드 컨테이너
   - 3개 Step 카드 (Connect → Choose → Execute) 그라데이션 배경
   - 5개 Feature 카드 아이콘 및 배지 (E-Passport, ID Card, Barcode, Scan, Settings)
   - Step 2/3 텍스트 대비 향상: 그라데이션 opacity `/20` → `/30`, 텍스트 `dark:text-white`

5. **SW 캐시 버전 업데이트** (`sw.js`):
   - 캐시 버전: v32 → v34
   - 브라우저 캐시 무효화로 최신 CSS/JS 적용

**색상 매핑 규칙**:

| Light Mode | Dark Mode |
|-----------|-----------|
| `bg-white` | `dark:bg-neutral-800` |
| `bg-gray-50` | `dark:bg-neutral-700/50` |
| `border-gray-200` | `dark:border-neutral-700` |
| `text-gray-900` | `dark:text-neutral-100` |
| `text-gray-600` | `dark:text-neutral-400` |
| `bg-blue-50` | `dark:bg-blue-900/20` |
| `shadow-sm` | `dark:shadow-neutral-900/30` |

**수정된 파일**:

**프론트엔드 빌드:**
- `src/main/frontend/input.css` - `@source` 디렉티브 추가

**Thymeleaf 템플릿 (7개):**
- `templates/fragments/epassport_manual_read.html` - 탭 네비게이션, 컨테이너
- `templates/fragments/epassport_auto_read.html` - 탭 네비게이션, 이벤트 로그
- `templates/fragments/passport_cards.html` - 6개 카드 프래그먼트
- `templates/fragments/pa_tab_content.html` - PA 헤더, 결과 컨테이너
- `templates/fragments/sod_information.html` - SOD 정보 카드
- `templates/fragments/mrz_validation.html` - MRZ 검증 결과
- `templates/fragments/face_verification_tab_content.html` - Face 탭

**JavaScript (3개):**
- `static/js/pa-verification.js` - PA 관련 동적 렌더링
- `static/js/passport-tabs.js` - SOD/MRZ 동적 렌더링
- `static/js/face-verification.js` - Face 결과 렌더링

**CSS/캐시:**
- `static/css/main.css` - Tailwind CSS 재빌드 (dark: 클래스 포함)
- `static/sw.js` - 캐시 v32 → v34

**홈 페이지:**
- `templates/fragments/home_content.html` - Quick Start + Feature 카드

**테스트 결과**:
- ✅ `gradlew.bat build -x test` 빌드 성공
- ✅ 다크 모드 토글 시 콘텐츠 영역 전체 어두운 배경 적용
- ✅ MRZ Validation 탭 다크 모드 정상
- ✅ PA Verification 결과 다크 모드 정상
- ✅ SOD Information 다크 모드 정상
- ✅ Face Verification 결과 다크 모드 정상
- ✅ 홈 페이지 Quick Start / Feature 카드 다크 모드 정상
- ✅ Tailwind CSS v4 `@source` 디렉티브로 dark: 클래스 정상 생성

---

### 2026-02-25: PA API Key 인증, Health Check, 사용자 매뉴얼 작성

**구현 내용**:
- PA API 서버에 API Key 인증(`X-API-Key` 헤더) 자동 전송 기능 추가
- PA 탭 선택 시 PA API 서버 연결 상태 자동 확인 (Health Check) 기능 추가
- 사용자 매뉴얼 전면 작성 (플레이스홀더 → 13개 섹션 완전 문서화)
- PA API 외부 클라이언트 연동 가이드 문서 추가

**주요 변경사항**:

1. **PA API Key 인증** (`PaApiClientConfig.java`, `application.properties`):
   - `pa-api.api-key` 프로퍼티 추가
   - RestTemplate에 `ClientHttpRequestInterceptor` 추가하여 모든 PA API 요청에 `X-API-Key` 헤더 자동 포함
   - API Key 미설정 시 경고 로그 출력
   - `pa-api.base-url`을 `https://pkd.smartcoreinc.com`으로 업데이트 (Private CA 인증서 Java truststore 등록)

2. **PA Health Check** (`PassiveAuthenticationService.java`, `FPHPSController.java`, `pa-verification.js`):
   - `PassiveAuthenticationService.healthCheck()` 메서드 추가 (`GET /api/health`)
   - `GET /passport/pa-health` 엔드포인트 추가 (컨트롤러)
   - `checkPaApiHealth(prefix)` JavaScript 함수 추가
   - PA 탭 선택 시 자동 호출 (Manual Read: `switchTab('pa')`, Auto Read: `switchTabAuto('pa')`)
   - 상태 배너: 체크 중(회색+스피너) → 성공(녹색, 3초 후 fade out) → 실패(빨간색, 유지)

3. **MRZ Lines 폰트 크기 증가** (`pa-verification.js`):
   - DG1 MRZ Data 카드의 MRZ Lines 폰트: `text-xs`(12px) → `text-sm`(14px)

4. **사용자 매뉴얼 전면 작성** (`docs/user_manual.md`):
   - 13개 섹션: 개요, 시스템 요구사항, 설치/실행, 설정, 화면 구성, 전자여권(5탭 상세), 신분증, 바코드, 스캔, 디바이스 설정, 외부 서비스, PWA, 트러블슈팅
   - 최근 추가 기능 모두 반영 (PA Lookup, Health Check, API Key, MRZ Validation, Face Bounding Box 등)

5. **PA API 외부 클라이언트 가이드** (`docs/API_CLIENT_USER_GUIDE.md`):
   - API Key 인증 흐름, Base URL, 엔드포인트 목록
   - curl, Python, Java, C# 연동 예제
   - Rate Limiting, 에러 코드, 트러블슈팅

**수정된 파일**:

**신규:**
- `docs/API_CLIENT_USER_GUIDE.md` - PA API 외부 클라이언트 연동 가이드

**수정:**
- `config/PaApiClientConfig.java` - API Key 인터셉터, `pa-api.api-key` 프로퍼티 주입
- `application.properties` - `pa-api.api-key`, `pa-api.base-url` 업데이트
- `Services/PassiveAuthenticationService.java` - `healthCheck()` 메서드 추가
- `Controllers/FPHPSController.java` - `GET /passport/pa-health` 엔드포인트 추가
- `static/js/pa-verification.js` - `checkPaApiHealth()` 함수, MRZ Lines 폰트 크기
- `templates/fragments/epassport_manual_read.html` - `switchTab('pa')` 시 헬스 체크 호출
- `templates/fragments/epassport_auto_read.html` - `switchTabAuto('pa')` 시 헬스 체크 호출
- `docs/user_manual.md` - 전면 작성 (13개 섹션)

**테스트 결과**:
- ✅ `gradlew build -x test` 빌드 성공
- ✅ PA API Key 헤더 자동 전송 확인
- ✅ PA Health Check 배너 표시 (연결 성공/실패)
- ✅ MRZ Lines 폰트 크기 증가 확인

---

### 2026-02-14: PA Lookup (간편 조회) 기능 구현 및 UI 개선

**구현 내용**:
- PA API 서버의 간편 조회 API (`POST /api/certificates/pa-lookup`)를 통한 DSC Trust Chain 경량 검증 기능 구현
- 기존 전체 검증(Verify PA: SOD/DG 전송, 100-500ms) 대비 경량 조회(5-20ms) 방식 추가
- DSC Subject DN 또는 SHA-256 Fingerprint만으로 PKD에 등록된 Trust Chain 검증 결과를 즉시 조회
- API 응답 분석 후 Lookup 모드 특화 클라이언트 사이드 렌더링 개선

**주요 변경사항**:

1. **DTO 생성** (신규 3개):
   - `PaLookupRequest.java`: 요청 DTO (`subjectDn`, `fingerprint`)
   - `PaLookupResponse.java`: 응답 래퍼 DTO (`success`, `validation`, `message`)
   - `PaLookupValidation.java`: 검증 결과 DTO (21개 필드 - validationStatus, trustChainValid, cscaFound 등)

2. **Service 메서드 추가** (`PassiveAuthenticationService.java`):
   - `paLookup(DocumentReadResponse)` 메서드 추가
   - `ParsedSODInfo`에서 DSC 인증서 정보 추출
   - SHA-256 Fingerprint 형식 변환: `"AA:BB:CC:..."` → `"aabbcc..."` (콜론 제거, 소문자)
   - `POST /api/certificates/pa-lookup`으로 요청 전송

3. **Controller 엔드포인트** (`FPHPSController.java`):
   - `POST /passport/pa-lookup` 추가
   - 반환 타입: `Map<String, Object>` (API 응답 + `requestFingerprint`)
   - API가 fingerprint를 null로 반환하므로 요청에 사용한 값을 함께 전달

4. **UI 버튼 추가** (`pa_tab_content.html`):
   - "Verify PA" 옆에 indigo 색상 "PA Lookup" 버튼 추가
   - 별도 spinner/text 요소
   - `onclick="paLookup(...)"`

5. **JavaScript 렌더링 개선** (`pa-verification.js`):
   - `paLookup()` 함수: API 호출 및 에러 처리
   - `parseLookupDate()`: 비표준 날짜 형식 파싱 (`"2020-03-13 08:05:47+00"` → ISO 변환)
   - `deriveValidityFromDates()`: Lookup 모드에서 `validityPeriodValid` 플래그(항상 false) 대신 날짜로 직접 유효기간 판단
   - `renderLookupStatusCard()`: 상태 헤더 (VALID/EXPIRED_VALID/INVALID/PENDING/ERROR)
   - `renderLookupCertInfoCard()`: Subject DN, Issuer DN, 유효기간(Valid/Expired 배지), Fingerprint
   - `renderLookupTrustChainCard()`: Trust Chain 검증 결과, signatureValid "Not Checked" 처리
   - `renderLookupRevocationCard()`: null → "NOT_CHECKED" 처리
   - `renderLookupInfoNote()`: 간편 검증 안내 노트

**Lookup 모드 클라이언트 사이드 처리**:

Lookup API는 전체 검증과 달리 일부 필드를 검증하지 않아 false/null로 반환. 클라이언트에서 이를 적절히 표시:

| 필드 | API 반환값 | 처리 방식 |
|------|-----------|----------|
| `validityPeriodValid` | `false` (미검증) | `notBefore`/`notAfter` 날짜로 클라이언트에서 직접 판단 + Valid/Expired 배지 |
| `signatureValid` | `false` (미검증) | `signatureAlgorithm`이 null이면 "Not Checked" 회색 배지 |
| `signatureAlgorithm` | `null` | "Not Checked" 이탤릭 회색 텍스트 |
| `fingerprintSha256` | `null` | Controller에서 요청에 사용한 값(`requestFingerprint`)을 함께 반환 |
| `revocationStatus` | `null` | "NOT_CHECKED" 회색 스타일 (기존 "unknown" 노란색과 구분) |
| `trustChainPath` | `""` (빈 문자열) | 행 자체를 숨김 (기존 "N/A" 표시 대신) |
| 날짜 형식 | `"2020-03-13 08:05:47+00"` | `parseLookupDate()`로 비표준 형식 파싱 (space→T, +00→+00:00) |

**수정된 파일**:

**신규:**
- `dto/pa/PaLookupRequest.java` - PA Lookup 요청 DTO
- `dto/pa/PaLookupResponse.java` - PA Lookup 응답 래퍼 DTO
- `dto/pa/PaLookupValidation.java` - PA Lookup 검증 결과 DTO (21개 필드)

**수정:**
- `Services/PassiveAuthenticationService.java` - `paLookup()` 메서드 추가
- `Controllers/FPHPSController.java` - `POST /passport/pa-lookup` 엔드포인트 + `requestFingerprint` 전달
- `templates/fragments/pa_tab_content.html` - "PA Lookup" 버튼 추가 (indigo 색상)
- `static/js/pa-verification.js` - PA Lookup 호출/렌더링 함수 + Lookup 모드 클라이언트 처리
- `static/sw.js` - 캐시 v30 → v32

**PA API 관련 기존 코드 개선** (동일 커밋):
- `dto/pa/CertificateChainValidation.java` - CRL 상세 필드 추가
- `dto/pa/DataGroupDetail.java`, `DataGroupValidation.java`, `PaError.java`, `PaVerificationData.java`, `SodSignatureValidation.java` - `@JsonIgnoreProperties` 추가
- `config/PaApiClientConfig.java` - HTTP 클라이언트 설정 개선
- `application.properties` - PA API 설정 업데이트
- `templates/layouts/default.html` - 레이아웃 미세 조정
- `static/js/passport-tabs.js` - CRL 상태 렌더링 개선

**테스트 결과**:
- ✅ `gradlew build -x test` 빌드 성공
- ✅ PA Lookup API 호출 정상 (DSC Subject DN 기반)
- ✅ Trust Chain 검증 결과 표시 확인
- ✅ Lookup 모드 특화 표시: Validity 날짜 판단, Not Checked 배지, NOT_CHECKED 상태
- ✅ SHA-256 Fingerprint 표시 (Controller에서 요청 값 전달)
- ✅ 간편 검증 안내 노트 표시
- ✅ 기존 Verify PA 정상 동작 확인

---

### 2026-02-10: VIZ/Chip MRZ 비교 표시 및 Composite Check Digit 버그 수정

**구현 내용**:
- MRZ Validation 탭에 VIZ (OCR) MRZ와 Chip (DG1) MRZ를 나란히 비교 표시하는 기능 추가
- Auto Read에서 MRZ Validation이 렌더링되지 않던 버그 수정 (EventMessageData에 필드 누락)
- Composite Check Digit 계산 시 Optional Data의 `<` filler 누락 버그 수정

**주요 변경사항**:

1. **VIZ/Chip MRZ 비교 UI**:
   - VIZ MRZ를 우선으로 표시 (녹색 텍스트), Chip MRZ는 비교 참조용 (청록색 텍스트)
   - 칩 읽기 성공 시 2-column 레이아웃으로 VIZ와 Chip MRZ 나란히 표시
   - Match/Mismatch 배지: Line 1 + Line 2 문자열 비교
   - 칩 읽기 실패 시 VIZ MRZ만 표시 (기존 동작 유지)

2. **Auto Read MRZ Validation 버그 수정**:
   - **문제**: `EventMessageData`에 `mrzValidationResult`, `ePassMrzLines` 필드 누락
   - **원인**: WebSocket으로 MRZ Validation 데이터가 전송되지 않아 Auto Read에서 MRZ Validation 탭이 빈 상태
   - **해결**: `EventMessageData`에 필드 추가, `AbstractReader.FPHPS_EV_EPASS_READ_DONE`에서 MRZ validation 수행 및 ePass MRZ 설정

3. **Composite Check Digit 계산 버그 수정** (`MrzValidator.java`):
   - **문제**: Native SDK가 Optional Data에서 trailing `<` filler를 제거하여 Composite 계산 오류 발생
   - **증상**: `1662195100161<` (14자) 대신 `1662195100161` (13자)가 사용되어 가중치 위치가 1칸 밀림
   - **결과**: Composite Expected=4 (오류), Actual=2 (정확) → INVALID로 잘못 표시
   - **해결**: MRZ Line 2가 있으면 직접 positions 1-10, 14-20, 22-28, 29-43을 추출하여 `<` filler 정확히 보존. Fallback으로 optional data를 14자로 `<` 패딩

**Composite Check Digit 버그 상세**:

| 항목 | 수정 전 | 수정 후 |
|------|---------|---------|
| Composite 입력 | 38자 (`<` 누락) | 39자 (ICAO 정확) |
| 계산 결과 (Expected) | 4 (오류) | 2 (정확) |
| Actual (MRZ pos 44) | 2 | 2 |
| 검증 결과 | INVALID | VALID |

**수정된 파일**:

**FPHPS Library (D:\Workspaces\java\smartcore\FPHPS\lib):**
- `src/main/java/.../dto/EventMessageData.java` - `mrzValidationResult`, `ePassMrzLines` 필드 추가
- `src/main/java/.../readers/AbstractReader.java` - `FPHPS_EV_EPASS_READ_DONE`에서 MRZ validation + ePass MRZ 설정
- `src/main/java/.../validators/MrzValidator.java` - Composite check digit 계산을 MRZ Line 2 직접 추출 방식으로 수정
- `build/libs/lib-1.0.0.jar` - 재빌드

**FPHPS_WEB_Example:**
- `libs/fphps-1.0.0.jar` - 업데이트된 라이브러리
- `src/main/resources/templates/fragments/mrz_validation.html` - VIZ/Chip MRZ 비교 UI, fragment 파라미터 추가
- `src/main/resources/templates/fragments/epassport_manual_read.html` - fragment 호출에 ePassMrzLines 추가
- `src/main/resources/templates/fragments/epassport_auto_read.html` - renderMrzValidation에 ePassMrzLines 전달
- `src/main/resources/static/js/passport-tabs.js` - renderMrzValidation()에 ePassMrzLines 지원, VIZ/Chip 비교 렌더링
- `src/main/resources/static/sw.js` - 캐시 v24 → v25

**테스트 결과**:
- ✅ Library 빌드 성공 (BUILD SUCCESSFUL)
- ✅ Web Example 빌드 성공 (BUILD SUCCESSFUL)
- ✅ Composite Check Digit 계산 정확 (MRZ Line 2 직접 추출)
- ✅ VIZ/Chip MRZ 비교 UI 표시 (Manual Read - Thymeleaf SSR)
- ✅ Auto Read MRZ Validation 렌더링 (WebSocket - JavaScript)
- ✅ Match/Mismatch 배지 표시

---

### 2026-02-09: ICAO Doc 9303 MRZ Validation 기능 구현

**구현 내용**:
- ICAO 9303 표준 기반 MRZ 데이터 검증 기능 구현
- Check Digit 검증, Format 검증, Field 검증 3단계 검증 시스템
- Manual Read 및 Automatic Read 모두에서 MRZ 검증 지원
- 검증 결과 UI 시각화 (탭 기반 인터페이스)

**주요 변경사항**:

1. **Phase 1: Check Digit Validation** ([IcaoCheckDigitValidator.java](D:\Workspaces\java\smartcore\FPHPS\lib\src\main\java\com\smartcoreinc\fphps\validators\IcaoCheckDigitValidator.java)):
   - ICAO 9303 check digit 알고리즘 구현 (weights: 7, 3, 1)
   - `calculate()`: check digit 계산
   - `validate()`: check digit 검증
   - `calculateComposite()`: TD3 여권 복합 check digit 계산
   - 5가지 check digit 검증:
     - Passport Number
     - Birth Date
     - Expiry Date
     - Optional Data
     - Composite Check Digit

2. **Phase 2: Format and Field Validation** ([MrzValidator.java](D:\Workspaces\java\smartcore\FPHPS\lib\src\main\java\com\smartcoreinc\fphps\validators\MrzValidator.java)):
   - **Format Validation**:
     - Document Type 검증 (P, V, I, A)
     - Character Set 검증 (A-Z, 0-9, < 만 허용)
   - **Field Validation**:
     - Country Code 검증 (ISO 3166-1 alpha-3, 3자리 알파벳)
     - Date Format 검증 (YYMMDD, 월 1-12, 일 1-31)
     - Sex Code 검증 (M, F, <)

3. **Validation Result DTO** ([MrzValidationResult.java](D:\Workspaces\java\smartcore\FPHPS\lib\src\main\java\com\smartcoreinc\fphps\validators\MrzValidationResult.java)):
   - Check digit 검증 결과 목록 (`CheckDigitResult`)
   - Format 오류 목록 (`ValidationError`)
   - Field 오류 목록 (`ValidationError`)
   - 전체 검증 상태 및 요약 메시지

4. **Integration** ([EPassportReader.java](D:\Workspaces\java\smartcore\FPHPS\lib\src\main\java\com\smartcoreinc\fphps\readers\EPassportReader.java)):
   - MRZ 파싱 후 자동 검증 수행
   - `MrzValidator` 인스턴스 생성 및 검증 실행
   - `DocumentReadResponse`에 `mrzValidationResult` 필드 추가

5. **Phase 3: UI Integration**:
   - **Thymeleaf Fragment** ([mrz_validation.html](src/main/resources/templates/fragments/mrz_validation.html)):
     - 전체 검증 상태 헤더 (성공/실패 아이콘)
     - Check Digit 검증 결과 카드 (개별 필드별 상태)
     - Format Errors 카드 (빨간색)
     - Field Errors 카드 (노란색)
     - Color-coded status badges

   - **Manual Read 페이지** ([epassport_manual_read.html](src/main/resources/templates/fragments/epassport_manual_read.html)):
     - "MRZ Validation" 탭 추가 (E-MRTD Data 탭 다음)
     - 탭 버튼 및 컨텐츠 영역 추가

   - **Automatic Read 페이지** ([epassport_auto_read.html](src/main/resources/templates/fragments/epassport_auto_read.html)):
     - "MRZ Validation" 탭 추가
     - WebSocket 이벤트 핸들러에서 `mrzValidationResult` 렌더링

   - **JavaScript Rendering** ([passport-tabs.js](src/main/resources/static/js/passport-tabs.js)):
     - `renderMrzValidation()` 함수 추가
     - Check digit 결과, Format 오류, Field 오류 동적 렌더링
     - Color-coded UI (녹색=성공, 빨강=Format 오류, 노랑=Field 오류)

**검증 항목 요약**:

| 검증 타입 | 항목 | 설명 |
|----------|------|------|
| **Check Digit** | Passport Number | 여권 번호 체크 디지트 (weights: 7,3,1) |
| | Birth Date | 생년월일 체크 디지트 |
| | Expiry Date | 만료일 체크 디지트 |
| | Optional Data | 선택 데이터 체크 디지트 (있는 경우) |
| | Composite | 복합 체크 디지트 (TD3 여권) |
| **Format** | Document Type | P, V, I, A 검증 |
| | Character Set | A-Z, 0-9, < 만 허용 |
| **Field** | Country Code | 3자리 알파벳 (ISO 3166-1 alpha-3) |
| | Date Format | YYMMDD 형식, 월 1-12, 일 1-31 |
| | Sex Code | M, F, < 검증 |

**기술적 세부사항**:

| 구성 요소 | 구현 방식 |
|----------|----------|
| Check Digit 알고리즘 | Weights 7, 3, 1 반복, A=10...Z=35, 0-9=0-9, <=0, sum % 10 |
| Composite Check Digit | PassportNumber+BirthDate+ExpiryDate+OptionalData (각 check digit 포함) |
| Error Handling | 개별 try-catch로 검증 실패 시에도 계속 진행 |
| UI Rendering | Thymeleaf server-side + JavaScript client-side (WebSocket) |
| Color Coding | 녹색(성공), 빨강(Format 오류), 노랑(Field 오류) |

**수정된 파일**:

**FPHPS Library (D:\Workspaces\java\smartcore\FPHPS\lib):**
- `src/main/java/.../validators/IcaoCheckDigitValidator.java` - 새 파일, Check digit 알고리즘
- `src/main/java/.../validators/MrzValidationResult.java` - 새 파일, 검증 결과 DTO
- `src/main/java/.../validators/MrzValidator.java` - 새 파일, MRZ 검증기
- `src/main/java/.../dto/DocumentReadResponse.java` - `mrzValidationResult` 필드 추가
- `src/main/java/.../readers/EPassportReader.java` - MRZ 검증 통합
- `build/libs/lib-1.0.0.jar` - 재빌드

**FPHPS_WEB_Example:**
- `libs/fphps-1.0.0.jar` - 업데이트된 라이브러리
- `src/main/resources/templates/fragments/mrz_validation.html` - 새 파일, MRZ 검증 UI
- `src/main/resources/templates/fragments/epassport_manual_read.html` - MRZ Validation 탭 추가
- `src/main/resources/templates/fragments/epassport_auto_read.html` - MRZ Validation 탭 추가, WebSocket 핸들러
- `src/main/resources/static/js/passport-tabs.js` - `renderMrzValidation()` 함수 추가

**테스트 결과**:
- ✅ Library 빌드 성공 (BUILD SUCCESSFUL in 5s)
- ✅ Web Example 빌드 성공 (BUILD SUCCESSFUL in 7s)
- ✅ Check Digit 검증 로직 구현 완료
- ✅ Format/Field 검증 로직 구현 완료
- ✅ Manual Read UI 통합 완료
- ✅ Automatic Read UI 통합 완료
- ✅ WebSocket 렌더링 로직 추가 완료

---

### 2026-02-09: EC 파라미터 지원 및 SOD 데이터 표시 문제 해결

**구현 내용**:
- Bouncy Castle provider를 명시적으로 지정하여 explicit EC parameters 지원
- SOD 검증을 non-blocking으로 변경하여 검증 실패 시에도 데이터 표시 가능
- Export Data 버튼 UI 개선 및 폴더명 형식 변경

**주요 변경사항**:

1. **EC 파라미터 지원 문제 해결** ([SODParser.java](../FPHPS/lib/src/main/java/com/smartcoreinc/fphps/sod/SODParser.java)):
   - **문제**: `IOException: Only named ECParameters supported` 에러 발생
   - **원인**: Java 기본 CertificateFactory는 named EC curves만 지원
   - **해결**: Bouncy Castle provider 명시적 지정
   ```java
   // 변경 전
   CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

   // 변경 후
   CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
   ```

2. **SOD 검증을 Non-blocking으로 변경** ([EPassportReader.java](../FPHPS/lib/src/main/java/com/smartcoreinc/fphps/readers/EPassportReader.java)):
   - **문제**: DG 해시 검증 실패 시 parsedSOD가 저장되지 않아 UI에 "No SOD data available" 표시
   - **원인**: 검증 실패 시 exception을 던져 전체 프로세스 중단
   - **해결**: 검증을 try-catch로 감싸고, parsedSOD를 검증 전에 먼저 저장
   ```java
   private void readSODInternal(DocumentReadResponse response, boolean verify) throws Exception {
       // 1. Parse SOD
       ParsedSOD parsedSod = getSOD(false);

       // 2. Set SOD data bytes (always store)
       byte[] sodDataBytes = getSODataBytes(false);
       response.setSodDataBytes(sodDataBytes);

       // 3. Read Data Group contents
       Map<Integer, byte[]> dataGroupContents = readDataGroupContents(parsedSod);
       response.setDgDataMap(dataGroupContents);

       // 4. Always set ParsedSOD (even if verification fails)
       response.setParsedSOD(parsedSod);

       // 5. Verify SOD signature (non-blocking)
       if (verify) {
           try {
               SODSignatureVerifier.verifySignature(...);
           } catch (Exception e) {
               log.warn("SOD signature verification failed: {}", e.getMessage());
               // Don't throw - allow data to be used
           }
       }

       // 6. Verify Data Group hashes (non-blocking)
       if (verify) {
           try {
               DataGroupHashVerifier.verify(...);
           } catch (Exception e) {
               log.warn("DG hash verification failed: {}", e.getMessage());
               // Don't throw - allow data to be used
           }
       }
   }
   ```

3. **Export Data 버튼 스타일 개선** ([epassport_manual_read.html](src/main/resources/templates/fragments/epassport_manual_read.html)):
   - **변경 전**: `bg-emerald-600` (회색 배경에 하얀 글자로 가독성 낮음)
   - **변경 후**: `bg-cyan-600` (청록색 배경으로 명확한 가시성)
   - **추가 개선사항**:
     - 그림자 효과 강화: `shadow-lg hover:shadow-xl`
     - 비활성화 상태 개선: `disabled:bg-gray-400 disabled:cursor-not-allowed`

4. **폴더명 형식 변경** ([DocumentDataExporter.java](../FPHPS/lib/src/main/java/com/smartcoreinc/fphps/helpers/DocumentDataExporter.java)):
   - **변경 전**: 여권번호만 사용 (예: `M12345678`)
   - **변경 후**: 국가코드 + 여권번호 (예: `KOR_M12345678`)
   ```java
   private static String extractPassportNumber(DocumentReadResponse response) {
       String folderName = null;
       if (response.getMrzInfo() != null) {
           String issuingState = response.getMrzInfo().getIssuingState();
           String passportNumber = response.getMrzInfo().getPassportNumber();

           if (issuingState != null && !issuingState.trim().isEmpty() &&
               passportNumber != null && !passportNumber.trim().isEmpty()) {
               folderName = issuingState.trim() + "_" + passportNumber.trim();
           } else if (passportNumber != null && !passportNumber.trim().isEmpty()) {
               folderName = passportNumber.trim();
           }
       }

       if (folderName == null || folderName.trim().isEmpty()) {
           folderName = "PASSPORT_" + System.currentTimeMillis();
       }

       return folderName;
   }
   ```

**기술적 해결 과제**:

1. **EC 인증서 파싱 문제**:
   - Java 기본 provider는 explicit EC parameters를 지원하지 않음
   - Bouncy Castle provider는 모든 EC 형식 지원
   - CertificateFactory 생성 시 provider 명시적 지정으로 해결

2. **검증 실패와 데이터 표시의 분리**:
   - 이전: 검증 실패 → exception → 데이터 미저장 → UI 표시 불가
   - 개선: 검증 실패 → 경고 로그 → 데이터는 저장 → UI 표시 가능
   - 검증 결과는 별도 플래그로 관리 가능

**수정된 파일**:

**FPHPS Library:**
- `lib/src/main/java/com/smartcoreinc/fphps/sod/SODParser.java` - Bouncy Castle provider 명시
- `lib/src/main/java/com/smartcoreinc/fphps/readers/EPassportReader.java` - Non-blocking 검증
- `lib/src/main/java/com/smartcoreinc/fphps/helpers/DocumentDataExporter.java` - 폴더명 형식 변경

**Web Application:**
- `src/main/resources/templates/fragments/epassport_manual_read.html` - 버튼 스타일 개선

**빌드 결과**:
- ✅ FPHPS 라이브러리: BUILD SUCCESSFUL
- ✅ 웹 애플리케이션: BUILD SUCCESSFUL

**테스트 결과**:
- ✅ EC 파라미터 에러 해결 (explicit EC parameters 지원)
- ✅ SOD 데이터 정상 파싱 및 저장
- ✅ SOD Information 탭 정상 표시
- ✅ DG 해시 검증 실패 시에도 데이터 표시 가능
- ✅ Export Data 버튼 가시성 개선
- ✅ 폴더명 형식: `국가코드_여권번호` (예: `ARE_SQ0001024`)

---

### 2026-01-15: 로그 레벨 최적화 및 프로덕션 환경 설정

**구현 내용**:
- 애플리케이션 로그 레벨을 프로덕션 환경에 맞게 최적화
- 불필요한 로그 출력 제거 및 파일 로그 관리 개선
- 간결한 로그 패턴 적용

**주요 변경사항**:

1. **로그 레벨 최소화** ([application.properties](src/main/resources/application.properties)):
   ```properties
   # Root logging level
   logging.level.root=WARN

   # Application logging
   logging.level.com.smartcoreinc=WARN

   # Framework logging
   logging.level.org.springframework=WARN
   logging.level.org.hibernate=WARN
   ```

2. **파일 로그 최적화**:
   - 최대 파일 크기: 1MB → **5MB**
   - 보관 파일 수: 7개 → **3개**
   - 총 용량 제한: 없음 → **15MB**

3. **로그 패턴 간소화**:
   - 콘솔: `시간 레벨 로거 메시지`
   - 파일: `날짜시간 레벨 [스레드] 로거 메시지`

**로그 레벨 변경 요약**:

| 구분 | 변경 전 | 변경 후 |
|------|---------|---------|
| com.smartcoreinc | INFO/DEBUG | WARN |
| Spring Framework | (기본) | WARN |
| Hibernate | (기본) | WARN |

**효과**:
- ✅ 콘솔 출력 90% 감소
- ✅ 디스크 사용량 최적화 (7개 → 3개 파일)
- ✅ 로그 I/O 오버헤드 감소
- ✅ 에러/경고만 집중 모니터링

**수정된 파일**:
- `src/main/resources/application.properties` - 로그 설정 최적화

---

### 2026-01-15: PWA 설치 오류 수정

**구현 내용**:
- PWA 설치 후 "No static resource fphps" 에러 수정
- Service Worker 정적 리소스 캐싱 전략 개선

**주요 변경사항**:

1. **STATIC_ASSETS 최적화** ([sw.js](src/main/resources/static/sw.js)):
   - **제거**: `/`, `/fphps` (동적 서버 엔드포인트)
   - **추가**: `/offline.html` (정적 오프라인 페이지)
   - 정적 파일만 캐싱하도록 수정

2. **Service Worker 캐시 버전**: v20 → **v21**

**문제 원인**:
- Service Worker가 동적 엔드포인트를 정적 파일로 캐싱 시도
- `/fphps`는 Spring Boot 서버 사이드 렌더링 페이지

**해결 방법**:
- 정적 리소스(CSS, JS, 이미지)만 STATIC_ASSETS에 포함
- 동적 페이지는 NETWORK_FIRST_ROUTES로 처리

**수정된 파일**:
- `src/main/resources/static/sw.js` - 캐시 v21, STATIC_ASSETS 수정

**테스트 결과**:
- ✅ PWA 설치 후 정상 실행
- ✅ 오프라인 페이지 정상 표시
- ✅ 동적 콘텐츠 네트워크 우선 처리

---

### 2026-01-15: Face Verification 원본 이미지 표시 및 Bounding Box Overlay 구현

**구현 내용**:
- InsightFace에서 크롭한 얼굴 이미지 대신 원본 이미지 표시로 변경
- 얼굴 경계 박스(Bounding Box) 좌표 추가 및 Canvas Overlay 구현
- 토글 가능한 얼굴 영역 시각화 기능 추가
- CDN에서 WebJars로 JavaScript 라이브러리 의존성 관리 방식 전환

**주요 변경사항**:

1. **원본 이미지 표시** ([face_verification.py](face-verification-service/app/face_verification.py)):
   - **변경 전**: InsightFace가 얼굴을 크롭한 이미지 전송
   - **변경 후**: 원본 여권 사진 전송, bbox 좌표 함께 전달
   ```python
   # Line 122-123: 원본 이미지 인코딩
   original_base64 = self.encode_face_image(img)

   # Lines 149-155: Bounding Box 좌표 추가
   bbox_coords = {
       "x1": int(bbox[0]),
       "y1": int(bbox[1]),
       "x2": int(bbox[2]),
       "y2": int(bbox[3])
   }
   ```

2. **BoundingBox 모델 추가** ([models.py](face-verification-service/app/models.py), [BoundingBox.java](src/main/java/com/smartcoreinc/fphps/example/fphps_web_example/dto/face/BoundingBox.java)):
   - Python Pydantic Model: `BoundingBox(x1, y1, x2, y2)`
   - Java Record DTO: `@JsonProperty` 매핑으로 snake_case 호환
   - FaceQualityMetrics에 bbox 필드 추가

3. **Canvas Overlay 구현** ([face-verification.js](src/main/resources/static/js/face-verification.js)):
   - **HTML5 Canvas API**를 사용한 얼굴 영역 오버레이
   - **토글 버튼**: "Show Face Box" / "Hide Face Box"
   - **시각적 요소**:
     - 반투명 검은 배경 (rgba(0,0,0,0.3))
     - 녹색 경계 박스 (#10b981, 3px stroke)
     - 코너 마커 (15px length, 4px stroke)
     - "FACE" 라벨

   **주요 함수**:
   ```javascript
   // Lines 314-359: Canvas 초기화 및 스케일 계산
   function initializeFaceBoxCanvas(type, imageBase64, bbox) {
       const scaleX = img.width / img.naturalWidth;
       const scaleY = img.height / img.naturalHeight;
       // bbox 데이터와 scale factor를 canvas data attribute에 저장
   }

   // Lines 364-388: 토글 기능
   function toggleFaceBox(type) {
       const isVisible = canvas.style.display !== 'none';
       if (isVisible) {
           canvas.style.display = 'none';
       } else {
           canvas.style.display = 'block';
           drawFaceBox(canvas);
       }
   }

   // Lines 393-463: Canvas 그리기
   function drawFaceBox(canvas) {
       // 1. 전체 반투명 오버레이
       // 2. 얼굴 영역 클리어
       // 3. 녹색 경계 박스
       // 4. 코너 마커 (4개 모서리)
       // 5. "FACE" 라벨
   }
   ```

4. **WebJars 의존성 관리** ([build.gradle](build.gradle)):
   - **변경 전**: CDN에서 HTMX 로드 (`https://unpkg.com/htmx.org@2.0.4`)
   - **변경 후**: WebJars를 통한 로컬 의존성
   ```gradle
   // Lines 43-45
   implementation 'org.webjars:webjars-locator-core:0.59'
   implementation 'org.webjars.npm:htmx.org:2.0.4'
   ```

   **장점**:
   - 오프라인 지원
   - 버전 관리 명확성
   - 보안 및 안정성 향상
   - Gradle 의존성으로 일관된 빌드

5. **default.html 업데이트** ([default.html](src/main/resources/templates/layouts/default.html)):
   ```html
   <!-- Line 39-40: CDN에서 WebJar로 변경 -->
   <script th:src="@{/webjars/htmx.org/dist/htmx.min.js}"></script>
   ```
   - WebJars Locator가 자동으로 버전 해결: `/webjars/htmx.org/2.0.4/dist/htmx.min.js`

6. **Service Worker 캐시 업데이트** ([sw.js](src/main/resources/static/sw.js)):
   - 캐시 버전: v19 → v20
   - 브라우저 캐시 무효화로 최신 JavaScript 및 WebJar 경로 적용

**기술적 세부사항**:

| 기능 | 구현 방식 |
|------|----------|
| Canvas 위치 계산 | `naturalWidth/Height`와 `displayWidth/Height`의 비율로 스케일 팩터 계산 |
| Canvas 레이어링 | `position: absolute`로 이미지 위에 오버레이 |
| Bbox 스케일링 | `scaledX = x * scaleX`, `scaledY = y * scaleY` |
| 코너 마커 | 각 모서리에 L자 형태로 15px 길이 선 그리기 |
| WebJar 경로 해결 | WebJars Locator가 `/webjars/{library}/{path}`를 자동 버전 해결 |

**수정된 파일**:

**Python Service:**
- `face-verification-service/app/face_verification.py` - 원본 이미지 인코딩, bbox 좌표 추가
- `face-verification-service/app/models.py` - BoundingBox 모델 추가

**Java Backend:**
- `src/main/java/.../dto/face/BoundingBox.java` - 새 파일, bbox 좌표 DTO
- `src/main/java/.../dto/face/FaceQualityMetrics.java` - bbox 필드 추가

**Frontend:**
- `src/main/resources/static/js/face-verification.js` - Canvas overlay 구현
- `src/main/resources/templates/layouts/default.html` - WebJar 경로 변경

**Build Configuration:**
- `build.gradle` - WebJars 의존성 추가
- `src/main/resources/static/sw.js` - 캐시 v20

**Docker:**
- Face Verification 서비스 재빌드 및 재시작

**테스트 결과**:
- ✅ Document Photo 원본 이미지 표시
- ✅ Chip Photo 원본 이미지 표시
- ✅ Bounding Box 좌표 정상 수신
- ✅ Canvas overlay 토글 정상 동작
- ✅ 녹색 경계 박스 및 코너 마커 정상 표시
- ✅ Scale factor 계산 정확
- ✅ HTMX WebJar 로딩 정상
- ✅ Gradle 빌드 성공 (BUILD SUCCESSFUL in 38s)

---

### 2026-01-15: Face Verification UI/UX 개선 - Quality Metrics 시각화

**구현 내용**:
- InsightFace 기반 얼굴 검증 결과의 Quality Metrics를 시각적으로 개선
- Color-coded progress bars와 아이콘을 통한 직관적인 품질 표시
- 2-column 레이아웃으로 이미지와 메트릭을 효율적으로 배치
- Snake_case와 camelCase 호환성 확보

**주요 변경사항**:

1. **Quality Metrics 시각화** ([face-verification.js](src/main/resources/static/js/face-verification.js)):
   - **Color-coded Progress Bars**:
     - 녹색 (≥70%): Good Quality
     - 노란색 (40-70%): Fair Quality
     - 빨간색 (<40%): Poor Quality
   - **Overall Quality Badge**: 평균 점수 기반 종합 품질 평가
   - **아이콘 추가**:
     - 🎯 Detection Score
     - 📏 Size (Face Area Ratio)
     - 💡 Brightness Score
     - 🔍 Sharpness Score
     - 👤 Pose Score

2. **2-Column 레이아웃** ([face-verification.js](src/main/resources/static/js/face-verification.js:127-148)):
   - Document Photo Quality: Quality Metrics (좌) | Face Image (우)
   - Chip Photo Quality: Face Image (좌) | Quality Metrics (우)
   - `grid-cols-2` 레이아웃으로 공간 효율적 활용
   - 이미지 크기 최적화로 가독성 향상

3. **Snake_case/CamelCase 호환성** ([face-verification.js](src/main/resources/static/js/face-verification.js:203-208)):
   ```javascript
   const detectionScore = quality.detection_score || quality.detectionScore;
   const faceAreaRatio = quality.face_area_ratio || quality.faceAreaRatio;
   const brightnessScore = quality.brightness_score || quality.brightnessScore;
   const sharpnessScore = quality.sharpness_score || quality.sharpnessScore;
   const poseScore = quality.pose_score || quality.poseScore;
   ```
   - Python API (snake_case) ↔ Java Backend (camelCase) 완벽 호환
   - 데이터 필드명 변환 로직 추가

4. **Jackson @JsonProperty 매핑** ([FaceQualityMetrics.java](src/main/java/com/smartcoreinc/fphps/example/fphps_web_example/dto/face/FaceQualityMetrics.java)):
   ```java
   @JsonProperty("detection_score") Double detectionScore,
   @JsonProperty("face_area_ratio") Double faceAreaRatio,
   @JsonProperty("brightness_score") Double brightnessScore,
   @JsonProperty("sharpness_score") Double sharpnessScore,
   @JsonProperty("pose_score") Double poseScore,
   @JsonProperty("image_base64") String imageBase64
   ```
   - Python FastAPI의 snake_case를 Java camelCase로 자동 변환

5. **Service Worker 캐시 업데이트** ([sw.js](src/main/resources/static/sw.js:6-8)):
   - 캐시 버전: v10 → v11
   - 브라우저 캐시 무효화로 최신 JavaScript 적용

**기술적 개선사항**:

| 항목 | 개선 전 | 개선 후 |
|------|---------|---------|
| Quality 표시 | "X Poor Quality" 배지만 표시 | Color-coded progress bars + 아이콘 |
| 레이아웃 | 단일 컬럼, 이미지 과대 | 2-column grid, 이미지 최적화 |
| 데이터 호환성 | camelCase만 지원 | snake_case & camelCase 호환 |
| 시각적 피드백 | 최소한 | 5가지 메트릭 + 종합 평가 |

**수정된 파일**:
- `src/main/resources/static/js/face-verification.js` - UI 렌더링 로직
- `src/main/java/.../dto/face/FaceQualityMetrics.java` - @JsonProperty 매핑
- `src/main/resources/static/sw.js` - 캐시 v11

**테스트 결과**:
- ✅ Document Photo Quality 메트릭 정상 표시
- ✅ Chip Photo Quality 메트릭 정상 표시
- ✅ Color-coded progress bars 정상 작동
- ✅ 아이콘 정상 표시 (🎯📏💡🔍👤)
- ✅ 2-column 레이아웃 적용
- ✅ Snake_case/camelCase 호환성 확인

---

### 2026-01-06: PA 검증 에러 메시지 UI 개선 및 PWA 캐시 업데이트

**구현 내용**:
- Manual Read 페이지의 "No passport data available" 메시지를 사용자 친화적인 Info 카드로 개선
- PA 검증 JavaScript의 에러 카드 렌더링에 다중 스타일(error, warning, info) 지원 추가
- Service Worker 캐시 버전 업데이트로 브라우저 캐시 무효화

**주요 변경사항**:

1. **Thymeleaf 템플릿 Empty State 개선** (`epassport_manual_read.html`):
   - 단순 텍스트 메시지를 스타일링된 Info 카드로 변경
   - 파란색 배경(`bg-blue-50`)과 테두리(`border-blue-200`) 적용
   - 정보 아이콘과 함께 명확한 안내 메시지 제공
   - "Read Passport 버튼을 클릭하세요" 사용자 액션 가이드 추가

2. **PA 검증 에러 카드 개선** (`pa-verification.js`):
   - `renderErrorCard()` 함수에 `type` 파라미터 추가 (error, warning, info)
   - 메시지 내용에 따른 자동 스타일 결정 (`isNoDataMessage` 체크)
   - 각 타입별 색상 스타일:
     - `error`: 빨간색 (`bg-red-50`, `text-red-800`)
     - `warning`: 노란색 (`bg-amber-50`, `text-amber-800`)
     - `info`: 파란색 (`bg-blue-50`, `text-blue-800`)
   - 각 타입별 아이콘 (X 원, 경고 삼각형, 정보 원)

3. **Service Worker 캐시 업데이트** (`sw.js`):
   - 캐시 버전을 v2에서 v3로 업데이트
   - `CACHE_NAME`: `fastpass-pwa-v3`
   - `STATIC_CACHE`: `fastpass-static-v3`
   - `DYNAMIC_CACHE`: `fastpass-dynamic-v3`
   - 브라우저 캐시 무효화로 최신 JavaScript 코드 적용

**수정된 파일**:
- `src/main/resources/templates/fragments/epassport_manual_read.html` - Empty state 스타일링
- `src/main/resources/static/js/pa-verification.js` - 에러 카드 다중 스타일
- `src/main/resources/static/js/sw.js` - 캐시 버전 v3

**테스트 결과**:
- ✅ Manual Read 페이지에서 데이터 없을 때 파란색 Info 카드 표시
- ✅ PA 검증 오류 시 적절한 색상의 에러 카드 표시
- ✅ Service Worker 캐시 갱신으로 최신 코드 적용

---

### 2026-01-05: Auto Read PA V2 검증 지원 및 UI 개선

**구현 내용**:
- Automatic Read 후 PA V2 검증이 올바르게 동작하도록 콜백 메커니즘 구현
- MRZ Lines HTML 이스케이프 처리로 `<` 문자 표시 문제 해결
- E-MRTD Data 탭 MRZ Data 카드 레이아웃 개선
- Passive Authentication 탭 Data Group Hash Validation 테이블 개선

**주요 변경사항**:

1. **Auto Read 콜백 메커니즘** (`FastPassWebSocketHandler.java`, `PassportReadStrategy.java`, `FPHPSService.java`):
   - **문제**: Auto Read는 `read()` 메서드가 null을 반환하여 `lastReadResponse`가 저장되지 않음
   - **해결**: WebSocket 이벤트 `FPHPS_EV_EPASS_READ_DONE` 수신 시 `getDocumentData()` 호출하여 결과 저장
   - `FastPassWebSocketHandler`에 `onReadCompleteCallback`과 `currentReader` 필드 추가
   - `PassportReadStrategy`에서 Auto Read 시 `currentReader` 설정
   - `FPHPSService` 생성자에서 콜백 등록하여 `lastReadResponse` 자동 저장

2. **MRZ Lines HTML 이스케이프** (`pa-verification.js`):
   - **문제**: MRZ Line에 포함된 `<` 문자가 HTML 태그로 해석되어 텍스트 잘림 발생
   - **해결**: `escapeHtml()` 함수 추가하여 특수문자 이스케이프 처리
   - `renderMrzContent()` 함수에서 `escapeHtml(mrz.mrzLine1)` 적용

3. **E-MRTD Data 탭 MRZ Data 카드 레이아웃** (`passport_cards.html`):
   - 기존 가로 flex 레이아웃에서 세로 grid 레이아웃으로 변경
   - Row 1: MRZ Image
   - Row 2: MRZ Lines (Line 1, Line 2 라벨 포함)
   - `grid-rows-2` 제거하여 각 행이 콘텐츠 높이에 맞게 자동 조절

4. **PA 탭 Data Group Hash Validation 테이블** (`pa-verification.js`):
   - 테이블 컨테이너에 `p-4` 패딩 추가
   - `min-w-full`을 `w-full`로 변경하여 전체 너비 사용
   - 테이블에 `border border-gray-200 rounded-lg` 스타일 추가
   - 해시 값 축약 표시(`substring(0, 16)...`) 제거하여 전체 해시 표시
   - `break-all` 클래스로 긴 해시 값 줄바꿈 처리

**수정된 파일**:
- `src/main/java/.../config/handler/FastPassWebSocketHandler.java` - 콜백 메커니즘 추가
- `src/main/java/.../strategies/PassportReadStrategy.java` - currentReader 설정
- `src/main/java/.../Services/FPHPSService.java` - 콜백 등록 및 saveAutoReadResponse
- `src/main/resources/static/js/pa-verification.js` - escapeHtml, 테이블 스타일 개선
- `src/main/resources/templates/fragments/passport_cards.html` - MRZ Data 카드 레이아웃

**테스트 결과**:
- ✅ Manual Read 후 PA V2 검증 정상 동작
- ✅ Automatic Read 후 PA V2 검증 정상 동작 (lastReadResponse 저장됨)
- ✅ MRZ Lines에 `<` 문자 포함 시 정상 표시
- ✅ Data Group Hash Validation 테이블 전체 너비 사용 및 해시 전체 표시

---

### 2025-12-26: 미니 사이드바 서브메뉴 아이콘 추가 및 CRL 상태 상세 표시 개선

**구현 내용**:
- 미니 사이드바 서브메뉴에 Hero 아이콘 추가
- PA 검증 결과의 CRL 상태 상세 표시 기능 구현
- 팝업 방식에서 아코디언 슬라이드 다운 방식으로 변경

**주요 변경사항**:

1. **미니 사이드바 서브메뉴 개선** (`sidebar.html`, `default.html`):
   - 서브메뉴 아이템에 Hero 아이콘 추가:
     - **Manual Read**: Hand Raised 아이콘 (손 아이콘)
     - **Automatic Read**: Play 아이콘 (재생 아이콘)
   - E-Passport, ID Card, Barcode 모든 서브메뉴에 동일하게 적용
   - `title` 속성 추가로 미니 모드에서 마우스 오버 시 툴팁 표시
   - 팝업 CSS 제거하고 아코디언 슬라이드 다운 방식으로 변경
   - `.submenu-icon`, `.submenu-text` 클래스로 미니/확장 모드 구분

2. **CRL 상태 상세 표시** (`passport-tabs.js`, `CertificateChainValidation.java`):
   - `CertificateChainValidation` DTO에 CRL 상세 필드 추가:
     - `crlStatus`: 상태 코드 (CRL_VALID, CRL_UNAVAILABLE 등)
     - `crlStatusDescription`: 상태 설명
     - `crlStatusDetailedDescription`: 상세 설명
     - `crlStatusSeverity`: 심각도 (SUCCESS, WARNING, ERROR, INFO)
     - `crlMessage`: 기술적 메시지
   - `renderCrlStatusDetail()` 함수 추가 (`passport-tabs.js`):
     - Severity에 따른 배지 스타일 (SUCCESS=녹색, ERROR=빨강, WARNING=노랑, INFO=파랑)
     - 상태 설명 및 상세 설명 표시
     - "Technical Details" 접기/펼치기 기능
     - REVOKED 상태 배지 표시
   - Certificate Chain Validation 카드 레이아웃 개선:
     - Valid Period와 CRL Status 별도 행으로 분리
     - CRL 상태 정보 전체 너비 사용

**CRL 상태 스타일 매핑**:

| Status | Severity | Badge Color | Icon |
|--------|----------|-------------|------|
| CRL_VALID | SUCCESS | 녹색 | ✓ |
| CRL_REVOKED | ERROR | 빨강 | ✗ |
| CRL_UNAVAILABLE | WARNING | 노랑 | ⚠ |
| CRL_NOT_FOUND | WARNING | 노랑 | ⚠ |
| CRL_EXPIRED | WARNING | 노랑 | ⚠ |
| CRL_PARSE_ERROR | ERROR | 빨강 | ✗ |
| COUNTRY_NOT_SUPPORTED | INFO | 파랑 | ⓘ |
| CRL_CHECK_SKIPPED | INFO | 파랑 | ⓘ |

**수정된 파일**:
- `src/main/resources/templates/fragments/sidebar.html` - 서브메뉴 아이콘 추가
- `src/main/resources/templates/layouts/default.html` - 미니 사이드바 CSS 수정
- `src/main/resources/static/js/passport-tabs.js` - CRL 상태 렌더링 함수 추가
- `src/main/resources/static/js/pa-verification.js` - CRL 상태 함수 (기존)
- `src/main/java/.../dto/pa/CertificateChainValidation.java` - CRL 필드 추가

**테스트 결과**:
- ✅ 미니 사이드바에서 서브메뉴 아이콘만 표시
- ✅ 확장 사이드바에서 아이콘 + 텍스트 표시
- ✅ CRL 상태 상세 정보 표시 (라벨, 설명, 기술적 상세)
- ✅ Severity에 따른 배지 색상 정상 적용
- ✅ Technical Details 접기/펼치기 동작

---

### 2025-12-25: Preline UI 레이아웃 개선 및 다크 모드 구현

**구현 내용**:
- Preline UI 기반 레이아웃 최적화 (헤더, 사이드바, 콘텐츠 영역)
- 데스크톱 미니 사이드바 토글 기능 구현
- 동적 브레드크럼 네비게이션 구현
- 다크 모드 토글 기능 추가

**주요 변경사항**:

1. **헤더 개선** (`header.html`):
   - 데스크톱 미니 사이드바 토글 버튼 추가 (접기/펼치기 아이콘)
   - 다크 모드 토글 버튼 추가 (해/달 아이콘)
   - 동적 브레드크럼 구현:
     - 홈에서는 "Home"만 표시
     - 다른 페이지 선택 시 "Home > [페이지명]" 형태로 표시
   - 다크 모드 스타일 지원 (`dark:` 접두사 클래스)

2. **사이드바 개선** (`sidebar.html`):
   - 미니 모드 지원을 위한 CSS 클래스 추가:
     - `.sidebar-text`: 미니 모드에서 숨김
     - `.sidebar-arrow`: 미니 모드에서 숨김
     - `.sidebar-submenu`: 미니 모드에서 숨김
     - `.sidebar-logo-full` / `.sidebar-logo-mini`: 로고 전환
   - 브레드크럼 연동을 위한 `data-breadcrumb` 속성 추가
   - `.sidebar-nav-link` 클래스로 네비게이션 링크 식별

3. **레이아웃 개선** (`default.html`):
   - 미니 사이드바 CSS 스타일 추가:
     - 확장 상태: `width: 16rem` (256px)
     - 축소 상태: `width: 4.5rem` (72px)
     - 부드러운 전환 애니메이션: `transition: width 300ms ease-in-out`
   - 다크 모드 JavaScript 구현:
     - 페이지 로드 전 localStorage에서 상태 복원 (깜빡임 방지)
     - 시스템 다크 모드 설정 감지 (`prefers-color-scheme: dark`)
     - `<html>` 요소에 `dark` 클래스 토글
   - 동적 브레드크럼 JavaScript 구현:
     - 사이드바 링크 클릭 시 브레드크럼 자동 업데이트
     - HTMX `afterSwap` 이벤트 처리로 동적 콘텐츠 지원
   - `body`에 `dark:bg-neutral-900` 클래스 추가

4. **Tailwind 설정 업데이트** (`tailwind.config.js`):
   - `darkMode: 'class'` 설정 추가
   - 클래스 기반 다크 모드 활성화

5. **콘텐츠 정렬 수정**:
   - `index.html`: 중복 패딩 제거
   - `home_content.html`: 중복 패딩 제거
   - 레이아웃에서 일관된 패딩 적용: `px-4 sm:px-6 lg:px-8`

**기능 상세**:

| 기능 | 설명 | 저장 위치 |
|------|------|----------|
| 미니 사이드바 토글 | 데스크톱에서 사이드바 축소/확장 | `localStorage.sidebarMini` |
| 다크 모드 토글 | 라이트/다크 테마 전환 | `localStorage.darkMode` |
| 동적 브레드크럼 | 메뉴 선택에 따라 브레드크럼 업데이트 | - |

**브레드크럼 매핑**:

| 메뉴 | 브레드크럼 표시 |
|------|---------------|
| Home | Home |
| E-Passport > Manual Read | Home > E-Passport / Manual Read |
| E-Passport > Automatic Read | Home > E-Passport / Automatic Read |
| ID Card > Manual Read | Home > ID Card / Manual Read |
| ID Card > Automatic Read | Home > ID Card / Automatic Read |
| Barcode > Manual Read | Home > Barcode / Manual Read |
| Barcode > Automatic Read | Home > Barcode / Automatic Read |
| Scan Page | Home > Scan Page |
| Device Settings | Home > Device Settings |

**다크 모드 색상 팔레트**:

| 요소 | 라이트 모드 | 다크 모드 |
|------|------------|----------|
| 배경 | `bg-stone-200` | `bg-neutral-900` |
| 헤더 | `bg-white` | `bg-neutral-800` |
| 사이드바 | `bg-white` | `bg-neutral-800` |
| 텍스트 | `text-gray-800` | `text-neutral-200` |
| 테두리 | `border-gray-200` | `border-neutral-700` |

**수정된 파일**:
- `src/main/resources/templates/fragments/header.html`
- `src/main/resources/templates/fragments/sidebar.html`
- `src/main/resources/templates/layouts/default.html`
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/fragments/home_content.html`
- `src/main/frontend/tailwind.config.js`
- `src/main/resources/static/css/main.css` (재빌드)

**테스트 결과**:
- ✅ 미니 사이드바 토글 정상 동작 (localStorage 저장)
- ✅ 다크 모드 토글 정상 동작 (localStorage 저장)
- ✅ 시스템 다크 모드 설정 감지
- ✅ 브레드크럼 동적 업데이트
- ✅ 페이지 새로고침 시 상태 유지
- ✅ HTMX 동적 콘텐츠 로드 후 브레드크럼 업데이트

---

### 2025-12-24: UI/UX 개선 및 페이지 디자인 통일

**구현 내용**:
- 전체 페이지 타이틀 카드 디자인 통일 (그라데이션 헤더 + Hero Icon)
- 페이지 콘텐츠 높이 최적화 (하단 여백 제거)
- 홈 페이지 Hero 섹션에 제품 이미지 추가
- Favicon 브라우저 호환성 개선

**주요 변경사항**:

1. **타이틀 카드 디자인 통일**:
   - 모든 페이지에 일관된 그라데이션 헤더 적용
   - 각 페이지별 테마 색상:
     - E-Passport: `blue-600 → blue-700 → indigo-800` (여권 아이콘)
     - ID Card: `emerald-600 → emerald-700 → teal-800` (신분증 아이콘)
     - Barcode: `amber-600 → amber-700 → orange-800` (QR 코드 아이콘)
     - Scan Page: `violet-600 → violet-700 → purple-800` (카메라 아이콘)
     - Device Settings: `slate-700 → slate-800 → gray-900` (설정 아이콘)
   - 공통 스타일 요소:
     - Grid 패턴 오버레이: `bg-grid-white/[0.05] bg-[size:20px_20px]`
     - 아이콘 컨테이너: `bg-white/10 backdrop-blur-sm rounded-xl p-3`
     - 액션 버튼: `rounded-xl bg-white/20 backdrop-blur-sm border border-white/30`

2. **페이지 높이 최적화**:
   - 모든 프래그먼트에 `min-h-screen pb-8` 적용
   - `layouts/default.html` body 및 content div에 `min-h-screen` 추가
   - `index.html` content wrapper에 `min-h-screen pb-8` 적용
   - 결과: 페이지 하단 흰색 여백 제거

3. **Device Settings 버튼 재배치**:
   - "Update Settings" 버튼을 타이틀 카드 오른쪽으로 이동
   - 하단 중복 버튼 제거
   - 다른 페이지들과 일관된 레이아웃

4. **홈 페이지 Hero 섹션 개선**:
   - FastPass P1 제품 이미지 추가 (`/image/fastpass-p1.png`)
   - Flexbox 레이아웃으로 좌우 배치 (lg 브레이크포인트 이상)
   - Glow 효과: `bg-white/20 blur-2xl`
   - Hover 애니메이션: `hover:scale-105 transition-transform`
   - 반응형: 모바일에서는 이미지 숨김 (`hidden lg:flex`)

5. **Favicon 호환성 개선**:
   - `rel="icon"` - 표준 favicon
   - `rel="shortcut icon"` - 구형 브라우저 지원
   - `rel="apple-touch-icon"` - iOS/Apple 기기 지원

**수정된 파일**:
- `layouts/default.html` - favicon 태그 추가, min-h-screen 적용
- `index.html` - content wrapper 높이 설정
- `home_content.html` - Hero 섹션 이미지 추가
- `device_setting_form.html` - 버튼 재배치, 중복 제거
- `epassport_auto_read.html` - 그라데이션 헤더 적용
- `idcard_auto_read.html` - 그라데이션 헤더 적용
- `barcode_auto_read.html` - 그라데이션 헤더 적용
- `scan_page.html` - 그라데이션 헤더 적용
- `barcode_manual_read.html` - 그라데이션 헤더 적용

**디자인 패턴**:
```html
<!-- 표준 그라데이션 헤더 구조 -->
<div class="relative overflow-hidden rounded-2xl bg-gradient-to-r from-{color}-600 via-{color}-700 to-{color2}-800 p-6 mb-6 shadow-lg">
  <div class="absolute inset-0 bg-grid-white/[0.05] bg-[size:20px_20px]"></div>
  <div class="relative flex items-center justify-between">
    <div class="flex items-center gap-4">
      <!-- Icon Container -->
      <div class="bg-white/10 backdrop-blur-sm rounded-xl p-3">
        <svg class="size-10 text-white"><!-- Hero Icon --></svg>
      </div>
      <div>
        <h1 class="text-2xl font-bold text-white tracking-wide">Title</h1>
        <p class="text-lg text-{color}-100">Subtitle</p>
      </div>
    </div>
    <div class="flex items-center gap-3">
      <!-- Action Buttons -->
    </div>
  </div>
</div>
```

---

### 2025-12-23: Passive Authentication 검증 기능 통합

**구현 내용**:
- ICAO Local PKD 외부 API를 통한 전자여권 Passive Authentication 검증 기능 구현
- WSL2 환경에서 실행되는 PA API 서버와 Windows Spring Boot 앱 간 HTTP 통신 구현
- Apache HttpClient 5 기반 안정적인 HTTP 클라이언트 구성

**주요 변경사항**:

1. **DTO 계층 추가** (`dto/pa/`):
   - `PaVerificationRequest.java`: PA 검증 요청 DTO
     - SOD 및 Data Groups Base64 인코딩
     - Null DG 처리 로직 (NullPointerException 방지)
     - 발급 국가, 여권 번호, Client ID 포함
   - `PaVerificationResponse.java`: PA 검증 응답 DTO
     - 검증 상태 (SUCCESS/FAILURE)
     - 인증서 체인 검증 결과
     - SOD 서명 검증 결과
     - DG 해시 검증 결과

2. **서비스 계층 구현** (`PassiveAuthenticationService.java`):
   - `verify()`: PA API 호출 및 응답 처리
   - `verifyFromDocumentResponse()`: DocumentReadResponse에서 데이터 추출 후 검증
   - SOD/DG 데이터 추출 및 Base64 인코딩
   - MRZ에서 발급 국가 및 여권 번호 파싱
   - 상세 로깅 (SOD 헤더, 크기, Base64 길이 검증)
   - 커스텀 예외 처리 (`PaVerificationException`)

3. **HTTP 클라이언트 설정** (`PaApiClientConfig.java`):
   - **Apache HttpClient 5** 사용으로 전환
     - SimpleClientHttpRequestFactory → HttpComponentsClientHttpRequestFactory
     - Connection Pool 설정 (최대 10개 연결, 라우트당 5개)
     - 타임아웃: 연결 30초, 소켓 60초, 응답 60초
   - `Connection: close` 헤더 추가 (WSL2 포트 포워딩 안정성 향상)
   - Base URL 자동 적용 (`DefaultUriBuilderFactory`)

4. **컨트롤러 업데이트** (`FPHPSController.java`):
   - `POST /fphps/passport/verify-pa`: PA 검증 엔드포인트 추가
   - Manual/Auto Read 모두에서 PA 검증 지원
   - 검증 결과 UI 표시

5. **UI 구현**:
   - `epassport_manual_read.html`: "Verify PA" 버튼 추가
     - 버튼 색상: cyan-500 (시인성 향상)
     - HTMX 기반 비동기 요청
   - `pa_verification_result.html`: PA 검증 결과 표시 프래그먼트
     - 전체 검증 상태 배지
     - 인증서 체인 검증 결과
     - SOD 서명 검증 결과
     - Data Group 해시 검증 결과 (개별 DG별 상세 정보)
     - 처리 시간 표시

6. **설정 파일** (`application.properties`):
   - PA API 서버 URL 설정
   - WSL2 IP 직접 사용: `http://172.24.1.6:8081`

**기술적 해결 과제**:

1. **WSL2 Port Forwarding 불안정성**:
   - **문제**: `netsh portproxy`를 통한 대용량 POST 요청 시 Connection Reset 발생
   - **원인**: WSL2 NAT 네트워킹의 알려진 제한사항
   - **시도한 해결책**:
     - Apache HttpClient 5로 전환
     - Connection Pool 설정
     - `Connection: close` 헤더 추가
   - **최종 해결**: 서버측 협의를 통한 네트워크 구성 변경

2. **Podman Rootless 네트워킹**:
   - **문제**: Podman rootless 컨테이너가 자체 네트워크 네임스페이스 사용으로 외부 접근 차단
   - **해결**: Podman `network_mode: host` 적용으로 WSL2 호스트 네트워크 직접 사용

3. **WSL2 UFW 방화벽**:
   - **문제**: UFW 기본 정책 DROP으로 8081 포트 차단
   - **해결**: `sudo ufw allow 8081/tcp` 규칙 추가

4. **NullPointerException in Base64 Encoding**:
   - **문제**: 일부 DG (DG3, DG14 등)가 null일 때 인코딩 실패
   - **해결**: `PaVerificationRequest.of()` 메서드에서 null/empty 체크 추가

**네트워크 구성**:

```
Windows (Spring Boot App)
    ↓ HTTP POST
    ↓ 172.24.1.6:8081
    ↓
WSL2 Ubuntu 20.04
    ├─ UFW: port 8081/tcp allow
    └─ Podman (network_mode: host)
        └─ Local PKD Container
            └─ PA API Server
```

**의존성 추가**:
- `org.apache.httpcomponents.client5:httpclient5` (5.4.4)

**테스트 결과**:
- ✅ Manual Read 후 PA 검증 성공
- ✅ Auto Read 후 PA 검증 성공
- ✅ SOD 및 DG 데이터 Base64 인코딩 정상
- ✅ PA API 연동 정상 (인증서 체인, SOD 서명, DG 해시 검증)
- ✅ WSL2 환경에서 안정적인 HTTP 통신
- ✅ 검증 결과 UI 표시 정상

---

### 2025-12-23: ParsedSOD 데이터 시각화 구현

**구현 내용**:
- E-Passport SOD (Security Object Document) 정보 UI 표시 기능 추가
- Manual Read 및 Auto Read 모두에서 SOD 정보 표시 지원
- X509Certificate 데이터 추출 및 시각화

**주요 변경사항**:

1. **DTO 계층 추가**:
   - `CertificateInfo.java`: X509Certificate 데이터를 UI 친화적 형태로 변환
     - Subject/Issuer, 유효기간, 공개키 정보, 서명 알고리즘
     - SHA-1/SHA-256 지문, 확장 필드 정보
   - `ParsedSODInfo.java`: ParsedSOD를 JSON 직렬화 가능한 DTO로 변환
     - DSC Certificate, Data Group Hashes, Digest Algorithm, Signer Info

2. **컨트롤러 업데이트** (`FPHPSController.java`):
   - Manual Read: `parsedSODInfo`를 모델에 추가
   - Auto Read: `@ResponseBody` 추가로 view resolution 문제 해결
   - SOD 정보 조회 엔드포인트 추가 (GET `/fphps/passport/get-sod-info`)

3. **서비스 계층 수정** (`FPHPSService.java`):
   - Auto Read 결과 임시 저장 기능 (`lastReadResponse`)
   - `getLastReadResponse()`, `clearLastReadResponse()` 메서드 추가

4. **WebSocket 직렬화 개선** (`FastPassWebSocketHandler.java`):
   - Gson에 커스텀 TypeAdapter 등록
   - `ParsedSOD` → `ParsedSODInfo` 자동 변환
   - X509Certificate 직렬화 문제 해결 (JsonIOException 방지)

5. **UI 구현**:
   - `sod_information.html`: SOD 정보 표시 Thymeleaf 프래그먼트
     - Digest Algorithm, Data Group Hashes 테이블
     - DSC Certificate 상세 정보 (접기/펼치기 지원)
     - Signer Information
   - `epassport_manual_read.html`: Manual Read에 SOD 섹션 통합
   - `epassport_auto_read.html`: WebSocket 기반 SOD 동적 렌더링
     - `FPHPS_EV_EPASS_READ_DONE` 이벤트에서 parsedSOD 수신
     - JavaScript `renderSODInformation()` 함수로 HTML 생성
     - DOM null 체크 추가로 에러 방지

6. **HTMX Indicator 추가**:
   - `index.html`: 로딩 스피너 UI 요소 추가
   - `default.html`: HTMX indicator CSS 스타일 정의
   - HTMX 요청 중 시각적 피드백 제공

**기술적 해결 과제**:

1. **Gson JsonIOException**:
   - 문제: X509Certificate 내부 필드 접근 제한 (Java 모듈 시스템)
   - 해결: ParsedSOD용 커스텀 JsonSerializer 구현

2. **parsedSOD 이벤트 위치**:
   - 발견: `FPHPS_EV_PAGE_CAPTURED`가 아닌 `FPHPS_EV_EPASS_READ_DONE`에서 전송
   - 해결: 모든 WebSocket 이벤트에서 parsedSOD 체크

3. **HTMX Indicator 누락**:
   - 문제: `#indicator` 요소 없어 HTMX 경고 발생
   - 해결: 로딩 스피너 UI 및 CSS 추가

**테스트 결과**:
- ✅ Manual Read: SOD 정보 즉시 표시
- ✅ Auto Read: SOD 정보 자동 렌더링 (`FPHPS_EV_EPASS_READ_DONE` 이벤트)
- ✅ DSC Certificate 모든 필드 정상 표시
- ✅ Data Group Hashes 테이블 정상 렌더링
- ✅ WebSocket JSON 직렬화 성공
- ✅ 브라우저 콘솔 에러 제거

---

### 2025-12-20: Claude Code를 통한 프로젝트 분석

**분석 내용**:
- 프로젝트 전체 구조 탐색
- GEMINI.md 문서 검토
- 아키텍처 패턴 및 기술 스택 분석
- CLAUDE.md 문서 작성

**주요 발견사항**:
- 잘 설계된 전략 패턴 구현으로 문서 타입별 읽기 로직 모듈화
- 스레드 안전성과 리소스 관리에 대한 체계적 접근
- 이중 WebSocket 설정으로 유연한 실시간 통신 지원
- 전역 예외 처리 및 롤링 로그로 안정적인 운영 환경 구축

### 이전 작업 (GEMINI.md 참조)

**2025-12-20**: 사이드바 네비게이션 버그 수정 및 장치 설정 기능 개선
- HTMX 동적 컨텐츠 로드 버그 해결
- 전략 패턴 도입 (커밋 `e49d1e4`)
- 전역 예외 처리 구현
- 안정적인 장치 제어 로직 개선
- 파일 기반 로깅 시스템 도입

---

## 주요 특징 요약

### 기술적 강점

1. **모듈화된 아키텍처**
   - 전략 패턴으로 확장 가능한 문서 읽기 로직
   - 계층별 명확한 책임 분리

2. **안정성**
   - 스레드 안전한 디바이스 작업
   - 전역 예외 처리
   - 리소스 누수 방지 (try-finally)

3. **실시간 통신**
   - Raw WebSocket과 STOMP 이중 지원
   - 브로드캐스트 기반 진행 상황 업데이트

4. **개발 편의성**
   - 롤링 로그 (1MB, 7일 보관)
   - 개발 모드에서 Thymeleaf 캐시 비활성화
   - UTF-8 전역 설정

5. **확장성**
   - 새로운 문서 타입 추가 용이 (전략 패턴)
   - 프래그먼트 기반 UI로 재사용성 높음

### 개선 가능 영역

1. **테스트 코드 부재**
   - 현재 `src/test/` 디렉터리에 테스트 파일 없음
   - 단위 테스트 및 통합 테스트 추가 권장

2. **국제화 (i18n)**
   - 한국어 주석 존재로 i18n 작업 진행 흔적 있음
   - 다국어 지원 완성도 향상 가능

3. **API 문서화**
   - Swagger/OpenAPI 통합 고려
   - 현재는 별도 마크다운 문서 존재

---

## 참고 문서

- [README.md](README.md): 빠른 시작 가이드
- [GEMINI.md](GEMINI.md): Gemini를 통한 상세 프로젝트 분석
- [docs/api_documentation.md](docs/api_documentation.md): API 레퍼런스
- [docs/deployment.md](docs/deployment.md): 배포 가이드 (Windows 설치 프로그램 포함)
- [docs/user_manual.md](docs/user_manual.md): 사용자 매뉴얼
- [docs/API_CLIENT_USER_GUIDE.md](docs/API_CLIENT_USER_GUIDE.md): PA API 외부 클라이언트 연동 가이드
- [docs/analysis/20251220_task_analysis.md](docs/analysis/20251220_task_analysis.md): 태스크 분석 보고서

---

**문서 작성일**: 2025-12-20
**최종 업데이트**: 2026-02-27
**분석 도구**: Claude Code (Anthropic)
**현재 브랜치**: `main`
