# SolanaLogin Wallet Integration Guide

This document provides a guide for adding support for additional wallet providers to the SolanaLogin plugin.

## Table of Contents

1. [Introduction](#introduction)
2. [Architecture Overview](#architecture-overview)
3. [Adding a New Wallet Provider](#adding-a-new-wallet-provider)
4. [Testing Your Integration](#testing-your-integration)
5. [Example: Adding Solflare Wallet](#example-adding-solflare-wallet)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

## Introduction

The SolanaLogin plugin currently supports Phantom wallet for Solana blockchain authentication. However, the architecture is designed to be extensible, allowing for the addition of other wallet providers. This guide will walk you through the process of adding support for a new wallet provider.

## Architecture Overview

The wallet integration system consists of several components:

1. **Web Server Component**:
   - Handles wallet connection requests
   - Manages QR code generation
   - Verifies wallet signatures
   - Communicates with the Minecraft plugin

2. **Minecraft Plugin Component**:
   - Manages player authentication
   - Stores wallet information
   - Provides in-game commands for wallet management

3. **Wallet Provider Interface**:
   - Defines the contract for wallet providers
   - Handles wallet-specific connection logic
   - Manages signature verification

## Adding a New Wallet Provider

### Step 1: Create a Wallet Provider Class

Create a new JavaScript class in the `web-server/providers` directory (create this directory if it doesn't exist):

```javascript
// web-server/providers/NewWalletProvider.js

class NewWalletProvider {
    constructor() {
        this.name = "NewWallet"; // Name of the wallet provider
        this.displayName = "New Wallet"; // Display name for UI
        this.logo = "/images/new-wallet-logo.png"; // Path to logo image
        this.deepLinkPrefix = "newwallet://"; // Deep link prefix for mobile
        this.supportsMobile = true; // Whether the wallet supports mobile
        this.supportsExtension = true; // Whether the wallet supports browser extension
    }

    // Check if the wallet is installed/available
    async isAvailable() {
        // Implementation depends on the wallet
        // For browser extension, typically check window.newWallet
        return typeof window.newWallet !== 'undefined';
    }

    // Connect to the wallet
    async connect() {
        try {
            // Implementation depends on the wallet
            // For example:
            const response = await window.newWallet.connect();
            return {
                publicKey: response.publicKey.toString(),
                success: true
            };
        } catch (error) {
            console.error("Error connecting to wallet:", error);
            return {
                success: false,
                error: error.message
            };
        }
    }

    // Sign a message with the wallet
    async signMessage(message, publicKey) {
        try {
            // Implementation depends on the wallet
            // For example:
            const encodedMessage = new TextEncoder().encode(message);
            const signatureBytes = await window.newWallet.signMessage(encodedMessage, "utf8");
            const signature = Buffer.from(signatureBytes).toString("base64");
            
            return {
                signature,
                success: true
            };
        } catch (error) {
            console.error("Error signing message:", error);
            return {
                success: false,
                error: error.message
            };
        }
    }

    // Generate a deep link for mobile wallet
    generateDeepLink(url, callbackUrl) {
        // Implementation depends on the wallet
        // For example:
        return `${this.deepLinkPrefix}dapp/browse?url=${encodeURIComponent(url)}&redirect=${encodeURIComponent(callbackUrl)}`;
    }

    // Verify a signature
    async verifySignature(message, signature, publicKey) {
        try {
            // Implementation depends on the wallet and blockchain
            // For Solana, you might use:
            const { verify } = require('@solana/web3.js');
            const verified = verify(
                publicKey,
                new TextEncoder().encode(message),
                Buffer.from(signature, 'base64')
            );
            
            return {
                verified,
                success: true
            };
        } catch (error) {
            console.error("Error verifying signature:", error);
            return {
                success: false,
                error: error.message
            };
        }
    }
}

module.exports = NewWalletProvider;
```

### Step 2: Register the Wallet Provider

Update the wallet provider registry in the web server:

```javascript
// web-server/walletProviders.js

const PhantomWalletProvider = require('./providers/PhantomWalletProvider');
const NewWalletProvider = require('./providers/NewWalletProvider');

// Registry of wallet providers
const walletProviders = {
    'phantom': new PhantomWalletProvider(),
    'newwallet': new NewWalletProvider()
};

// Get a wallet provider by name
function getWalletProvider(name) {
    return walletProviders[name.toLowerCase()] || null;
}

// Get all wallet providers
function getAllWalletProviders() {
    return Object.values(walletProviders);
}

module.exports = {
    getWalletProvider,
    getAllWalletProviders
};
```

### Step 3: Update the Web Interface

Update the web interface to include the new wallet provider:

```html
<!-- web-server/public/login.html -->

<div class="wallet-options">
    <h2>Connect your wallet</h2>
    
    <!-- Existing Phantom wallet option -->
    <button id="connect-phantom" class="wallet-button">
        <img src="/images/phantom-logo.png" alt="Phantom" />
        Connect with Phantom
    </button>
    
    <!-- New wallet option -->
    <button id="connect-newwallet" class="wallet-button">
        <img src="/images/new-wallet-logo.png" alt="New Wallet" />
        Connect with New Wallet
    </button>
</div>

<script>
    // Add event listener for the new wallet button
    document.getElementById('connect-newwallet').addEventListener('click', async () => {
        try {
            await connectWallet('newwallet');
        } catch (error) {
            showError('Failed to connect wallet: ' + error.message);
        }
    });
</script>
```

### Step 4: Update the Server API

Update the server API to handle the new wallet provider:

```javascript
// web-server/server.js

// Import the wallet provider registry
const { getWalletProvider } = require('./walletProviders');

// Update the connect endpoint to accept a wallet provider parameter
app.post('/api/connect', async (req, res) => {
    try {
        const { session, walletProvider = 'phantom' } = req.body;
        
        // Get the wallet provider
        const provider = getWalletProvider(walletProvider);
        if (!provider) {
            return res.status(400).json({ success: false, error: 'Unsupported wallet provider' });
        }
        
        // Rest of the connection logic...
        // Use the provider for wallet-specific operations
        
        res.json({ success: true, data: { /* connection data */ } });
    } catch (error) {
        console.error('Error connecting wallet:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Update other endpoints as needed...
```

### Step 5: Update the Minecraft Plugin

Update the Minecraft plugin to support the new wallet provider:

```java
// src/main/java/com/nftlogin/walletlogin/utils/WalletValidator.java

public class WalletValidator {
    // Add support for the new wallet provider
    public static boolean isValidWalletAddress(String address, String provider) {
        // Basic validation for Solana addresses
        if (provider.equalsIgnoreCase("phantom") || provider.equalsIgnoreCase("newwallet")) {
            // Solana addresses are 44 characters long and base58 encoded
            return address != null && address.length() == 44 && isBase58(address);
        }
        
        // Add validation for other wallet types if needed
        
        return false;
    }
    
    // Rest of the class...
}
```

```java
// src/main/java/com/nftlogin/walletlogin/database/DatabaseManager.java

public class DatabaseManager {
    // Update the database schema to include wallet provider information
    private void createTables() {
        try (Connection conn = getConnection()) {
            // Create wallets table with provider column
            String createWalletsTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "wallets ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "player_uuid VARCHAR(36) NOT NULL, "
                    + "wallet_address VARCHAR(44) NOT NULL, "
                    + "wallet_provider VARCHAR(20) NOT NULL DEFAULT 'phantom', "
                    + "verified BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "UNIQUE (player_uuid, wallet_address)"
                    + ")";
            
            Statement stmt = conn.createStatement();
            stmt.execute(createWalletsTable);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create tables", e);
        }
    }
    
    // Update methods to include wallet provider
    public void addWallet(UUID playerUuid, String walletAddress, String provider) {
        try (Connection conn = getConnection()) {
            String query = "INSERT INTO " + tablePrefix + "wallets (player_uuid, wallet_address, wallet_provider) "
                    + "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE wallet_provider = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, walletAddress);
            stmt.setString(3, provider.toLowerCase());
            stmt.setString(4, provider.toLowerCase());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to add wallet", e);
        }
    }
    
    // Rest of the class...
}
```

### Step 6: Update the Configuration

Update the plugin configuration to include wallet provider settings:

```yaml
# src/main/resources/config.yml

# Wallet settings
wallet:
  providers:
    - phantom
    - newwallet
  default-provider: phantom
  validation: true
```

## Testing Your Integration

### Web Server Testing

1. Start the web server in development mode:
   ```bash
   cd web-server
   NODE_ENV=development npm start
   ```

2. Open the test pages in your browser:
   - `http://localhost:3000/test-flow.html` - Test the complete flow
   - `http://localhost:3000/simple-connect.html` - Test basic connection

3. Test with your new wallet provider:
   - Select your new wallet provider from the options
   - Verify that connection works
   - Verify that signature verification works

### Minecraft Plugin Testing

1. Start your Minecraft server with the updated plugin
2. Join the server and register/login
3. Use the `/connectwallet` command
4. Select your new wallet provider
5. Complete the connection process
6. Verify that the wallet is connected with `/walletinfo`

## Example: Adding Solflare Wallet

Here's a concrete example of adding support for Solflare wallet:

```javascript
// web-server/providers/SolflareWalletProvider.js

class SolflareWalletProvider {
    constructor() {
        this.name = "Solflare";
        this.displayName = "Solflare";
        this.logo = "/images/solflare-logo.png";
        this.deepLinkPrefix = "solflare://";
        this.supportsMobile = true;
        this.supportsExtension = true;
    }

    async isAvailable() {
        return typeof window.solflare !== 'undefined';
    }

    async connect() {
        try {
            if (!window.solflare) {
                throw new Error("Solflare extension not found");
            }
            
            // Connect to Solflare
            if (!window.solflare.isConnected) {
                await window.solflare.connect();
            }
            
            return {
                publicKey: window.solflare.publicKey.toString(),
                success: true
            };
        } catch (error) {
            console.error("Error connecting to Solflare:", error);
            return {
                success: false,
                error: error.message
            };
        }
    }

    async signMessage(message, publicKey) {
        try {
            if (!window.solflare) {
                throw new Error("Solflare extension not found");
            }
            
            const encodedMessage = new TextEncoder().encode(message);
            const signatureBytes = await window.solflare.signMessage(encodedMessage, "utf8");
            const signature = Buffer.from(signatureBytes).toString("base64");
            
            return {
                signature,
                success: true
            };
        } catch (error) {
            console.error("Error signing message with Solflare:", error);
            return {
                success: false,
                error: error.message
            };
        }
    }

    generateDeepLink(url, callbackUrl) {
        return `${this.deepLinkPrefix}dapp/browse?url=${encodeURIComponent(url)}&redirect=${encodeURIComponent(callbackUrl)}`;
    }

    async verifySignature(message, signature, publicKey) {
        try {
            const { verify } = require('@solana/web3.js');
            const verified = verify(
                publicKey,
                new TextEncoder().encode(message),
                Buffer.from(signature, 'base64')
            );
            
            return {
                verified,
                success: true
            };
        } catch (error) {
            console.error("Error verifying Solflare signature:", error);
            return {
                success: false,
                error: error.message
            };
        }
    }
}

module.exports = SolflareWalletProvider;
```

Then register it in the wallet providers registry:

```javascript
// web-server/walletProviders.js

const PhantomWalletProvider = require('./providers/PhantomWalletProvider');
const SolflareWalletProvider = require('./providers/SolflareWalletProvider');

const walletProviders = {
    'phantom': new PhantomWalletProvider(),
    'solflare': new SolflareWalletProvider()
};

// Rest of the file...
```

## Best Practices

1. **Error Handling**: Implement robust error handling in all wallet provider methods
2. **Logging**: Add detailed logging for debugging purposes
3. **Fallbacks**: Provide fallback mechanisms when a wallet is not available
4. **Testing**: Test thoroughly with different scenarios and edge cases
5. **Documentation**: Document the wallet provider's features and limitations
6. **Security**: Ensure secure signature verification and message handling
7. **User Experience**: Provide clear feedback to users during the connection process
8. **Compatibility**: Test with different browsers and devices

## Troubleshooting

### Common Issues

1. **Wallet Not Detected**:
   - Check if the wallet extension is installed and enabled
   - Verify that the wallet's global object is available in the browser

2. **Connection Failures**:
   - Check browser console for errors
   - Verify that the wallet is unlocked
   - Ensure the wallet is connected to the correct network

3. **Signature Verification Failures**:
   - Check the signature format (base64, hex, etc.)
   - Verify that the correct message is being signed
   - Ensure the correct public key is being used for verification

4. **Mobile Deep Link Issues**:
   - Test deep links on different mobile devices
   - Verify that the wallet app is installed on the device
   - Check the deep link format for the specific wallet

### Debugging Tips

1. Use the browser console to debug wallet interactions
2. Add console.log statements to track the flow of execution
3. Test with the wallet's own examples and documentation
4. Use the test pages provided with the plugin
5. Check the wallet provider's documentation for any specific requirements

---

**Note**: This guide assumes a basic understanding of JavaScript, Java, and blockchain concepts. For more detailed information, refer to the specific wallet provider's documentation.
