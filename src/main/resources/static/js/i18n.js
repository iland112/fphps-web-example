/**
 * 전역 i18n (Internationalization) 시스템
 * 한글/영문 자동 감지 및 수동 전환 지원
 *
 * 우선순위: localStorage.appLang > navigator.language
 */

const APP_LANG = (function() {
  var saved = localStorage.getItem('appLang');
  if (saved) return saved;
  return /^ko\b/i.test(navigator.language) ? 'ko' : 'en';
})();

const IS_KO = APP_LANG === 'ko';

function _t(ko, en) { return IS_KO ? ko : en; }

// ============================================================
// 전역 번역 사전
// ============================================================
const I18N = {

  // ── 공통 ──
  home:               _t('홈', 'Home'),
  settings:           _t('설정', 'Settings'),
  close:              _t('닫기', 'Close'),
  save:               _t('저장', 'Save'),
  delete_:            _t('삭제', 'Delete'),
  cancel:             _t('취소', 'Cancel'),
  loading:            _t('로딩 중...', 'Loading...'),
  success:            _t('성공', 'Success'),
  error:              _t('오류', 'Error'),
  warning:            _t('경고', 'Warning'),
  connected:          _t('연결됨', 'Connected'),
  notConnected:       _t('미연결', 'Not Connected'),

  // ── 헤더 ──
  brandName:          _t('FastPass SDK 데모', 'FastPass SDK Demo'),
  toggleNav:          _t('네비게이션 토글', 'Toggle Navigation'),
  toggleSidebar:      _t('사이드바 토글', 'Toggle sidebar'),
  toggleDarkMode:     _t('다크 모드 토글', 'Toggle dark mode'),
  toggleLang:         _t('언어 전환', 'Toggle language'),

  // ── 사이드바 메뉴 ──
  ePassport:          _t('전자여권', 'E-Passport'),
  idCard:             _t('신분증', 'ID Card'),
  barcode:            _t('바코드', 'Barcode'),
  scanPage:           _t('페이지 스캔', 'Scan Page'),
  deviceSettings:     _t('디바이스 설정', 'Device Settings'),
  manualRead:         _t('수동 판독', 'Manual Read'),
  automaticRead:      _t('자동 판독', 'Automatic Read'),

  // ── 홈 페이지 ──
  heroTitle1:         _t('SMARTCORE FastPass SDK', 'SMARTCORE FastPass SDK'),
  heroTitle2:         _t('전자여권 판독기', 'E-Passport Reader'),
  heroTitle3:         _t('SDK 데모 애플리케이션', 'SDK Demo Application'),
  heroDesc:           _t('ICAO 표준 문서 판독 기술을 체험하세요. 이 웹 애플리케이션은 실시간 여권 인증, 생체 데이터 추출, 보안 검증 기능을 시연합니다.',
                         'Experience the power of ICAO-compliant document reading technology. This web application demonstrates real-time passport authentication, biometric data extraction, and security verification capabilities.'),
  connectedDevice:    _t('연결된 장치', 'Connected Device'),
  quickStartTitle:    _t('빠른 시작 가이드', 'Quick Start Guide'),
  quickStartDesc:     _t('3단계로 간편하게 시작하세요', 'Get up and running in 3 simple steps'),
  step1Title:         _t('기능 선택', 'Choose Feature'),
  step1Desc:          _t('사이드바에서 원하는 문서 유형을 선택하세요', 'Select your desired document type from the sidebar navigation'),
  step2Title:         _t('문서 배치', 'Place Document'),
  step2Desc:          _t('판독기 위에 문서를 뒤집어 놓으세요', 'Position your document face-down on the reader glass surface'),
  step3Title:         _t('판독 시작', 'Start Reading'),
  step3Desc:          _t('판독 버튼을 클릭하거나 자동 감지 모드를 활성화하세요', 'Click the read button or enable auto-detection mode'),
  comprehensiveTitle: _t('종합 문서 판독', 'Comprehensive Document Reading'),
  comprehensiveDesc:  _t('다양한 판독 모드와 문서 유형을 선택하세요. 사이드바를 사용하여 각 기능을 탐색하세요.',
                         'Choose from multiple reading modes and document types. Navigate using the sidebar to explore each capability.'),
  featureEPassport:   _t('전자여권 판독', 'E-Passport Reading'),
  featureEPassDesc:   _t('ICAO-9303 표준 전자여권 판독기로 생체 데이터 추출 및 보안 검증을 수행합니다.',
                         'ICAO-9303 compliant electronic passport reader with biometric data extraction and security verification.'),
  featureIdCard:      _t('전자 신분증', 'Electronic ID Cards'),
  featureIdCardDesc:  _t('RFID 칩이 내장된 전자 신분증을 판독합니다. 다양한 문서 형식과 보안 기능을 지원합니다.',
                         'Read national electronic ID cards with RFID chips. Supports multiple document formats and security features.'),
  featureBarcode:     _t('바코드 스캐너', 'Barcode Scanner'),
  featureBarcodeDesc: _t('탑승권, 비자, 여행 문서의 1D 및 2D 바코드를 높은 정확도로 판독합니다.',
                         '1D and 2D barcode reading for boarding passes, visas, and travel documents with high accuracy.'),
  featureScan:        _t('페이지 스캐너', 'Full Page Scanner'),
  featureScanDesc:    _t('보안 기능 탐지를 위한 다양한 광원의 고해상도 문서 이미지를 촬영합니다.',
                         'High-resolution document imaging with multiple light sources for security feature detection.'),
  featureConfig:      _t('디바이스 설정', 'Device Configuration'),
  featureConfigDesc:  _t('디바이스 매개변수를 조정하고 기능을 활성화하여 최적 성능을 구성합니다.',
                         'Fine-tune device parameters, enable features, and configure reading modes for optimal performance.'),
  manual:             _t('수동', 'Manual'),
  auto:               _t('자동', 'Auto'),
  rfidEnabled:        _t('RFID 지원', 'RFID Enabled'),
  whiteLight:         _t('백색광', 'White Light'),
  infrared:           _t('적외선', 'Infrared'),
  ultraviolet:        _t('자외선', 'UV'),
  advancedSettings:   _t('고급 설정', 'Advanced Settings'),
  codes1D:            _t('1D 코드', '1D Codes'),
  codesQR2D:          _t('QR / 2D', 'QR / 2D'),

  // ── E-Passport 페이지 ──
  passportInfo:       _t('여권 정보', 'Passport Information'),
  passportInfoDesc:   _t('여권/전자여권 판독 후 데이터 상세 정보', 'Data details after reading the passport/e-passport'),
  exportData:         _t('데이터 내보내기', 'Export Data'),
  readSuccess:        _t('판독 성공', 'Read Success'),
  chipReadFailed:     _t('칩 데이터 판독 실패', 'Chip Data Read Failed'),
  chipReadFailedDesc: _t('VIZ(시각 검사 영역) 데이터는 성공적으로 읽었으나, 칩(RFID) 데이터를 가져올 수 없습니다.',
                         'VIZ (Visual Inspection Zone) data was successfully read, but the chip (RFID) data could not be retrieved.'),
  chipReadFailedHint: _t('여권이 올바르게 배치되지 않았거나 칩이 손상되었을 수 있습니다.',
                         'The passport may not be positioned correctly, or the chip may be damaged.'),
  chipReadFailedNote: _t('아래에 사용 가능한 VIZ 데이터를 표시합니다. 칩 전용 기능(전자여권 사진, 데이터 그룹, SOD)은 사용할 수 없습니다.',
                         'Showing available VIZ data below. Chip-specific features (e-Passport photo, Data Groups, SOD) are not available.'),
  passportDataReq:    _t('여권 데이터 필요', 'Passport Data Required'),
  passportDataReqMsg: _t('데이터를 보려면 먼저 여권을 판독하세요.', 'Please read a passport first to view the data.'),
  passportDataReqAct: _t('위의 <strong>여권 판독</strong> 버튼을 클릭하여 여권을 스캔하세요.',
                         'Click the <strong>Read Passport</strong> button above to scan a passport.'),

  // ── 탭 이름 ──
  tabEmrtd:           _t('E-MRTD 데이터', 'E-MRTD Data'),
  tabMrzValidation:   _t('MRZ 검증', 'MRZ Validation'),
  tabSodInfo:         _t('SOD 정보', 'SOD Information'),
  tabPA:              _t('수동 인증', 'Passive Authentication'),
  tabFace:            _t('얼굴 검증', 'Face Verification'),

  // ── Auto Read ──
  ePassAutoTitle:     _t('전자여권 자동 판독', 'E-Passport Auto Read'),
  ePassAutoDesc:      _t('자동 여권 스캔 및 칩 데이터 추출', 'Automatic passport scanning and chip data extraction'),
  runAutoRead:        _t('자동 판독 시작', 'RUN AUTO READ'),
  reading:            _t('판독 중...', 'Reading...'),
  eventLog:           _t('이벤트 로그', 'Event Log'),
  autoScrollOn:       _t('자동 스크롤: 켜짐', 'Auto-scroll: ON'),
  autoScrollOff:      _t('자동 스크롤: 꺼짐', 'Auto-scroll: OFF'),
  clearLog:           _t('지우기', 'Clear'),

  // ── ID Card ──
  idCardInfo:         _t('신분증 정보', 'ID Card Information'),
  idCardInfoDesc:     _t('신분증 판독 후 데이터 상세 정보', 'Data details after reading the ID Card'),
  electronicIdCard:   _t('전자 신분증', 'Electronic ID Card'),
  rfidChipExtracted:  _t('RFID 칩 데이터 추출 완료', 'RFID Chip Data Extracted'),
  chipPhoto:          _t('칩 사진', 'Chip Photo'),

  // ── Barcode ──
  barcodeInfo:        _t('바코드 정보', 'Barcode Information'),
  barcodeInfoDesc:    _t('바코드 판독 후 데이터 상세 정보', 'Data details after reading the Barcode'),
  barcodeImage:       _t('바코드 이미지', 'Barcode Image'),
  barcodeData:        _t('바코드 데이터', 'Barcode Data'),
  barcodeType:        _t('바코드 유형', 'Barcode Type'),
  dataContent:        _t('데이터 내용', 'Data Content'),

  // ── Scan Page ──
  pageScanner:        _t('페이지 스캐너', 'Page Scanner'),
  pageScannerDesc:    _t('다양한 광원으로 문서 이미지를 촬영합니다', 'Capture document images with different light sources'),
  scanControls:       _t('스캔 컨트롤', 'Scan Controls'),
  lightSource:        _t('광원', 'Light Source'),
  selectLightType:    _t('광원 선택...', 'Select light type...'),
  irLight:            _t('적외선 (IR)', 'Infrared (IR)'),
  whLight:            _t('백색광 (WH)', 'White Light (WH)'),
  uvLight:            _t('자외선 (UV)', 'Ultraviolet (UV)'),
  captureImage:       _t('이미지 촬영', 'Capture Image'),
  capturedImage:      _t('촬영된 이미지', 'Captured Image'),
  security:           _t('보안', 'Security'),
  standard:           _t('표준', 'Standard'),
  fluorescence:       _t('형광', 'Fluorescence'),

  // ── Device Settings ──
  devSettingsTitle:    _t('디바이스 설정', 'Device Settings'),
  devSettingsDesc:     _t('디바이스 속성 및 스캔 옵션 구성', 'Configure device properties and scanning options'),
  updateSettings:      _t('설정 업데이트', 'UPDATE SETTINGS'),
  ePassProperties:     _t('전자여권 속성', 'E-Passport Properties'),
  ePassPropertiesDesc: _t('데이터 그룹 및 인증 옵션 구성', 'Configure data groups and authentication options'),
  imgCtrlProperties:   _t('이미지 제어 속성', 'Image Control Properties'),
  imgCtrlDesc:         _t('이미지 캡처 및 향상 설정 구성', 'Configure image capture and enhancement settings'),
  paApiSettings:       _t('PA API 설정', 'PA API Settings'),
  paApiSettingsDesc:   _t('수동 인증 API 연결 구성', 'Configure Passive Authentication API connection'),
  paApiBaseUrl:        _t('PA API 기본 URL', 'PA API Base URL'),
  apiKey:              _t('API 키', 'API Key'),
  enterApiKey:         _t('API 키 입력', 'Enter API Key'),
  caCertificate:       _t('CA 인증서 (Private CA)', 'CA Certificate (Private CA)'),
  uploadCert:          _t('.crt / .pem 업로드', 'Upload .crt / .pem'),
  saveSettings:        _t('설정 저장', 'Save Settings'),
  testConnection:      _t('연결 테스트', 'Test Connection'),
  testing:             _t('테스트 중...', 'Testing...'),
  saving:              _t('저장 중...', 'Saving...'),
  restartService:      _t('서비스 재시작', 'RESTART SERVICE'),

  // ── Passport Cards ──
  photoComparison:     _t('사진 비교', 'Photo Comparison'),
  document:            _t('문서', 'Document'),
  chip:                _t('칩', 'Chip'),
  personalInfo:        _t('개인 정보', 'Personal Information'),
  documentType:        _t('문서 유형', 'Document Type'),
  issuingState:        _t('발급 국가', 'Issuing State'),
  documentNo:          _t('문서 번호', 'Document No.'),
  name_:               _t('이름', 'Name'),
  nationality:         _t('국적', 'Nationality'),
  dateOfBirth:         _t('생년월일', 'Date of Birth'),
  sex:                 _t('성별', 'Sex'),
  expiryDate:          _t('만료일', 'Expiry Date'),
  dgResults:           _t('데이터 그룹 결과', 'Data Group Results'),
  mrzData:             _t('MRZ 데이터', 'MRZ Data'),
  mrzLines:            _t('MRZ 라인', 'MRZ Lines'),
  line1:               _t('라인 1', 'Line 1'),
  line2:               _t('라인 2', 'Line 2'),
  line3:               _t('라인 3', 'Line 3'),

  // ── Face Verification ──
  faceVerification:    _t('얼굴 검증', 'Face Verification'),
  faceVerDesc:         _t('InsightFace를 사용하여 문서 사진과 칩 사진 비교',
                          'Compare document photo with chip photo using InsightFace'),
  verifyFace:          _t('얼굴 검증', 'Verify Face'),
  faceEmptyTitle:      _t('"얼굴 검증" 버튼을 클릭하여 사진을 비교하세요',
                          'Click "Verify Face" button to compare photos'),
  faceEmptySubtitle:   _t('검증 결과가 여기에 표시됩니다', 'Verification results will appear here'),

  // ── 디바이스 정보 ──
  deviceConnected:     _t('장치가 데스크톱에 연결되었습니다!', 'was connected on your desktop computer!!'),
};

