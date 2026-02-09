/**
 * Document Data Export Module
 * Handles exporting passport data to file system
 */

/**
 * Export passport data to file system
 * @param {string} btnId - ID of the export button (for UI state management)
 */
async function exportPassportData(btnId) {
  console.log('exportPassportData() called with btnId:', btnId);

  // Get button elements
  const button = document.getElementById(btnId);
  const spinner = document.getElementById(btnId.replace('-btn', '-spinner'));
  const buttonText = document.getElementById(btnId.replace('-btn', '-text'));

  if (!button) {
    console.error('Export button not found:', btnId);
    Toast.error('Button element not found');
    return;
  }

  // Update button state - show loading
  button.disabled = true;
  if (spinner) spinner.classList.remove('hidden');
  if (buttonText) buttonText.textContent = 'Exporting...';

  try {
    // Send POST request to export endpoint
    const response = await fetch('/passport/export-data', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const result = await response.json();
    console.log('Export result:', result);

    if (result.success) {
      // Show success message with export path
      const exportPath = result.exportPath || 'Unknown location';
      const passportNumber = result.passportNumber || 'Unknown';

      Toast.success(
        `Passport data exported successfully!\nPassport: ${passportNumber}\nLocation: ${exportPath}`,
        7000  // Show for 7 seconds
      );

      console.log('Export successful:', exportPath);
    } else {
      // Show error message from server
      const errorMessage = result.message || 'Export failed';
      Toast.error(errorMessage, 7000);
      console.error('Export failed:', errorMessage);
    }

  } catch (error) {
    console.error('Export request failed:', error);
    Toast.error(`Export failed: ${error.message}`, 7000);

  } finally {
    // Restore button state
    button.disabled = false;
    if (spinner) spinner.classList.add('hidden');
    if (buttonText) buttonText.textContent = 'Export Data';
  }
}

/**
 * Show export confirmation dialog (optional enhancement)
 * @param {string} btnId - ID of the export button
 * @returns {Promise<boolean>} - true if user confirms, false otherwise
 */
async function confirmExport(btnId) {
  return new Promise((resolve) => {
    const confirmed = confirm('Export passport data to file system?');
    resolve(confirmed);
  });
}

// Expose globally
window.exportPassportData = exportPassportData;
window.confirmExport = confirmExport;
