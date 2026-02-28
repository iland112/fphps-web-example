/**
 * PA (Passive Authentication) Verification JavaScript
 * 공통 PA 검증 함수들
 */

/**
 * PA API 서버 연결 상태 확인
 * PA 탭 선택 시 호출되어 연결 상태를 배너로 표시
 * @param {string} prefix - 'manual' 또는 'auto'
 */
function checkPaApiHealth(prefix) {
  const bannerId = prefix + '-pa-connection-banner';
  const existingBanner = document.getElementById(bannerId);

  // 이전 배너 제거
  if (existingBanner) {
    existingBanner.remove();
  }

  const wrapper = document.querySelector('#content-pa .pa-tab-wrapper');
  if (!wrapper) return;

  // 체크 중 배너
  const banner = document.createElement('div');
  banner.id = bannerId;
  banner.className = 'flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm bg-gray-50 dark:bg-neutral-700/50 border border-gray-200 dark:border-neutral-700 text-gray-500 dark:text-neutral-400';
  banner.innerHTML = `
    <svg class="animate-spin size-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
    <span>Checking PA API server connection...</span>
  `;
  wrapper.insertBefore(banner, wrapper.firstChild);

  fetch('/passport/pa-health')
    .then(resp => resp.json())
    .then(data => {
      const b = document.getElementById(bannerId);
      if (!b) return;

      if (data.connected) {
        b.className = 'flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300';
        b.innerHTML = `
          <svg class="size-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <span>PA API server connected</span>
        `;
        // 성공 시 3초 후 fade out
        setTimeout(() => {
          if (b) {
            b.style.transition = 'opacity 0.5s';
            b.style.opacity = '0';
            setTimeout(() => b.remove(), 500);
          }
        }, 3000);
      } else {
        b.className = 'flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300';
        b.innerHTML = `
          <svg class="size-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"/>
          </svg>
          <span>PA API server is not reachable. Verify PA and PA Lookup features are unavailable.</span>
        `;
      }
    })
    .catch(() => {
      const b = document.getElementById(bannerId);
      if (!b) return;
      b.className = 'flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300';
      b.innerHTML = `
        <svg class="size-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"/>
        </svg>
        <span>PA API server is not reachable. Verify PA and PA Lookup features are unavailable.</span>
      `;
    });
}

/**
 * HTML 특수문자 이스케이프 (XSS 방지)
 * @param {string} text - 이스케이프할 텍스트
 * @returns {string} - 이스케이프된 텍스트
 */
function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

/**
 * PA 검증 API 호출 (API Gateway)
 * @param {string} btnId - 버튼 ID
 * @param {string} containerId - 결과 컨테이너 ID
 * @param {string} emptyStateId - 빈 상태 요소 ID
 */
async function verifyPassportPA(btnId, containerId, emptyStateId) {
  const btn = document.getElementById(btnId);
  const btnSpinner = document.getElementById(btnId + '-spinner');
  const resultContainer = document.getElementById(containerId);
  const emptyState = document.getElementById(emptyStateId);

  // 버튼 상태 변경
  btn.disabled = true;
  if (btnSpinner) btnSpinner.classList.remove('hidden');

  // 빈 상태 숨기기
  if (emptyState) emptyState.classList.add('hidden');

  try {
    // PA API 호출 (API Gateway)
    const response = await fetch('/passport/verify-pa-v2', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });

    // JSON으로 파싱 시도
    let data;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      // JSON이 아닌 경우 텍스트로 읽어서 에러 처리
      const errorText = await response.text();
      throw new Error(`Unexpected response: ${errorText.substring(0, 200)}`);
    }

    // 에러 응답 체크 (서버에서 JSON 에러 응답을 반환한 경우)
    if (!response.ok || data.error === true || data.status === 'FAILURE') {
      const errorMessage = data.message || `HTTP ${response.status} error`;
      throw new Error(errorMessage);
    }

    console.log('PA Verification Result:', data);

    // 응답은 {paResult, mrzData, faceImageBase64} 구조
    renderPAResultV2(data, resultContainer);

  } catch (error) {
    console.error('PA verification failed:', error);
    resultContainer.innerHTML = renderErrorCard('PA Verification Error', error.message);
  } finally {
    // 버튼 상태 복원
    btn.disabled = false;
    if (btnSpinner) btnSpinner.classList.add('hidden');
  }
}

/**
 * 에러 카드 렌더링
 * @param {string} title - 에러 제목
 * @param {string} message - 에러 메시지
 * @param {string} type - 'error' | 'warning' | 'info' (기본: 'error')
 */
function renderErrorCard(title, message, type = 'error') {
  // "No passport data available" 메시지는 안내 메시지로 처리
  const isNoDataMessage = message && message.toLowerCase().includes('no passport data available');
  const effectiveType = isNoDataMessage ? 'info' : type;

  const styles = {
    error: {
      container: 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800',
      iconBg: 'bg-red-100 dark:bg-red-900/30',
      iconColor: 'text-red-600 dark:text-red-400',
      titleColor: 'text-red-800 dark:text-red-300',
      textColor: 'text-red-700 dark:text-red-300',
      icon: '<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/>'
    },
    warning: {
      container: 'bg-amber-50 dark:bg-amber-900/20 border-amber-200 dark:border-amber-800',
      iconBg: 'bg-amber-100 dark:bg-amber-900/30',
      iconColor: 'text-amber-600 dark:text-amber-400',
      titleColor: 'text-amber-800 dark:text-amber-300',
      textColor: 'text-amber-700 dark:text-amber-300',
      icon: '<path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>'
    },
    info: {
      container: 'bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-800',
      iconBg: 'bg-blue-100 dark:bg-blue-900/30',
      iconColor: 'text-blue-600 dark:text-blue-400',
      titleColor: 'text-blue-800 dark:text-blue-300',
      textColor: 'text-blue-700 dark:text-blue-300',
      icon: '<path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z" clip-rule="evenodd"/>'
    }
  };

  const style = styles[effectiveType] || styles.error;

  // 안내 메시지인 경우 더 자세한 설명 추가
  let displayMessage = message;
  let helpText = '';

  if (isNoDataMessage) {
    displayMessage = 'Please read a passport first before running PA verification.';
    helpText = `
      <div class="mt-4 flex items-center gap-2 text-sm ${style.textColor}">
        <svg class="size-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <span>Use the <strong>Manual Read</strong> or <strong>Auto Read</strong> function to scan a passport.</span>
      </div>
    `;
  }

  return `
    <div class="rounded-xl ${style.container} border p-6">
      <div class="flex items-start gap-4">
        <div class="flex-shrink-0">
          <div class="flex size-12 items-center justify-center rounded-xl ${style.iconBg}">
            <svg class="size-6 ${style.iconColor}" viewBox="0 0 20 20" fill="currentColor">
              ${style.icon}
            </svg>
          </div>
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="text-base font-semibold ${style.titleColor}">${isNoDataMessage ? 'Passport Data Required' : title}</h3>
          <p class="mt-2 text-sm ${style.textColor}">${displayMessage}</p>
          ${helpText}
        </div>
      </div>
    </div>
  `;
}

