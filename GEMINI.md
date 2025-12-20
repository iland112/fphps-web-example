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

## 6. 작업 이력

### 2025-12-20: 이전 작업 분석 및 문서화
- **내용**: 사이드바 네비게이션 버그 수정 및 장치 설정 기능 개선에 대한 이전 작업 내용을 분석하고 문서화했습니다.
- **주요 변경 사항**:
  - **사이드바 버그 수정**: HTMX가 동적 컨텐츠를 로드할 기준점(`id="response"`)이 일부 HTML 조각에 누락되었던 문제를 해결하여 네비게이션 안정성을 확보했습니다.
  - **아키텍처 리팩토링 (커밋 `e49d1e4`)**:
    - **전략 패턴(Strategy Pattern)** 도입으로 문서 타입별 판독 로직을 모듈화하여 코드 확장성과 유지보수성을 향상시켰습니다.
    - **전역 예외 처리** 및 **안정적인 장치 제어 로직**을 구현하여 시스템의 안정성을 크게 높였습니다.
    - **파일 기반 로깅** 시스템을 도입하여 추적 및 디버깅 환경을 개선했습니다.
- **상세 분석 문서**: [작업 분석 보고서 (2025-12-20)](./docs/analysis/20251220_task_analysis.md)