// ============================================================
// DOM 텍스트 교체 함수
// data-i18n 속성 기반 자동 교체
// ============================================================

function applyGlobalI18n() {
  document.querySelectorAll('[data-i18n]').forEach(function(el) {
    var key = el.getAttribute('data-i18n');
    if (I18N[key] !== undefined) {
      el.textContent = I18N[key];
    }
  });

  // data-i18n-html (innerHTML 교체, strong 등 포함)
  document.querySelectorAll('[data-i18n-html]').forEach(function(el) {
    var key = el.getAttribute('data-i18n-html');
    if (I18N[key] !== undefined) {
      el.innerHTML = I18N[key];
    }
  });

  // data-i18n-placeholder
  document.querySelectorAll('[data-i18n-placeholder]').forEach(function(el) {
    var key = el.getAttribute('data-i18n-placeholder');
    if (I18N[key] !== undefined) {
      el.placeholder = I18N[key];
    }
  });

  // data-i18n-title (title/tooltip)
  document.querySelectorAll('[data-i18n-title]').forEach(function(el) {
    var key = el.getAttribute('data-i18n-title');
    if (I18N[key] !== undefined) {
      el.title = I18N[key];
    }
  });
}

// DOM 로드 시 적용
document.addEventListener('DOMContentLoaded', applyGlobalI18n);

// HTMX 콘텐츠 교체 후 적용
document.body.addEventListener('htmx:afterSwap', function() {
  setTimeout(applyGlobalI18n, 50);
});