/**
 * PA 검증 결과 렌더링 - Grid Layout (MRZ/Face 포함)
 */
function renderPAResultV2(data, container) {
  const result = data.paResult;
  const mrzData = data.mrzData;
  const faceImageBase64 = data.faceImageBase64;

  const statusCard = renderStatusCard(result);
  const certChainCard = renderCertificateChainCard(result.certificateChainValidation);
  const sodSigCard = renderSODSignatureCard(result.sodSignatureValidation);
  const dgValidationCard = renderDataGroupValidationCard(result.dataGroupValidation);
  const dgParsedCard = renderDGParsedDataCard(mrzData, faceImageBase64);
  const errorsCard = renderErrorsCard(result.errors);

  container.innerHTML = `
    <div class="space-y-6">
      <!-- 상태 요약 카드 -->
      ${statusCard}

      <!-- 2열 그리드: Certificate Chain + SOD Signature -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        ${certChainCard}
        ${sodSigCard}
      </div>

      <!-- Data Group Hash Validation -->
      ${dgValidationCard}

      <!-- DG1/DG2 파싱 결과 -->
      ${dgParsedCard}

      <!-- 에러 목록 -->
      ${errorsCard}
    </div>
  `;
}

/**
 * 상태 요약 카드
 */
function renderStatusCard(result) {
  const statusConfig = getStatusConfig(result.status);

  return `
    <div class="rounded-xl ${statusConfig.bgGradient} p-6 shadow-sm dark:shadow-neutral-900/30">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="flex size-14 items-center justify-center rounded-xl ${statusConfig.iconBg}">
            ${statusConfig.icon}
          </div>
          <div>
            <h3 class="text-xl font-bold ${statusConfig.textColor}">${result.status}</h3>
            <p class="text-sm ${statusConfig.subtextColor}">Passive Authentication Result</p>
          </div>
        </div>
        <div class="text-right">
          <p class="text-xs ${statusConfig.subtextColor}">Processing Time</p>
          <p class="text-lg font-semibold ${statusConfig.textColor}">${result.processingDurationMs || 0} ms</p>
        </div>
      </div>

      <div class="mt-4 pt-4 border-t ${statusConfig.borderColor}">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
          <div>
            <dt class="font-medium ${statusConfig.subtextColor}">Verification ID</dt>
            <dd class="${statusConfig.textColor} font-mono text-xs mt-1 truncate" title="${result.verificationId || 'N/A'}">${result.verificationId ? result.verificationId.substring(0, 8) + '...' : 'N/A'}</dd>
          </div>
          <div>
            <dt class="font-medium ${statusConfig.subtextColor}">Country</dt>
            <dd class="${statusConfig.textColor} mt-1">${result.issuingCountry || 'N/A'}</dd>
          </div>
          <div>
            <dt class="font-medium ${statusConfig.subtextColor}">Document No.</dt>
            <dd class="${statusConfig.textColor} font-mono mt-1">${result.documentNumber || 'N/A'}</dd>
          </div>
          <div>
            <dt class="font-medium ${statusConfig.subtextColor}">Timestamp</dt>
            <dd class="${statusConfig.textColor} mt-1 text-xs">${result.verificationTimestamp ? new Date(result.verificationTimestamp).toLocaleString() : 'N/A'}</dd>
          </div>
        </div>
      </div>
    </div>
  `;
}

/**
 * 상태별 설정 반환
 */
function getStatusConfig(status) {
  const configs = {
    'VALID': {
      bgGradient: 'bg-gradient-to-r from-green-50 to-emerald-50 dark:from-green-900/20 dark:to-emerald-900/20 border border-green-200 dark:border-green-800',
      iconBg: 'bg-green-100 dark:bg-green-900/30',
      icon: '<svg class="size-7 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>',
      textColor: 'text-green-800 dark:text-green-300',
      subtextColor: 'text-green-600 dark:text-green-400',
      borderColor: 'border-green-200 dark:border-green-800'
    },
    'INVALID': {
      bgGradient: 'bg-gradient-to-r from-red-50 to-rose-50 dark:from-red-900/20 dark:to-rose-900/20 border border-red-200 dark:border-red-800',
      iconBg: 'bg-red-100 dark:bg-red-900/30',
      icon: '<svg class="size-7 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>',
      textColor: 'text-red-800 dark:text-red-300',
      subtextColor: 'text-red-600 dark:text-red-400',
      borderColor: 'border-red-200 dark:border-red-800'
    },
    'ERROR': {
      bgGradient: 'bg-gradient-to-r from-yellow-50 to-amber-50 dark:from-yellow-900/20 dark:to-amber-900/20 border border-yellow-200 dark:border-yellow-800',
      iconBg: 'bg-yellow-100 dark:bg-yellow-900/30',
      icon: '<svg class="size-7 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>',
      textColor: 'text-yellow-800 dark:text-yellow-300',
      subtextColor: 'text-yellow-600 dark:text-yellow-400',
      borderColor: 'border-yellow-200 dark:border-yellow-800'
    }
  };
  return configs[status] || configs['ERROR'];
}

/**
 * 인증서 체인 검증 카드
 */
