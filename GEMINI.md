# FPHPS_WEB_Example 프로젝트 문서

## 1. 프로젝트 개요

이 프로젝트는 C/C++로 작성된 전자여권 판독기 SDK (Windows DLL)를 JNA(Java Native Access)로 래핑한 Java 라이브러리를 사용하는 웹 기반 샘플 애플리케이션입니다.
주요 목적은 웹 환경에서 전자여권 판독기 SDK의 API 기능을 테스트하고 시연하는 것입니다.

## 2. 주요 기술 스택

### 백엔드 (Backend)
- **언어**: Java 21
- **프레임워크**: Spring Boot 3.5.0
- **핵심 라이브러리**:
  - `fphps-1.0.0.jar`: 전자여권 판독기 SDK의 핵심 기능을 담은 로컬 Java 라이브러리
  - `JNA (Java Native Access)`: Java에서 네이티브 라이브러리(DLL)를 호출하기 위해 사용
  - `Spring Web & WebSocket`: 웹 및 실시간 통신 기능 제공
  - `Thymeleaf & Layout Dialect`: 서버 사이드 렌더링을 위한 템플릿 엔진
  - `Bouncy Castle`: 전자여권의 PKI 암호화/복호화 및 인증서 처리를 위한 보안 라이브러리
  - `Gson`: 데이터 처리를 위한 JSON 라이브러리

### 프론트엔드 (Frontend)
- **CSS 프레임워크**: `Tailwind CSS`
- **UI 라이브러리**: `Preline`, `DaisyUI`
- **빌드**: `npm` 스크립트를 사용하여 Tailwind CSS 빌드

### 빌드 및 의존성 관리
- **Java**: Gradle
- **Frontend**: npm

## 3. 프로젝트 구조

- `src/main/java`: Spring Boot 애플리케이션의 Java 소스 코드가 위치합니다.
- `src/main/resources/templates`: Thymeleaf HTML 템플릿 파일들이 위치합니다. 각 페이지 및 프래그먼트(조각)들로 구성되어 UI를 정의합니다.
- `src/main/frontend`: 프론트엔드 소스 코드 (Tailwind CSS `input.css`) 및 `package.json` 파일이 위치합니다.
- `src/main/resources/static`: 빌드된 CSS, JavaScript, 이미지 등 정적 리소스가 위치합니다.
- `build.gradle`: Java (Gradle) 프로젝트의 의존성 및 빌드 설정을 관리합니다.
- `lib/fphps-1.0.0.jar`: 핵심 로컬 라이브러리입니다. (정확한 위치는 `build.gradle` 참조)

## 4. 핵심 기능 (추정)

HTML 템플릿 파일들을 기반으로 추정한 핵심 기능은 다음과 같습니다.

- **장치 연동**: 판독기 장치 정보 표시 및 설정 변경
- **여권 정보 읽기**:
  - MRZ(Machine Readable Zone) 정보 파싱 및 표시
  - 전자여권(e-Passport) 자동 및 수동 읽기 기능
  - ID 카드(신분증) 읽기 기능
- **결과 표시**:
  - 여권 이미지 및 개인 사진(Photo) 표시
  - 판독 결과 데이터(MRZ, 인증서 등) 표시
- **부가 기능**:
  - 바코드 읽기 및 설정
  - 국가별 국기 아이콘 표시

## 5. 빌드 및 실행 방법

### 1. 프론트엔드 빌드 (Tailwind CSS)
프로젝트의 UI를 올바르게 표시하려면 먼저 프론트엔드 리소스를 빌드해야 합니다.

`src/main/frontend` 디렉터리로 이동하여 다음 명령어를 실행합니다.
```bash
# 의존성 설치 (최초 1회)
npm install

# CSS 빌드
npm run build
```

### 2. 백엔드 실행 (Spring Boot)
Gradle을 사용하여 Spring Boot 애플리케이션을 실행합니다.

프로젝트 루트 디렉터리에서 다음 명령어를 실행합니다.
```bash
./gradlew bootRun
```
애플리케이션이 실행되면 웹 브라우저에서 `http://localhost:8080` (기본 포트)으로 접속하여 확인할 수 있습니다.

## 6. 개선 과제 (TODO)

- [ ] `docs` 디렉터리 생성 및 사용자 매뉴얼, API 문서 등 산출물 정리
- [ ] JNA 인터페이스 관련 로직 설명 주석 추가
- [ ] Bouncy Castle을 사용한 전자여권 인증(BAC, PACE, AA 등) 과정 상세 설명 추가
- [ ] 로컬 라이브러리(`fphps-1.0.0.jar`)를 Nexus 와 같은 사설 Maven 저장소에 배포하여 관리하는 방식으로 전환 검토
- [ ] WebSocket을 통한 실시간 장치 상태 업데이트 로직 고도화
- [ ] 테스트 코드 작성 (단위 테스트, 통합 테스트)
