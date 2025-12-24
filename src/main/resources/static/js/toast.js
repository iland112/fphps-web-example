/**
 * Toast Notification System
 * Provides non-intrusive notifications for success, error, warning, and info messages
 */

const Toast = {
  container: null,
  defaultDuration: 5000,

  /**
   * Initialize the toast container
   */
  init() {
    if (this.container) return;

    this.container = document.createElement('div');
    this.container.id = 'toast-container';
    this.container.className = 'fixed top-4 right-4 z-50 flex flex-col gap-3 max-w-sm w-full pointer-events-none';
    document.body.appendChild(this.container);
  },

  /**
   * Show a toast notification
   * @param {string} message - The message to display
   * @param {string} type - Type of toast: 'success', 'error', 'warning', 'info'
   * @param {number} duration - Duration in milliseconds (0 for persistent)
   */
  show(message, type = 'info', duration = this.defaultDuration) {
    this.init();

    const toast = document.createElement('div');
    toast.className = `pointer-events-auto transform transition-all duration-300 ease-out translate-x-full opacity-0`;

    const config = this.getTypeConfig(type);

    toast.innerHTML = `
      <div class="flex items-start gap-3 ${config.bgClass} border ${config.borderClass} rounded-lg p-4 shadow-lg">
        <div class="shrink-0">
          ${config.icon}
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium ${config.titleClass}">${config.title}</p>
          <p class="mt-1 text-sm ${config.messageClass} break-words">${this.escapeHtml(message)}</p>
        </div>
        <button type="button" class="shrink-0 ${config.closeClass} hover:opacity-70 transition-opacity" onclick="Toast.dismiss(this.closest('[data-toast]'))">
          <svg class="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>
    `;

    toast.setAttribute('data-toast', '');
    this.container.appendChild(toast);

    // Trigger animation
    requestAnimationFrame(() => {
      toast.classList.remove('translate-x-full', 'opacity-0');
      toast.classList.add('translate-x-0', 'opacity-100');
    });

    // Auto dismiss
    if (duration > 0) {
      setTimeout(() => this.dismiss(toast), duration);
    }

    return toast;
  },

  /**
   * Dismiss a toast
   * @param {HTMLElement} toast - The toast element to dismiss
   */
  dismiss(toast) {
    if (!toast || !toast.parentNode) return;

    toast.classList.remove('translate-x-0', 'opacity-100');
    toast.classList.add('translate-x-full', 'opacity-0');

    setTimeout(() => {
      if (toast.parentNode) {
        toast.parentNode.removeChild(toast);
      }
    }, 300);
  },

  /**
   * Dismiss all toasts
   */
  dismissAll() {
    if (!this.container) return;
    const toasts = this.container.querySelectorAll('[data-toast]');
    toasts.forEach(toast => this.dismiss(toast));
  },

  /**
   * Show success toast
   */
  success(message, duration) {
    return this.show(message, 'success', duration);
  },

  /**
   * Show error toast
   */
  error(message, duration) {
    return this.show(message, 'error', duration);
  },

  /**
   * Show warning toast
   */
  warning(message, duration) {
    return this.show(message, 'warning', duration);
  },

  /**
   * Show info toast
   */
  info(message, duration) {
    return this.show(message, 'info', duration);
  },

  /**
   * Get configuration for toast type
   */
  getTypeConfig(type) {
    const configs = {
      success: {
        title: 'Success',
        bgClass: 'bg-green-50',
        borderClass: 'border-green-200',
        titleClass: 'text-green-800',
        messageClass: 'text-green-700',
        closeClass: 'text-green-500',
        icon: `<svg class="size-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/>
        </svg>`
      },
      error: {
        title: 'Error',
        bgClass: 'bg-red-50',
        borderClass: 'border-red-200',
        titleClass: 'text-red-800',
        messageClass: 'text-red-700',
        closeClass: 'text-red-500',
        icon: `<svg class="size-5 text-red-500" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/>
        </svg>`
      },
      warning: {
        title: 'Warning',
        bgClass: 'bg-yellow-50',
        borderClass: 'border-yellow-200',
        titleClass: 'text-yellow-800',
        messageClass: 'text-yellow-700',
        closeClass: 'text-yellow-500',
        icon: `<svg class="size-5 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>
        </svg>`
      },
      info: {
        title: 'Info',
        bgClass: 'bg-blue-50',
        borderClass: 'border-blue-200',
        titleClass: 'text-blue-800',
        messageClass: 'text-blue-700',
        closeClass: 'text-blue-500',
        icon: `<svg class="size-5 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z" clip-rule="evenodd"/>
        </svg>`
      }
    };

    return configs[type] || configs.info;
  },

  /**
   * Escape HTML to prevent XSS
   */
  escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
};

// HTMX Integration - Listen for response errors
document.addEventListener('htmx:responseError', function(event) {
  const xhr = event.detail.xhr;
  let message = 'An error occurred while processing your request.';

  // Try to get error message from response header
  const errorMessage = xhr.getResponseHeader('HX-Error-Message');
  if (errorMessage) {
    message = errorMessage;
  } else if (xhr.status === 0) {
    message = 'Network error. Please check your connection.';
  } else if (xhr.status === 500) {
    message = 'Server error. Please try again later.';
  }

  Toast.error(message);
});

// Listen for custom toast events from HTMX responses
document.addEventListener('htmx:afterRequest', function(event) {
  const xhr = event.detail.xhr;

  // Check for toast trigger header
  const toastMessage = xhr.getResponseHeader('HX-Trigger-Toast');
  const toastType = xhr.getResponseHeader('HX-Trigger-Toast-Type') || 'info';

  if (toastMessage) {
    Toast.show(decodeURIComponent(toastMessage), toastType);
  }
});

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', function() {
  Toast.init();
});

// Expose globally
window.Toast = Toast;
