/**
 * PA (Passive Authentication) Verification JavaScript
 * 공통 PA 검증 함수들
 */

/**
 * PA 검증 API 호출
 */
async function verifyPassportPA() {
  const btn = document.getElementById('verify-pa-btn');
  const btnText = document.getElementById('verify-pa-btn-text');
  const btnSpinner = document.getElementById('verify-pa-btn-spinner');
  const resultContainer = document.getElementById('pa-result-container');

  // 버튼 상태 변경
  btn.disabled = true;
  btnText.classList.add('hidden');
  btnSpinner.classList.remove('hidden');

  try {
    // PA API 호출
    const response = await fetch('/fphps/passport/verify-pa', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`HTTP ${response.status}: ${errorText}`);
    }

    const paResult = await response.json();
    console.log('PA Verification Result:', paResult);

    // 결과 렌더링
    renderPAResult(paResult, resultContainer);

  } catch (error) {
    console.error('PA verification failed:', error);
    resultContainer.innerHTML = `
      <div class="rounded-md bg-red-50 p-4">
        <div class="flex">
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">PA Verification Error</h3>
            <div class="mt-2 text-sm text-red-700">
              <p>${error.message}</p>
            </div>
          </div>
        </div>
      </div>
    `;
  } finally {
    // 버튼 상태 복원
    btn.disabled = false;
    btnText.classList.remove('hidden');
    btnSpinner.classList.add('hidden');
  }
}

/**
 * PA 검증 결과 렌더링
 */
function renderPAResult(result, container) {
  const statusBadge = getStatusBadge(result.status);
  const certChainHtml = renderCertificateChain(result.certificateChainValidation);
  const sodSigHtml = renderSODSignature(result.sodSignatureValidation);
  const dgValidationHtml = renderDataGroupValidation(result.dataGroupValidation);
  const errorsHtml = renderErrors(result.errors);

  container.innerHTML = `
    <!-- 전체 상태 -->
    <div class="px-4 py-3 bg-gray-50 rounded-lg">
      ${statusBadge}
      <div class="mt-2 grid grid-cols-3 gap-4 text-sm">
        <div>
          <dt class="font-medium text-gray-500">Verification ID</dt>
          <dd class="mt-1 text-gray-900 font-mono text-xs">${result.verificationId || 'N/A'}</dd>
        </div>
        <div>
          <dt class="font-medium text-gray-500">Timestamp</dt>
          <dd class="mt-1 text-gray-900">${new Date(result.verificationTimestamp).toLocaleString()}</dd>
        </div>
        <div>
          <dt class="font-medium text-gray-500">Processing Time</dt>
          <dd class="mt-1 text-gray-900">${result.processingDurationMs} ms</dd>
        </div>
      </div>
    </div>

    ${certChainHtml}
    ${sodSigHtml}
    ${dgValidationHtml}
    ${errorsHtml}
  `;
}

/**
 * 상태 배지 생성
 */
function getStatusBadge(status) {
  const badges = {
    'VALID': '<span class="inline-flex items-center rounded-md bg-green-50 px-3 py-2 text-base font-semibold text-green-700 ring-1 ring-inset ring-green-600/20">✓ VALID</span>',
    'INVALID': '<span class="inline-flex items-center rounded-md bg-red-50 px-3 py-2 text-base font-semibold text-red-700 ring-1 ring-inset ring-red-600/20">✗ INVALID</span>',
    'ERROR': '<span class="inline-flex items-center rounded-md bg-yellow-50 px-3 py-2 text-base font-semibold text-yellow-800 ring-1 ring-inset ring-yellow-600/20">⚠ ERROR</span>'
  };
  return badges[status] || '<span class="text-gray-500">UNKNOWN</span>';
}

/**
 * CRL 상태 정보를 API 응답 기반으로 처리
 * API 응답 구조:
 * {
 *   "crlStatus": "CRL_UNAVAILABLE",
 *   "crlStatusDescription": "CRL not available in LDAP",
 *   "crlStatusDetailedDescription": "LDAP 서버에서 CRL을 조회할 수 없습니다...",
 *   "crlStatusSeverity": "WARNING",
 *   "crlMessage": "LDAP에서 해당 CSCA의 CRL을 찾을 수 없음..."
 * }
 */