function renderCertificateChainCard(cert) {
  if (!cert) return '';

  const isValid = cert.valid;
  const statusColor = isValid ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400';
  const statusBg = isValid ? 'bg-green-100 dark:bg-green-900/30' : 'bg-red-100 dark:bg-red-900/30';
  const statusIcon = isValid
    ? '<svg class="size-5 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>'
    : '<svg class="size-5 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>';

  // CRL 정보 추출
  const crlInfo = getCrlStatusInfo({
    crlStatus: cert.crlStatus,
    crlStatusDescription: cert.crlStatusDescription,
    crlStatusDetailedDescription: cert.crlStatusDetailedDescription,
    crlStatusSeverity: cert.crlStatusSeverity,
    crlMessage: cert.crlMessage
  });

  // v1.2.0 Certificate Expiration 정보
  const expirationInfo = getExpirationStatusInfo(cert);

  return `
    <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
      <!-- 헤더 -->
      <div class="px-5 py-4 border-b border-gray-100 dark:border-neutral-700 bg-gray-50 dark:bg-neutral-700/50">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg ${statusBg}">
              ${statusIcon}
            </div>
            <h4 class="font-semibold text-gray-900 dark:text-neutral-100">Certificate Chain</h4>
          </div>
          <span class="text-xs font-medium ${statusColor} uppercase">${isValid ? 'Valid' : 'Invalid'}</span>
        </div>
      </div>

      <!-- 본문 -->
      <div class="p-5 space-y-4">
        <!-- DSC -->
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">DSC Subject</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono break-all">${cert.dscSubject || 'N/A'}</dd>
        </div>

        <!-- CSCA -->
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">CSCA Subject</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono break-all">${cert.cscaSubject || 'N/A'}</dd>
        </div>

        <!-- CRL 유효기간 & CRL 상태 -->
        <div class="grid grid-cols-2 gap-4">
          <div>
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">CRL Valid Period</dt>
            <dd class="mt-1 text-sm ${cert.crlNextUpdate && new Date(cert.crlNextUpdate) < new Date() ? 'text-red-600 dark:text-red-400 font-semibold' : 'text-gray-900 dark:text-neutral-100'}">
              ${cert.crlThisUpdate ? new Date(cert.crlThisUpdate).toLocaleDateString() : 'N/A'} -
              ${cert.crlNextUpdate ? new Date(cert.crlNextUpdate).toLocaleDateString() : 'N/A'}
            </dd>
          </div>
          <div>
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">CRL Status</dt>
            <dd class="mt-1">
              <span class="inline-flex items-center gap-1 rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${crlInfo.badgeClass}">
                ${crlInfo.icon} ${crlInfo.label}
              </span>
            </dd>
          </div>
        </div>

        ${crlInfo.description ? `
        <div class="p-3 rounded-lg ${crlInfo.bgClass} border">
          <p class="text-xs text-gray-700 dark:text-neutral-300">${crlInfo.description}</p>
        </div>
        ` : ''}

        <!-- v1.2.0 Certificate Expiration Status -->
        ${expirationInfo.show ? `
        <div class="p-3 rounded-lg ${expirationInfo.bgClass} border ${expirationInfo.borderClass}">
          <div class="flex items-start gap-3">
            <div class="flex-shrink-0">${expirationInfo.icon}</div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="text-sm font-semibold ${expirationInfo.titleColor}">${expirationInfo.title}</span>
                ${cert.dscExpired ? '<span class="inline-flex items-center rounded-md bg-red-50 dark:bg-red-900/20 px-1.5 py-0.5 text-xs font-medium text-red-700 dark:text-red-300 ring-1 ring-inset ring-red-600/20">DSC Expired</span>' : ''}
                ${cert.cscaExpired ? '<span class="inline-flex items-center rounded-md bg-red-50 dark:bg-red-900/20 px-1.5 py-0.5 text-xs font-medium text-red-700 dark:text-red-300 ring-1 ring-inset ring-red-600/20">CSCA Expired</span>' : ''}
                ${cert.validAtSigningTime ? '<span class="inline-flex items-center rounded-md bg-blue-50 dark:bg-blue-900/20 px-1.5 py-0.5 text-xs font-medium text-blue-700 dark:text-blue-300 ring-1 ring-inset ring-blue-600/20">Valid at Signing</span>' : ''}
              </div>
              ${cert.expirationMessage ? `<p class="mt-1 text-xs ${expirationInfo.textColor}">${cert.expirationMessage}</p>` : ''}
            </div>
          </div>
        </div>
        ` : ''}
      </div>
    </div>
  `;
}

/**
 * v1.2.0 Certificate Expiration 상태 정보 추출
 */
function getExpirationStatusInfo(cert) {
  // 새 필드가 없으면 표시하지 않음
  if (cert.expirationStatus === undefined && cert.dscExpired === undefined) {
    return { show: false };
  }

  const status = cert.expirationStatus || 'VALID';

  const statusConfig = {
    VALID: {
      show: false  // 유효한 경우 추가 표시 불필요
    },
    WARNING: {
      show: true,
      bgClass: 'bg-amber-50 dark:bg-amber-900/20',
      borderClass: 'border-amber-200 dark:border-amber-800',
      titleColor: 'text-amber-800 dark:text-amber-300',
      textColor: 'text-amber-700 dark:text-amber-300',
      title: 'Certificate Expiration Warning',
      icon: '<svg class="size-5 text-amber-600 dark:text-amber-400" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/></svg>'
    },
    EXPIRED: {
      show: true,
      bgClass: 'bg-red-50 dark:bg-red-900/20',
      borderClass: 'border-red-200 dark:border-red-800',
      titleColor: 'text-red-800 dark:text-red-300',
      textColor: 'text-red-700 dark:text-red-300',
      title: 'Certificate Expired',
      icon: '<svg class="size-5 text-red-600 dark:text-red-400" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>'
    }
  };

  // 만료되었지만 서명 당시 유효했던 경우 특별 표시
  if (status === 'EXPIRED' && cert.validAtSigningTime) {
    return {
      show: true,
      bgClass: 'bg-blue-50 dark:bg-blue-900/20',
      borderClass: 'border-blue-200 dark:border-blue-800',
      titleColor: 'text-blue-800 dark:text-blue-300',
      textColor: 'text-blue-700 dark:text-blue-300',
      title: 'Certificate Expired (Valid at Signing Time)',
      icon: '<svg class="size-5 text-blue-600 dark:text-blue-400" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z" clip-rule="evenodd"/></svg>'
    };
  }

  return statusConfig[status] || { show: false };
}

/**
 * SOD 서명 검증 카드
 */
function renderSODSignatureCard(sod) {
  if (!sod) return '';

  const isValid = sod.valid;
  const statusColor = isValid ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400';
  const statusBg = isValid ? 'bg-green-100 dark:bg-green-900/30' : 'bg-red-100 dark:bg-red-900/30';
  const statusIcon = isValid
    ? '<svg class="size-5 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>'
    : '<svg class="size-5 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>';

  return `
    <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
      <!-- 헤더 -->
      <div class="px-5 py-4 border-b border-gray-100 dark:border-neutral-700 bg-gray-50 dark:bg-neutral-700/50">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg ${statusBg}">
              ${statusIcon}
            </div>
            <h4 class="font-semibold text-gray-900 dark:text-neutral-100">SOD Signature</h4>
          </div>
          <span class="text-xs font-medium ${statusColor} uppercase">${isValid ? 'Valid' : 'Invalid'}</span>
        </div>
      </div>

      <!-- 본문 -->
      <div class="p-5 space-y-4">
        <div class="grid grid-cols-1 gap-4">
          <div>
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">Signature Algorithm</dt>
            <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono">${sod.signatureAlgorithm || 'N/A'}</dd>
          </div>
          <div>
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">Hash Algorithm</dt>
            <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono">${sod.hashAlgorithm || 'N/A'}</dd>
          </div>
        </div>

        <!-- 시그니처 검증 상태 표시 -->
        <div class="p-3 rounded-lg ${isValid ? 'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800' : 'bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800'}">
          <div class="flex items-center gap-2">
            ${statusIcon}
            <span class="text-sm font-medium ${statusColor}">
              ${isValid ? 'Digital signature verified successfully' : 'Signature verification failed'}
            </span>
          </div>
        </div>
      </div>
    </div>
  `;
}

/**
 * Data Group 검증 카드
 */
