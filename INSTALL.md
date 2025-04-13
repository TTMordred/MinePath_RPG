# Installation Guide for SolanaLogin Plugin

This guide will help you set up and install the SolanaLogin plugin for your Minecraft Spigot server.

## Prerequisites

1. **Java Development Kit (JDK) 8 or higher**
   - Download from: [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://adoptopenjdk.net/)

2. **Maven** (for building from source)
   - Download from: [Maven Official Site](https://maven.apache.org/download.cgi)
   - Installation guide: [Maven Installation](https://maven.apache.org/install.html)

3. **MySQL Database**
   - Download from: [MySQL](https://dev.mysql.com/downloads/mysql/)
   - Or use a hosted MySQL service

4. **Spigot/Paper Server (1.18+)**
   - Set up a Spigot server using [BuildTools](https://www.spigotmc.org/wiki/buildtools/) or download a pre-built version
   - Or use [Paper](https://papermc.io/downloads) (recommended for better performance)

5. **Node.js 14+** (for the web server component)
   - Download from: [Node.js](https://nodejs.org/)

6. **Phantom Wallet**
   - Install the [Phantom browser extension](https://phantom.app/) or mobile app

## Installation Options

### Option 1: Using Pre-built JAR (Recommended)

1. Download the latest release JAR file from the [Releases](https://github.com/yourusername/SolanaLogin/releases) page
2. Place the JAR file in your server's `plugins` folder
3. Proceed to the [Server Setup](#server-setup) section

### Option 2: Building from Source

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/SolanaLogin.git
   cd SolanaLogin
   ```

2. Build using Maven:

   ```bash
   mvn clean package
   ```

   Or use the provided scripts:

   ```bash
   # On Windows
   build.bat

   # On Linux/Mac
   ./build.sh
   ```

3. The compiled plugin JAR file will be in the `target` directory
4. Copy the JAR file to your server's `plugins` folder

## Server Setup

### Database Setup

1. Create a MySQL database for the plugin:

   ```sql
   CREATE DATABASE minecraft;
   ```

2. Create a user with appropriate permissions:

   ```sql
   CREATE USER 'minecraft'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON minecraft.* TO 'minecraft'@'localhost';
   FLUSH PRIVILEGES;
   ```

### Plugin Configuration

1. Start your Minecraft server once to generate the default configuration files
2. Stop the server
3. Edit the `plugins/SolanaLogin/config.yml` file to set your database credentials:

   ```yaml
   database:
     host: localhost
     port: 3306
     database: minecraft
     username: minecraft
     password: your_password
     table-prefix: walletlogin_
   ```

4. Configure other settings as needed (see the [Configuration](#configuration) section below)

### Web Server Setup

1. Navigate to the `web-server` directory
2. Install dependencies:

   ```bash
   npm install
   ```

3. Start the web server:

   ```bash
   npm start
   ```

   Or use the provided scripts:

   ```bash
   # On Windows
   start-web-server.bat

   # On Linux/Mac
   ./start-web-server.sh
   ```

4. Update the `web-server.url` in the plugin's config.yml to match your server's address:

   ```yaml
   web-server:
     enabled: true
     url: "http://your-server-address:3000"  # Change this to your server's address
     port: 3000
     qr-code-timeout: 300
     check-interval: 5
   ```

## Configuration

The plugin is highly configurable through the `config.yml` file. Here are the key sections:

### Database Configuration

```yaml
database:
  host: localhost          # Database server hostname
  port: 3306               # Database server port
  database: minecraft      # Database name
  username: minecraft      # Database username
  password: your_password  # Database password
  table-prefix: walletlogin_  # Prefix for database tables
```

### Plugin Settings

```yaml
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
```

### Solana Settings

```yaml
solana:
  network: "devnet"        # mainnet, testnet, or devnet
  rpc-url: "https://api.devnet.solana.com"  # RPC URL for Solana network
  verification-message: "I confirm that I own this wallet and authorize its use on the Minecraft server."  # Message to sign for verification
```

### Web Server Settings

```yaml
web-server:
  enabled: true            # Whether to enable the web server for QR code login
  url: "http://localhost:3000"  # URL of the web server
  port: 3000               # Port of the web server
  qr-code-timeout: 300     # Time in seconds for QR code to expire (5 minutes)
  check-interval: 5        # Time in seconds to check for wallet connection status
```

## Verifying Installation

1. Start your Minecraft server
2. Check the server console for messages indicating that the SolanaLogin plugin has been enabled
3. Join the server and try the plugin commands:
   - `/register <password> <confirmPassword>` - Register an account
   - `/login <password>` - Login to your account
   - `/connectwallet` - Open wallet connection interface via QR code or browser extension
   - `/connectwallet qr` - Show QR code for wallet connection
   - `/walletinfo` - View your wallet information

## Troubleshooting

For detailed troubleshooting information, please refer to the [TROUBLESHOOTING.md](TROUBLESHOOTING.md) file.

Common issues:

- **Database Connection Issues**: Ensure your MySQL server is running and accessible, and that the credentials in the config file are correct
- **Plugin Not Loading**: Check the server logs for any error messages related to the plugin
- **Web Server Connection Issues**: Ensure the web server is running and accessible from the Minecraft server
- **Wallet Connection Failures**: Check that the Phantom wallet is properly installed and configured

## Support

If you encounter any issues or have questions, please:

1. Check the [README.md](README.md) file for documentation
2. Review the [TROUBLESHOOTING.md](TROUBLESHOOTING.md) file for common issues and solutions
3. Open an issue on the [GitHub repository](https://github.com/yourusername/SolanaLogin/issues)
4. Join our [Discord server](https://discord.gg/yourserver) for community support
