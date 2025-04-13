# SolanaLogin Plugin: Complete Guide

A comprehensive Minecraft Spigot plugin that integrates Solana wallet authentication with Minecraft login, supporting both premium and cracked Minecraft accounts.

## Changelog

### Version 1.1

- Added QR code and browser extension wallet connection support
- Added web server component for Solana wallet authentication
- Added proper API version in plugin.yml
- Fixed code structure in command classes by extracting methods
- Improved logging with level checks for better performance
- Added proper null checks and error handling
- Fixed potential memory leaks in database connections
- Improved wallet validation for Phantom wallets
- Enhanced code documentation and comments
- Updated comprehensive documentation

### Version 1.0

- Initial release
- Basic authentication system
- Solana wallet integration
- MySQL database support
- Session management

## Documentation

This project includes comprehensive documentation split across multiple files:

- [README.md](README.md) - Main project overview and documentation (this file)
- [INSTALL.md](INSTALL.md) - Detailed installation instructions
- [DEVELOPMENT.md](DEVELOPMENT.md) - Guide for developers contributing to the project
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Solutions for common issues

## Table of Contents

1. [Development Setup](#development-setup)
2. [Server Administrator Guide](#server-administrator-guide)
3. [User Guide](#user-guide)
4. [Workflow Examples](#workflow-examples)
5. [Technical Architecture](#technical-architecture)
6. [Next Steps and Enhancements](#next-steps-and-enhancements)

## Development Setup

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Maven
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code)
- MySQL database server

### Setting Up the Development Environment

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/SolanaLogin.git
   cd SolanaLogin
   ```

2. **Build the project with Maven**

   ```bash
   mvn clean package
   ```

   This will generate a JAR file in the `target` directory.

3. **Set up a test server**
   - Download Spigot or Paper server
   - Create a test server directory
   - Copy the generated JAR file to the server's `plugins` folder

4. **Set up the database**
   - Create a MySQL database for the plugin

   ```sql
   CREATE DATABASE minecraft;
   CREATE USER 'minecraft'@'localhost' IDENTIFIED BY 'yourpassword';
   GRANT ALL PRIVILEGES ON minecraft.* TO 'minecraft'@'localhost';
   FLUSH PRIVILEGES;
   ```

5. **Configure the plugin**
   - Start the server once to generate the config.yml
   - Stop the server
   - Edit the config.yml to set up your database connection

### Development Workflow

1. **Make changes to the code**
   - Implement new features or fix bugs
   - Follow the existing code structure and patterns

2. **Run tests**

   ```bash
   ./run-test.bat  # On Windows
   ./run-test.sh   # On Linux/Mac
   ```

3. **Build and deploy**

   ```bash
   mvn clean package
   ```

   Then copy the JAR file to your test server's plugins folder.

4. **Test on a local server**
   - Start your test server
   - Test the functionality in-game

5. **Commit and push changes**

   ```bash
   git add .
   git commit -m "Description of changes"
   git push
   ```

## Server Administrator Guide

### Installation

1. **Download the plugin**
   - Download the latest release JAR file from the project's releases page

2. **Server requirements**
   - Spigot or Paper server (1.19+)
   - MySQL database

3. **Install the plugin**
   - Place the JAR file in your server's `plugins` folder
   - Restart your server

4. **Configure the plugin**
   - Edit the `plugins/SolanaLogin/config.yml` file
   - Set up your database connection details
   - Configure authentication settings
   - Configure Solana network settings

### Configuration Options

Here's a detailed explanation of the configuration options:

```yaml
# Database Configuration
database:
  host: localhost          # Database server hostname
  port: 3306               # Database server port
  database: minecraft      # Database name
  username: minecraft      # Database username
  password: password       # Database password
  table-prefix: walletlogin_  # Prefix for database tables

# Plugin Settings
settings:
  require-login: true      # If true, players must login to play
  require-wallet-login: false  # If true, players must connect a wallet to play
  login-timeout: 60        # Time in seconds for players to login after joining
  session-timeout: 1440    # Time in minutes for session to expire (24 hours)
  max-login-attempts: 5    # Maximum number of login attempts before timeout
  login-attempt-timeout: 10  # Time in minutes for login attempt timeout
  register-ip-limit: 3     # Maximum number of accounts per IP
  wallet-validation: true  # Whether to validate wallet addresses format
  solana-only: true        # Only allow Solana wallets

# Authentication Settings
auth:
  min-password-length: 6   # Minimum password length
  max-password-length: 32  # Maximum password length
  hash-algorithm: "PBKDF2WithHmacSHA256"  # Password hashing algorithm
  hash-iterations: 65536   # Number of iterations for password hashing
  hash-key-length: 256     # Key length for password hashing
  salt-length: 16          # Salt length for password hashing

# Solana Settings
solana:
  network: "mainnet"       # mainnet, testnet, or devnet
  rpc-url: "https://api.mainnet-beta.solana.com"  # RPC URL for Solana network
  verification-message: "I confirm that I own this wallet and authorize its use on the Minecraft server."  # Message to sign for verification

# Web Server Settings
web-server:
  enabled: true            # Whether to enable the web server for QR code login
  url: "http://localhost:3000"  # URL of the web server
  port: 3000               # Port of the web server
  qr-code-timeout: 300     # Time in seconds for QR code to expire (5 minutes)
  check-interval: 5        # Time in seconds to check for wallet connection status
```

### Commands and Permissions

#### Admin Commands

- `/solanalogin reload` - Reload the plugin configuration
  - Permission: `solanalogin.admin`
- `/solanalogin info` - View plugin information
  - Permission: `solanalogin.admin`

#### Authentication Commands

- `/register <password> <confirmPassword>` - Register an account
  - Permission: `solanalogin.register` (default: true)
- `/login <password>` - Login to your account
  - Permission: `solanalogin.login` (default: true)
- `/changepassword <oldPassword> <newPassword> <confirmNewPassword>` - Change your password
  - Permission: `solanalogin.changepassword` (default: true)
- `/logout` - Logout from your account
  - Permission: `solanalogin.logout` (default: true)

#### Wallet Commands

- `/connectwallet <wallet_address>` - Connect your Solana wallet to your Minecraft account
  - Permission: `solanalogin.wallet.connect` (default: true)
- `/disconnectwallet` - Disconnect your Solana wallet from your Minecraft account
  - Permission: `solanalogin.wallet.disconnect` (default: true)
- `/walletinfo` - View your Solana wallet information
  - Permission: `solanalogin.wallet.info` (default: true)
- `/verifycode <code>` - Verify your wallet with a verification code
  - Permission: `solanalogin.wallet.verify` (default: true)

### Troubleshooting

1. **Database connection issues**
   - Check your database credentials in the config.yml
   - Ensure your MySQL server is running
   - Check if the database and user exist with proper permissions

2. **Plugin not loading**
   - Check the server console for error messages
   - Ensure you're using a compatible server version
   - Check if all dependencies are installed

3. **Authentication issues**
   - Check if the player is registered in the database
   - Reset a player's password using the database if needed
   - Clear a player's session if they're stuck

4. **Wallet connection issues**
   - Verify the wallet address format
   - Check if the Solana network settings are correct
   - Ensure the player is authenticated before connecting a wallet

## User Guide

### Getting Started

1. **Joining the server**
   - Connect to the Minecraft server
   - You'll be prompted to register if you're a new player

2. **Registration**
   - Use `/register <password> <confirmPassword>` to create an account
   - Choose a secure password that meets the server's requirements
   - Remember your password for future logins

3. **Logging in**
   - Use `/login <password>` to authenticate
   - You'll need to login each time you join the server
   - Your session will remain active for the configured session timeout

### Wallet Integration

1. **Connecting your Solana wallet (Manual Method)**
   - Get your Solana wallet address (from Phantom or another wallet)
   - Use `/connectwallet <wallet_address>` to connect your wallet
   - You'll receive a verification code

2. **Connecting your Solana wallet (QR Code/Link Method)**
   - Use `/connectwallet` or `/connectwallet qr` to get connection options
   - Click on the link to open the web interface or scan the QR code with your mobile wallet
   - Approve the connection request in your wallet
   - Your wallet will be automatically verified

3. **Verifying your wallet (Manual Method Only)**
   - Use `/verifycode <code>` to verify your wallet ownership
   - This proves you own the wallet you're connecting

4. **Managing your wallet**
   - Use `/walletinfo` to view your connected wallet information
   - Use `/disconnectwallet` to disconnect your wallet if needed

### Account Management

1. **Changing your password**
   - Use `/changepassword <oldPassword> <newPassword> <confirmNewPassword>`
   - Choose a secure new password

2. **Logging out**
   - Use `/logout` to end your session
   - You'll need to login again to play

## Workflow Examples

### New Player Registration and Wallet Connection

1. Player joins the server for the first time
2. Server prompts player to register
3. Player uses `/register password confirmPassword`
4. Server creates account and prompts player to login
5. Player uses `/login password`
6. Player is authenticated and can now play
7. Player decides to connect their Solana wallet
8. Player can choose one of two methods:
   - **Method 1 (Manual):** Player uses `/connectwallet <wallet_address>`, receives a verification code, and uses `/verifycode <code>` to verify
   - **Method 2 (QR/Link):** Player uses `/connectwallet`, clicks a link or scans QR code, and approves the connection in their wallet
9. Player's wallet is now connected and verified

### Returning Player Login

1. Player joins the server
2. Server recognizes the player and prompts for login
3. Player uses `/login password`
4. Player is authenticated and can now play
5. If wallet connection is required, the server checks if the player has a connected wallet
6. If the wallet is not verified, the server prompts the player to verify it

### Server with Required Wallet Connection

1. Player joins the server
2. Player logs in with `/login password`
3. Server checks if player has a connected wallet
4. If not, server prompts player to connect a wallet
5. Player has a limited time to connect a wallet before being kicked
6. Player connects wallet using one of two methods:
   - **Method 1:** Player uses `/connectwallet <wallet_address>` and then verifies with `/verifycode <code>`
   - **Method 2:** Player uses `/connectwallet` or `/connectwallet qr` to get QR code/link options
7. Player scans QR code with mobile wallet or clicks link to connect via browser extension
8. Wallet is automatically verified through direct signature verification
9. Player can now play on the server

## Technical Architecture

### Component Overview

1. **Authentication System**
   - Registration and login handling
   - Password hashing and verification
   - Session management

2. **Wallet Integration**
   - Wallet address validation
   - Wallet connection and verification
   - Wallet type detection (Phantom, Solana)

3. **Database Management**
   - Player data storage
   - Wallet information storage
   - Session tracking

4. **Command Handling**
   - User command processing
   - Permission checking
   - Response formatting

### Database Schema

The plugin uses three main tables:

1. **players**
   - `uuid` (Primary Key): Player's UUID
   - `username`: Player's username
   - `password`: Hashed password
   - `ip`: Player's IP address
   - `last_login`: Timestamp of last login
   - `registered_at`: Timestamp of registration

2. **wallets**
   - `uuid` (Primary Key, Foreign Key to players): Player's UUID
   - `wallet_address`: Solana wallet address
   - `wallet_type`: Type of wallet (Phantom, Solana)
   - `verified`: Whether the wallet is verified
   - `connected_at`: Timestamp of wallet connection

3. **sessions**
   - `uuid` (Primary Key, Foreign Key to players): Player's UUID
   - `ip`: Player's IP address
   - `last_login`: Timestamp of last login

### Security Features

1. **Password Security**
   - PBKDF2 hashing with configurable iterations
   - Unique salt for each password
   - Configurable password requirements

2. **Session Management**
   - IP-based session tracking
   - Configurable session timeout
   - Automatic logout on server restart

3. **Wallet Verification**
   - Verification code generation
   - Wallet address validation
   - Ownership verification

## Next Steps and Enhancements

### Immediate Improvements

1. **Web Interface**
   - Create a web portal for account management
   - Allow wallet connection and verification through the website
   - Provide account recovery options

2. **Enhanced Wallet Integration**
   - Implement wallet signature verification
   - Add support for wallet transactions
   - Integrate with Solana Pay

3. **NFT Integration**
   - Add NFT ownership verification
   - Implement NFT-based permissions and features
   - Create in-game representation of NFTs

### Medium-term Roadmap

1. **Multi-wallet Support**
   - Add support for multiple wallets per player
   - Allow different wallet types (Ethereum, Bitcoin, etc.)
   - Implement cross-chain verification

2. **Token Economy**
   - Integrate with server economy
   - Allow token-based transactions
   - Implement token rewards for in-game activities

3. **Enhanced Security**
   - Add two-factor authentication
   - Implement IP-based security measures
   - Add anti-bot protection

### Long-term Vision

1. **Blockchain Game Integration**
   - Create a full blockchain-based game economy
   - Implement on-chain assets and inventory
   - Allow trading of in-game items as NFTs

2. **Decentralized Identity**
   - Implement decentralized identity verification
   - Allow cross-server authentication
   - Create a reputation system

3. **Developer API**
   - Create a public API for other plugins to integrate with
   - Allow third-party wallet integrations
   - Support custom blockchain implementations

### Implementation Plan

1. **Phase 1: Core Functionality (Current)**
   - Basic authentication system
   - Solana wallet integration
   - Database storage

2. **Phase 2: Enhanced Security and Usability**
   - Web interface
   - Wallet signature verification
   - Improved user experience

3. **Phase 3: NFT and Token Integration**
   - NFT ownership verification
   - Token economy
   - In-game NFT representation

4. **Phase 4: Advanced Features**
   - Multi-wallet support
   - Cross-chain verification
   - Developer API

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

If you have any questions or need help, please:

1. Check our documentation:
   - [README.md](README.md) - Main project overview
   - [INSTALL.md](INSTALL.md) - Installation instructions
   - [DEVELOPMENT.md](DEVELOPMENT.md) - Development guide
   - [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Common issues and solutions
2. Open an issue on the GitHub repository
3. Contact us at [NFTLogin](https://nftlogin.com)
