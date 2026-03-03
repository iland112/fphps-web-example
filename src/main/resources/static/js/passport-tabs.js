/**
 * Passport Tabs - Shared JavaScript for SOD and PA rendering
 * Used by both epassport_manual_read.html and epassport_auto_read.html
 */

// ============================================
// Passive Authentication Functions
// ============================================

/**
 * Verify Passport Passive Authentication
 * @param {string} btnId - ID of the verify button
 * @param {string} containerId - ID of the result container
 * @param {string} emptyStateId - ID of the empty state element
 */
async function verifyPassportPA(btnId, containerId, emptyStateId) {
  const btn = document.getElementById(btnId);
  const btnText = document.getElementById(btnId + '-text');
  const btnSpinner = document.getElementById(btnId + '-spinner');
  const container = document.getElementById(containerId);

  // Disable button and show loading
  btn.disabled = true;
  if (btnText) btnText.classList.add('hidden');
  if (btnSpinner) btnSpinner.classList.remove('hidden');

  try {
    const response = await fetch('/passport/verify-pa', {
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

    // Render results
    renderPAResult(paResult, container);

  } catch (error) {
    console.error('PA verification failed:', error);
    container.innerHTML = `
      <div class="rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 p-4">
        <div class="flex items-start gap-3">
          <svg class="size-5 text-red-600 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <div>
            <h3 class="text-sm font-semibold text-red-800 dark:text-red-300">PA Verification Error</h3>
            <p class="mt-1 text-sm text-red-700 dark:text-red-300">${error.message}</p>
          </div>
        </div>
      </div>
    `;
  } finally {
    // Restore button state
    btn.disabled = false;
    if (btnText) btnText.classList.remove('hidden');
    if (btnSpinner) btnSpinner.classList.add('hidden');
  }
}

/**
 * Render PA verification result
 */
function renderPAResult(result, container) {
  const statusBadge = getPAStatusBadge(result.status);
  const certChainHtml = renderCertificateChain(result.certificateChainValidation);
  const sodSigHtml = renderSODSignature(result.sodSignatureValidation);
  const dgValidationHtml = renderDataGroupValidation(result.dataGroupValidation);
  const errorsHtml = renderPAErrors(result.errors);

  container.innerHTML = `
    <div class="space-y-4">
      <!-- Overall Status -->
      <div class="bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 rounded-lg p-4">
        <div class="flex items-center justify-between mb-4">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-neutral-100">Verification Result</h4>
          ${statusBadge}
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 text-sm">
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Verification ID</dt>
            <dd class="mt-1 text-gray-900 dark:text-neutral-100 font-mono text-xs break-all">${result.verificationId || 'N/A'}</dd>
          </div>
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Timestamp</dt>
            <dd class="mt-1 text-gray-900 dark:text-neutral-100 text-sm">${new Date(result.verificationTimestamp).toLocaleString()}</dd>
          </div>
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Processing Time</dt>
            <dd class="mt-1 text-gray-900 dark:text-neutral-100 text-sm font-semibold">${result.processingDurationMs} ms</dd>
          </div>
        </div>
      </div>

      ${certChainHtml}
      ${sodSigHtml}
      ${dgValidationHtml}
      ${errorsHtml}
    </div>
  `;
}

function getPAStatusBadge(status) {
  const badges = {
    'VALID': '<span class="inline-flex items-center gap-1.5 rounded-full bg-green-50 dark:bg-green-900/20 px-3 py-1.5 text-sm font-semibold text-green-700 dark:text-green-300 ring-1 ring-inset ring-green-600/20"><svg class="size-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>VALID</span>',
    'INVALID': '<span class="inline-flex items-center gap-1.5 rounded-full bg-red-50 dark:bg-red-900/20 px-3 py-1.5 text-sm font-semibold text-red-700 dark:text-red-300 ring-1 ring-inset ring-red-600/20"><svg class="size-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>INVALID</span>',
    'ERROR': '<span class="inline-flex items-center gap-1.5 rounded-full bg-yellow-50 dark:bg-yellow-900/20 px-3 py-1.5 text-sm font-semibold text-yellow-800 ring-1 ring-inset ring-yellow-600/20"><svg class="size-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/></svg>ERROR</span>'
  };
  return badges[status] || '<span class="inline-flex items-center rounded-full bg-gray-50 dark:bg-neutral-700/50 px-3 py-1.5 text-sm font-semibold text-gray-600 dark:text-neutral-400 ring-1 ring-inset ring-gray-500/10">UNKNOWN</span>';
}

function renderCertificateChain(cert) {
  if (!cert) return '';
  const isValid = cert.valid;
  const iconColor = isValid ? 'text-green-600' : 'text-red-600';
  const bgColor = isValid ? 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800' : 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800';
  const icon = isValid
    ? '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>'
    : '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>';

  // CRL 상태 정보 생성
  const crlStatusHtml = renderCrlStatusDetail(cert);

  return `
    <div class="bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 rounded-lg overflow-hidden">
      <div class="flex items-center gap-2 px-4 py-3 ${bgColor}">
        <span class="${iconColor}">${icon}</span>
        <h4 class="text-base font-semibold text-gray-900 dark:text-neutral-100">Certificate Chain Validation</h4>
      </div>
      <div class="p-4">
        <dl class="space-y-3">
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">DSC Subject</dt>
            <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono break-all">${cert.dscSubject || 'N/A'}</dd>
          </div>
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">CSCA Subject</dt>
            <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100 font-mono break-all">${cert.cscaSubject || 'N/A'}</dd>
          </div>
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">CRL Valid Period</dt>
            <dd class="mt-1 text-sm ${cert.crlNextUpdate && new Date(cert.crlNextUpdate) < new Date() ? 'text-red-600 font-semibold' : 'text-gray-900 dark:text-neutral-100'}">
              ${cert.crlThisUpdate ? new Date(cert.crlThisUpdate).toLocaleDateString() : 'N/A'} -
              ${cert.crlNextUpdate ? new Date(cert.crlNextUpdate).toLocaleDateString() : 'N/A'}
            </dd>
          </div>
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">CRL Status</dt>
            <dd class="mt-1">
              ${crlStatusHtml}
            </dd>
          </div>
        </dl>
      </div>
    </div>
  `;
}

/**
 * CRL 상태 상세 정보 렌더링 (passport-tabs.js용)
 */
function renderCrlStatusDetail(cert) {
  if (!cert) return 'N/A';

  // Severity에 따른 스타일 매핑
  const severityStyles = {
    'SUCCESS': { badgeClass: 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 ring-green-600/20', icon: '✓' },
    'ERROR': { badgeClass: 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 ring-red-600/20', icon: '✗' },
    'WARNING': { badgeClass: 'bg-yellow-100 text-yellow-800 ring-yellow-600/20', icon: '⚠' },
    'INFO': { badgeClass: 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 ring-blue-600/20', icon: 'ⓘ' }
  };

  // status로 severity 추론
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

  const severity = cert.crlStatusSeverity || statusStyleMap[cert.crlStatus] || 'INFO';
  const style = severityStyles[severity] || severityStyles['INFO'];

  // 라벨 (API description 사용, 없으면 status에서 생성)
  const label = cert.crlStatusDescription || (cert.crlStatus ? cert.crlStatus.replace(/_/g, ' ') : 'Unknown');

  // 설명 (상세 설명 > crlMessage > 기본 메시지)
  const description = cert.crlStatusDetailedDescription || cert.crlMessage || '';

  // 기술적 상세 정보 (crlMessage가 있고 description과 다른 경우)
  const hasDetailedInfo = cert.crlMessage && cert.crlMessage !== description;

  let detailsHtml = '';
  if (hasDetailedInfo) {
    detailsHtml = `
      <details class="mt-2">
        <summary class="cursor-pointer text-xs text-gray-500 dark:text-neutral-400 hover:text-gray-700 dark:hover:text-neutral-300 flex items-center gap-1">
          <svg class="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          Technical Details
        </summary>
        <div class="mt-2 p-2 bg-gray-100 dark:bg-neutral-700 rounded text-xs font-mono text-gray-700 dark:text-neutral-300 break-all">
          ${cert.crlMessage}
        </div>
      </details>
    `;
  }

  return `
    <div class="flex flex-col gap-2">
      <div class="flex items-center gap-2 flex-wrap">
        <span class="inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${style.badgeClass}">
          ${style.icon} ${label}
        </span>
        <span class="text-xs text-gray-400 dark:text-neutral-500">(${severity})</span>
        ${cert.revoked ? '<span class="inline-flex items-center rounded-md bg-red-100 dark:bg-red-900/30 px-2 py-1 text-xs font-medium text-red-700 dark:text-red-300 ring-1 ring-inset ring-red-600/20">REVOKED</span>' : ''}
      </div>
      ${description ? `<p class="text-sm text-gray-600 dark:text-neutral-400">${description}</p>` : ''}
      ${detailsHtml}
    </div>
  `;
}

function renderSODSignature(sod) {
  if (!sod) return '';
  const isValid = sod.valid;
  const iconColor = isValid ? 'text-green-600' : 'text-red-600';
  const bgColor = isValid ? 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800' : 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800';
  const icon = isValid
    ? '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>'
    : '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>';

  return `
    <div class="bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 rounded-lg overflow-hidden">
      <div class="flex items-center gap-2 px-4 py-3 ${bgColor}">
        <span class="${iconColor}">${icon}</span>
        <h4 class="text-base font-semibold text-gray-900 dark:text-neutral-100">SOD Signature Validation</h4>
      </div>
      <div class="p-4">
        <dl class="grid grid-cols-2 gap-3">
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Signature Algorithm</dt>
            <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${sod.signatureAlgorithm || 'N/A'}</dd>
          </div>
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Hash Algorithm</dt>
            <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${sod.hashAlgorithm || 'N/A'}</dd>
          </div>
        </dl>
      </div>
    </div>
  `;
}

function renderDataGroupValidation(dg) {
  if (!dg) return '';

  const isAllValid = dg.invalidGroups === 0;
  const iconColor = isAllValid ? 'text-green-600' : 'text-red-600';
  const bgColor = isAllValid ? 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800' : 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800';
  const icon = isAllValid
    ? '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>'
    : '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>';

  let detailsHtml = '';
  if (dg.details) {
    for (const [dgName, detail] of Object.entries(dg.details)) {
      const dgValid = detail.valid;
      const rowBg = dgValid ? '' : 'bg-red-50 dark:bg-red-900/20';
      const statusIcon = dgValid
        ? '<span class="text-green-600 font-semibold">&#10003;</span>'
        : '<span class="text-red-600 font-semibold">&#10007;</span>';

      detailsHtml += `
        <tr class="${rowBg}">
          <td class="px-3 py-2 text-sm font-medium text-gray-900 dark:text-neutral-100">${statusIcon} ${dgName}</td>
          <td class="px-3 py-2 text-xs text-gray-600 dark:text-neutral-400 font-mono">${detail.expectedHash?.substring(0, 24) || 'N/A'}...</td>
          <td class="px-3 py-2 text-xs text-gray-600 dark:text-neutral-400 font-mono">${detail.actualHash?.substring(0, 24) || 'N/A'}...</td>
        </tr>
      `;
    }
  }

  return `
    <div class="bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 rounded-lg overflow-hidden">
      <div class="flex items-center justify-between px-4 py-3 ${bgColor}">
        <div class="flex items-center gap-2">
          <span class="${iconColor}">${icon}</span>
          <h4 class="text-base font-semibold text-gray-900 dark:text-neutral-100">Data Group Hash Validation</h4>
        </div>
        <span class="text-sm font-medium ${isAllValid ? 'text-green-700 dark:text-green-300' : 'text-red-700 dark:text-red-300'}">${dg.validGroups}/${dg.totalGroups} valid</span>
      </div>
      <div class="p-4">
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200 dark:divide-neutral-700">
            <thead class="bg-gray-50 dark:bg-neutral-700/50">
              <tr>
                <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Data Group</th>
                <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Expected Hash</th>
                <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase tracking-wider">Actual Hash</th>
              </tr>
            </thead>
            <tbody class="bg-white dark:bg-neutral-800 divide-y divide-gray-200 dark:divide-neutral-700">
              ${detailsHtml}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `;
}

function renderPAErrors(errors) {
  if (!errors || errors.length === 0) return '';

  const errorsHtml = errors.map(err => `
    <li class="flex items-start gap-2 py-2">
      <svg class="size-4 text-red-500 mt-0.5 shrink-0" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/>
      </svg>
      <div>
        <span class="font-semibold text-red-800 dark:text-red-300">[${err.code}]</span>
        <span class="text-red-700 dark:text-red-300">${err.message}</span>
        <span class="text-xs text-red-500 ml-1">(${err.severity})</span>
      </div>
    </li>
  `).join('');

  return `
    <div class="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4">
      <h4 class="text-sm font-semibold text-red-800 dark:text-red-300 mb-2">Validation Errors</h4>
      <ul class="space-y-1">
        ${errorsHtml}
      </ul>
    </div>
  `;
}

// ============================================
// SOD Information Rendering Functions
// ============================================

/**
 * Render SOD Information to container
 * @param {Object} parsedSOD - ParsedSOD data from server
 * @param {string} containerId - ID of the container element (default: 'sod-info-container')
 */
function renderSODInformation(parsedSOD, containerId = 'sod-info-container') {
  const container = document.getElementById(containerId);
  if (!container) {
    console.error("SOD container not found:", containerId);
    return;
  }

  if (!parsedSOD) {
    container.innerHTML = `
      <div class="flex flex-col items-center justify-center py-12">
        <svg class="size-16 text-yellow-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"/>
        </svg>
        <p class="text-sm text-gray-500 dark:text-neutral-400">No SOD data available</p>
      </div>
    `;
    return;
  }

  let html = '<div class="space-y-4">';

  // Header
  html += `
    <div class="flex items-center gap-3 pb-3 border-b border-gray-200 dark:border-neutral-700">
      <div class="flex size-10 items-center justify-center rounded-lg bg-indigo-100 dark:bg-indigo-900/30">
        <svg class="size-5 text-indigo-600 dark:text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
        </svg>
      </div>
      <div>
        <h3 class="text-lg font-semibold text-gray-900 dark:text-neutral-100">Security Object Document (SOD)</h3>
        <p class="text-sm text-gray-500 dark:text-neutral-400">E-Passport chip security and integrity information</p>
      </div>
    </div>
  `;

  // Digest Algorithm
  html += `
    <div class="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
      <h4 class="text-sm font-semibold text-blue-900 dark:text-blue-200 mb-2">Digest Algorithm</h4>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <dt class="text-xs font-medium text-blue-700 dark:text-blue-300">Algorithm Name</dt>
          <dd class="mt-1 text-sm font-semibold text-blue-900 dark:text-blue-200">${parsedSOD.digestAlgorithmName || 'N/A'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-blue-700 dark:text-blue-300">OID</dt>
          <dd class="mt-1 text-sm font-mono text-blue-800 dark:text-blue-300">${parsedSOD.digestAlgorithmOid || 'N/A'}</dd>
        </div>
      </div>
    </div>
  `;

  // Data Group Hashes
  if (parsedSOD.dgHashes && parsedSOD.dgHashes.length > 0) {
    html += `
      <div class="bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 rounded-lg overflow-hidden">
        <div class="bg-gray-50 dark:bg-neutral-700/50 px-4 py-3 border-b border-gray-200 dark:border-neutral-700">
          <h4 class="text-sm font-semibold text-gray-900 dark:text-neutral-100">Data Group Hashes</h4>
          <p class="text-xs text-gray-500 dark:text-neutral-400 mt-0.5">Hash values stored in the SOD for each Data Group</p>
        </div>
        <div class="p-4">
          <div class="space-y-2">
    `;

    parsedSOD.dgHashes.forEach(function(dg) {
      html += `
        <div class="flex items-center gap-3 bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
          <span class="inline-flex items-center justify-center w-14 rounded bg-indigo-100 dark:bg-indigo-900/30 px-2 py-1 text-xs font-bold text-indigo-800 dark:text-indigo-300">
            DG${String(dg.dgNumber).padStart(2, '0')}
          </span>
          <code class="flex-1 text-xs font-mono text-gray-700 dark:text-neutral-300 break-all">${dg.hash || 'N/A'}</code>
        </div>
      `;
    });

    html += `
          </div>
        </div>
      </div>
    `;
  }

  // DSC Certificate
  if (parsedSOD.dscCertificate) {
    const cert = parsedSOD.dscCertificate;
    const validityClass = cert.valid ? 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800' : 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800';
    const validityTextClass = cert.valid ? 'text-green-700 dark:text-green-300' : 'text-red-700 dark:text-red-300';
    const validityIcon = cert.valid
      ? '<svg class="size-4 text-green-600" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>'
      : '<svg class="size-4 text-red-600" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>';

    html += `
      <div class="bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 rounded-lg overflow-hidden">
        <div class="bg-gray-50 dark:bg-neutral-700/50 px-4 py-3 border-b border-gray-200 dark:border-neutral-700">
          <h4 class="text-sm font-semibold text-gray-900 dark:text-neutral-100">Document Signer Certificate (DSC)</h4>
        </div>
        <div class="p-4 space-y-3">
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">Subject</dt>
            <dd class="mt-1 text-sm font-mono text-gray-900 dark:text-neutral-100 break-all">${cert.subject || 'N/A'}</dd>
          </div>
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">Issuer</dt>
            <dd class="mt-1 text-sm font-mono text-gray-900 dark:text-neutral-100 break-all">${cert.issuer || 'N/A'}</dd>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">Valid From</dt>
              <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${cert.notBefore || 'N/A'}</dd>
            </div>
            <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">Valid Until</dt>
              <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${cert.notAfter || 'N/A'}</dd>
            </div>
          </div>
          <div class="${validityClass} rounded-lg p-3">
            <div class="flex items-center gap-2">
              ${validityIcon}
              <span class="text-sm font-semibold ${validityTextClass}">${cert.valid ? 'Certificate Valid' : 'Certificate Invalid/Expired'}</span>
            </div>
          </div>
          <div class="grid grid-cols-3 gap-3">
            <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">Algorithm</dt>
              <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${cert.publicKeyAlgorithm || 'N/A'}</dd>
            </div>
            <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">Key Size</dt>
              <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${cert.publicKeySize || 'N/A'} bits</dd>
            </div>
            <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">Signature</dt>
              <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${cert.signatureAlgorithm || 'N/A'}</dd>
            </div>
          </div>
          <div class="space-y-2">
            <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">SHA-1 Fingerprint</dt>
              <dd class="mt-1 text-xs font-mono text-gray-700 dark:text-neutral-300 break-all">${cert.sha1Fingerprint || 'N/A'}</dd>
            </div>
            <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">SHA-256 Fingerprint</dt>
              <dd class="mt-1 text-xs font-mono text-gray-700 dark:text-neutral-300 break-all">${cert.sha256Fingerprint || 'N/A'}</dd>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // Signer Information (matches sod_information.html structure)
  if (parsedSOD.signerInfo) {
    const signer = parsedSOD.signerInfo;
    html += `
      <div class="bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 rounded-lg overflow-hidden">
        <div class="bg-gray-50 dark:bg-neutral-700/50 px-4 py-3 border-b border-gray-200 dark:border-neutral-700">
          <h4 class="text-sm font-semibold text-gray-900 dark:text-neutral-100">Signer Information</h4>
        </div>
        <div class="p-4 space-y-3">
          <div class="grid grid-cols-2 gap-3">
            <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">Digest Algorithm</dt>
              <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${signer.digestAlgorithmName || signer.digestAlgorithm || 'N/A'}</dd>
            </div>
            <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">Encryption Algorithm</dt>
              <dd class="mt-1 text-sm text-gray-900 dark:text-neutral-100">${signer.encryptionAlgorithmName || signer.encryptionAlgorithm || 'N/A'}</dd>
            </div>
          </div>
          <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 dark:text-neutral-400 uppercase">Digital Signature</dt>
            <details class="mt-2">
              <summary class="cursor-pointer text-xs text-gray-500 dark:text-neutral-400 hover:text-gray-700 dark:hover:text-neutral-300">
                Show signature (click to expand)
              </summary>
              <code class="mt-2 block break-all rounded bg-gray-100 dark:bg-neutral-700 p-2 font-mono text-xs text-gray-700 dark:text-neutral-300">${signer.signature || 'N/A'}</code>
            </details>
          </div>
        </div>
      </div>
    `;
  }

  html += '</div>';
  container.innerHTML = html;
  console.log("SOD information rendered successfully");
}

// ============================================
// MRZ Tooltip Descriptions
// ============================================

const MRZ_TOOLTIPS = {
  mrzLines:
    'TD3 여권의 MRZ는 2줄 × 44자로 구성됩니다.\n' +
    '• Line 1: 문서 타입(P) + 발급국(3자리) + 이름(성<<이름)\n' +
    '• Line 2: 여권번호 + CD + 국적 + 생년월일 + CD + 성별 + 만료일 + CD + 선택데이터 + CD + 복합 CD',
  checkDigitHeader:
    'ICAO 9303 Check Digit 알고리즘:\n' +
    '1. 각 문자를 숫자로 변환 (A=10...Z=35, 0-9=그대로, <=0)\n' +
    '2. 가중치 7, 3, 1을 순서대로 반복 곱함\n' +
    '3. 모든 곱의 합을 10으로 나눈 나머지 = Check Digit\n\n' +
    '이 방식은 단일 문자 오류와 인접 문자 전치를 높은 확률로 감지합니다.',
  passportNumber:
    'Line 2 위치 1-9 (여권번호)에 대한 Check Digit\n' +
    '검증: 위치 10의 숫자와 계산된 값 비교\n' +
    '목적: 여권번호 OCR 판독 오류 검출',
  birthDate:
    'Line 2 위치 14-19 (생년월일, YYMMDD 형식)에 대한 Check Digit\n' +
    '검증: 위치 20의 숫자와 계산된 값 비교',
  expiryDate:
    'Line 2 위치 22-27 (만료일, YYMMDD 형식)에 대한 Check Digit\n' +
    '검증: 위치 28의 숫자와 계산된 값 비교',
  optionalData:
    'Line 2 위치 29-42 (선택 데이터)에 대한 Check Digit\n' +
    '검증: 위치 43의 숫자와 계산된 값 비교\n' +
    '참고: 데이터가 없거나 모두 \'<\'인 경우 검증 생략 (N/A)',
  composite:
    'Line 2의 여권번호+CD + 생년월일+CD + 만료일+CD + 선택데이터+CD를\n' +
    '모두 연결한 문자열의 Check Digit (위치 44)\n' +
    '목적: MRZ Line 2 전체의 데이터 무결성 보장',
  formatErrors:
    'ICAO 9303에서 정의한 MRZ 형식 규칙을 검증합니다.\n' +
    '• 문서 타입: 첫 글자가 P(여권), V(비자), I(신분증), A(승무원)\n' +
    '• 문자 집합: A-Z, 0-9, \'<\'(filler) 문자만 허용',
  fieldErrors:
    'MRZ 개별 필드 값의 유효성을 검증합니다.\n' +
    '• 국가 코드: ISO 3166-1 alpha-3 (3자리 대문자)\n' +
    '• 날짜: YYMMDD 형식, 월 01-12, 일 01-31\n' +
    '• 성별: M(남), F(여), <(미지정)'
};

// ============================================
// MRZ Tooltip System (hover-based)
// ============================================

let _mrzTooltipTimer = null;
let _mrzTooltip = null;
let _mrzTouchDevice = false;

document.addEventListener('touchstart', function() { _mrzTouchDevice = true; }, { once: true });

/** Show tooltip on hover with delay */
function showMrzTooltip(event) {
  event.stopPropagation();
  const btn = event.currentTarget;
  if (_mrzTouchDevice) { _toggleMrzTooltipTouch(btn); return; }
  clearTimeout(_mrzTooltipTimer);
  if (_mrzTooltip && _mrzTooltip._btn === btn) return;
  _removeMrzTooltip();
  _mrzTooltipTimer = setTimeout(() => _createMrzTooltip(btn), 150);
}

/** Hide tooltip on mouse leave with delay */
function hideMrzTooltip(event) {
  if (_mrzTouchDevice) return;
  clearTimeout(_mrzTooltipTimer);
  _mrzTooltipTimer = setTimeout(() => _removeMrzTooltip(), 120);
}

function _createMrzTooltip(btn) {
  const key = btn.getAttribute('data-mrz-tooltip');
  const text = MRZ_TOOLTIPS[key] || '';
  if (!text) return;

  const rect = btn.getBoundingClientRect();
  const tip = document.createElement('div');
  tip.className = 'mrz-active-tooltip';
  tip.style.cssText =
    'position:fixed;width:20rem;background:#1f2937;color:#fff;font-size:0.75rem;' +
    'border-radius:0.5rem;padding:0.75rem;box-shadow:0 20px 25px -5px rgba(0,0,0,.1),0 8px 10px -6px rgba(0,0,0,.1);' +
    'z-index:9999;pointer-events:auto;';
  tip._btn = btn;

  tip.addEventListener('mouseenter', () => clearTimeout(_mrzTooltipTimer));
  tip.addEventListener('mouseleave', () => {
    _mrzTooltipTimer = setTimeout(() => _removeMrzTooltip(), 120);
  });

  const arrow = document.createElement('div');
  arrow.style.cssText =
    'position:absolute;top:-5px;left:12px;width:10px;height:10px;background:#1f2937;transform:rotate(45deg);';

  const content = document.createElement('p');
  content.style.cssText = 'white-space:pre-line;line-height:1.625;position:relative;z-index:1;';
  content.textContent = text;

  tip.appendChild(arrow);
  tip.appendChild(content);
  document.body.appendChild(tip);

  let top = rect.bottom + 8;
  let left = rect.left;
  if (left + 320 > window.innerWidth) left = window.innerWidth - 328;
  if (top + tip.offsetHeight > window.innerHeight) {
    top = rect.top - tip.offsetHeight - 8;
    arrow.style.cssText =
      'position:absolute;bottom:-5px;left:12px;width:10px;height:10px;background:#1f2937;transform:rotate(45deg);';
  }
  tip.style.top = top + 'px';
  tip.style.left = left + 'px';
  _mrzTooltip = tip;
}

function _removeMrzTooltip() {
  if (_mrzTooltip) { _mrzTooltip.remove(); _mrzTooltip = null; }
}

function _toggleMrzTooltipTouch(btn) {
  if (_mrzTooltip && _mrzTooltip._btn === btn) { _removeMrzTooltip(); return; }
  _removeMrzTooltip();
  _createMrzTooltip(btn);
}

// Close tooltip on tap outside (touch devices)
document.addEventListener('click', function(e) {
  if (!_mrzTouchDevice) return;
  if (!e.target.closest('.mrz-tooltip-wrapper') && !e.target.closest('.mrz-active-tooltip')) {
    _removeMrzTooltip();
  }
});

/**
 * Build info icon HTML for MRZ tooltip (hover-based)
 */
function mrzInfoIcon(tooltipKey) {
  return `<span class="mrz-tooltip-wrapper relative inline-flex items-center ml-1">` +
    `<button type="button" data-mrz-tooltip="${tooltipKey}" ` +
    `onmouseenter="showMrzTooltip(event)" onmouseleave="hideMrzTooltip(event)" onclick="showMrzTooltip(event)" ` +
    `class="text-gray-400 dark:text-neutral-500 hover:text-blue-500 focus:text-blue-500 transition-colors">` +
    `<svg class="size-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">` +
    `<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>` +
    `</svg></button></span>`;
}

// ============================================
// MRZ Validation Functions
// ============================================

/**
 * Render MRZ Validation Result
 * @param {Object} validationResult - MrzValidationResult from server
 * @param {string} containerId - ID of the container element
 * @param {Object} ePassMrzLines - e-Passport MRZ Lines from Chip DG1 (optional)
 */
function renderMrzValidation(validationResult, containerId = 'auto-mrz-validation-container', ePassMrzLines = null) {
  const container = document.getElementById(containerId);
  if (!container) {
    console.error("MRZ validation container not found:", containerId);
    return;
  }

  if (!validationResult) {
    container.innerHTML = `
      <div class="flex flex-col items-center justify-center py-12">
        <svg class="size-16 text-gray-400 dark:text-neutral-500 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
        </svg>
        <p class="text-sm text-gray-500 dark:text-neutral-400">No MRZ validation data available</p>
      </div>
    `;
    return;
  }

  const isValid = validationResult.valid;
  const statusColor = isValid ? 'green' : 'red';
  const statusIcon = isValid
    ? `<svg class="size-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
         <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/>
       </svg>`
    : `<svg class="size-5 text-red-600" fill="currentColor" viewBox="0 0 20 20">
         <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/>
       </svg>`;

  let html = '<div class="space-y-5">';

  // Header - card wrapped with gradient
  html += `
    <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
      <div class="px-5 py-4 ${isValid ? 'bg-gradient-to-r from-green-50 to-emerald-50 dark:from-green-900/20 dark:to-emerald-900/20' : 'bg-gradient-to-r from-red-50 to-rose-50 dark:from-red-900/20 dark:to-rose-900/20'}">
        <div class="flex items-center gap-3">
          <div class="flex size-10 items-center justify-center rounded-xl bg-${statusColor}-100">
            ${statusIcon}
          </div>
          <div>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-neutral-100">ICAO 9303 MRZ Validation</h3>
            <p class="text-sm text-${statusColor}-600">${validationResult.summaryMessage || 'Validation complete'}</p>
          </div>
        </div>
      </div>
    </div>
  `;

  // MRZ Lines - VIZ/Chip comparison
  const line1 = validationResult.mrzLine1;
  const line2 = validationResult.mrzLine2;
  const hasChipMrz = ePassMrzLines && ePassMrzLines.line1;
  if (line1 || line2) {
    // Match/Mismatch badge
    let matchBadge = '';
    let isMatch = false;
    if (hasChipMrz) {
      isMatch = line1 === ePassMrzLines.line1 && line2 === ePassMrzLines.line2;
      if (isMatch) {
        matchBadge = `<span class="inline-flex items-center gap-1 rounded-full bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 px-2.5 py-0.5 text-[10px] font-semibold"><svg class="size-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clip-rule="evenodd"/></svg>MATCH</span>`;
      } else {
        const diffCount = countMrzDiffs(line1, line2, ePassMrzLines.line1, ePassMrzLines.line2);
        matchBadge = `<span class="inline-flex items-center gap-1 rounded-full bg-amber-100 dark:bg-amber-900/30 text-amber-800 dark:text-amber-300 px-2.5 py-0.5 text-[10px] font-semibold"><svg class="size-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/></svg>MISMATCH (${diffCount} char${diffCount > 1 ? 's' : ''})</span>`;
      }
    }

    html += `
      <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
        <div class="px-5 py-4 border-b border-gray-100 dark:border-neutral-700 bg-gray-50 dark:bg-neutral-700/50">
          <div class="flex items-center justify-between">
            <h4 class="text-sm font-semibold text-gray-900 dark:text-neutral-100 inline-flex items-center">MRZ Lines (TD3 - 2 x 44 characters)${mrzInfoIcon('mrzLines')}</h4>
            ${matchBadge}
          </div>
        </div>
        <div class="p-4 ${hasChipMrz ? 'grid grid-cols-1 lg:grid-cols-2 gap-3' : ''}">
          <div class="bg-gray-900 rounded-lg p-3 overflow-x-auto">
            <p class="text-[10px] text-gray-400 mb-1.5 font-semibold uppercase tracking-wider">${hasChipMrz ? 'VIZ (OCR)' : 'VIZ MRZ'}</p>
            <div class="font-mono text-sm text-green-400 tracking-wider leading-relaxed">
    `;
    const useDiff = hasChipMrz && !isMatch;
    if (line1) {
      const l1Html = useDiff ? renderMrzCharsWithDiff(line1, ePassMrzLines.line1) : escapeHtmlMrz(line1);
      html += `<div><span class="text-gray-500 text-xs mr-2">L1</span><span>${l1Html}</span></div>`;
    }
    if (line2) {
      const l2Html = useDiff ? renderMrzCharsWithDiff(line2, ePassMrzLines.line2) : escapeHtmlMrz(line2);
      html += `<div><span class="text-gray-500 text-xs mr-2">L2</span><span>${l2Html}</span></div>`;
    }
    html += `</div></div>`;

    // Chip MRZ (DG1) - only when available
    if (hasChipMrz) {
      html += `
          <div class="bg-gray-900 rounded-lg p-3 overflow-x-auto">
            <p class="text-[10px] text-cyan-400 mb-1.5 font-semibold uppercase tracking-wider">Chip (DG1)</p>
            <div class="font-mono text-sm text-cyan-400 tracking-wider leading-relaxed">
      `;
      if (ePassMrzLines.line1) {
        const cl1Html = useDiff ? renderMrzCharsWithDiff(ePassMrzLines.line1, line1) : escapeHtmlMrz(ePassMrzLines.line1);
        html += `<div><span class="text-gray-500 text-xs mr-2">L1</span><span>${cl1Html}</span></div>`;
      }
      if (ePassMrzLines.line2) {
        const cl2Html = useDiff ? renderMrzCharsWithDiff(ePassMrzLines.line2, line2) : escapeHtmlMrz(ePassMrzLines.line2);
        html += `<div><span class="text-gray-500 text-xs mr-2">L2</span><span>${cl2Html}</span></div>`;
      }
      html += `</div></div>`;
    }

    html += `</div></div>`;
  }

  // Check Digit Results - compact grid
  if (validationResult.checkDigitResults && validationResult.checkDigitResults.length > 0) {
    html += `
      <div class="rounded-xl bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
        <div class="px-5 py-4 border-b border-gray-100 dark:border-neutral-700 bg-gray-50 dark:bg-neutral-700/50">
          <h4 class="text-sm font-semibold text-gray-900 dark:text-neutral-100 inline-flex items-center">Check Digit Validation${mrzInfoIcon('checkDigitHeader')}</h4>
          <p class="text-xs text-gray-500 dark:text-neutral-400 mt-0.5">ICAO 9303 check digit verification (weights: 7, 3, 1 repeating, sum mod 10)</p>
        </div>
        <div class="p-4">
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
    `;

    validationResult.checkDigitResults.forEach(checkResult => {
      const cv = checkResult.valid;
      const cc = cv ? 'green' : 'red';
      const ci = cv
        ? `<svg class="size-3 text-green-600" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clip-rule="evenodd"/></svg>`
        : `<svg class="size-3 text-red-600" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>`;
      const dl = checkResult.label || checkResult.fieldName;
      const dd = checkResult.data && checkResult.data.length > 0 ? checkResult.data : 'N/A';

      html += `
        <div class="bg-gray-50 dark:bg-neutral-700/50 rounded-lg p-3 border border-gray-100 dark:border-neutral-700">
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-1.5">
              <div class="flex size-5 items-center justify-center rounded-full bg-${cc}-100">${ci}</div>
              <span class="text-xs font-semibold text-gray-900 dark:text-neutral-100">${dl}</span>
              ${mrzInfoIcon(checkResult.fieldName)}
            </div>
            <span class="inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold bg-${cc}-100 text-${cc}-800">${cv ? 'VALID' : 'INVALID'}</span>
          </div>
          <div class="font-mono text-[11px] text-gray-600 dark:text-neutral-400 truncate mb-1.5" title="${escapeHtmlMrz(dd)}">${escapeHtmlMrz(dd)}</div>
          <div class="flex items-center gap-3 text-[11px]">
            <div><span class="text-gray-400 dark:text-neutral-500">Expected:</span> <span class="font-mono font-semibold text-${cc}-700">${checkResult.expectedCheckDigit || 'N/A'}</span></div>
            <div><span class="text-gray-400 dark:text-neutral-500">Actual:</span> <span class="font-mono font-semibold text-${cc}-700">${checkResult.actualCheckDigit || 'N/A'}</span></div>
          </div>
          ${checkResult.errorMessage ? `<p class="mt-1.5 text-[10px] rounded px-1.5 py-0.5 ${cv ? 'bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-300' : 'bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-300'}">${checkResult.errorMessage}</p>` : ''}
        </div>
      `;
    });

    html += `</div></div></div>`;
  }

  // Format Errors
  if (validationResult.formatErrors && validationResult.formatErrors.length > 0) {
    html += `
      <div class="rounded-xl bg-white dark:bg-neutral-800 border border-red-200 dark:border-red-800 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
        <div class="px-5 py-4 border-b border-red-100 bg-red-50 dark:bg-red-900/20">
          <h4 class="text-sm font-semibold text-red-900 dark:text-red-200 inline-flex items-center">Format Errors${mrzInfoIcon('formatErrors')}</h4>
          <p class="text-xs text-red-700 dark:text-red-300 mt-0.5">MRZ format validation failures</p>
        </div>
        <div class="divide-y divide-red-100">
    `;

    validationResult.formatErrors.forEach(error => {
      html += `
        <div class="px-5 py-4">
          <div class="flex items-start gap-3">
            <div class="flex size-6 items-center justify-center rounded-full bg-red-100 dark:bg-red-900/30 flex-shrink-0 mt-0.5">
              <svg class="size-3.5 text-red-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-5a.75.75 0 01.75.75v4.5a.75.75 0 01-1.5 0v-4.5A.75.75 0 0110 5zm0 10a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <span class="text-sm font-semibold text-red-900 dark:text-red-200">${error.type}</span>
              <p class="text-xs text-red-700 dark:text-red-300 mt-0.5">${error.message}</p>
              ${error.rule ? `<p class="text-xs text-red-600 mt-1 bg-red-50 dark:bg-red-900/20 rounded px-2 py-1"><span class="font-medium">Rule:</span> ${error.rule}</p>` : ''}
            </div>
          </div>
        </div>
      `;
    });

    html += `</div></div>`;
  }

  // Field Errors
  if (validationResult.fieldErrors && validationResult.fieldErrors.length > 0) {
    html += `
      <div class="rounded-xl bg-white dark:bg-neutral-800 border border-amber-200 dark:border-amber-800 shadow-sm dark:shadow-neutral-900/30 overflow-hidden">
        <div class="px-5 py-4 border-b border-amber-100 bg-amber-50 dark:bg-amber-900/20">
          <h4 class="text-sm font-semibold text-amber-900 dark:text-amber-200 inline-flex items-center">Field Errors${mrzInfoIcon('fieldErrors')}</h4>
          <p class="text-xs text-amber-700 dark:text-amber-300 mt-0.5">Field validation failures</p>
        </div>
        <div class="divide-y divide-amber-100">
    `;

    validationResult.fieldErrors.forEach(error => {
      html += `
        <div class="px-5 py-4">
          <div class="flex items-start gap-3">
            <div class="flex size-6 items-center justify-center rounded-full bg-amber-100 dark:bg-amber-900/30 flex-shrink-0 mt-0.5">
              <svg class="size-3.5 text-amber-600 dark:text-amber-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <span class="text-sm font-semibold text-amber-900 dark:text-amber-200">${error.type}</span>
              <p class="text-xs text-amber-700 dark:text-amber-300 mt-0.5">${error.message}</p>
              ${error.rule ? `<p class="text-xs text-amber-600 dark:text-amber-400 mt-1 bg-amber-50 dark:bg-amber-900/20 rounded px-2 py-1"><span class="font-medium">Rule:</span> ${error.rule}</p>` : ''}
            </div>
          </div>
        </div>
      `;
    });

    html += `</div></div>`;
  }

  html += '</div>';
  container.innerHTML = html;
  console.log("MRZ validation rendered successfully");
}

/**
 * Escape HTML special characters for MRZ display
 */
function escapeHtmlMrz(text) {
  if (!text) return '';
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/**
 * Render MRZ text with character-level diff highlighting.
 * Differing characters are wrapped in a red box span.
 * @param {string} text - The MRZ text to render
 * @param {string} compareText - The MRZ text to compare against
 * @returns {string} HTML string with diff highlights
 */
function renderMrzCharsWithDiff(text, compareText) {
  if (!text) return '';
  let html = '';
  for (let i = 0; i < text.length; i++) {
    const char = escapeHtmlMrz(text[i]);
    const isDiff = compareText && i < compareText.length && text[i] !== compareText[i];
    if (isDiff) {
      html += `<span class="bg-red-500/30 border border-red-500 rounded-sm px-px">${char}</span>`;
    } else {
      html += char;
    }
  }
  return html;
}

/**
 * Count character differences between two MRZ lines.
 * @returns {number} total diff count across both lines
 */
function countMrzDiffs(vizLine1, vizLine2, chipLine1, chipLine2) {
  let count = 0;
  if (vizLine1 && chipLine1) {
    const len = Math.max(vizLine1.length, chipLine1.length);
    for (let i = 0; i < len; i++) {
      if (vizLine1[i] !== chipLine1[i]) count++;
    }
  }
  if (vizLine2 && chipLine2) {
    const len = Math.max(vizLine2.length, chipLine2.length);
    for (let i = 0; i < len; i++) {
      if (vizLine2[i] !== chipLine2[i]) count++;
    }
  }
  return count;
}
