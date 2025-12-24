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
      <div class="rounded-lg bg-red-50 border border-red-200 p-4">
        <div class="flex items-start gap-3">
          <svg class="size-5 text-red-600 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <div>
            <h3 class="text-sm font-semibold text-red-800">PA Verification Error</h3>
            <p class="mt-1 text-sm text-red-700">${error.message}</p>
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
      <div class="bg-white border border-gray-200 rounded-lg p-4">
        <div class="flex items-center justify-between mb-4">
          <h4 class="text-lg font-semibold text-gray-900">Verification Result</h4>
          ${statusBadge}
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 text-sm">
          <div class="bg-gray-50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wider">Verification ID</dt>
            <dd class="mt-1 text-gray-900 font-mono text-xs break-all">${result.verificationId || 'N/A'}</dd>
          </div>
          <div class="bg-gray-50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wider">Timestamp</dt>
            <dd class="mt-1 text-gray-900 text-sm">${new Date(result.verificationTimestamp).toLocaleString()}</dd>
          </div>
          <div class="bg-gray-50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wider">Processing Time</dt>
            <dd class="mt-1 text-gray-900 text-sm font-semibold">${result.processingDurationMs} ms</dd>
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
    'VALID': '<span class="inline-flex items-center gap-1.5 rounded-full bg-green-50 px-3 py-1.5 text-sm font-semibold text-green-700 ring-1 ring-inset ring-green-600/20"><svg class="size-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>VALID</span>',
    'INVALID': '<span class="inline-flex items-center gap-1.5 rounded-full bg-red-50 px-3 py-1.5 text-sm font-semibold text-red-700 ring-1 ring-inset ring-red-600/20"><svg class="size-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>INVALID</span>',
    'ERROR': '<span class="inline-flex items-center gap-1.5 rounded-full bg-yellow-50 px-3 py-1.5 text-sm font-semibold text-yellow-800 ring-1 ring-inset ring-yellow-600/20"><svg class="size-4" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/></svg>ERROR</span>'
  };
  return badges[status] || '<span class="inline-flex items-center rounded-full bg-gray-50 px-3 py-1.5 text-sm font-semibold text-gray-600 ring-1 ring-inset ring-gray-500/10">UNKNOWN</span>';
}

function renderCertificateChain(cert) {
  if (!cert) return '';
  const isValid = cert.valid;
  const iconColor = isValid ? 'text-green-600' : 'text-red-600';
  const bgColor = isValid ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200';
  const icon = isValid
    ? '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>'
    : '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>';

  return `
    <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div class="flex items-center gap-2 px-4 py-3 ${bgColor}">
        <span class="${iconColor}">${icon}</span>
        <h4 class="text-base font-semibold text-gray-900">Certificate Chain Validation</h4>
      </div>
      <div class="p-4">
        <dl class="space-y-3">
          <div class="bg-gray-50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wider">DSC Subject</dt>
            <dd class="mt-1 text-sm text-gray-900 font-mono break-all">${cert.dscSubject || 'N/A'}</dd>
          </div>
          <div class="bg-gray-50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wider">CSCA Subject</dt>
            <dd class="mt-1 text-sm text-gray-900 font-mono break-all">${cert.cscaSubject || 'N/A'}</dd>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase tracking-wider">Valid Period</dt>
              <dd class="mt-1 text-sm text-gray-900">
                ${cert.notBefore ? new Date(cert.notBefore).toLocaleDateString() : 'N/A'} -
                ${cert.notAfter ? new Date(cert.notAfter).toLocaleDateString() : 'N/A'}
              </dd>
            </div>
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase tracking-wider">CRL Status</dt>
              <dd class="mt-1 text-sm text-gray-900">
                ${cert.crlStatus || 'N/A'} ${cert.revoked ? '<span class="text-red-600 font-semibold">(REVOKED)</span>' : ''}
              </dd>
            </div>
          </div>
        </dl>
      </div>
    </div>
  `;
}

