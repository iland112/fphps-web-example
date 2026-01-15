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

  // API uses snake_case, handle both snake_case and camelCase
  const matchScore = data.match_score || data.matchScore || 0;
  const matchStatus = data.match_status || data.matchStatus || data.status;
  const confidenceLevel = data.confidence_level || data.confidenceLevel || 'N/A';
  const threshold = data.threshold || 0.4;
  const processingTime = data.processing_duration_ms || data.processingDurationMs || 0;
  const docQuality = data.document_photo_quality || data.documentPhotoQuality;
  const chipQuality = data.chip_photo_quality || data.chipPhotoQuality;

  // 디버깅: quality 객체 확인
  console.log('Document Quality Object:', docQuality);
  console.log('Chip Quality Object:', chipQuality);

  // 이미지는 quality 객체 안에 있음
  const docImage = docQuality?.image_base64 || docQuality?.imageBase64;
  const chipImage = chipQuality?.image_base64 || chipQuality?.imageBase64;

  // 디버깅: 이미지 데이터 확인
  console.log('Document Image exists:', !!docImage, docImage ? 'Length: ' + docImage.length : 'No data');
  console.log('Chip Image exists:', !!chipImage, chipImage ? 'Length: ' + chipImage.length : 'No data');

  // Match Score 시각적 개선
  const scorePercent = (matchScore * 100).toFixed(1);
  const thresholdPercent = (threshold * 100).toFixed(1);
  const isPassed = matchScore >= threshold;

  const html = '<div class="space-y-4">' +
    // Status Summary Card
    '<div class="bg-gradient-to-r from-' + statusColor + '-50 to-' + statusColor + '-100 border border-' + statusColor + '-200 rounded-xl p-6 shadow-sm">' +
      '<div class="flex items-center justify-between">' +
        '<div class="flex items-center gap-4">' +
          '<div class="flex size-14 items-center justify-center rounded-full bg-white shadow-sm">' +
            '<span class="text-3xl">' + statusIcon + '</span>' +
          '</div>' +
          '<div>' +
            '<h3 class="text-lg font-bold text-' + statusColor + '-900">Face Verification ' + matchStatus + '</h3>' +
            '<p class="text-sm text-' + statusColor + '-700 mt-1">Confidence Level: <span class="font-semibold">' + confidenceLevel + '</span> | Processing: ' + processingTime.toFixed(0) + 'ms</p>' +
          '</div>' +
        '</div>' +
        // Large Match Score Display
        '<div class="text-center">' +
          '<div class="text-4xl font-bold text-' + statusColor + '-900">' + scorePercent + '%</div>' +
          '<div class="text-xs text-' + statusColor + '-600 mt-1">Match Score</div>' +
        '</div>' +
      '</div>' +
    '</div>' +

    // Visual Match Score Bar
    '<div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">' +
      '<div class="flex items-center justify-between mb-4">' +
        '<h4 class="text-base font-semibold text-gray-900">Similarity Analysis</h4>' +
        '<span class="text-xs px-2 py-1 rounded-full ' + (isPassed ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700') + '">' +
          (isPassed ? 'PASSED' : 'FAILED') + ' (Threshold: ' + thresholdPercent + '%)' +
        '</span>' +
      '</div>' +
      '<div class="relative">' +
        // Progress Bar Background
        '<div class="w-full bg-gray-100 rounded-full h-10 relative overflow-hidden">' +
          // Threshold Marker
          '<div class="absolute top-0 bottom-0 border-l-2 border-dashed border-gray-400" style="left: ' + thresholdPercent + '%"></div>' +
          // Match Score Bar
          '<div class="bg-gradient-to-r from-blue-500 via-blue-600 to-indigo-600 h-10 rounded-full transition-all duration-700 flex items-center justify-center" style="width: ' + scorePercent + '%">' +
            '<span class="text-sm font-bold text-white px-3">' + scorePercent + '%</span>' +
          '</div>' +
        '</div>' +
        // Labels
        '<div class="flex justify-between mt-2 px-1">' +
          '<span class="text-xs text-gray-400">0%</span>' +
          '<span class="text-xs text-gray-600 font-medium">← Threshold: ' + thresholdPercent + '%</span>' +
          '<span class="text-xs text-gray-400">100%</span>' +
        '</div>' +
      '</div>' +
    '</div>' +

    // Photo Quality Cards
    '<div class="grid grid-cols-2 gap-4">' +
      // Document Photo Quality: Text | Image
      '<div class="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">' +
        '<h4 class="text-sm font-semibold text-gray-900 mb-3">Document Photo Quality</h4>' +
        '<div class="grid grid-cols-2 gap-4">' +
          '<div>' + renderQualityMetrics(docQuality) + '</div>' +
          '<div>' +
            (docImage ? '<div class="rounded-lg overflow-hidden border border-gray-200"><img src="' + docImage + '" alt="Document Photo" class="w-full h-full object-cover"></div>' : '<div class="h-full bg-gray-100 rounded-lg flex items-center justify-center"><span class="text-gray-400 text-xs">No image</span></div>') +
          '</div>' +
        '</div>' +
      '</div>' +
      // Chip Photo Quality: Image | Text
      '<div class="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">' +
        '<h4 class="text-sm font-semibold text-gray-900 mb-3">Chip Photo Quality</h4>' +
        '<div class="grid grid-cols-2 gap-4">' +
          '<div>' +
            (chipImage ? '<div class="rounded-lg overflow-hidden border border-gray-200"><img src="' + chipImage + '" alt="Chip Photo" class="w-full h-full object-cover"></div>' : '<div class="h-full bg-gray-100 rounded-lg flex items-center justify-center"><span class="text-gray-400 text-xs">No image</span></div>') +
          '</div>' +
          '<div>' + renderQualityMetrics(chipQuality) + '</div>' +
        '</div>' +
      '</div>' +
    '</div>' +
    (data.errors && data.errors.length > 0 ? renderFaceErrors(data.errors) : '') +
  '</div>';

  container.innerHTML = html;
}

