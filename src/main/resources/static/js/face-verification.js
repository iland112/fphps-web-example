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

  // Bounding Box 좌표
  const docBbox = docQuality?.bbox;
  const chipBbox = chipQuality?.bbox;

  // 디버깅: 이미지 데이터 확인
  console.log('Document Image exists:', !!docImage, docImage ? 'Length: ' + docImage.length : 'No data');
  console.log('Chip Image exists:', !!chipImage, chipImage ? 'Length: ' + chipImage.length : 'No data');
  console.log('Document BBox:', docBbox);
  console.log('Chip BBox:', chipBbox);

  // Match Score 시각적 개선
  const scorePercent = (matchScore * 100).toFixed(1);
  const thresholdPercent = (threshold * 100).toFixed(1);
  const isPassed = matchScore >= threshold;

  const html = '<div class="space-y-4">' +
    // Status Summary Card - Consistent design
    '<div class="group relative bg-gradient-to-r from-' + statusColor + '-50 to-' + statusColor + '-100 border border-' + statusColor + '-200 rounded-2xl shadow-sm hover:shadow-lg transition-all duration-300 overflow-hidden p-6">' +
      '<div class="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-white/20 to-white/5 rounded-bl-full"></div>' +
      '<div class="relative flex items-center justify-between">' +
        '<div class="flex items-center gap-4">' +
          '<div class="flex size-14 items-center justify-center rounded-full bg-white dark:bg-neutral-800 shadow-sm dark:shadow-neutral-900/30 group-hover:shadow-md transition-shadow">' +
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

    // Visual Match Score Bar - Consistent design
    '<div class="group relative bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 rounded-2xl shadow-sm dark:shadow-neutral-900/30 hover:shadow-lg transition-all duration-300 overflow-hidden p-6 h-fit">' +
      '<div class="absolute top-0 right-0 w-24 h-24 bg-gradient-to-br from-indigo-500/10 to-blue-500/10 rounded-bl-full"></div>' +
      '<div class="relative">' +
        '<div class="flex items-center gap-2 mb-4">' +
          '<div class="flex size-8 items-center justify-center rounded-lg bg-indigo-100 dark:bg-indigo-900/30 group-hover:bg-indigo-200 dark:group-hover:bg-indigo-900/50 transition-colors">' +
            '<svg class="size-4 text-indigo-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">' +
              '<path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" />' +
            '</svg>' +
          '</div>' +
          '<h4 class="text-sm font-semibold text-gray-900 dark:text-neutral-100">Similarity Analysis</h4>' +
          '<span class="ml-auto text-xs px-2 py-1 rounded-full ' + (isPassed ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300') + '">' +
            (isPassed ? 'PASSED' : 'FAILED') + ' (Threshold: ' + thresholdPercent + '%)' +
          '</span>' +
        '</div>' +
        '<div class="relative">' +
          // Progress Bar Background
          '<div class="w-full bg-gray-100 dark:bg-neutral-700 rounded-full h-10 relative overflow-hidden">' +
            // Threshold Marker
            '<div class="absolute top-0 bottom-0 border-l-2 border-dashed border-gray-400 dark:border-neutral-500" style="left: ' + thresholdPercent + '%"></div>' +
            // Match Score Bar
            '<div class="bg-gradient-to-r from-blue-500 via-blue-600 to-indigo-600 h-10 rounded-full transition-all duration-700 flex items-center justify-center" style="width: ' + scorePercent + '%">' +
              '<span class="text-sm font-bold text-white px-3">' + scorePercent + '%</span>' +
            '</div>' +
          '</div>' +
          // Labels
          '<div class="flex justify-between mt-2 px-1">' +
            '<span class="text-sm text-gray-500 dark:text-neutral-400">0%</span>' +
            '<span class="text-sm text-gray-600 dark:text-neutral-400 font-medium">&larr; Threshold: ' + thresholdPercent + '%</span>' +
            '<span class="text-sm text-gray-500 dark:text-neutral-400">100%</span>' +
          '</div>' +
        '</div>' +
      '</div>' +
    '</div>' +

    // Photo Quality Cards - Consistent design with other tabs
    '<div class="grid grid-cols-2 gap-4">' +
      // Document Photo Quality: Text | Image
      '<div class="group relative bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 rounded-2xl shadow-sm dark:shadow-neutral-900/30 hover:shadow-lg transition-all duration-300 overflow-hidden p-5 h-fit">' +
        '<div class="absolute top-0 right-0 w-24 h-24 bg-gradient-to-br from-purple-500/10 to-pink-500/10 rounded-bl-full"></div>' +
        '<div class="relative">' +
          '<div class="flex items-center gap-2 mb-4">' +
            '<div class="flex size-8 items-center justify-center rounded-lg bg-purple-100 dark:bg-purple-900/30 group-hover:bg-purple-200 dark:group-hover:bg-purple-900/50 transition-colors">' +
              '<svg class="size-4 text-purple-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">' +
                '<path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />' +
              '</svg>' +
            '</div>' +
            '<h4 class="text-sm font-semibold text-gray-900 dark:text-neutral-100">Document Photo Quality</h4>' +
          '</div>' +
          '<div class="flex gap-3">' +
            '<div class="flex-1">' + renderQualityMetrics(docQuality) + '</div>' +
            '<div class="flex flex-col items-center gap-2">' +
              (docImage ?
                '<div class="relative border border-gray-200 dark:border-neutral-700 rounded-xl overflow-hidden bg-gray-50 dark:bg-neutral-700/50 p-2">' +
                  '<img id="doc-photo-img" src="' + docImage + '" alt="Document Photo" class="w-36 h-44 object-contain bg-white dark:bg-neutral-800 rounded-lg shadow-sm dark:shadow-neutral-900/30" style="display:block;">' +
                  '<canvas id="doc-photo-canvas" class="absolute top-2 left-2 rounded-lg" style="display:none; pointer-events:none;"></canvas>' +
                '</div>' +
                '<button onclick="toggleFaceBox(\'doc\')" class="px-3 py-1 text-xs font-medium text-gray-700 dark:text-neutral-300 bg-white dark:bg-neutral-800 border border-gray-300 dark:border-neutral-600 rounded-lg hover:bg-gray-50 dark:hover:bg-neutral-700 transition-colors">' +
                  '<span id="doc-toggle-text">Show Face Box</span>' +
                '</button>'
                : '<div class="w-36 h-44 bg-gray-100 dark:bg-neutral-700 rounded flex items-center justify-center"><span class="text-gray-400 dark:text-neutral-500 text-sm">No image</span></div>') +
            '</div>' +
          '</div>' +
        '</div>' +
      '</div>' +
      // Chip Photo Quality: Image | Text
      '<div class="group relative bg-white dark:bg-neutral-800 border border-gray-200 dark:border-neutral-700 rounded-2xl shadow-sm dark:shadow-neutral-900/30 hover:shadow-lg transition-all duration-300 overflow-hidden p-5 h-fit">' +
        '<div class="absolute top-0 right-0 w-24 h-24 bg-gradient-to-br from-cyan-500/10 to-blue-500/10 rounded-bl-full"></div>' +
        '<div class="relative">' +
          '<div class="flex items-center gap-2 mb-4">' +
            '<div class="flex size-8 items-center justify-center rounded-lg bg-cyan-100 dark:bg-cyan-900/30 group-hover:bg-cyan-200 dark:group-hover:bg-cyan-900/50 transition-colors">' +
              '<svg class="size-4 text-cyan-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">' +
                '<path stroke-linecap="round" stroke-linejoin="round" d="M8.25 3v1.5M4.5 8.25H3m18 0h-1.5M4.5 12H3m18 0h-1.5m-15 3.75H3m18 0h-1.5M8.25 19.5V21M12 3v1.5m0 15V21m3.75-18v1.5m0 15V21m-9-1.5h10.5a2.25 2.25 0 002.25-2.25V6.75a2.25 2.25 0 00-2.25-2.25H6.75A2.25 2.25 0 004.5 6.75v10.5a2.25 2.25 0 002.25 2.25zm.75-12h9v9h-9v-9z" />' +
              '</svg>' +
            '</div>' +
            '<h4 class="text-sm font-semibold text-gray-900 dark:text-neutral-100">Chip Photo Quality</h4>' +
          '</div>' +
          '<div class="flex gap-3">' +
            '<div class="flex flex-col items-center gap-2">' +
              (chipImage ?
                '<div class="relative border border-gray-200 dark:border-neutral-700 rounded-xl overflow-hidden bg-gray-50 dark:bg-neutral-700/50 p-2">' +
                  '<img id="chip-photo-img" src="' + chipImage + '" alt="Chip Photo" class="w-36 h-44 object-contain bg-white dark:bg-neutral-800 rounded-lg shadow-sm dark:shadow-neutral-900/30" style="display:block;">' +
                  '<canvas id="chip-photo-canvas" class="absolute top-2 left-2 rounded-lg" style="display:none; pointer-events:none;"></canvas>' +
                '</div>' +
                '<button onclick="toggleFaceBox(\'chip\')" class="px-3 py-1 text-xs font-medium text-gray-700 dark:text-neutral-300 bg-white dark:bg-neutral-800 border border-gray-300 dark:border-neutral-600 rounded-lg hover:bg-gray-50 dark:hover:bg-neutral-700 transition-colors">' +
                  '<span id="chip-toggle-text">Show Face Box</span>' +
                '</button>'
                : '<div class="w-36 h-44 bg-gray-100 dark:bg-neutral-700 rounded flex items-center justify-center"><span class="text-gray-400 dark:text-neutral-500 text-sm">No image</span></div>') +
            '</div>' +
            '<div class="flex-1">' + renderQualityMetrics(chipQuality) + '</div>' +
          '</div>' +
        '</div>' +
      '</div>' +
    '</div>' +
    (data.errors && data.errors.length > 0 ? renderFaceErrors(data.errors) : '') +
  '</div>';

  container.innerHTML = html;

  // Store bbox data for toggle function
  window.faceVerificationData = {
    docBbox: docBbox,
    chipBbox: chipBbox,
    docImage: docImage,
    chipImage: chipImage
  };

  // Initialize canvases after images load
  if (docImage && docBbox) {
    initializeFaceBoxCanvas('doc', docImage, docBbox);
  }
  if (chipImage && chipBbox) {
    initializeFaceBoxCanvas('chip', chipImage, chipBbox);
  }
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
    badgeClass = 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 border-green-200 dark:border-green-800';
    badgeText = '✓ Good';
  } else if (avgScore >= 0.40) {
    badgeClass = 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300 border-yellow-200 dark:border-yellow-800';
    badgeText = '⚠ Fair';
  } else {
    badgeClass = 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 border-red-200 dark:border-red-800';
    badgeText = '✗ Poor';
  }
  return '<div class="inline-flex items-center px-3 py-1.5 rounded-full text-base font-semibold border ' + badgeClass + '">' + badgeText + '</div>';
}

