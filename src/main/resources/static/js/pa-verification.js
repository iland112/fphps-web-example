/**
 * PA (Passive Authentication) Verification JavaScript
 * 공통 PA 검증 함수들
 */

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
 * PA 검증 API 호출 (V1 - 기존 API)
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
    // PA API 호출 (V1)
    const response = await fetch('/passport/verify-pa', {
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

    console.log('PA Verification Result (V1):', data);

    // 결과 렌더링
    renderPAResult(data, resultContainer);

  } catch (error) {
    console.error('PA verification failed (V1):', error);
    resultContainer.innerHTML = renderErrorCard('PA Verification Error (V1)', error.message);
  } finally {
    // 버튼 상태 복원
    btn.disabled = false;
    if (btnSpinner) btnSpinner.classList.add('hidden');
  }
}

/**
 * PA 검증 API 호출 (V2 - 새로운 API Gateway)
 * @param {string} btnId - 버튼 ID
 * @param {string} containerId - 결과 컨테이너 ID
 * @param {string} emptyStateId - 빈 상태 요소 ID
 */
async function verifyPassportPAV2(btnId, containerId, emptyStateId) {
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
    // PA API 호출 (V2 - API Gateway)
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

    console.log('PA Verification Result (V2 - API Gateway):', data);

    // V2 응답은 {paResult, mrzData, faceImageBase64} 구조
    // paResult를 메인 결과로 전달하고, mrzData와 faceImage는 별도로 전달
    renderPAResultV2(data, resultContainer);

  } catch (error) {
    console.error('PA verification failed (V2):', error);
    resultContainer.innerHTML = renderErrorCard('PA Verification Error (V2)', error.message);
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
      container: 'bg-red-50 border-red-200',
      iconBg: 'bg-red-100',
      iconColor: 'text-red-600',
      titleColor: 'text-red-800',
      textColor: 'text-red-700',
      icon: '<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/>'
    },
    warning: {
      container: 'bg-amber-50 border-amber-200',
      iconBg: 'bg-amber-100',
      iconColor: 'text-amber-600',
      titleColor: 'text-amber-800',
      textColor: 'text-amber-700',
      icon: '<path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>'
    },
    info: {
      container: 'bg-blue-50 border-blue-200',
      iconBg: 'bg-blue-100',
      iconColor: 'text-blue-600',
      titleColor: 'text-blue-800',
      textColor: 'text-blue-700',
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
 * PA 검증 결과 렌더링 - Grid Layout (V1 API용)
 */
function renderPAResult(result, container) {
  const statusCard = renderStatusCard(result);
  const certChainCard = renderCertificateChainCard(result.certificateChainValidation);
  const sodSigCard = renderSODSignatureCard(result.sodSignatureValidation);
  const dgValidationCard = renderDataGroupValidationCard(result.dataGroupValidation);
  const dgParsedCard = renderDGParsedDataCard(null, null); // V1은 MRZ/Face 데이터 없음
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
 * PA 검증 결과 렌더링 - Grid Layout (V2 API용 - MRZ/Face 포함)
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
    <div class="rounded-xl ${statusConfig.bgGradient} p-6 shadow-sm">
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
      bgGradient: 'bg-gradient-to-r from-green-50 to-emerald-50 border border-green-200',
      iconBg: 'bg-green-100',
      icon: '<svg class="size-7 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>',
      textColor: 'text-green-800',
      subtextColor: 'text-green-600',
      borderColor: 'border-green-200'
    },
    'INVALID': {
      bgGradient: 'bg-gradient-to-r from-red-50 to-rose-50 border border-red-200',
      iconBg: 'bg-red-100',
      icon: '<svg class="size-7 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>',
      textColor: 'text-red-800',
      subtextColor: 'text-red-600',
      borderColor: 'border-red-200'
    },
    'ERROR': {
      bgGradient: 'bg-gradient-to-r from-yellow-50 to-amber-50 border border-yellow-200',
      iconBg: 'bg-yellow-100',
      icon: '<svg class="size-7 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>',
      textColor: 'text-yellow-800',
      subtextColor: 'text-yellow-600',
      borderColor: 'border-yellow-200'
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
  const statusColor = isValid ? 'text-green-600' : 'text-red-600';
  const statusBg = isValid ? 'bg-green-100' : 'bg-red-100';
  const statusIcon = isValid
    ? '<svg class="size-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>'
    : '<svg class="size-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>';

  // CRL 정보 추출
  const crlInfo = getCrlStatusInfo({
    crlStatus: cert.crlStatus,
    crlStatusDescription: cert.crlStatusDescription,
    crlStatusDetailedDescription: cert.crlStatusDetailedDescription,
    crlStatusSeverity: cert.crlStatusSeverity,
    crlMessage: cert.crlMessage
  });

  return `
    <div class="rounded-xl bg-white border border-gray-200 shadow-sm overflow-hidden">
      <!-- 헤더 -->
      <div class="px-5 py-4 border-b border-gray-100 bg-gray-50">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg ${statusBg}">
              ${statusIcon}
            </div>
            <h4 class="font-semibold text-gray-900">Certificate Chain</h4>
          </div>
          <span class="text-xs font-medium ${statusColor} uppercase">${isValid ? 'Valid' : 'Invalid'}</span>
        </div>
      </div>

      <!-- 본문 -->
      <div class="p-5 space-y-4">
        <!-- DSC -->
        <div>
          <dt class="text-xs font-medium text-gray-500 uppercase tracking-wide">DSC Subject</dt>
          <dd class="mt-1 text-sm text-gray-900 font-mono break-all">${cert.dscSubject || 'N/A'}</dd>
        </div>

        <!-- CSCA -->
        <div>
          <dt class="text-xs font-medium text-gray-500 uppercase tracking-wide">CSCA Subject</dt>
          <dd class="mt-1 text-sm text-gray-900 font-mono break-all">${cert.cscaSubject || 'N/A'}</dd>
        </div>

        <!-- 유효기간 & CRL 상태 -->
        <div class="grid grid-cols-2 gap-4">
          <div>
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wide">Valid Period</dt>
            <dd class="mt-1 text-sm text-gray-900">
              ${cert.notBefore ? new Date(cert.notBefore).toLocaleDateString() : 'N/A'} -
              ${cert.notAfter ? new Date(cert.notAfter).toLocaleDateString() : 'N/A'}
            </dd>
          </div>
          <div>
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wide">CRL Status</dt>
            <dd class="mt-1">
              <span class="inline-flex items-center gap-1 rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${crlInfo.badgeClass}">
                ${crlInfo.icon} ${crlInfo.label}
              </span>
            </dd>
          </div>
        </div>

        ${crlInfo.description ? `
        <div class="p-3 rounded-lg ${crlInfo.bgClass} border">
          <p class="text-xs text-gray-700">${crlInfo.description}</p>
        </div>
        ` : ''}
      </div>
    </div>
  `;
}

/**
 * SOD 서명 검증 카드
 */
function renderSODSignatureCard(sod) {
  if (!sod) return '';

  const isValid = sod.valid;
  const statusColor = isValid ? 'text-green-600' : 'text-red-600';
  const statusBg = isValid ? 'bg-green-100' : 'bg-red-100';
  const statusIcon = isValid
    ? '<svg class="size-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>'
    : '<svg class="size-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>';

  return `
    <div class="rounded-xl bg-white border border-gray-200 shadow-sm overflow-hidden">
      <!-- 헤더 -->
      <div class="px-5 py-4 border-b border-gray-100 bg-gray-50">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg ${statusBg}">
              ${statusIcon}
            </div>
            <h4 class="font-semibold text-gray-900">SOD Signature</h4>
          </div>
          <span class="text-xs font-medium ${statusColor} uppercase">${isValid ? 'Valid' : 'Invalid'}</span>
        </div>
      </div>

      <!-- 본문 -->
      <div class="p-5 space-y-4">
        <div class="grid grid-cols-1 gap-4">
          <div>
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wide">Signature Algorithm</dt>
            <dd class="mt-1 text-sm text-gray-900 font-mono">${sod.signatureAlgorithm || 'N/A'}</dd>
          </div>
          <div>
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wide">Hash Algorithm</dt>
            <dd class="mt-1 text-sm text-gray-900 font-mono">${sod.hashAlgorithm || 'N/A'}</dd>
          </div>
        </div>

        <!-- 시그니처 검증 상태 표시 -->
        <div class="p-3 rounded-lg ${isValid ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'}">
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
  const statusColor = allValid ? 'text-green-600' : 'text-red-600';
  const statusBg = allValid ? 'bg-green-100' : 'bg-red-100';

  let detailsHtml = '';
  if (dg.details) {
    const entries = Object.entries(dg.details);
    detailsHtml = entries.map(([dgName, detail]) => {
      const isValid = detail.valid;
      const icon = isValid
        ? '<svg class="size-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>'
        : '<svg class="size-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>';
      const rowBg = isValid ? '' : 'bg-red-50';

      return `
        <tr class="${rowBg}">
          <td class="px-4 py-3 whitespace-nowrap">
            <div class="flex items-center gap-2">
              ${icon}
              <span class="font-medium text-gray-900">${dgName}</span>
            </div>
          </td>
          <td class="px-4 py-3">
            <code class="text-xs text-gray-600 bg-gray-100 px-2 py-1 rounded break-all">${detail.expectedHash || 'N/A'}</code>
          </td>
          <td class="px-4 py-3">
            <code class="text-xs text-gray-600 bg-gray-100 px-2 py-1 rounded break-all">${detail.actualHash || 'N/A'}</code>
          </td>
          <td class="px-4 py-3 text-right">
            <span class="inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${isValid ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}">
              ${isValid ? 'Match' : 'Mismatch'}
            </span>
          </td>
        </tr>
      `;
    }).join('');
  }

  return `
    <div class="rounded-xl bg-white border border-gray-200 shadow-sm overflow-hidden">
      <!-- 헤더 -->
      <div class="px-5 py-4 border-b border-gray-100 bg-gray-50">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg ${statusBg}">
              <svg class="size-5 ${statusColor}" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
              </svg>
            </div>
            <div>
              <h4 class="font-semibold text-gray-900">Data Group Hash Validation</h4>
              <p class="text-xs text-gray-500">${dg.validGroups}/${dg.totalGroups} groups verified</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-2xl font-bold ${statusColor}">${dg.validGroups}</span>
            <span class="text-gray-400">/</span>
            <span class="text-lg text-gray-500">${dg.totalGroups}</span>
          </div>
        </div>
      </div>

      <!-- 테이블 -->
      <div class="overflow-x-auto p-4">
        <table class="w-full divide-y divide-gray-200 border border-gray-200 rounded-lg overflow-hidden">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Data Group</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Expected Hash</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actual Hash</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
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
      <div class="rounded-xl bg-white border border-gray-200 shadow-sm">
        <div class="px-5 py-4 border-b border-gray-100 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-t-xl">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg bg-blue-100">
              <svg class="size-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V8a2 2 0 00-2-2h-5m-4 0V5a2 2 0 114 0v1m-4 0a2 2 0 104 0m-5 8a2 2 0 100-4 2 2 0 000 4zm0 0c1.306 0 2.417.835 2.83 2M9 14a3.001 3.001 0 00-2.83 2M15 11h3m-3 4h2"/>
              </svg>
            </div>
            <div>
              <h4 class="font-semibold text-gray-900">DG1 - MRZ Data</h4>
              <p class="text-xs text-gray-500">Machine Readable Zone</p>
            </div>
          </div>
        </div>

        <div class="p-5" id="dg1-mrz-content">
          ${mrzData ? renderMrzContent(mrzData) : renderMrzPlaceholder()}
        </div>
      </div>

      <!-- DG2: Face Image -->
      <div class="rounded-xl bg-white border border-gray-200 shadow-sm">
        <div class="px-5 py-4 border-b border-gray-100 bg-gradient-to-r from-purple-50 to-pink-50 rounded-t-xl">
          <div class="flex items-center gap-3">
            <div class="flex size-9 items-center justify-center rounded-lg bg-purple-100">
              <svg class="size-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
              </svg>
            </div>
            <div>
              <h4 class="font-semibold text-gray-900">DG2 - Face Image</h4>
              <p class="text-xs text-gray-500">Facial Biometric Data</p>
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
          <dt class="text-xs font-medium text-gray-500">Document Type</dt>
          <dd class="mt-1 text-sm font-semibold text-gray-900">${mrz.documentType || 'P'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-gray-500">Issuing Country</dt>
          <dd class="mt-1 text-sm font-semibold text-gray-900">${mrz.issuingState || 'N/A'}</dd>
        </div>
      </div>
      <div>
        <dt class="text-xs font-medium text-gray-500">Full Name</dt>
        <dd class="mt-1 text-sm font-semibold text-gray-900">${formattedName}</dd>
      </div>
      <div class="grid grid-cols-2 gap-3">
        <div>
          <dt class="text-xs font-medium text-gray-500">Passport No.</dt>
          <dd class="mt-1 text-sm font-mono text-gray-900">${mrz.passportNumber || 'N/A'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-gray-500">Nationality</dt>
          <dd class="mt-1 text-sm font-semibold text-gray-900">${mrz.nationality || 'N/A'}</dd>
        </div>
      </div>
      <div class="grid grid-cols-3 gap-3">
        <div>
          <dt class="text-xs font-medium text-gray-500">Date of Birth</dt>
          <dd class="mt-1 text-sm text-gray-900">${mrz.dateOfBirth || 'N/A'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-gray-500">Sex</dt>
          <dd class="mt-1 text-sm text-gray-900">${mrz.sex || 'N/A'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-gray-500">Expiry Date</dt>
          <dd class="mt-1 text-sm text-gray-900">${mrz.expiryDate || 'N/A'}</dd>
        </div>
      </div>
      ${mrz.mrzLine1 || mrz.mrzLine2 ? `
      <div class="mt-3 pt-3 border-t border-gray-100">
        <dt class="text-xs font-medium text-gray-500 mb-2">MRZ Lines</dt>
        <div class="overflow-x-auto">
          <dd class="font-mono text-xs text-gray-700 bg-gray-50 p-2 rounded whitespace-nowrap" style="min-width: max-content;">
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
      <svg class="size-12 text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M10 6H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V8a2 2 0 00-2-2h-5m-4 0V5a2 2 0 114 0v1m-4 0a2 2 0 104 0m-5 8a2 2 0 100-4 2 2 0 000 4zm0 0c1.306 0 2.417.835 2.83 2M9 14a3.001 3.001 0 00-2.83 2M15 11h3m-3 4h2"/>
      </svg>
      <p class="text-sm text-gray-500">MRZ data available in passport read results</p>
      <p class="text-xs text-gray-400 mt-1">Check the MRZ tab for detailed information</p>
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
      <div class="size-24 rounded-full bg-gray-100 flex items-center justify-center mb-3">
        <svg class="size-12 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
        </svg>
      </div>
      <p class="text-sm text-gray-500">Face image available in passport read results</p>
      <p class="text-xs text-gray-400 mt-1">Check the Face tab for biometric data</p>
    </div>
  `;
}

/**
 * 에러 카드 렌더링
 */
function renderErrorsCard(errors) {
  if (!errors || errors.length === 0) return '';

  const errorsHtml = errors.map(err => `
    <div class="flex items-start gap-3 p-3 bg-red-50 rounded-lg">
      <div class="flex-shrink-0">
        <svg class="size-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
      </div>
      <div class="flex-1 min-w-0">
        <p class="text-sm font-medium text-red-800">[${err.code}] ${err.message}</p>
        <p class="text-xs text-red-600 mt-1">Severity: ${err.severity}</p>
      </div>
    </div>
  `).join('');

  return `
    <div class="rounded-xl bg-white border border-red-200 shadow-sm overflow-hidden">
      <div class="px-5 py-4 border-b border-red-100 bg-red-50">
        <div class="flex items-center gap-3">
          <div class="flex size-9 items-center justify-center rounded-lg bg-red-100">
            <svg class="size-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
            </svg>
          </div>
          <h4 class="font-semibold text-red-800">Validation Errors (${errors.length})</h4>
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
      badgeClass: 'bg-green-100 text-green-800 ring-green-600/20',
      icon: '✓',
      bgClass: 'bg-green-50 border-green-200'
    },
    'ERROR': {
      badgeClass: 'bg-red-100 text-red-800 ring-red-600/20',
      icon: '✗',
      bgClass: 'bg-red-50 border-red-200'
    },
    'WARNING': {
      badgeClass: 'bg-yellow-100 text-yellow-800 ring-yellow-600/20',
      icon: '⚠',
      bgClass: 'bg-yellow-50 border-yellow-200'
    },
    'INFO': {
      badgeClass: 'bg-blue-100 text-blue-800 ring-blue-600/20',
      icon: 'ⓘ',
      bgClass: 'bg-blue-50 border-blue-200'
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