function renderSODSignature(sod) {
  if (!sod) return '';
  const isValid = sod.valid;
  const iconColor = isValid ? 'text-green-600' : 'text-red-600';
  const bgColor = isValid ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200';
  const icon = isValid
    ? '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>'
    : '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>';

  return `
    <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div class="flex items-center gap-2 px-4 py-3 ${bgColor}">
        <span class="${iconColor}">${icon}</span>
        <h4 class="text-base font-semibold text-gray-900">SOD Signature Validation</h4>
      </div>
      <div class="p-4">
        <dl class="grid grid-cols-2 gap-3">
          <div class="bg-gray-50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wider">Signature Algorithm</dt>
            <dd class="mt-1 text-sm text-gray-900">${sod.signatureAlgorithm || 'N/A'}</dd>
          </div>
          <div class="bg-gray-50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 uppercase tracking-wider">Hash Algorithm</dt>
            <dd class="mt-1 text-sm text-gray-900">${sod.hashAlgorithm || 'N/A'}</dd>
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
  const bgColor = isAllValid ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200';
  const icon = isAllValid
    ? '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>'
    : '<svg class="size-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>';

  let detailsHtml = '';
  if (dg.details) {
    for (const [dgName, detail] of Object.entries(dg.details)) {
      const dgValid = detail.valid;
      const rowBg = dgValid ? '' : 'bg-red-50';
      const statusIcon = dgValid
        ? '<span class="text-green-600 font-semibold">&#10003;</span>'
        : '<span class="text-red-600 font-semibold">&#10007;</span>';

      detailsHtml += `
        <tr class="${rowBg}">
          <td class="px-3 py-2 text-sm font-medium text-gray-900">${statusIcon} ${dgName}</td>
          <td class="px-3 py-2 text-xs text-gray-600 font-mono">${detail.expectedHash?.substring(0, 24) || 'N/A'}...</td>
          <td class="px-3 py-2 text-xs text-gray-600 font-mono">${detail.actualHash?.substring(0, 24) || 'N/A'}...</td>
        </tr>
      `;
    }
  }

  return `
    <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <div class="flex items-center justify-between px-4 py-3 ${bgColor}">
        <div class="flex items-center gap-2">
          <span class="${iconColor}">${icon}</span>
          <h4 class="text-base font-semibold text-gray-900">Data Group Hash Validation</h4>
        </div>
        <span class="text-sm font-medium ${isAllValid ? 'text-green-700' : 'text-red-700'}">${dg.validGroups}/${dg.totalGroups} valid</span>
      </div>
      <div class="p-4">
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Data Group</th>
                <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Expected Hash</th>
                <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actual Hash</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
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
        <span class="font-semibold text-red-800">[${err.code}]</span>
        <span class="text-red-700">${err.message}</span>
        <span class="text-xs text-red-500 ml-1">(${err.severity})</span>
      </div>
    </li>
  `).join('');

  return `
    <div class="bg-red-50 border border-red-200 rounded-lg p-4">
      <h4 class="text-sm font-semibold text-red-800 mb-2">Validation Errors</h4>
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
        <p class="text-sm text-gray-500">No SOD data available</p>
      </div>
    `;
    return;
  }

  let html = '<div class="space-y-4">';

  // Header
  html += `
    <div class="flex items-center gap-3 pb-3 border-b border-gray-200">
      <div class="flex size-10 items-center justify-center rounded-lg bg-indigo-100">
        <svg class="size-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
        </svg>
      </div>
      <div>
        <h3 class="text-lg font-semibold text-gray-900">Security Object Document (SOD)</h3>
        <p class="text-sm text-gray-500">E-Passport chip security and integrity information</p>
      </div>
    </div>
  `;

  // Digest Algorithm
  html += `
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
      <h4 class="text-sm font-semibold text-blue-900 mb-2">Digest Algorithm</h4>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <dt class="text-xs font-medium text-blue-700">Algorithm Name</dt>
          <dd class="mt-1 text-sm font-semibold text-blue-900">${parsedSOD.digestAlgorithmName || 'N/A'}</dd>
        </div>
        <div>
          <dt class="text-xs font-medium text-blue-700">OID</dt>
          <dd class="mt-1 text-sm font-mono text-blue-800">${parsedSOD.digestAlgorithmOid || 'N/A'}</dd>
        </div>
      </div>
    </div>
  `;

  // Data Group Hashes
  if (parsedSOD.dgHashes && parsedSOD.dgHashes.length > 0) {
    html += `
      <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
        <div class="bg-gray-50 px-4 py-3 border-b border-gray-200">
          <h4 class="text-sm font-semibold text-gray-900">Data Group Hashes</h4>
          <p class="text-xs text-gray-500 mt-0.5">Hash values stored in the SOD for each Data Group</p>
        </div>
        <div class="p-4">
          <div class="space-y-2">
    `;

    parsedSOD.dgHashes.forEach(function(dg) {
      html += `
        <div class="flex items-center gap-3 bg-gray-50 rounded-lg p-3">
          <span class="inline-flex items-center justify-center w-14 rounded bg-indigo-100 px-2 py-1 text-xs font-bold text-indigo-800">
            DG${String(dg.dgNumber).padStart(2, '0')}
          </span>
          <code class="flex-1 text-xs font-mono text-gray-700 break-all">${dg.hash || 'N/A'}</code>
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
    const validityClass = cert.valid ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200';
    const validityTextClass = cert.valid ? 'text-green-700' : 'text-red-700';
    const validityIcon = cert.valid
      ? '<svg class="size-4 text-green-600" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/></svg>'
      : '<svg class="size-4 text-red-600" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/></svg>';

    html += `
      <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
        <div class="bg-gray-50 px-4 py-3 border-b border-gray-200">
          <h4 class="text-sm font-semibold text-gray-900">Document Signer Certificate (DSC)</h4>
        </div>
        <div class="p-4 space-y-3">
          <div class="bg-gray-50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 uppercase">Subject</dt>
            <dd class="mt-1 text-sm font-mono text-gray-900 break-all">${cert.subject || 'N/A'}</dd>
          </div>
          <div class="bg-gray-50 rounded-lg p-3">
            <dt class="text-xs font-medium text-gray-500 uppercase">Issuer</dt>
            <dd class="mt-1 text-sm font-mono text-gray-900 break-all">${cert.issuer || 'N/A'}</dd>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase">Valid From</dt>
              <dd class="mt-1 text-sm text-gray-900">${cert.notBefore || 'N/A'}</dd>
            </div>
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase">Valid Until</dt>
              <dd class="mt-1 text-sm text-gray-900">${cert.notAfter || 'N/A'}</dd>
            </div>
          </div>
          <div class="${validityClass} rounded-lg p-3">
            <div class="flex items-center gap-2">
              ${validityIcon}
              <span class="text-sm font-semibold ${validityTextClass}">${cert.valid ? 'Certificate Valid' : 'Certificate Invalid/Expired'}</span>
            </div>
          </div>
          <div class="grid grid-cols-3 gap-3">
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase">Algorithm</dt>
              <dd class="mt-1 text-sm text-gray-900">${cert.publicKeyAlgorithm || 'N/A'}</dd>
            </div>
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase">Key Size</dt>
              <dd class="mt-1 text-sm text-gray-900">${cert.publicKeySize || 'N/A'} bits</dd>
            </div>
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase">Signature</dt>
              <dd class="mt-1 text-sm text-gray-900">${cert.signatureAlgorithm || 'N/A'}</dd>
            </div>
          </div>
          <div class="space-y-2">
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase">SHA-1 Fingerprint</dt>
              <dd class="mt-1 text-xs font-mono text-gray-700 break-all">${cert.sha1Fingerprint || 'N/A'}</dd>
            </div>
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase">SHA-256 Fingerprint</dt>
              <dd class="mt-1 text-xs font-mono text-gray-700 break-all">${cert.sha256Fingerprint || 'N/A'}</dd>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // Signer Information
  if (parsedSOD.signerInfo) {
    const signer = parsedSOD.signerInfo;
    html += `
      <div class="bg-white border border-gray-200 rounded-lg overflow-hidden">
        <div class="bg-gray-50 px-4 py-3 border-b border-gray-200">
          <h4 class="text-sm font-semibold text-gray-900">Signer Information</h4>
        </div>
        <div class="p-4">
          <div class="grid grid-cols-2 gap-3">
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase">Digest Algorithm</dt>
              <dd class="mt-1 text-sm text-gray-900">${signer.digestAlgorithmName || signer.digestAlgorithmOid || 'N/A'}</dd>
            </div>
            <div class="bg-gray-50 rounded-lg p-3">
              <dt class="text-xs font-medium text-gray-500 uppercase">Signature Algorithm</dt>
              <dd class="mt-1 text-sm text-gray-900">${signer.signatureAlgorithmName || signer.signatureAlgorithmOid || 'N/A'}</dd>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  html += '</div>';
  container.innerHTML = html;
  console.log("SOD information rendered successfully");
}
