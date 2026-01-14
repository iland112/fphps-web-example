/**
 * Face Verification JavaScript
 * InsightFace 기반 얼굴 검증 함수들
 */

async function verifyFace(btnId, containerId, emptyStateId) {
  const btn = document.getElementById(btnId);
  const btnSpinner = document.getElementById(btnId + '-spinner');
  const resultContainer = document.getElementById(containerId);
  const emptyState = document.getElementById(emptyStateId);

  btn.disabled = true;
  if (btnSpinner) btnSpinner.classList.remove('hidden');
  if (emptyState) emptyState.classList.add('hidden');

  try {
    const response = await fetch('/passport/verify-face', {
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
      throw new Error('Unexpected response: ' + errorText.substring(0, 200));
    }

    if (!response.ok) {
      const errorMessage = data.message || 'HTTP ' + response.status + ' error';
      throw new Error(errorMessage);
    }

    console.log('Face Verification Result:', data);
    renderFaceVerificationResult(data, resultContainer);

  } catch (error) {
    console.error('Face verification failed:', error);
    resultContainer.innerHTML = renderFaceErrorCard('Face Verification Error', error.message);
  } finally {
    btn.disabled = false;
    if (btnSpinner) btnSpinner.classList.add('hidden');
  }
}

function renderFaceVerificationResult(data, container) {
  const statusColorMap = {
    'MATCHED': 'green',
    'NOT_MATCHED': 'red',
    'ERROR': 'red'
  };
  const statusColor = statusColorMap[data.status] || 'gray';
  const statusIcon = data.status === 'MATCHED' ? '✓' : '✗';
  
  const confidenceColorMap = {
    'HIGH': 'green',
    'MEDIUM': 'yellow',
    'LOW': 'red'
  };
  
  const html = '<div class="space-y-4">' +
    '<div class="bg-' + statusColor + '-50 border-l-4 border-' + statusColor + '-400 p-4 rounded-lg">' +
      '<div class="flex items-center">' +
        '<div class="flex-shrink-0"><span class="text-2xl">' + statusIcon + '</span></div>' +
        '<div class="ml-3">' +
          '<h3 class="text-sm font-medium text-' + statusColor + '-800">Face Verification ' + data.matchStatus + '</h3>' +
          '<p class="mt-1 text-sm text-' + statusColor + '-700">Match Score: ' + (data.matchScore * 100).toFixed(2) + '% (Threshold: ' + (data.threshold * 100).toFixed(2) + '%)</p>' +
          '<p class="text-xs text-' + statusColor + '-600 mt-1">Confidence: ' + data.confidenceLevel + ' | Processing Time: ' + data.processingDurationMs + 'ms</p>' +
        '</div>' +
      '</div>' +
    '</div>' +
    '<div class="bg-white border border-gray-200 rounded-lg p-4">' +
      '<h4 class="text-sm font-semibold text-gray-900 mb-3">Match Score</h4>' +
      '<div class="w-full bg-gray-200 rounded-full h-6 relative">' +
        '<div class="bg-gradient-to-r from-blue-500 to-blue-600 h-6 rounded-full transition-all duration-500" style="width: ' + (data.matchScore * 100) + '%">' +
          '<span class="absolute inset-0 flex items-center justify-center text-xs font-bold text-white">' + (data.matchScore * 100).toFixed(2) + '%</span>' +
        '</div>' +
      '</div>' +
      '<div class="flex justify-between mt-2 text-xs text-gray-500">' +
        '<span>0%</span><span>Threshold: ' + (data.threshold * 100).toFixed(2) + '%</span><span>100%</span>' +
      '</div>' +
    '</div>' +
    '<div class="grid grid-cols-2 gap-4">' +
      '<div class="bg-white border border-gray-200 rounded-lg p-4">' +
        '<h4 class="text-sm font-semibold text-gray-900 mb-3">Document Photo Quality</h4>' +
        renderQualityMetrics(data.documentPhotoQuality) +
      '</div>' +
      '<div class="bg-white border border-gray-200 rounded-lg p-4">' +
        '<h4 class="text-sm font-semibold text-gray-900 mb-3">Chip Photo Quality</h4>' +
        renderQualityMetrics(data.chipPhotoQuality) +
      '</div>' +
    '</div>' +
    (data.errors && data.errors.length > 0 ? renderFaceErrors(data.errors) : '') +
  '</div>';
  
  container.innerHTML = html;
}

function renderQualityMetrics(quality) {
  return '<dl class="space-y-2">' +
    '<div class="flex justify-between text-xs"><dt class="text-gray-500">Detection Score</dt><dd class="font-medium text-gray-900">' + (quality.detectionScore * 100).toFixed(1) + '%</dd></div>' +
    '<div class="flex justify-between text-xs"><dt class="text-gray-500">Face Area Ratio</dt><dd class="font-medium text-gray-900">' + (quality.faceAreaRatio * 100).toFixed(1) + '%</dd></div>' +
    '<div class="flex justify-between text-xs"><dt class="text-gray-500">Brightness</dt><dd class="font-medium text-gray-900">' + (quality.brightnessScore * 100).toFixed(1) + '%</dd></div>' +
    '<div class="flex justify-between text-xs"><dt class="text-gray-500">Sharpness</dt><dd class="font-medium text-gray-900">' + (quality.sharpnessScore * 100).toFixed(1) + '%</dd></div>' +
    '<div class="flex justify-between text-xs"><dt class="text-gray-500">Pose Score</dt><dd class="font-medium text-gray-900">' + (quality.poseScore * 100).toFixed(1) + '%</dd></div>' +
  '</dl>';
}

function renderFaceErrors(errors) {
  return '<div class="bg-red-50 border border-red-200 rounded-lg p-4">' +
    '<h4 class="text-sm font-semibold text-red-900 mb-2">Errors</h4>' +
    '<ul class="list-disc list-inside text-xs text-red-700 space-y-1">' +
      errors.map(function(err) { return '<li>' + escapeHtml(err) + '</li>'; }).join('') +
    '</ul>' +
  '</div>';
}

function renderFaceErrorCard(title, message) {
  return '<div class="bg-red-50 border-l-4 border-red-400 p-4 rounded-lg">' +
    '<div class="flex items-center">' +
      '<div class="flex-shrink-0">' +
        '<svg class="size-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">' +
          '<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>' +
        '</svg>' +
      '</div>' +
      '<div class="ml-3">' +
        '<h3 class="text-sm font-medium text-red-800">' + escapeHtml(title) + '</h3>' +
        '<p class="mt-1 text-sm text-red-700">' + escapeHtml(message) + '</p>' +
      '</div>' +
    '</div>' +
  '</div>';
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}