function renderDataGroupValidationCard(dg) {
  if (!dg) return '';

  const allValid = dg.invalidGroups === 0;
  const statusColor = allValid ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400';
  const statusBg = allValid ? 'bg-green-100 dark:bg-green-900/30' : 'bg-red-100 dark:bg-red-900/30';

  let detailsHtml = '';
  if (dg.details) {
    const entries = Object.entries(dg.details);
    detailsHtml = entries.map(([dgName, detail]) => {
      const isValid = detail.valid;
      const icon = isValid
        ? '<svg class="size-4 text-green-500 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>'
        : '<svg class="size-4 text-red-500 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>';
      const rowBg = isValid ? '' : 'bg-red-50 dark:bg-red-900/20';

      return `
        <tr class="${rowBg}">
          <td class="px-4 py-3 whitespace-nowrap">
            <div class="flex items-center gap-2">
              ${icon}
              <span class="font-medium text-gray-900 dark:text-neutral-100">${dgName}</span>
            </div>
          </td>
          <td class="px-4 py-3">
            <code class="text-xs text-gray-600 dark:text-neutral-400 bg-gray-100 dark:bg-neutral-700 px-2 py-1 rounded break-all">${detail.expectedHash || 'N/A'}</code>
          </td>
          <td class="px-4 py-3">
            <code class="text-xs text-gray-600 dark:text-neutral-400 bg-gray-100 dark:bg-neutral-700 px-2 py-1 rounded break-all">${detail.actualHash || 'N/A'}</code>
          </td>
          <td class="px-4 py-3 text-right">
            <span class="inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${isValid ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300'}">
              ${isValid ? 'Match' : 'Mismatch'}
            </span>
          </td>
        </tr>
      `;
    }).join('');
  }

  return `
    <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
      <!-- 헤더 -->
      <div class="px-5 py-4 border-b border-gray-100 dark:border-neutral-700 bg-gray-50 dark:bg-neutral-700/50">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg ${statusBg}">
              <svg class="size-5 ${statusColor}" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
              </svg>
            </div>
            <div>
              <h4 class="font-semibold text-gray-900 dark:text-neutral-100">Data Group Hash Validation</h4>
              <p class="text-xs text-gray-500 dark:text-neutral-400">${dg.validGroups}/${dg.totalGroups} groups verified</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-2xl font-bold ${statusColor}">${dg.validGroups}</span>
            <span class="text-gray-400 dark:text-neutral-500">/</span>
            <span class="text-lg text-gray-500 dark:text-neutral-400">${dg.totalGroups}</span>
          </div>
        </div>
      </div>

      <!-- 테이블 -->
      <div class="overflow-x-auto p-4">
        <table class="w-full divide-y divide-gray-200 dark:divide-neutral-700 border border-gray-200 dark:border-neutral-700 rounded-lg overflow-hidden">
          <thead class="bg-gray-50 dark:bg-neutral-700/50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Data Group</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Expected Hash</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Actual Hash</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Status</th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-neutral-800 divide-y divide-gray-200 dark:divide-neutral-700">
            ${detailsHtml}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

/**
 * DG1/DG2 파싱 결과 카드
 * @param {Object} mrzData - MRZ 데이터 객체
 * @param {string} faceImage - Face 이미지 Base64 data URI
 */
function renderDGParsedDataCard(mrzData, faceImage) {
  return `
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- DG1: MRZ 정보 -->
      <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30">
        <div class="px-5 py-4 border-b border-gray-100 dark:border-neutral-700 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-t-xl">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg bg-blue-100 dark:bg-blue-900/30">
              <svg class="size-5 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V8a2 2 0 00-2-2h-5m-4 0V5a2 2 0 114 0v1m-4 0a2 2 0 104 0m-5 8a2 2 0 100-4 2 2 0 000 4zm0 0c1.306 0 2.417.835 2.83 2M9 14a3.001 3.001 0 00-2.83 2M15 11h3m-3 4h2"/>
              </svg>
            </div>
            <div>
              <h4 class="font-semibold text-gray-900 dark:text-neutral-100">DG1 - MRZ Data</h4>
              <p class="text-xs text-gray-500 dark:text-neutral-400">Machine Readable Zone</p>
            </div>
          </div>
        </div>

        <div class="p-5" id="dg1-mrz-content">
          ${mrzData ? renderMrzContent(mrzData) : renderMrzPlaceholder()}
        </div>
      </div>

      <!-- DG2: Face Image -->
      <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30">
        <div class="px-5 py-4 border-b border-gray-100 dark:border-neutral-700 bg-gradient-to-r from-purple-50 to-pink-50 dark:from-purple-900/20 dark:to-pink-900/20 rounded-t-xl">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg bg-purple-100 dark:bg-purple-900/30">
              <svg class="size-5 text-purple-600 dark:text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
              </svg>
            </div>
            <div>
              <h4 class="font-semibold text-gray-900 dark:text-neutral-100">DG2 - Face Image</h4>
              <p class="text-xs text-gray-500 dark:text-neutral-400">Facial Biometric Data</p>
            </div>
          </div>
        </div>

        <div class="p-5" id="dg2-face-content">
          ${faceImage ? renderFaceImage(faceImage) : renderFacePlaceholder()}
        </div>
      </div>
    </div>
  `;
}

/**
 * MRZ 콘텐츠 렌더링
 */
function renderMrzContent(mrz) {
  // 이름 포맷팅: "SURNAME<<GIVENNAME" 형식을 "Surname, Givenname"으로 변환
  let formattedName = mrz.name || 'N/A';
  if (formattedName && formattedName.includes('<')) {
    const nameParts = formattedName.split('<<');
    if (nameParts.length >= 2) {
      const surname = nameParts[0].replace(/</g, ' ').trim();
      const givenName = nameParts[1].replace(/</g, ' ').trim();
      formattedName = `${surname}, ${givenName}`;
    }
  }

  return `
    <div class="space-y-3">
      <div class="grid grid-cols-2 gap-3">
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400">Document Type</dt>
          <dd class="mt-1 text-sm font-semibold text-gray-900 dark:text-neutral-100">${mrz.documentType || 'P'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400">Issuing Country</dt>
          <dd class="mt-1 text-sm font-semibold text-gray-900 dark:text-neutral-100">${mrz.issuingState || 'N/A'}</dd>
        </div>
      </div>
      <div>
        <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400">Full Name</dt>
        <dd class="mt-1 text-sm font-semibold text-gray-900 dark:text-neutral-100">${formattedName}</dd>
      </div>
      <div class="grid grid-cols-2 gap-3">
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400">Passport No.</dt>
          <dd class="mt-1 text-sm font-mono text-gray-900 dark:text-neutral-100">${mrz.passportNumber || 'N/A'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400">Nationality</dt>
          <dd class="mt-1 text-sm font-semibold text-gray-900 dark:text-neutral-100">${mrz.nationality || 'N/A'}</dd>
        </div>
      </div>
      <div class="grid grid-cols-3 gap-3">
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400">Date of Birth</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${mrz.dateOfBirth || 'N/A'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400">Sex</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${mrz.sex || 'N/A'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400">Expiry Date</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${mrz.expiryDate || 'N/A'}</dd>
        </div>
      </div>
      ${mrz.mrzLine1 || mrz.mrzLine2 ? `
      <div class="mt-3 pt-3 border-t border-gray-100 dark:border-neutral-700">
        <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 mb-2">MRZ Lines</dt>
        <div class="overflow-x-auto">
          <dd class="font-mono text-sm text-gray-700 dark:text-neutral-300 bg-gray-50 dark:bg-neutral-700/50 p-2 rounded whitespace-nowrap" style="min-width: max-content;">
            ${mrz.mrzLine1 ? `<div>${escapeHtml(mrz.mrzLine1)}</div>` : ''}
            ${mrz.mrzLine2 ? `<div>${escapeHtml(mrz.mrzLine2)}</div>` : ''}
          </dd>
        </div>
      </div>
      ` : ''}
    </div>
  `;
}

/**
 * MRZ 플레이스홀더
 */
function renderMrzPlaceholder() {
  return `
    <div class="flex flex-col items-center justify-center py-8 text-center">
      <svg class="size-12 text-gray-300 dark:text-neutral-600 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M10 6H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V8a2 2 0 00-2-2h-5m-4 0V5a2 2 0 114 0v1m-4 0a2 2 0 104 0m-5 8a2 2 0 100-4 2 2 0 000 4zm0 0c1.306 0 2.417.835 2.83 2M9 14a3.001 3.001 0 00-2.83 2M15 11h3m-3 4h2"/>
      </svg>
      <p class="text-sm text-gray-500 dark:text-neutral-400">MRZ data available in passport read results</p>
      <p class="text-xs text-gray-400 dark:text-neutral-500 mt-1">Check the MRZ tab for detailed information</p>
    </div>
  `;
}

/**
 * Face 이미지 렌더링
 */
function renderFaceImage(imageData) {
  return `
    <div class="flex justify-center">
      <div class="relative">
        <img src="${imageData}" alt="Face Image" class="rounded-lg shadow-md max-h-48 object-contain"/>
        <div class="absolute bottom-2 right-2 bg-black/50 text-white text-xs px-2 py-1 rounded">
          DG2 Biometric
        </div>
      </div>
    </div>
  `;
}

/**
 * Face 플레이스홀더
 */
function renderFacePlaceholder() {
  return `
    <div class="flex flex-col items-center justify-center py-8 text-center">
      <div class="size-24 rounded-full bg-gray-100 dark:bg-neutral-700 flex items-center justify-center mb-3">
        <svg class="size-12 text-gray-300 dark:text-neutral-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
        </svg>
      </div>
      <p class="text-sm text-gray-500 dark:text-neutral-400">Face image available in passport read results</p>
      <p class="text-xs text-gray-400 dark:text-neutral-500 mt-1">Check the Face tab for biometric data</p>
    </div>
  `;
}

/**
 * 에러 카드 렌더링
 */
function renderErrorsCard(errors) {
  if (!errors || errors.length === 0) return '';

  const errorsHtml = errors.map(err => `
    <div class="flex items-start gap-3 p-3 bg-red-50 dark:bg-red-900/20 rounded-lg">
      <div class="flex-shrink-0">
        <svg class="size-5 text-red-500 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
      </div>
      <div class="flex-1 min-w-0">
        <p class="text-sm font-medium text-red-800 dark:text-red-300">[${err.code}] ${err.message}</p>
        <p class="text-xs text-red-600 dark:text-red-400 mt-1">Severity: ${err.severity}</p>
      </div>
    </div>
  `).join('');

  return `
    <div class="rounded-xl bg-white dark:bg-neutral-800 border border-red-200 dark:border-red-800 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
      <div class="px-5 py-4 border-b border-red-100 dark:border-red-800 bg-red-50 dark:bg-red-900/20">
        <div class="flex items-center gap-3">
          <div class="flex size-9 items-center justify-center rounded-lg bg-red-100 dark:bg-red-900/30">
            <svg class="size-5 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
            </svg>
          </div>
          <h4 class="font-semibold text-red-800 dark:text-red-300">Validation Errors (${errors.length})</h4>
        </div>
      </div>
      <div class="p-5 space-y-3">
        ${errorsHtml}
      </div>
    </div>
  `;
}

/**
 * CRL 상태 정보를 API 응답 기반으로 처리
 */
function getCrlStatusInfo(crlData) {
  if (typeof crlData === 'string') {
    crlData = { crlStatus: crlData };
  }

  const {
    crlStatus,
    crlStatusDescription,
    crlStatusDetailedDescription,
    crlStatusSeverity,
    crlMessage
  } = crlData || {};

  const severityStyles = {
    'SUCCESS': {
      badgeClass: 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 ring-green-600/20',
      icon: '✓',
      bgClass: 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800'
    },
    'ERROR': {
      badgeClass: 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 ring-red-600/20',
      icon: '✗',
      bgClass: 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800'
    },
    'WARNING': {
      badgeClass: 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300 ring-yellow-600/20',
      icon: '⚠',
      bgClass: 'bg-yellow-50 dark:bg-yellow-900/20 border-yellow-200 dark:border-yellow-800'
    },
    'INFO': {
      badgeClass: 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 ring-blue-600/20',
      icon: 'ⓘ',
      bgClass: 'bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-800'
    }
  };

  const statusStyleMap = {
    'CRL_VALID': 'SUCCESS',
    'CRL_REVOKED': 'ERROR',
    'CRL_UNAVAILABLE': 'WARNING',
    'CRL_NOT_FOUND': 'WARNING',
    'CRL_EXPIRED': 'WARNING',
    'CRL_PARSE_ERROR': 'ERROR',
    'CRL_SIGNATURE_INVALID': 'ERROR',
    'COUNTRY_NOT_SUPPORTED': 'INFO',
    'CRL_CHECK_SKIPPED': 'INFO'
  };

  const effectiveSeverity = crlStatusSeverity || statusStyleMap[crlStatus] || 'INFO';
  const style = severityStyles[effectiveSeverity] || severityStyles['INFO'];

  const label = crlStatusDescription || formatStatusLabel(crlStatus);
  const description = crlStatusDetailedDescription || crlMessage || getDefaultDescription(crlStatus);

  return {
    status: crlStatus,
    label: label,
    description: description,
    detailedDescription: crlStatusDetailedDescription,
    message: crlMessage,
    severity: effectiveSeverity,
    badgeClass: style.badgeClass,
    bgClass: style.bgClass,
    icon: style.icon
  };
}

function formatStatusLabel(status) {
  if (!status) return 'Unknown';

  const labelMap = {
    'CRL_VALID': 'Valid',
    'CRL_REVOKED': 'Revoked',
    'CRL_UNAVAILABLE': 'CRL Unavailable',
    'CRL_NOT_FOUND': 'CRL Not Found',
    'CRL_EXPIRED': 'CRL Expired',
    'CRL_PARSE_ERROR': 'Parse Error',
    'CRL_SIGNATURE_INVALID': 'Signature Invalid',
    'COUNTRY_NOT_SUPPORTED': 'Not Supported',
    'CRL_CHECK_SKIPPED': 'Skipped'
  };

  return labelMap[status] || status.replace(/_/g, ' ');
}

function getDefaultDescription(status) {
  const descriptions = {
    'CRL_VALID': 'Certificate is not on the revocation list',
    'CRL_REVOKED': 'Certificate has been revoked by the issuing authority',
    'CRL_UNAVAILABLE': 'CRL data is not available',
    'CRL_NOT_FOUND': 'No CRL entry found for this certificate',
    'CRL_EXPIRED': 'The CRL data has expired',
    'CRL_PARSE_ERROR': 'Failed to parse the CRL data',
    'CRL_SIGNATURE_INVALID': 'CRL signature verification failed',
    'COUNTRY_NOT_SUPPORTED': 'CRL verification not available for this country',
    'CRL_CHECK_SKIPPED': 'CRL verification was skipped'
  };

  return descriptions[status] || 'Status information not available';
}

// ============================================================
// PA Lookup (간편 조회) - DSC Subject DN / Fingerprint 기반
// ============================================================

/**
 * PA Lookup 날짜 파싱 헬퍼
 * API 날짜 형식: "2020-03-13 08:05:47+00" (비표준 - space 구분)
 * @param {string} dateStr - API 날짜 문자열
 * @returns {Date|null} - 파싱된 Date 객체 또는 null
 */
function parseLookupDate(dateStr) {
  if (!dateStr) return null;
  let normalized = dateStr.replace(/(\d{4}-\d{2}-\d{2})\s+(\d{2}:\d{2}:\d{2})/, '$1T$2');
  normalized = normalized.replace(/([+-]\d{2})$/, '$1:00');
  const date = new Date(normalized);
  return isNaN(date.getTime()) ? null : date;
}

/**
 * 날짜 범위로 유효기간 판단 (클라이언트 사이드)
 * Lookup 모드에서 validityPeriodValid가 항상 false이므로 날짜로 직접 판단
 */
function deriveValidityFromDates(notBefore, notAfter) {
  const now = new Date();
  const start = parseLookupDate(notBefore);
  const end = parseLookupDate(notAfter);
  if (!start || !end) return { valid: null, label: 'N/A' };
  const isValid = now >= start && now <= end;
  return { valid: isValid, label: isValid ? 'Currently Valid' : 'Expired' };
}

/**
 * PA Lookup API 호출
 * @param {string} btnId - 버튼 ID
 * @param {string} containerId - 결과 컨테이너 ID
 * @param {string} emptyStateId - 빈 상태 요소 ID
 */
async function paLookup(btnId, containerId, emptyStateId) {
  const btn = document.getElementById(btnId);
  const btnSpinner = document.getElementById(btnId + '-spinner');
  const resultContainer = document.getElementById(containerId);
  const emptyState = document.getElementById(emptyStateId);

  // 버튼 상태 변경
  btn.disabled = true;
  if (btnSpinner) btnSpinner.classList.remove('hidden');

  // 빈 상태 숨기기
  if (emptyState) emptyState.classList.add('hidden');

  try {
    const response = await fetch('/passport/pa-lookup', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });

    let data;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      const errorText = await response.text();
      throw new Error(`Unexpected response: ${errorText.substring(0, 200)}`);
    }

    if (!response.ok) {
      const errorMessage = data.message || `HTTP ${response.status} error`;
      throw new Error(errorMessage);
    }

    console.log('PA Lookup Result:', data);
    renderPaLookupResult(data, resultContainer);

  } catch (error) {
    console.error('PA Lookup failed:', error);
    resultContainer.innerHTML = renderErrorCard('PA Lookup Error', error.message);
  } finally {
    btn.disabled = false;
    if (btnSpinner) btnSpinner.classList.add('hidden');
  }
}

/**
 * PA Lookup 결과 렌더링
 * @param {Object} data - PaLookupResponse
 * @param {HTMLElement} container - 결과 컨테이너
 */
function renderPaLookupResult(data, container) {
  const validation = data.validation;

  // validation이 null이면 DSC를 PKD에서 찾지 못함
  if (!validation) {
    container.innerHTML = renderLookupNotFoundCard(data.message);
    return;
  }

  const statusCard = renderLookupStatusCard(validation);
  const certInfoCard = renderLookupCertInfoCard(validation, data);
  const trustChainCard = renderLookupTrustChainCard(validation);
  const revocationCard = renderLookupRevocationCard(validation);
  const infoNote = renderLookupInfoNote();

  container.innerHTML = `
    <div class="space-y-6">
      ${statusCard}
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        ${certInfoCard}
        ${trustChainCard}
      </div>
      ${revocationCard}
      ${infoNote}
    </div>
  `;
}

/**
 * PA Lookup - DSC를 PKD에서 찾지 못한 경우
 */
function renderLookupNotFoundCard(message) {
  return `
    <div class="rounded-xl bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 p-6">
      <div class="flex items-start gap-4">
        <div class="flex-shrink-0">
          <div class="flex size-12 items-center justify-center rounded-xl bg-blue-100 dark:bg-blue-900/30">
            <svg class="size-6 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
            </svg>
          </div>
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="text-base font-semibold text-blue-800 dark:text-blue-300">DSC Not Found in PKD</h3>
          <p class="mt-2 text-sm text-blue-700 dark:text-blue-300">
            ${message || 'The DSC certificate from this passport was not found in the Public Key Directory.'}</p>
          <p class="mt-2 text-xs text-blue-600 dark:text-blue-400">This may occur when the issuing country has not submitted their certificates to the ICAO PKD, or the certificate has not yet been imported.</p>
        </div>
      </div>
    </div>
  `;
}

/**
 * PA Lookup - 상태 요약 카드
 */
function renderLookupStatusCard(v) {
  const statusConfigs = {
    'VALID': {
      bgGradient: 'bg-gradient-to-r from-green-50 to-emerald-50 dark:from-green-900/20 dark:to-emerald-900/20 border border-green-200 dark:border-green-800',
      iconBg: 'bg-green-100 dark:bg-green-900/30',
      icon: '<svg class="size-7 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>',
      textColor: 'text-green-800 dark:text-green-300',
      subtextColor: 'text-green-600 dark:text-green-400',
      borderColor: 'border-green-200 dark:border-green-800',
      label: 'Trust Chain Valid'
    },
    'EXPIRED_VALID': {
      bgGradient: 'bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 border border-blue-200 dark:border-blue-800',
      iconBg: 'bg-blue-100 dark:bg-blue-900/30',
      icon: '<svg class="size-7 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>',
      textColor: 'text-blue-800 dark:text-blue-300',
      subtextColor: 'text-blue-600 dark:text-blue-400',
      borderColor: 'border-blue-200 dark:border-blue-800',
      label: 'Expired but Valid at Signing'
    },
    'INVALID': {
      bgGradient: 'bg-gradient-to-r from-red-50 to-rose-50 dark:from-red-900/20 dark:to-rose-900/20 border border-red-200 dark:border-red-800',
      iconBg: 'bg-red-100 dark:bg-red-900/30',
      icon: '<svg class="size-7 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>',
      textColor: 'text-red-800 dark:text-red-300',
      subtextColor: 'text-red-600 dark:text-red-400',
      borderColor: 'border-red-200 dark:border-red-800',
      label: 'Trust Chain Invalid'
    },
    'PENDING': {
      bgGradient: 'bg-gradient-to-r from-yellow-50 to-amber-50 dark:from-yellow-900/20 dark:to-amber-900/20 border border-yellow-200 dark:border-yellow-800',
      iconBg: 'bg-yellow-100 dark:bg-yellow-900/30',
      icon: '<svg class="size-7 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>',
      textColor: 'text-yellow-800 dark:text-yellow-300',
      subtextColor: 'text-yellow-600 dark:text-yellow-400',
      borderColor: 'border-yellow-200 dark:border-yellow-800',
      label: 'Pending Validation'
    },
    'ERROR': {
      bgGradient: 'bg-gradient-to-r from-gray-50 to-slate-50 dark:from-neutral-700/50 dark:to-neutral-700/30 border border-gray-200 dark:border-neutral-700',
      iconBg: 'bg-gray-100 dark:bg-neutral-700',
      icon: '<svg class="size-7 text-gray-600 dark:text-neutral-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>',
      textColor: 'text-gray-800 dark:text-neutral-200',
      subtextColor: 'text-gray-600 dark:text-neutral-400',
      borderColor: 'border-gray-200 dark:border-neutral-700',
      label: 'Validation Error'
    }
  };

  const config = statusConfigs[v.validationStatus] || statusConfigs['ERROR'];

  return `
    <div class="rounded-xl ${config.bgGradient} p-6 shadow-sm dark:shadow-neutral-900/30">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="flex size-14 items-center justify-center rounded-xl ${config.iconBg}">
            ${config.icon}
          </div>
          <div>
            <h3 class="text-xl font-bold ${config.textColor}">${v.validationStatus}</h3>
            <p class="text-sm ${config.subtextColor}">${config.label} - PA Lookup</p>
          </div>
        </div>
        <div class="text-right">
          <p class="text-xs ${config.subtextColor}">Country</p>
          <p class="text-lg font-semibold ${config.textColor}">${v.countryCode || 'N/A'}</p>
        </div>
      </div>

      <div class="mt-4 pt-4 border-t ${config.borderColor}">
        <div class="grid grid-cols-2 md:grid-cols-3 gap-4 text-sm">
          <div>
            <dt class="font-medium ${config.subtextColor}">Certificate Type</dt>
            <dd class="${config.textColor} mt-1">${v.certificateType || 'N/A'}</dd>
          </div>
          <div>
            <dt class="font-medium ${config.subtextColor}">Signature Algorithm</dt>
            <dd class="font-mono mt-1 text-xs">${v.signatureAlgorithm
              ? `<span class="${config.textColor}">${v.signatureAlgorithm}</span>`
              : '<span class="text-gray-400 dark:text-neutral-500 italic font-sans">Not Checked</span>'}</dd>
          </div>
          <div>
            <dt class="font-medium ${config.subtextColor}">Validated At</dt>
            <dd class="${config.textColor} mt-1 text-xs">${v.validatedAt
              ? (parseLookupDate(v.validatedAt) || new Date(v.validatedAt)).toLocaleString()
              : 'N/A'}</dd>
          </div>
        </div>
      </div>
    </div>
  `;
}

/**
 * PA Lookup - Certificate Info 카드
 */
function renderLookupCertInfoCard(v, data) {
  // Lookup 모드: validityPeriodValid가 항상 false → 날짜로 직접 판단
  const validity = deriveValidityFromDates(v.notBefore, v.notAfter);
  const periodColor = validity.valid === null ? 'text-gray-500 dark:text-neutral-400'
    : validity.valid ? 'text-green-700 dark:text-green-300' : 'text-red-600 dark:text-red-400 font-semibold';
  const periodBadge = validity.valid === null ? ''
    : validity.valid
      ? '<span class="ml-2 inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300">Valid</span>'
      : '<span class="ml-2 inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300">Expired</span>';
  const notBeforeDate = parseLookupDate(v.notBefore);
  const notAfterDate = parseLookupDate(v.notAfter);

  // Fingerprint: API가 null 반환 시 요청에 사용한 값 표시
  const fingerprint = v.fingerprintSha256 || (data && data.requestFingerprint) || null;

  return `
    <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
      <div class="px-5 py-4 border-b border-gray-100 dark:border-neutral-700 bg-gradient-to-r from-indigo-50 to-purple-50 dark:from-indigo-900/20 dark:to-purple-900/20">
        <div class="flex items-center gap-3">
          <div class="flex size-9 items-center justify-center rounded-lg bg-indigo-100 dark:bg-indigo-900/30">
            <svg class="size-5 text-indigo-600 dark:text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 9h3.75M15 12h3.75M15 15h3.75M4.5 19.5h15a2.25 2.25 0 002.25-2.25V6.75A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25v10.5A2.25 2.25 0 004.5 19.5zm6-10.125a1.875 1.875 0 11-3.75 0 1.875 1.875 0 013.75 0zm1.294 6.336a6.721 6.721 0 01-3.17.789 6.721 6.721 0 01-3.168-.789 3.376 3.376 0 016.338 0z"/>
            </svg>
          </div>
          <h4 class="font-semibold text-gray-900 dark:text-neutral-100">Certificate Info</h4>
        </div>
      </div>

      <div class="p-5 space-y-4">
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">Subject DN</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono break-all">${v.subjectDn || 'N/A'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">Issuer DN</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono break-all">${v.issuerDn || 'N/A'}</dd>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">Serial Number</dt>
            <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono break-all">${v.serialNumber || 'N/A'}</dd>
          </div>
          <div>
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">Validity Period</dt>
            <dd class="mt-1 text-sm ${periodColor}">
              ${notBeforeDate ? notBeforeDate.toLocaleDateString() : 'N/A'} -
              ${notAfterDate ? notAfterDate.toLocaleDateString() : 'N/A'}${periodBadge}
            </dd>
          </div>
        </div>
        ${fingerprint ? `
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">SHA-256 Fingerprint</dt>
          <dd class="mt-1 text-xs text-gray-700 dark:text-neutral-300 font-mono break-all">${fingerprint}</dd>
        </div>
        ` : ''}
      </div>
    </div>
  `;
}

/**
 * PA Lookup - Trust Chain 카드
 */
function renderLookupTrustChainCard(v) {
  const isChainValid = v.trustChainValid;
  const chainStatusBg = isChainValid ? 'bg-green-100 dark:bg-green-900/30' : 'bg-red-100 dark:bg-red-900/30';
  const chainStatusIcon = isChainValid
    ? '<svg class="size-5 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>'
    : '<svg class="size-5 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>';
  const chainStatusColor = isChainValid ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400';

  // Lookup 모드: signatureAlgorithm이 null이면 서명 검증 미수행
  const isSignatureChecked = v.signatureAlgorithm != null;

  return `
    <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
      <div class="px-5 py-4 border-b border-gray-100 dark:border-neutral-700 bg-gray-50 dark:bg-neutral-700/50">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg ${chainStatusBg}">
              ${chainStatusIcon}
            </div>
            <h4 class="font-semibold text-gray-900 dark:text-neutral-100">Trust Chain</h4>
          </div>
          <span class="text-xs font-medium ${chainStatusColor} uppercase">${isChainValid ? 'Valid' : 'Invalid'}</span>
        </div>
      </div>

      <div class="p-5 space-y-4">
        ${v.trustChainMessage ? `
        <div class="p-3 rounded-lg ${isChainValid ? 'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800' : 'bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800'}">
          <p class="text-sm ${isChainValid ? 'text-green-700 dark:text-green-300' : 'text-red-700 dark:text-red-300'}">${v.trustChainMessage}</p>
        </div>
        ` : ''}

        ${v.trustChainPath ? `
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">Trust Chain Path</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono break-all">${v.trustChainPath}</dd>
        </div>
        ` : ''}

        <div class="grid grid-cols-2 gap-4">
          <div>
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">CSCA Found</dt>
            <dd class="mt-1">
              <span class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${v.cscaFound ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300'}">
                ${v.cscaFound ? 'Yes' : 'No'}
              </span>
            </dd>
          </div>
          <div>
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">Signature Valid</dt>
            <dd class="mt-1">
              ${isSignatureChecked
                ? `<span class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${v.signatureValid ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300'}">${v.signatureValid ? 'Yes' : 'No'}</span>`
                : '<span class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium bg-gray-100 dark:bg-neutral-700 text-gray-500 dark:text-neutral-400">Not Checked</span>'
              }
            </dd>
          </div>
        </div>

        ${v.cscaSubjectDn ? `
        <div>
          <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wide">CSCA Subject DN</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono break-all">${v.cscaSubjectDn}</dd>
        </div>
        ` : ''}
      </div>
    </div>
  `;
}

/**
 * PA Lookup - Revocation Status 카드
 */
function renderLookupRevocationCard(v) {
  // null → not_checked (Lookup 모드에서는 revocation 검증 미수행)
  const effectiveStatus = v.revocationStatus || 'not_checked';

  const revocationConfigs = {
    'not_revoked': {
      bgClass: 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800',
      textColor: 'text-green-700 dark:text-green-300',
      iconColor: 'text-green-600 dark:text-green-400',
      label: 'Not Revoked',
      icon: '<svg class="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>'
    },
    'revoked': {
      bgClass: 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800',
      textColor: 'text-red-700 dark:text-red-300',
      iconColor: 'text-red-600 dark:text-red-400',
      label: 'Certificate Revoked',
      icon: '<svg class="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/></svg>'
    },
    'unknown': {
      bgClass: 'bg-yellow-50 dark:bg-yellow-900/20 border-yellow-200 dark:border-yellow-800',
      textColor: 'text-yellow-700 dark:text-yellow-300',
      iconColor: 'text-yellow-600 dark:text-yellow-400',
      label: 'Revocation Status Unknown',
      icon: '<svg class="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>'
    },
    'not_checked': {
      bgClass: 'bg-gray-50 dark:bg-neutral-700/50 border-gray-200 dark:border-neutral-700',
      textColor: 'text-gray-600 dark:text-neutral-400',
      iconColor: 'text-gray-400 dark:text-neutral-500',
      label: 'Not Checked',
      icon: '<svg class="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"/></svg>'
    }
  };

  const config = revocationConfigs[effectiveStatus] || revocationConfigs['not_checked'];

  return `
    <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
      <div class="px-5 py-4 border-b border-gray-100 dark:border-neutral-700 bg-gray-50 dark:bg-neutral-700/50">
        <div class="flex items-center gap-3">
          <div class="flex size-9 items-center justify-center rounded-lg bg-gray-100 dark:bg-neutral-700">
            <svg class="size-5 text-gray-600 dark:text-neutral-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
            </svg>
          </div>
          <h4 class="font-semibold text-gray-900 dark:text-neutral-100">Revocation Status</h4>
        </div>
      </div>

      <div class="p-5">
        <div class="flex items-center gap-4 p-4 rounded-lg border ${config.bgClass}">
          <div class="${config.iconColor}">${config.icon}</div>
          <div class="flex-1">
            <p class="text-sm font-semibold ${config.textColor}">${config.label}</p>
            <p class="text-xs text-gray-500 dark:text-neutral-400 mt-1">CRL Checked: ${v.crlChecked ? 'Yes' : 'No'}</p>
          </div>
          <span class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${effectiveStatus === 'not_checked' ? 'bg-gray-100 dark:bg-neutral-700 text-gray-500 dark:text-neutral-400' : v.crlChecked ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' : 'bg-gray-100 dark:bg-neutral-700 text-gray-700 dark:text-neutral-300'}">
            ${effectiveStatus === 'not_checked' ? 'NOT_CHECKED' : (v.revocationStatus || 'unknown')}
          </span>
        </div>
      </div>
    </div>
  `;
}

/**
 * PA Lookup - 간편 검증 안내 노트
 */
function renderLookupInfoNote() {
  return `
    <div class="rounded-xl bg-cyan-50 dark:bg-cyan-900/20 border border-cyan-200 dark:border-cyan-800 p-5">
      <div class="flex items-start gap-3">
        <div class="flex-shrink-0 mt-0.5">
          <svg class="size-5 text-cyan-600 dark:text-cyan-400" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z" clip-rule="evenodd"/>
          </svg>
        </div>
        <div class="flex-1 min-w-0">
          <h4 class="text-sm font-semibold text-cyan-800 dark:text-cyan-300">간편 검증 안내 (PA Lookup)</h4>
          <p class="mt-1 text-xs text-cyan-700 dark:text-cyan-300 leading-relaxed">
            PA Lookup은 DSC(Document Signer Certificate)의 Subject DN 또는 SHA-256 Fingerprint를 기반으로
            PKD에 등록된 Trust Chain 검증 결과를 조회합니다.
            SOD 서명 검증 및 Data Group 해시 검증은 전체 검증(<strong>Verify PA</strong>)에서만 수행됩니다.
          </p>
        </div>
      </div>
    </div>
  `;
}
