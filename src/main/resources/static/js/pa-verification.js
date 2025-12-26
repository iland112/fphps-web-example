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
 * CRL 상태 코드를 사람이 읽을 수 있는 설명으로 변환
 */
function getCrlStatusInfo(crlStatus, crlMessage) {
  const statusMap = {
    'CRL_VALID': {
      label: 'Valid',
      description: 'Certificate is not on the revocation list',
      badgeClass: 'bg-green-100 text-green-800 ring-green-600/20',
      icon: '✓'
    },
    'CRL_REVOKED': {
      label: 'Revoked',
      description: 'Certificate has been revoked by the issuing authority',
      badgeClass: 'bg-red-100 text-red-800 ring-red-600/20',
      icon: '✗'
    },
    'CRL_UNAVAILABLE': {
      label: 'CRL Unavailable',
      description: 'CRL data is not available for this issuing country',
      badgeClass: 'bg-yellow-100 text-yellow-800 ring-yellow-600/20',
      icon: '⚠'
    },
    'CRL_NOT_FOUND': {
      label: 'CRL Not Found',
      description: 'No CRL entry found for this DSC certificate',
      badgeClass: 'bg-yellow-100 text-yellow-800 ring-yellow-600/20',
      icon: '⚠'
    },
    'CRL_EXPIRED': {
      label: 'CRL Expired',
      description: 'The CRL data has expired and needs to be updated',
      badgeClass: 'bg-orange-100 text-orange-800 ring-orange-600/20',
      icon: '⚠'
    },
    'CRL_PARSE_ERROR': {
      label: 'CRL Parse Error',
      description: 'Failed to parse the CRL data',
      badgeClass: 'bg-red-100 text-red-800 ring-red-600/20',
      icon: '✗'
    },
    'CRL_SIGNATURE_INVALID': {
      label: 'CRL Signature Invalid',
      description: 'The CRL signature verification failed',
      badgeClass: 'bg-red-100 text-red-800 ring-red-600/20',
      icon: '✗'
    },
    'COUNTRY_NOT_SUPPORTED': {
      label: 'Country Not Supported',
      description: 'CRL verification is not available for this issuing country',
      badgeClass: 'bg-gray-100 text-gray-800 ring-gray-600/20',
      icon: 'ⓘ'
    },
    'CRL_CHECK_SKIPPED': {
      label: 'Check Skipped',
      description: 'CRL verification was skipped (not enabled or not required)',
      badgeClass: 'bg-gray-100 text-gray-800 ring-gray-600/20',
      icon: '−'
    }
  };

  const info = statusMap[crlStatus] || {
    label: crlStatus || 'Unknown',
    description: crlMessage || 'Status information not available',
    badgeClass: 'bg-gray-100 text-gray-800 ring-gray-600/20',
    icon: '?'
  };

  // crlMessage가 있으면 description을 오버라이드
  if (crlMessage && crlMessage.trim() !== '') {
    info.description = crlMessage;
  }

  return info;
}

/**
 * 인증서 체인 검증 렌더링
 */
function renderCertificateChain(cert) {
  if (!cert) return '';
  const icon = cert.valid ? '✓' : '✗';
  const colorClass = cert.valid ? 'text-green-600' : 'text-red-600';
  const crlInfo = getCrlStatusInfo(cert.crlStatus, cert.crlMessage);

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
            <div class="flex flex-col gap-1">
              <div class="flex items-center gap-2">
                <span class="inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${crlInfo.badgeClass}">
                  ${crlInfo.icon} ${crlInfo.label}
                </span>
                ${cert.revoked ? '<span class="inline-flex items-center rounded-md bg-red-100 px-2 py-1 text-xs font-medium text-red-700 ring-1 ring-inset ring-red-600/20">REVOKED</span>' : ''}
              </div>
              <p class="text-sm text-gray-600">${crlInfo.description}</p>
            </div>
          </dd>
        </div>
      </dl>
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