function renderQualityBar(label, score, icon) {
  const percent = (score * 100).toFixed(1);
  const color = getQualityColor(score);
  const gradient = getQualityGradient(score);

  return '<div class="mb-3">' +
    '<div class="flex items-center justify-between mb-1.5">' +
      '<span class="text-base font-medium text-gray-700 dark:text-neutral-300">' + icon + ' ' + label + '</span>' +
      '<span class="text-base font-bold text-' + color + '-700">' + percent + '%</span>' +
    '</div>' +
    '<div class="w-full bg-gray-200 dark:bg-neutral-600 rounded-full h-2.5 overflow-hidden">' +
      '<div class="bg-gradient-to-r ' + gradient + ' h-2.5 rounded-full transition-all duration-500" style="width: ' + percent + '%"></div>' +
    '</div>' +
  '</div>';
}

function renderQualityMetrics(quality) {
  if (!quality) {
    return '<p class="text-xs text-gray-500 dark:text-neutral-400">Quality metrics not available</p>';
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

  let html = '<div class="mb-3">' + getOverallQualityBadge(avgScore) + '</div>';

  html += '<div>';
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
  return '<div class="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4">' +
    '<h4 class="text-sm font-semibold text-red-900 dark:text-red-200 mb-2">Errors</h4>' +
    '<ul class="list-disc list-inside text-xs text-red-700 dark:text-red-300 space-y-1">' +
      errors.map(function(err) { return '<li>' + escapeHtml(err) + '</li>'; }).join('') +
    '</ul>' +
  '</div>';
}

function renderFaceErrorCard(title, message) {
  return '<div class="bg-red-50 dark:bg-red-900/20 border-l-4 border-red-400 dark:border-red-500 p-4 rounded-lg">' +
    '<div class="flex items-center">' +
      '<div class="flex-shrink-0">' +
        '<svg class="size-5 text-red-400 dark:text-red-500" fill="currentColor" viewBox="0 0 20 20">' +
          '<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>' +
        '</svg>' +
      '</div>' +
      '<div class="ml-3">' +
        '<h3 class="text-sm font-medium text-red-800 dark:text-red-300">' + escapeHtml(title) + '</h3>' +
        '<p class="mt-1 text-sm text-red-700 dark:text-red-300">' + escapeHtml(message) + '</p>' +
      '</div>' +
    '</div>' +
  '</div>';
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

/**
 * Initialize canvas for face bounding box overlay
 */
function initializeFaceBoxCanvas(type, imageBase64, bbox) {
  const imgId = type + '-photo-img';
  const canvasId = type + '-photo-canvas';

  const img = document.getElementById(imgId);
  const canvas = document.getElementById(canvasId);

  if (!img || !canvas || !bbox) {
    console.warn('Cannot initialize face box canvas:', type, 'Missing elements or bbox');
    return;
  }

  // Wait for image to load
  if (img.complete) {
    setupCanvas();
  } else {
    img.onload = setupCanvas;
  }

  function setupCanvas() {
    // Get actual rendered image dimensions
    const imgRect = img.getBoundingClientRect();
    const imgWidth = img.naturalWidth;
    const imgHeight = img.naturalHeight;
    const displayWidth = img.width;
    const displayHeight = img.height;

    // Set canvas size to match displayed image
    canvas.width = displayWidth;
    canvas.height = displayHeight;

    // Calculate scale factor
    const scaleX = displayWidth / imgWidth;
    const scaleY = displayHeight / imgHeight;

    // Store bbox and scale for drawing
    canvas.dataset.bboxX1 = bbox.x1;
    canvas.dataset.bboxY1 = bbox.y1;
    canvas.dataset.bboxX2 = bbox.x2;
    canvas.dataset.bboxY2 = bbox.y2;
    canvas.dataset.scaleX = scaleX;
    canvas.dataset.scaleY = scaleY;

    console.log('Canvas initialized for', type, 'bbox:', bbox, 'scale:', scaleX, scaleY);
  }
}

/**
 * Toggle face bounding box visibility
 */
function toggleFaceBox(type) {
  const canvasId = type + '-photo-canvas';
  const toggleTextId = type + '-toggle-text';

  const canvas = document.getElementById(canvasId);
  const toggleText = document.getElementById(toggleTextId);

  if (!canvas || !toggleText) {
    console.warn('Cannot toggle face box:', type);
    return;
  }

  const isVisible = canvas.style.display !== 'none';

  if (isVisible) {
    // Hide canvas
    canvas.style.display = 'none';
    toggleText.textContent = 'Show Face Box';
  } else {
    // Show and draw canvas
    canvas.style.display = 'block';
    drawFaceBox(canvas);
    toggleText.textContent = 'Hide Face Box';
  }
}

/**
 * Draw face bounding box on canvas
 */
function drawFaceBox(canvas) {
  const ctx = canvas.getContext('2d');

  // Get stored bbox and scale
  const x1 = parseFloat(canvas.dataset.bboxX1);
  const y1 = parseFloat(canvas.dataset.bboxY1);
  const x2 = parseFloat(canvas.dataset.bboxX2);
  const y2 = parseFloat(canvas.dataset.bboxY2);
  const scaleX = parseFloat(canvas.dataset.scaleX);
  const scaleY = parseFloat(canvas.dataset.scaleY);

  // Clear canvas
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Calculate scaled coordinates
  const scaledX1 = x1 * scaleX;
  const scaledY1 = y1 * scaleY;
  const scaledX2 = x2 * scaleX;
  const scaledY2 = y2 * scaleY;
  const width = scaledX2 - scaledX1;
  const height = scaledY2 - scaledY1;

  // Draw semi-transparent overlay outside face box
  ctx.fillStyle = 'rgba(0, 0, 0, 0.3)';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  // Clear the face box area
  ctx.clearRect(scaledX1, scaledY1, width, height);

  // Draw bounding box rectangle
  ctx.strokeStyle = '#10b981'; // Green
  ctx.lineWidth = 3;
  ctx.strokeRect(scaledX1, scaledY1, width, height);

  // Draw corner markers
  const cornerLength = 15;
  ctx.lineWidth = 4;

  // Top-left
  ctx.beginPath();
  ctx.moveTo(scaledX1, scaledY1 + cornerLength);
  ctx.lineTo(scaledX1, scaledY1);
  ctx.lineTo(scaledX1 + cornerLength, scaledY1);
  ctx.stroke();

  // Top-right
  ctx.beginPath();
  ctx.moveTo(scaledX2 - cornerLength, scaledY1);
  ctx.lineTo(scaledX2, scaledY1);
  ctx.lineTo(scaledX2, scaledY1 + cornerLength);
  ctx.stroke();

  // Bottom-left
  ctx.beginPath();
  ctx.moveTo(scaledX1, scaledY2 - cornerLength);
  ctx.lineTo(scaledX1, scaledY2);
  ctx.lineTo(scaledX1 + cornerLength, scaledY2);
  ctx.stroke();

  // Bottom-right
  ctx.beginPath();
  ctx.moveTo(scaledX2 - cornerLength, scaledY2);
  ctx.lineTo(scaledX2, scaledY2);
  ctx.lineTo(scaledX2, scaledY2 - cornerLength);
  ctx.stroke();

  // Draw label
  ctx.font = 'bold 12px sans-serif';
  ctx.fillStyle = '#10b981';
  ctx.fillText('FACE', scaledX1 + 5, scaledY1 - 5);
}
