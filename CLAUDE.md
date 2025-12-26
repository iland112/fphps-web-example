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
│   │   └── PassiveAuthenticationService.java   # PA 검증 서비스 (외부 API 연동)
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
│   │   │   └── PaVerificationResponse.java     # PA 검증 응답
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
│   ├── user_manual.md                          # 사용자 매뉴얼
│   └── analysis/
│       └── 20251220_task_analysis.md           # 태스크 분석
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

**기본 URL**: `http://localhost:8080`

---

## 작업 이력

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
- [docs/user_manual.md](docs/user_manual.md): 사용자 매뉴얼
- [docs/analysis/20251220_task_analysis.md](docs/analysis/20251220_task_analysis.md): 태스크 분석 보고서

---

**문서 작성일**: 2025-12-20
**최종 업데이트**: 2025-12-26
**분석 도구**: Claude Code (Anthropic)
**현재 브랜치**: `main`