function getCrlStatusInfo(crlData) {
  // crlData가 객체가 아닌 경우 (구버전 호환)
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

  // Severity에 따른 스타일 매핑
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

  // 기본 스타일 (severity가 없거나 알 수 없는 경우 status로 판단)
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

  // severity 결정 (API에서 제공되면 사용, 아니면 status로 추론)
  const effectiveSeverity = crlStatusSeverity || statusStyleMap[crlStatus] || 'INFO';
  const style = severityStyles[effectiveSeverity] || severityStyles['INFO'];

  // 라벨 결정 (API description 사용, 없으면 status 코드에서 생성)
  const label = crlStatusDescription || formatStatusLabel(crlStatus);

  // 설명 결정 (상세 설명 > crlMessage > 기본 메시지)
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

/**
 * CRL 상태 코드를 읽기 쉬운 라벨로 변환
 */
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

/**
 * 기본 설명 메시지 반환
 */
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

/**
 * 인증서 체인 검증 렌더링
 */
function renderCertificateChain(cert) {
  if (!cert) return '';
  const icon = cert.valid ? '✓' : '✗';
  const colorClass = cert.valid ? 'text-green-600' : 'text-red-600';

  // API 응답 구조에 맞게 CRL 정보 추출
  const crlInfo = getCrlStatusInfo({
    crlStatus: cert.crlStatus,
    crlStatusDescription: cert.crlStatusDescription,
    crlStatusDetailedDescription: cert.crlStatusDetailedDescription,
    crlStatusSeverity: cert.crlStatusSeverity,
    crlMessage: cert.crlMessage
  });

  // CRL 상세 정보 카드 생성
  const crlDetailHtml = renderCrlStatusCard(crlInfo, cert.revoked);

  return `
    <div class="mt-6 border-t border-gray-100 pt-4">
      <h4 class="text-lg font-medium ${colorClass} px-4">
        ${icon} Certificate Chain Validation
      </h4>
      <dl class="mt-2 divide-y divide-gray-100">
        <div class="px-4 py-2 sm:grid sm:grid-cols-3 sm:gap-4">
          <dt class="text-sm font-medium text-gray-500">DSC Subject</dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0 font-mono text-xs">${cert.dscSubject || 'N/A'}</dd>
        </div>
        <div class="px-4 py-2 sm:grid sm:grid-cols-3 sm:gap-4">
          <dt class="text-sm font-medium text-gray-500">CSCA Subject</dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0 font-mono text-xs">${cert.cscaSubject || 'N/A'}</dd>
        </div>
        <div class="px-4 py-2 sm:grid sm:grid-cols-3 sm:gap-4">
          <dt class="text-sm font-medium text-gray-500">Valid Period</dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
            ${new Date(cert.notBefore).toLocaleDateString()} - ${new Date(cert.notAfter).toLocaleDateString()}
          </dd>
        </div>
        <div class="px-4 py-2 sm:grid sm:grid-cols-3 sm:gap-4">
          <dt class="text-sm font-medium text-gray-500">CRL Status</dt>
          <dd class="mt-1 sm:col-span-2 sm:mt-0">
            ${crlDetailHtml}
          </dd>
        </div>
      </dl>
    </div>
  `;
}

/**
 * CRL 상태 카드 렌더링
 */
function renderCrlStatusCard(crlInfo, isRevoked) {
  // 상세 설명이 있으면 접기/펼치기로 표시
  const hasDetailedInfo = crlInfo.message && crlInfo.message !== crlInfo.description;

  let detailsHtml = '';
  if (hasDetailedInfo) {
    detailsHtml = `
      <details class="mt-2">
        <summary class="cursor-pointer text-xs text-gray-500 hover:text-gray-700 flex items-center gap-1">
          <svg class="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          Technical Details
        </summary>
        <div class="mt-2 p-2 bg-gray-100 rounded text-xs font-mono text-gray-700 break-all">
          ${crlInfo.message}
        </div>
      </details>
    `;
  }

  return `
    <div class="flex flex-col gap-2">
      <div class="flex items-center gap-2 flex-wrap">
        <span class="inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${crlInfo.badgeClass}">
          ${crlInfo.icon} ${crlInfo.label}
        </span>
        ${crlInfo.severity ? `<span class="text-xs text-gray-400">(${crlInfo.severity})</span>` : ''}
        ${isRevoked ? '<span class="inline-flex items-center rounded-md bg-red-100 px-2 py-1 text-xs font-medium text-red-700 ring-1 ring-inset ring-red-600/20">REVOKED</span>' : ''}
      </div>
      <p class="text-sm text-gray-600">${crlInfo.description}</p>
      ${detailsHtml}
    </div>
  `;
}

/**
 * SOD 서명 검증 렌더링
 */
function renderSODSignature(sod) {
  if (!sod) return '';
  const icon = sod.valid ? '✓' : '✗';
  const colorClass = sod.valid ? 'text-green-600' : 'text-red-600';

  return `
    <div class="mt-6 border-t border-gray-100 pt-4">
      <h4 class="text-lg font-medium ${colorClass} px-4">
        ${icon} SOD Signature Validation
      </h4>
      <dl class="mt-2 divide-y divide-gray-100">
        <div class="px-4 py-2 sm:grid sm:grid-cols-3 sm:gap-4">
          <dt class="text-sm font-medium text-gray-500">Signature Algorithm</dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">${sod.signatureAlgorithm || 'N/A'}</dd>
        </div>
        <div class="px-4 py-2 sm:grid sm:grid-cols-3 sm:gap-4">
          <dt class="text-sm font-medium text-gray-500">Hash Algorithm</dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">${sod.hashAlgorithm || 'N/A'}</dd>
        </div>
      </dl>
    </div>
  `;
}

/**
 * Data Group 검증 렌더링
 */
function renderDataGroupValidation(dg) {
  if (!dg) return '';

  let detailsHtml = '';
  if (dg.details) {
    for (const [dgName, detail] of Object.entries(dg.details)) {
      const icon = detail.valid ? '✓' : '✗';
      const colorClass = detail.valid ? 'text-green-600' : 'text-red-600';
      detailsHtml += `
        <tr>
          <td class="px-4 py-2 font-medium text-sm ${colorClass}">${icon} ${dgName}</td>
          <td class="px-4 py-2 text-sm text-gray-500 font-mono text-xs">${detail.expectedHash?.substring(0, 20)}...</td>
          <td class="px-4 py-2 text-sm text-gray-500 font-mono text-xs">${detail.actualHash?.substring(0, 20)}...</td>
        </tr>
      `;
    }
  }

  const validColor = dg.invalidGroups === 0 ? 'text-green-600' : 'text-red-600';

  return `
    <div class="mt-6 border-t border-gray-100 pt-4">
      <h4 class="text-lg font-medium ${validColor} px-4">
        Data Group Hash Validation (${dg.validGroups}/${dg.totalGroups} valid)
      </h4>
      <div class="mt-2 px-4">
        <table class="min-w-full divide-y divide-gray-200">
          <thead>
            <tr>
              <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Data Group</th>
              <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Expected Hash</th>
              <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Actual Hash</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            ${detailsHtml}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

/**
 * 에러 렌더링
 */
function renderErrors(errors) {
  if (!errors || errors.length === 0) return '';

  const errorsHtml = errors.map(err => `
    <li class="py-2">
      <span class="font-semibold">[${err.code}]</span> ${err.message}
      <span class="text-xs text-gray-500 ml-2">(${err.severity})</span>
    </li>
  `).join('');

  return `
    <div class="mt-6 border-t border-gray-100 pt-4 px-4">
      <div class="rounded-md bg-red-50 p-4">
        <h4 class="text-sm font-medium text-red-800">Validation Errors</h4>
        <ul class="mt-2 text-sm text-red-700 list-disc list-inside">
          ${errorsHtml}
        </ul>
      </div>
    </div>
  `;
}
