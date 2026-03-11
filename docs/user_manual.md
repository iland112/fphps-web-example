# FPHPS Web Example — 사용자 매뉴얼

**Version**: 1.4.0
**Last Updated**: 2026-03-11
**Application**: SMARTCORE FastPass SDK Web Demo

---

## 목차

1. [개요](#1-개요)
2. [시스템 요구사항](#2-시스템-요구사항)
3. [설치 및 실행](#3-설치-및-실행)
4. [애플리케이션 설정](#4-애플리케이션-설정)
5. [메인 화면 구성](#5-메인-화면-구성)
6. [전자여권 (E-Passport)](#6-전자여권-e-passport)
7. [신분증 (ID Card)](#7-신분증-id-card)
8. [바코드 (Barcode)](#8-바코드-barcode)
9. [페이지 스캔 (Scan Page)](#9-페이지-스캔-scan-page)
10. [디바이스 설정 (Device Settings)](#10-디바이스-설정-device-settings)
11. [외부 서비스 연동](#11-외부-서비스-연동)
12. [다국어 지원 (i18n)](#12-다국어-지원-i18n)
13. [트러블슈팅](#13-트러블슈팅)

---

## 1. 개요

FPHPS Web Example은 SMARTCORE FastPass SDK(전자여권 판독기)를 Spring Boot 웹 애플리케이션에 통합한 데모 프로젝트입니다.

C/C++ Windows DLL로 작성된 전자여권 판독기 SDK를 JNA(Java Native Access)로 래핑하여 웹 브라우저 환경에서 다음 기능을 사용할 수 있습니다:

- **전자여권 (E-Passport)** 판독 — RFID 칩 데이터 읽기, MRZ 파싱
- **신분증 (ID Card)** 판독
- **바코드 (Barcode/QR)** 판독
- **페이지 스캔** — 백색광, 적외선, 자외선 촬영
- **Passive Authentication** — ICAO Local PKD를 통한 전자여권 진위 검증
- **Face Verification** — InsightFace 기반 얼굴 매칭
- **MRZ Validation** — ICAO Doc 9303 표준 기반 MRZ 데이터 검증
- **데이터 내보내기** — 판독 데이터 파일 시스템 저장

---

## 2. 시스템 요구사항

### 필수 요구사항

| 항목 | 요구사항 |
|------|---------|
| **운영체제** | Windows 10/11 (x64) |
| **Java** | JDK 21 이상 |
| **FastPass SDK** | SMARTCORE FastPass SDK 설치 및 시스템 PATH 등록 |
| **판독기** | SMARTCORE FastPass P1 하드웨어 연결 (USB) |
| **브라우저** | Chrome, Edge 등 WebSocket 지원 브라우저 |

### 선택 요구사항 (외부 서비스)

| 서비스 | 용도 | 기본 URL |
|--------|------|---------|
| **ICAO Local PKD** | Passive Authentication 검증 | `http://pkd.smartcoreinc.com` |
| **InsightFace** | 얼굴 검증 (Face Verification) | `http://localhost:10100` |

---

## 3. 설치 및 실행

### 3.1 Windows 설치 프로그램 (권장)

Java가 설치되지 않은 PC에서도 설치 프로그램만으로 바로 사용할 수 있습니다.

#### 설치

1. `FastPassSetup-x.x.x.exe` 실행 (관리자 권한 필요)
2. 설치 경로 선택 (기본: `C:\Program Files\SMARTCORE\FastPass Web`)
3. 바탕화면 바로가기 생성 여부 선택
4. 설치 완료 후 자동으로:
   - Windows 서비스 등록 (`FastPass Web Application`)
   - 방화벽 인바운드 규칙 추가 (포트 50000)
   - 서비스 시작
5. 브라우저에서 `http://localhost:50000` 으로 접속

#### 서비스 관리

설치 후 Windows 서비스로 자동 등록되어 PC 부팅 시 자동으로 시작됩니다.

- **서비스 관리**: `services.msc` → "FastPass Web Application"
- **서비스 중지**: `net stop FastPassWeb`
- **서비스 시작**: `net start FastPassWeb`
- **서비스 재시작**: `net stop FastPassWeb && net start FastPassWeb`

#### 설정 변경

설치 경로의 `application.properties` 파일을 편집하여 설정을 변경할 수 있습니다.
변경 후 서비스를 재시작해야 적용됩니다.

#### 제거

제어판 > 프로그램 제거에서 "FastPass Web"을 선택하여 제거합니다.
서비스 제거 및 방화벽 규칙 삭제가 자동으로 처리됩니다.

> **Note**: FastPass SDK (FPHPS.dll)는 설치 프로그램에 포함되어 있지 않으므로 별도로 설치해야 합니다.

### 3.2 개발 환경 설치 (수동)

#### 3.2.1 프론트엔드 빌드 (최초 1회)

```bash
cd src/main/frontend
npm install
npm run build
```

#### 3.2.2 애플리케이션 빌드

```bash
./gradlew clean build -x test
```

#### 3.2.3 실행

```bash
./gradlew bootRun
```

#### 3.2.4 접속

브라우저에서 `http://localhost:50000` 으로 접속합니다.

> **Note**: 기본 포트는 `50000`입니다. `application.properties`의 `server.port`에서 변경할 수 있습니다.

---

## 4. 애플리케이션 설정

### 4.1 application.properties 주요 설정

설정 파일 위치: `src/main/resources/application.properties`

#### 서버 설정

| 속성 | 설명 | 기본값 |
|------|------|--------|
| `server.port` | 애플리케이션 포트 | `50000` |
| `spring.thymeleaf.cache` | 템플릿 캐시 (개발 시 `false`) | `false` |

#### PA API 설정 (Passive Authentication)

| 속성 | 설명 | 기본값 |
|------|------|--------|
| `pa-api.base-url` | PA API 서버 URL | `https://pkd.smartcoreinc.com` |
| `pa-api.api-key` | PA API 인증 키 (`X-API-Key` 헤더) | (비어있음) |

#### Face Verification API 설정

| 속성 | 설명 | 기본값 |
|------|------|--------|
| `face-api.base-url` | InsightFace API 서버 URL | `http://localhost:10100` |

#### 데이터 내보내기 설정

| 속성 | 설명 | 기본값 |
|------|------|--------|
| `document-export.base-dir` | 내보내기 기본 디렉토리 | `D:/passport_exports` |

#### 로깅 설정

| 속성 | 설명 | 기본값 |
|------|------|--------|
| `logging.level.com.smartcoreinc` | 애플리케이션 로그 레벨 | `INFO` |
| `logging.file.name` | 로그 파일 경로 | `log/application.log` |
| `logging.logback.rollingpolicy.max-file-size` | 로그 파일 최대 크기 | `5MB` |
| `logging.logback.rollingpolicy.max-history` | 보관 파일 수 | `3` |

#### 데이터베이스 설정 (SQLite)

| 속성 | 설명 | 기본값 |
|------|------|--------|
| `spring.datasource.url` | SQLite DB 경로 | `jdbc:sqlite:data/fphps_settings.db` |

디바이스 설정값은 SQLite DB에 자동 저장되며, 애플리케이션 재시작 시 복원됩니다.

---

## 5. 메인 화면 구성

### 5.1 레이아웃

애플리케이션은 다음과 같은 3영역 레이아웃으로 구성됩니다:

```
┌─────────────────────────────────────────────┐
│  Header (로고, 다크모드 토글, 사이드바 토글)     │
├──────┬──────────────────────────────────────┤
│      │                                      │
│ Side │          Content Area                │
│ bar  │      (페이지별 콘텐츠)                 │
│      │                                      │
│      │                                      │
└──────┴──────────────────────────────────────┘
```

### 5.2 헤더

- **사이드바 토글 버튼**: 데스크톱에서 사이드바를 미니/확장 모드로 전환
- **다크 모드 토글**: 라이트/다크 테마 전환 (해/달 아이콘)
- **브레드크럼**: 현재 페이지 위치 표시 (예: Home > E-Passport / Manual Read)

### 5.3 사이드바 네비게이션

| 메뉴 | 서브메뉴 | 설명 |
|------|---------|------|
| **Home** | — | 홈 화면 |
| **E-Passport** | Manual Read | 수동 전자여권 판독 |
| | Automatic Read | 자동 전자여권 판독 (WebSocket) |
| **ID Card** | Manual Read | 수동 신분증 판독 |
| | Automatic Read | 자동 신분증 판독 |
| **Barcode** | Manual Read | 수동 바코드 판독 |
| | Automatic Read | 자동 바코드 판독 |
| **Scan Page** | — | 페이지 스캔 (광원별 촬영) |
| **Device Settings** | — | 디바이스 설정 관리 |

- **미니 모드**: 사이드바가 아이콘만 표시되는 좁은 형태로 축소됩니다. 서브메뉴는 마우스 오버 시 툴팁으로 표시됩니다.
- **확장 모드**: 아이콘과 텍스트가 함께 표시됩니다.
- 사이드바 상태는 `localStorage`에 저장되어 페이지 새로고침 후에도 유지됩니다.

### 5.4 디바이스 미연결 경고

FastPass 판독기가 연결되지 않은 상태에서 애플리케이션에 접속하면 경고 모달이 표시됩니다.

> 판독기가 연결되어 있지 않아도 애플리케이션은 정상적으로 기동됩니다.

**경고 모달 기능**:

- **자동 로케일 감지**: 브라우저의 시스템 언어 설정에 따라 한글 또는 영문으로 안내 메시지가 표시됩니다.
  - 한글 시스템: "장치 미연결", "재연결", "연결 방법:" 등
  - 영문 시스템: "Device Not Connected", "Retry Connection", "How to connect:" 등
- **연결 안내**: USB 연결 → Windows 인식 대기 → 재연결 버튼 클릭 3단계 안내
- **재연결 (Retry Connection)**: 버튼 클릭 시 판독기 재탐색을 시도합니다.
  - 성공 시: 녹색 메시지 표시 후 페이지 자동 새로고침
  - 실패 시: 빨간색 에러 메시지 표시 (재시도 가능)
- **닫기 (Dismiss)**: 모달을 닫고 판독기 없이 UI를 확인할 수 있습니다.

### 5.5 다크 모드

- 헤더의 해/달 아이콘을 클릭하여 전환합니다.
- 시스템의 다크 모드 설정(`prefers-color-scheme: dark`)을 자동 감지합니다.
- 설정은 `localStorage`에 저장되어 페이지 새로고침 후에도 유지됩니다.

---

## 6. 전자여권 (E-Passport)

전자여권 판독은 이 애플리케이션의 핵심 기능으로, Manual Read와 Automatic Read 두 가지 모드를 제공합니다.

### 6.1 Manual Read (수동 판독)

1. 사이드바에서 **E-Passport > Manual Read**를 클릭합니다.
2. 판독기에 여권을 올려놓습니다.
3. **Read Passport** 버튼을 클릭합니다.
4. 판독이 완료되면 결과가 탭 형태로 표시됩니다.

### 6.2 Automatic Read (자동 판독)

1. 사이드바에서 **E-Passport > Automatic Read**를 클릭합니다.
2. **Start Auto Read** 버튼을 클릭합니다.
3. 판독기에 여권을 올려놓으면 자동으로 판독이 시작됩니다.
4. WebSocket을 통해 실시간 진행 상황이 표시됩니다:
   - 진행률 바 (이벤트별 퍼센트)
   - 로그 엔트리 (이벤트 타임라인)
5. 판독 완료 시 결과가 탭에 자동으로 렌더링됩니다.

### 6.3 결과 탭

판독 완료 후 5개의 탭에서 결과를 확인할 수 있습니다:

#### Tab 1: E-MRTD Data

전자여권 칩에서 읽은 MRZ(Machine Readable Zone) 데이터를 표시합니다.

- **DG1 - MRZ Data**: 문서 타입, 발급국, 성명, 여권번호, 국적, 생년월일, 성별, 만료일
- **MRZ Lines**: 원본 MRZ 텍스트 2줄
- **Passport Holder Photo**: DG2에서 추출한 여권 사진
- **캡처 이미지**: 백색광(WH), 적외선(IR), 자외선(UV) 이미지

#### Tab 2: MRZ Validation

ICAO Doc 9303 표준에 따른 MRZ 데이터 검증 결과를 표시합니다.

**Check Digit Validation** (5가지):
- Passport Number Check Digit (가중치: 7, 3, 1)
- Birth Date Check Digit
- Expiry Date Check Digit
- Optional Data Check Digit
- Composite Check Digit (TD3 여권)

**Format Validation**:
- Document Type 검증 (P, V, I, A)
- Character Set 검증 (A-Z, 0-9, < 만 허용)

**Field Validation**:
- Country Code (ISO 3166-1 alpha-3, 3자리 알파벳)
- Date Format (YYMMDD, 월 1-12, 일 1-31)
- Sex Code (M, F, <)

**VIZ/Chip MRZ 비교**:
- VIZ(OCR)로 읽은 MRZ와 칩(DG1)에서 읽은 MRZ를 나란히 비교
- Line 1, Line 2별 Match/Mismatch 배지 표시
- 칩 읽기 실패 시 VIZ MRZ만 표시

색상 코드:
- 🟢 녹색: 검증 성공 (VALID)
- 🔴 빨간색: Format 오류
- 🟡 노란색: Field 오류

#### Tab 3: SOD Information

Security Object Document(SOD) 정보를 표시합니다.

- **DSC Certificate**: Document Signer Certificate 상세 정보
  - Subject/Issuer, 유효기간, 공개키 정보
  - SHA-1/SHA-256 지문, 서명 알고리즘
  - 확장 필드 (접기/펼치기 지원)
- **Data Group Hashes**: DG별 해시 값 및 알고리즘
- **Signer Information**: 서명자 정보

#### Tab 4: Passive Authentication

ICAO Local PKD를 통한 전자여권 진위 검증 기능입니다.

> **PA 탭 선택 시** PA API 서버 연결 상태가 자동으로 확인됩니다.
> - 🟢 연결 성공: 녹색 배너 표시 후 3초 후 사라짐
> - 🔴 연결 실패: 빨간 배너 유지 ("PA API server is not reachable")

**Verify PA** (전체 검증):
1. **Verify PA** 버튼을 클릭합니다.
2. SOD와 Data Group 바이너리 데이터를 PA API 서버로 전송합니다.
3. 서버에서 8단계 전체 검증 프로세스를 수행합니다 (100-500ms).
4. 검증 결과 표시:
   - **Overall Status**: SUCCESS / FAILURE 배지
   - **Certificate Chain Validation**: DSC → CSCA 인증서 체인 검증
   - **CRL Status**: 인증서 폐기 여부 (Severity별 색상)
   - **SOD Signature Validation**: SOD 서명 검증
   - **Data Group Hash Validation**: 개별 DG별 해시 일치 여부

**PA Lookup** (간편 조회):
1. **PA Lookup** 버튼을 클릭합니다.
2. DSC 인증서의 Subject DN과 SHA-256 Fingerprint만 전송합니다.
3. PKD에 등록된 Trust Chain 검증 결과를 즉시 반환합니다 (5-20ms).
4. 검증 결과 표시:
   - **Status Card**: VALID / EXPIRED_VALID / INVALID / PENDING / ERROR
   - **Certificate Info**: Subject DN, Issuer DN, 유효기간 (Valid/Expired 배지)
   - **Trust Chain**: Trust Chain Valid, CSCA Found 등
   - **Revocation Status**: 폐기 상태 (NOT_CHECKED 표시 가능)
   - **Info Note**: "간편 검증은 DSC Trust Chain만 확인합니다..." 안내

> **Verify PA vs PA Lookup 차이점**
>
> | 항목 | Verify PA | PA Lookup |
> |------|-----------|-----------|
> | 전송 데이터 | SOD + DG 바이너리 전체 | Subject DN + Fingerprint만 |
> | 검증 범위 | 인증서 체인 + SOD 서명 + DG 해시 | Trust Chain만 |
> | 응답 시간 | 100-500ms | 5-20ms |
> | signatureValid | 실제 검증 결과 | Not Checked |
> | DG Hash | 개별 DG 해시 검증 | 검증하지 않음 |

#### Tab 5: Face Verification

InsightFace 기반 얼굴 매칭 기능입니다.

1. **Verify Face** 버튼을 클릭합니다.
2. Document Photo(VIZ)와 Chip Photo(DG2)를 InsightFace API로 전송합니다.
3. 결과 표시:
   - **Match Score**: 얼굴 유사도 점수
   - **Confidence Level**: 매칭 신뢰도
   - **Quality Metrics** (2-column 레이아웃):
     - 🎯 Detection Score
     - 📏 Size (Face Area Ratio)
     - 💡 Brightness Score
     - 🔍 Sharpness Score
     - 👤 Pose Score
   - **Bounding Box Overlay**: "Show Face Box" 버튼으로 얼굴 영역 표시 토글
     - 반투명 오버레이 + 녹색 경계 박스 + 코너 마커 + "FACE" 라벨

### 6.4 데이터 내보내기 (Export Data)

판독 데이터를 파일 시스템에 저장합니다.

1. 여권 판독 후 **Export Data** 버튼을 클릭합니다.
2. 저장 경로: `{document-export.base-dir}/{국가코드}_{여권번호}/`
   - 예: `D:/passport_exports/KOR_M12345678/`
3. 저장 완료 시 Toast 알림으로 저장 경로가 표시됩니다.

---

## 7. 신분증 (ID Card)

### 7.1 Manual Read

1. 사이드바에서 **ID Card > Manual Read**를 클릭합니다.
2. 판독기에 신분증을 올려놓습니다.
3. **Read ID Card** 버튼을 클릭합니다.
4. 판독 결과가 표시됩니다.

### 7.2 Automatic Read

1. 사이드바에서 **ID Card > Automatic Read**를 클릭합니다.
2. **Start Auto Read** 버튼을 클릭합니다.
3. 판독기에 신분증을 올려놓으면 자동으로 판독됩니다.
4. WebSocket을 통해 실시간 진행 상황이 표시됩니다.

> **Note**: 전자신분증(e-ID)의 경우 RFID 칩 읽기가 지원됩니다.

---

## 8. 바코드 (Barcode)

### 8.1 Manual Read

1. 사이드바에서 **Barcode > Manual Read**를 클릭합니다.
2. 판독기에 바코드/QR 코드를 올려놓습니다.
3. **Read Barcode** 버튼을 클릭합니다.
4. 바코드 데이터가 표시됩니다.

### 8.2 Automatic Read

1. 사이드바에서 **Barcode > Automatic Read**를 클릭합니다.
2. **Start Auto Read** 버튼을 클릭합니다.
3. 판독기에 바코드를 올려놓으면 자동으로 판독됩니다.

> **Note**: 바코드 판독 시 RF(RFID) 및 ID Card 모드가 자동으로 비활성화됩니다.

---

## 9. 페이지 스캔 (Scan Page)

다양한 광원으로 문서 이미지를 촬영합니다.

1. 사이드바에서 **Scan Page**를 클릭합니다.
2. 판독기에 문서를 올려놓습니다.
3. 광원 타입을 선택하고 스캔합니다:
   - **백색광 (White Light)**: 일반 가시광 촬영
   - **적외선 (IR)**: 적외선 이미지 촬영
   - **자외선 (UV)**: 자외선 이미지 촬영
4. 촬영된 이미지가 화면에 표시됩니다.

---

## 10. 디바이스 설정 (Device Settings)

판독기의 세부 설정을 관리합니다.

1. 사이드바에서 **Device Settings**를 클릭합니다.
2. 설정을 변경합니다.
3. **Update Settings** 버튼을 클릭하여 저장합니다.

### 주요 설정 카테고리

| 카테고리 | 설명 |
|---------|------|
| **RF/RFID 설정** | RFID 칩 읽기 활성화/비활성화 |
| **이미지 향상** | IR, UV, 백색광 이미지 캡처 옵션 |
| **바코드 설정** | 바코드 판독 옵션 |
| **전자여권 데이터 그룹** | DG1-DG16 선택적 읽기 |
| **전자여권 인증** | PA, AA, CA, TA, SAC 옵션 |
| **배치 모드** | 이미지 캡처 배치 설정 |
| **반사 방지/노이즈 감소** | 이미지 품질 향상 옵션 |

> **Note**: 설정 변경사항은 SQLite DB에 저장되어 애플리케이션 재시작 시 자동 복원됩니다.

---

## 11. 외부 서비스 연동

### 11.1 PA API (Passive Authentication)

ICAO Local PKD 시스템과 연동하여 전자여권의 진위를 검증합니다.

**설정 방법**:
```properties
# application.properties
pa-api.base-url=https://pkd.smartcoreinc.com
pa-api.api-key=icao_XXXXXXXX_YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY
```

**네트워크 환경별 URL**:

| 환경 | URL |
|------|-----|
| HTTPS (운영, 권장) | `https://pkd.smartcoreinc.com` |
| HTTP (내부 네트워크) | `http://pkd.smartcoreinc.com` |
| WiFi (SC-WiFi) | `http://192.168.1.70:8080` |
| 유선 LAN | `http://192.168.100.10:8080` |

**API Key**:
- PA API 서버 관리자에게 발급 요청
- 형식: `icao_{8자 prefix}_{32자 랜덤}` (총 46자)
- 모든 PA API 요청에 `X-API-Key` 헤더로 자동 전송
- API Key 미설정 시 애플리케이션 로그에 경고 표시

**HTTP 클라이언트 설정**:
- Apache HttpClient 5 사용
- Connection Pool: 최대 10개 연결, 라우트당 5개
- 타임아웃: 연결 30초, 소켓 60초, 응답 60초

> **참고**: 외부 클라이언트로서 PA API 연동 상세 가이드는 [API_CLIENT_USER_GUIDE.md](API_CLIENT_USER_GUIDE.md)를 참조하세요.

### 11.2 Face Verification API (InsightFace)

InsightFace 기반 얼굴 검증 마이크로서비스와 연동합니다.

**설정 방법**:
```properties
# application.properties
face-api.base-url=http://localhost:10100
```

**요구사항**:
- InsightFace 서비스가 별도로 실행 중이어야 합니다.
- Docker 또는 직접 실행 가능

---

## 12. 다국어 지원 (i18n)

애플리케이션의 일부 UI 요소는 시스템 로케일에 따라 한글 또는 영문으로 자동 전환됩니다.

### 지원 범위

| 기능 | 한글 (ko) | 영문 (en, 기타) |
|------|----------|----------------|
| 디바이스 미연결 경고 모달 | 한글 안내 | 영문 안내 |
| 재연결 상태 메시지 | 한글 응답 | 영문 응답 |

### 동작 방식

- 브라우저의 `navigator.language` 값을 기반으로 로케일을 자동 감지합니다.
- 한국어 로케일(`ko`, `ko-KR` 등)인 경우 한글, 그 외는 영문으로 표시됩니다.
- 서버 응답 메시지는 `Accept-Language` HTTP 헤더를 기반으로 로컬라이징됩니다.

---

## 13. 트러블슈팅

### 13.1 디바이스 관련

**Q. 애플리케이션 접속 시 "장치 미연결" 경고 모달이 표시됩니다.**
- FastPass SDK가 설치되었는지 확인하세요.
- 시스템 PATH에 SDK 경로가 등록되었는지 확인하세요.
- 판독기가 USB로 올바르게 연결되었는지 확인하세요.
- 모달의 **재연결** (또는 **Retry Connection**) 버튼을 클릭하여 재연결을 시도하세요.
- 재연결에 실패하면 판독기를 USB에서 분리 후 다시 연결한 뒤 재시도하세요.

**Q. "Device operation failed" 오류가 발생합니다.**
- 판독기가 다른 프로세스에서 사용 중일 수 있습니다.
- 한 번에 하나의 애플리케이션만 판독기를 사용할 수 있습니다.
- 다른 애플리케이션을 종료하고 재시도하세요.

### 13.2 PA 검증 관련

**Q. "PA API server is not reachable" 배너가 표시됩니다.**
- `application.properties`의 `pa-api.base-url`이 올바른지 확인하세요.
- PA API 서버가 실행 중인지 확인하세요.
- 네트워크 연결 상태를 확인하세요.
- 방화벽이 해당 포트를 차단하고 있지 않은지 확인하세요.

**Q. PA 검증 시 401/403 오류가 발생합니다.**
- `pa-api.api-key` 값이 올바른지 확인하세요.
- API Key가 만료되지 않았는지 PA API 서버 관리자에게 확인하세요.
- IP 화이트리스트가 설정된 경우, 현재 IP가 허용 목록에 있는지 확인하세요.

**Q. PA 검증 시 타임아웃 오류가 발생합니다.**
- PA API 서버의 응답 시간이 60초를 초과할 수 있습니다.
- 네트워크 상태를 확인하세요.
- WSL2 환경인 경우 Port Forwarding 설정을 확인하세요.

### 13.3 Face Verification 관련

**Q. "Face Verification API is not available" 오류가 발생합니다.**
- InsightFace 서비스가 실행 중인지 확인하세요.
- `face-api.base-url`이 올바른지 확인하세요.

### 13.4 일반

**Q. WebSocket 연결이 끊어집니다.**
- Auto Read 진행 중 브라우저 탭을 전환하거나 최소화하면 WebSocket이 끊어질 수 있습니다.
- 페이지를 새로고침하여 재연결하세요.

**Q. 페이지가 제대로 표시되지 않습니다.**
- 브라우저 캐시를 삭제하세요 (Ctrl+Shift+Delete).
- 또는 강력 새로고침을 수행하세요 (Ctrl+Shift+R).

**Q. 로그를 확인하고 싶습니다.**
- 애플리케이션 로그: `log/application.log`
- 브라우저 개발자 도구 Console 탭 (F12)

---

## 부록

### A. 단축키

| 동작 | 단축키 |
|------|--------|
| 강력 새로고침 | `Ctrl+Shift+R` |
| 브라우저 개발자 도구 | `F12` |
| 브라우저 캐시 삭제 | `Ctrl+Shift+Delete` |

### B. 기술 스택

| 분류 | 기술 |
|------|------|
| **Backend** | Java 21, Spring Boot 3.5.0, JNA 5.17.0 |
| **Frontend** | Thymeleaf, Tailwind CSS 4.1, DaisyUI 5.0, Preline 3.0, HTMX 2.0 |
| **통신** | WebSocket (Raw + STOMP), Apache HttpClient 5 |
| **암호화** | Bouncy Castle 1.78 |
| **데이터베이스** | SQLite (디바이스 설정 저장) |
| **빌드** | Gradle, npm |

### C. 관련 문서

- [CLAUDE.md](../CLAUDE.md) — 프로젝트 구조 및 아키텍처 상세 분석
- [deployment.md](deployment.md) — 배포 가이드 (Windows 설치 프로그램 빌드 포함)
- [API_CLIENT_USER_GUIDE.md](API_CLIENT_USER_GUIDE.md) — PA API 외부 클라이언트 연동 가이드
- [README.md](../README.md) — 빠른 시작 가이드
