/**
 * 서비스 재시작 모달 및 진행 화면
 * i18n.js 이후 로드되어야 함 (IS_KO 참조)
 */

var _ko = (typeof IS_KO !== 'undefined') ? IS_KO : /^ko\b/i.test(navigator.language);

var _restartI18n = {
  title:       _ko ? '서비스 재시작'                    : 'Restart Service',
  message:     _ko ? '웹 서비스를 재시작하시겠습니까?'   : 'Do you want to restart the web service?',
  detail:      _ko ? '판독기 연결 문제가 발생한 경우 서비스를 재시작하면 해결될 수 있습니다. 재시작 중에는 약 1분간 페이지가 응답하지 않습니다.'
                   : 'Restarting the service may resolve reader connection issues. The page will be unresponsive for about 1 minute during restart.',
  cancel:      _ko ? '취소'                              : 'Cancel',
  restart:     _ko ? '재시작'                            : 'Restart',
  restarting:  _ko ? '서비스 재시작 중...'               : 'Restarting Service...',
  pleaseWait:  _ko ? '서비스를 종료하고 다시 시작합니다. 잠시만 기다려 주세요.' : 'Shutting down and restarting the service. Please wait.',
  reconnecting:_ko ? '서버에 다시 연결하는 중...'        : 'Reconnecting to server...',
  almostDone:  _ko ? '거의 완료되었습니다...'            : 'Almost done...',
};

/**
 * 재시작 확인 모달 표시
 */
function restartService() {
  // 기존 모달 제거
  var existing = document.getElementById('restart-modal-overlay');
  if (existing) existing.remove();

  var overlay = document.createElement('div');
  overlay.id = 'restart-modal-overlay';
  overlay.className = 'fixed inset-0 z-[999] flex items-center justify-center bg-black/50 backdrop-blur-sm';
  overlay.style.animation = 'fadeIn 0.2s ease-out';

  overlay.innerHTML =
    '<div class="bg-white dark:bg-neutral-800 rounded-2xl shadow-2xl max-w-md w-full mx-4 overflow-hidden" style="animation: slideUp 0.3s ease-out">' +
      // 헤더
      '<div class="bg-gradient-to-r from-amber-500 to-orange-500 px-6 py-4">' +
        '<div class="flex items-center gap-3">' +
          '<div class="flex size-10 items-center justify-center rounded-xl bg-white/20">' +
            '<svg class="size-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">' +
              '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>' +
            '</svg>' +
          '</div>' +
          '<h3 class="text-lg font-bold text-white">' + _restartI18n.title + '</h3>' +
        '</div>' +
      '</div>' +
      // 본문
      '<div class="px-6 py-5">' +
        '<p class="text-sm font-medium text-gray-900 dark:text-neutral-100 mb-2">' + _restartI18n.message + '</p>' +
        '<p class="text-xs text-gray-500 dark:text-neutral-400 leading-relaxed">' + _restartI18n.detail + '</p>' +
      '</div>' +
      // 버튼
      '<div class="flex items-center gap-3 px-6 py-4 bg-gray-50 dark:bg-neutral-700/50 border-t border-gray-200 dark:border-neutral-700">' +
        '<button id="restart-cancel-btn" type="button" ' +
          'class="flex-1 inline-flex items-center justify-center gap-x-2 rounded-xl border border-gray-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-4 py-2.5 text-sm font-semibold text-gray-700 dark:text-neutral-300 hover:bg-gray-50 dark:hover:bg-neutral-700 transition-all">' +
          _restartI18n.cancel +
        '</button>' +
        '<button id="restart-confirm-btn" type="button" ' +
          'class="flex-1 inline-flex items-center justify-center gap-x-2 rounded-xl bg-gradient-to-r from-amber-500 to-orange-500 px-4 py-2.5 text-sm font-semibold text-white hover:from-amber-600 hover:to-orange-600 shadow-sm transition-all">' +
          '<svg class="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">' +
            '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>' +
          '</svg>' +
          _restartI18n.restart +
        '</button>' +
      '</div>' +
    '</div>';

  document.body.appendChild(overlay);

  // 취소 버튼
  document.getElementById('restart-cancel-btn').addEventListener('click', function() {
    overlay.style.animation = 'fadeOut 0.2s ease-in forwards';
    setTimeout(function() { overlay.remove(); }, 200);
  });

  // 오버레이 클릭으로 닫기
  overlay.addEventListener('click', function(e) {
    if (e.target === overlay) {
      overlay.style.animation = 'fadeOut 0.2s ease-in forwards';
      setTimeout(function() { overlay.remove(); }, 200);
    }
  });

  // 재시작 확인 버튼
  document.getElementById('restart-confirm-btn').addEventListener('click', function() {
    executeRestart(overlay);
  });
}