function getQualityColor(score) {
  if (score >= 0.70) return 'green';
  if (score >= 0.40) return 'yellow';
  return 'red';
}

function getQualityGradient(score) {
  if (score >= 0.70) return 'from-green-400 to-green-600';
  if (score >= 0.40) return 'from-yellow-400 to-yellow-600';
  return 'from-red-400 to-red-600';
}

function getOverallQualityBadge(avgScore) {
  let badgeClass, badgeText;
  if (avgScore >= 0.70) {
    badgeClass = 'bg-green-100 text-green-800 border-green-200';
    badgeText = '✓ Good Quality';
  } else if (avgScore >= 0.40) {
    badgeClass = 'bg-yellow-100 text-yellow-800 border-yellow-200';
    badgeText = '⚠ Fair Quality';
  } else {
    badgeClass = 'bg-red-100 text-red-800 border-red-200';
    badgeText = '✗ Poor Quality';
  }
  return '<div class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border ' + badgeClass + '">' + badgeText + '</div>';
}

function renderQualityBar(label, score, icon) {
  const percent = (score * 100).toFixed(1);
  const color = getQualityColor(score);
  const gradient = getQualityGradient(score);

  return '<div class="mb-2">' +
    '<div class="flex items-center justify-between mb-1">' +
      '<span class="text-xs font-medium text-gray-700">' + icon + ' ' + label + '</span>' +
      '<span class="text-xs font-bold text-' + color + '-700">' + percent + '%</span>' +
    '</div>' +
    '<div class="w-full bg-gray-200 rounded-full h-1.5 overflow-hidden">' +
      '<div class="bg-gradient-to-r ' + gradient + ' h-1.5 rounded-full transition-all duration-500" style="width: ' + percent + '%"></div>' +
    '</div>' +
  '</div>';
}

function renderQualityMetrics(quality) {
  if (!quality) {
    return '<p class="text-xs text-gray-500">Quality metrics not available</p>';
  }

  // Handle both snake_case and camelCase
  const detectionScore = quality.detection_score || quality.detectionScore;
  const faceAreaRatio = quality.face_area_ratio || quality.faceAreaRatio;
  const brightnessScore = quality.brightness_score || quality.brightnessScore;
  const sharpnessScore = quality.sharpness_score || quality.sharpnessScore;
  const poseScore = quality.pose_score || quality.poseScore;

  // Calculate overall quality score (average of all metrics)
  const scores = [];
  if (detectionScore !== undefined && detectionScore !== null) scores.push(detectionScore);
  if (faceAreaRatio !== undefined && faceAreaRatio !== null) scores.push(faceAreaRatio);
  if (brightnessScore !== undefined && brightnessScore !== null) scores.push(brightnessScore);
  if (sharpnessScore !== undefined && sharpnessScore !== null) scores.push(sharpnessScore);
  if (poseScore !== undefined && poseScore !== null) scores.push(poseScore);

  const avgScore = scores.length > 0 ? scores.reduce((a, b) => a + b, 0) / scores.length : 0;

  let html = '<div class="mb-2">' + getOverallQualityBadge(avgScore) + '</div>';

  html += '<div class="space-y-1.5">';
  if (detectionScore !== undefined && detectionScore !== null) {
    html += renderQualityBar('Detection', detectionScore, '🎯');
  }
  if (faceAreaRatio !== undefined && faceAreaRatio !== null) {
    html += renderQualityBar('Size', faceAreaRatio, '📏');
  }
  if (brightnessScore !== undefined && brightnessScore !== null) {
    html += renderQualityBar('Brightness', brightnessScore, '💡');
  }
  if (sharpnessScore !== undefined && sharpnessScore !== null) {
    html += renderQualityBar('Sharpness', sharpnessScore, '🔍');
  }
  if (poseScore !== undefined && poseScore !== null) {
    html += renderQualityBar('Pose', poseScore, '👤');
  }
  html += '</div>';

  return html;
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
