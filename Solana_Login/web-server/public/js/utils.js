/**
 * Utility functions for SolanaLogin
 */

// Signature handling utilities
const SignatureUtils = {
  /**
   * Process signature result from Phantom wallet
   * @param {any} signResult - The result from Phantom's signMessage
   * @returns {string} - The processed signature string
   */
  processSignature: function(signResult) {
    console.log('Processing signature result...');
    
    // Initialize signature variable
    let signature = '';
    let signatureBytes;
    
    // Inspect what we got back
    console.log('Signature result type:', typeof signResult);
    if (typeof signResult === 'object') {
      console.log('Signature result keys:', Object.keys(signResult).join(', '));
    }
    
    // Different ways to handle the signature based on what Phantom returns
    if (signResult instanceof Uint8Array) {
      // Case 1: Phantom returned a Uint8Array directly
      signatureBytes = signResult;
      console.log('Got Uint8Array signature of length', signatureBytes.length);
    } else if (typeof signResult === 'object' && signResult.signature) {
      // Case 2: Phantom returned an object with a signature property
      signatureBytes = signResult.signature;
      console.log('Got signature from result.signature property');
    } else if (typeof signResult === 'string') {
      // Case 3: Phantom returned a string
      signature = signResult;
      console.log('Got string signature of length', signature.length);
    } else {
      // Case 4: Unknown format, try to stringify it
      console.log('Unknown signature format:', JSON.stringify(signResult));
      signature = JSON.stringify(signResult);
    }
    
    // If we have signature bytes, convert them to a string
    if (signatureBytes && !signature) {
      try {
        // Try bs58 encoding if available
        if (typeof bs58 !== 'undefined' && bs58.encode) {
          signature = bs58.encode(signatureBytes);
          console.log('Converted signature using bs58');
        } else {
          // Try to convert to base64
          signature = this.bytesToBase64(signatureBytes);
          console.log('Converted signature to base64, length:', signature.length);
        }
      } catch (e) {
        console.error('Error converting signature:', e);
        // Fallback to hex
        signature = this.bytesToHex(signatureBytes);
        console.log('Fallback: Converted signature to hex, length:', signature.length);
      }
    }
    
    // Ensure we have a signature
    if (!signature) {
      throw new Error('Failed to get a valid signature from Phantom');
    }
    
    console.log('Final signature length:', signature.length);
    return signature;
  },
  
  /**
   * Convert Uint8Array to base64 string
   * @param {Uint8Array} bytes - The bytes to convert
   * @returns {string} - Base64 encoded string
   */
  bytesToBase64: function(bytes) {
    return btoa(Array.from(bytes).map(b => String.fromCharCode(b)).join(''));
  },
  
  /**
   * Convert Uint8Array to hex string
   * @param {Uint8Array} bytes - The bytes to convert
   * @returns {string} - Hex encoded string
   */
  bytesToHex: function(bytes) {
    return Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('');
  }
};

// UI utilities
const UIUtils = {
  /**
   * Show error message
   * @param {string} message - The error message
   * @param {HTMLElement} errorEl - The error element
   * @param {HTMLElement} errorMessageEl - The error message element
   * @param {HTMLElement} loadingEl - The loading element
   * @param {HTMLElement} contentEl - The content element
   * @param {HTMLElement} successEl - The success element
   */
  showError: function(message, errorEl, errorMessageEl, loadingEl, contentEl, successEl) {
    console.error('Error:', message);
    
    if (errorMessageEl) errorMessageEl.textContent = message;
    
    if (loadingEl) loadingEl.style.display = 'none';
    if (contentEl) contentEl.style.display = 'none';
    if (successEl) successEl.style.display = 'none';
    if (errorEl) errorEl.style.display = 'block';
  },
  
  /**
   * Show success message
   * @param {string} walletAddress - The wallet address
   * @param {HTMLElement} successEl - The success element
   * @param {HTMLElement} walletAddressEl - The wallet address element
   * @param {HTMLElement} loadingEl - The loading element
   * @param {HTMLElement} contentEl - The content element
   * @param {HTMLElement} errorEl - The error element
   */
  showSuccess: function(walletAddress, successEl, walletAddressEl, loadingEl, contentEl, errorEl) {
    console.log('Success! Wallet connected:', walletAddress);
    
    if (walletAddressEl) walletAddressEl.textContent = walletAddress;
    
    if (loadingEl) loadingEl.style.display = 'none';
    if (contentEl) contentEl.style.display = 'none';
    if (errorEl) errorEl.style.display = 'none';
    if (successEl) successEl.style.display = 'block';
  },
  
  /**
   * Show loading state
   * @param {HTMLElement} loadingEl - The loading element
   * @param {HTMLElement} contentEl - The content element
   * @param {HTMLElement} errorEl - The error element
   * @param {HTMLElement} successEl - The success element
   */
  showLoading: function(loadingEl, contentEl, errorEl, successEl) {
    console.log('Loading...');
    
    if (contentEl) contentEl.style.display = 'none';
    if (errorEl) errorEl.style.display = 'none';
    if (successEl) successEl.style.display = 'none';
    if (loadingEl) loadingEl.style.display = 'block';
  }
};

// Phantom wallet utilities
const PhantomUtils = {
  /**
   * Check if Phantom wallet is installed
   * @returns {Object|null} - Phantom provider or null if not installed
   */
  getProvider: function() {
    if (!window.phantom?.solana) {
      console.error('Phantom extension not found');
      return null;
    }
    return window.phantom.solana;
  },
  
  /**
   * Connect to Phantom wallet
   * @returns {Promise<string>} - Public key of the connected wallet
   */
  connect: async function() {
    const provider = this.getProvider();
    if (!provider) {
      throw new Error('Phantom extension not found. Please install it first.');
    }
    
    try {
      // Try to get the current connection first
      const resp = await provider.request({ method: 'connect' });
      const publicKey = resp.publicKey.toString();
      console.log('Already connected to Phantom:', publicKey);
      return publicKey;
    } catch (e) {
      // If not connected, connect to wallet
      console.log('Connecting to Phantom...');
      const connection = await provider.connect();
      const publicKey = connection.publicKey.toString();
      console.log('Connected to Phantom:', publicKey);
      return publicKey;
    }
  },
  
  /**
   * Sign a message with Phantom wallet
   * @param {string} message - The message to sign
   * @returns {Promise<string>} - The signature
   */
  signMessage: async function(message) {
    const provider = this.getProvider();
    if (!provider) {
      throw new Error('Phantom extension not found. Please install it first.');
    }
    
    console.log('Signing message:', message);
    const encodedMessage = new TextEncoder().encode(message);
    
    try {
      console.log('Requesting signature from Phantom...');
      const signResult = await provider.signMessage(encodedMessage, 'utf8');
      console.log('Received signature response from Phantom');
      
      return SignatureUtils.processSignature(signResult);
    } catch (error) {
      console.error('Error signing message:', error);
      throw error;
    }
  }
};

// Export utilities for use in other files
if (typeof module !== 'undefined') {
  module.exports = {
    SignatureUtils,
    UIUtils,
    PhantomUtils
  };
}