/**
 * 재시작 실행 및 진행 화면 전환
 */
function executeRestart(overlay) {
  // 모달을 진행 화면으로 전환
  overlay.innerHTML =
    '<div class="bg-white dark:bg-neutral-800 rounded-2xl shadow-2xl max-w-md w-full mx-4 overflow-hidden">' +
      '<div class="px-8 py-10 text-center">' +
        // 스피너
        '<div class="relative mx-auto mb-6 size-20">' +
          '<div class="absolute inset-0 rounded-full border-4 border-gray-200 dark:border-neutral-700"></div>' +
          '<div class="absolute inset-0 rounded-full border-4 border-amber-500 border-t-transparent animate-spin"></div>' +
          '<div class="absolute inset-0 flex items-center justify-center">' +
            '<svg class="size-8 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">' +
              '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>' +
            '</svg>' +
          '</div>' +
        '</div>' +
        '<h3 class="text-lg font-bold text-gray-900 dark:text-neutral-100 mb-2">' + _restartI18n.restarting + '</h3>' +
        '<p id="restart-status-text" class="text-sm text-gray-500 dark:text-neutral-400">' + _restartI18n.pleaseWait + '</p>' +
        // 프로그레스 바
        '<div class="mt-6 w-full bg-gray-200 dark:bg-neutral-700 rounded-full h-1.5 overflow-hidden">' +
          '<div id="restart-progress" class="bg-gradient-to-r from-amber-500 to-orange-500 h-full rounded-full transition-all duration-1000" style="width:10%"></div>' +
        '</div>' +
      '</div>' +
    '</div>';

  // API 호출
  fetch('/service-restart', { method: 'POST' })
    .then(function(resp) { return resp.json(); })
    .catch(function() { /* 서버가 이미 종료 */ });

  // 프로그레스 바 애니메이션
  var progress = document.getElementById('restart-progress');
  var statusText = document.getElementById('restart-status-text');
  var step = 0;

  var progressTimer = setInterval(function() {
    step++;
    if (step <= 5) {
      if (progress) progress.style.width = (10 + step * 10) + '%';
    } else if (step === 6) {
      if (progress) progress.style.width = '70%';
      if (statusText) statusText.textContent = _restartI18n.reconnecting;
    } else if (step > 10) {
      if (progress) progress.style.width = '85%';
      if (statusText) statusText.textContent = _restartI18n.almostDone;
    }
  }, 1000);

  // 5초 후부터 서버 폴링
  setTimeout(function pollServer() {
    fetch('/device-status')
      .then(function(resp) {
        if (resp.ok) {
          clearInterval(progressTimer);
          if (progress) progress.style.width = '100%';
          if (statusText) {
            statusText.textContent = _ko ? '재시작 완료!' : 'Restart complete!';
            statusText.className = 'text-sm text-green-600 dark:text-green-400 font-semibold';
          }
          setTimeout(function() { location.href = '/'; }, 1000);
        } else {
          setTimeout(pollServer, 1500);
        }
      })
      .catch(function() {
        setTimeout(pollServer, 1500);
      });
  }, 5000);
}

// CSS 애니메이션 주입
(function() {
  if (document.getElementById('restart-modal-styles')) return;
  var style = document.createElement('style');
  style.id = 'restart-modal-styles';
  style.textContent =
    '@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }' +
    '@keyframes fadeOut { from { opacity: 1; } to { opacity: 0; } }' +
    '@keyframes slideUp { from { opacity: 0; transform: translateY(20px) scale(0.95); } to { opacity: 1; transform: translateY(0) scale(1); } }';
  document.head.appendChild(style);
})();
