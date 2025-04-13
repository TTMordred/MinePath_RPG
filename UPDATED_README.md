# SolanaLogin - Minecraft Solana Wallet Authentication

SolanaLogin is a Minecraft plugin that allows players to authenticate using Solana wallets, integrating blockchain into the gaming experience. This project includes a Spigot plugin and a web server component for wallet authentication.

## Features

- Minecraft login with Solana wallet (Phantom)
- Support for two authentication methods:
  - Direct connection via Phantom browser extension
  - QR code scanning with Phantom mobile app
- Secure authentication through blockchain signatures
- Integration with Solana devnet
- Support for "cracked" Minecraft versions

## Project Structure

The project is divided into two main components:

1. **Minecraft Plugin (`src/` directory)**: Spigot plugin that handles in-game commands and interacts with the web server.
2. **Web Server (`web-server/` directory)**: Express.js server that handles Solana wallet authentication and QR code generation.

## Requirements

- Java 8 or higher
- Minecraft Spigot/Paper server
- Node.js 14 or higher
- Phantom wallet (browser extension or mobile app)

## Installation

### Minecraft Plugin

1. Compile the plugin or download the JAR file from the Releases section
2. Place the JAR file in the `plugins` directory of your Minecraft server
3. Restart the server
4. Configure the plugin in the `plugins/SolanaLogin/config.yml` file

### Web Server

1. Navigate to the `web-server` directory
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the server:
   ```bash
   npm start
   ```

## Configuration

### Minecraft Plugin (config.yml)

```yaml
# Web Server Settings
web-server:
  enabled: true  # Enable/disable web server for QR code login
  url: "http://localhost:3000"  # Web server URL
  port: 3000  # Web server port
  qr-code-timeout: 300  # QR code expiration time (seconds)
  check-interval: 5  # Connection status check interval (seconds)

# Solana Settings
solana:
  network: "devnet"  # Solana network (devnet, testnet, mainnet-beta)
  rpc-url: "https://api.devnet.solana.com"  # Solana RPC URL

# Security Settings
security:
  max-login-attempts: 5  # Maximum login attempts before lockout
  login-timeout: 300  # Lockout time after failed login attempts (seconds)
  session-timeout: 3600  # Session expiration time (seconds)

# Database Settings
database:
  type: "sqlite"  # Database type (sqlite, mysql)
  file: "database.db"  # SQLite file name
```

### Web Server (.env or environment variables)

```
PORT=3000  # Web server port
NODE_ENV=development  # Environment (development, production)
```

## Usage

### In-game Commands

- `/connectwallet` - Open wallet connection interface
- `/login` - Login with connected wallet
- `/logout` - Logout from current session
- `/walletinfo` - Display connected wallet information

### Login Process

#### Method 1: Connect via Phantom Extension

1. Use the `/connectwallet` command in Minecraft
2. Click on the link displayed in the game
3. On the web page, click "Connect with Phantom"
4. Confirm the connection in the Phantom extension
5. Sign the verification message
6. Return to Minecraft and use the `/login` command

#### Method 2: Scan QR Code with Phantom Mobile

1. Use the `/connectwallet qr` command in Minecraft
2. Click on the link displayed in the game
3. Scan the QR code with the Phantom app on your phone
4. Confirm the connection and sign the message in the Phantom app
5. Return to Minecraft and use the `/login` command

## Troubleshooting

### Wallet Connection Issues

- **"Missing required parameters" error**: Ensure the login URL contains all required parameters: session, nonce, and player.
- **Cannot connect to Phantom**: Check that the Phantom extension is installed and logged in.
- **QR code not working**: Make sure the Phantom app is updated to the latest version.

### Web Server Issues

- **Web server won't start**: Check that Node.js is installed and dependencies are correctly installed.
- **CORS errors**: Ensure the URL in config.yml matches the actual URL of the web server.

### Plugin Issues

- **Plugin won't load**: Check Java and Spigot/Paper versions.
- **Commands not working**: Check player permissions and plugin configuration.

## Testing and Debug Pages

The web server includes several test pages to help debug wallet connection issues:

1. **Test Flow**: `http://localhost:3000/test-flow.html`
   - Test the complete wallet connection flow
   - Create session, connect wallet, and verify connection

2. **Simple Connect**: `http://localhost:3000/simple-connect.html`
   - Test basic connection with Phantom extension
   - Test message signing

3. **Simple QR**: `http://localhost:3000/simple-qr.html`
   - Test QR code generation and scanning
   - Test deep linking for Phantom mobile

4. **Simple Redirect**: `http://localhost:3000/simple-redirect.html`
   - Test Phantom redirect handling
   - Test URL parameters

## Changelog

### Version 1.3 (Latest Update)

- Optimized code structure with utility functions
- Improved signature handling from Phantom wallet
- Fixed "Empty signature" error when connecting from Minecraft
- Added multiple fallback methods for signature handling
- Improved UI and user experience
- Added more detailed logging for easier debugging

### Version 1.2

- Switched to Solana devnet network
- Improved Phantom wallet connection handling
- Fixed "Missing required parameters" error during signature verification
- Simplified deep link format for Phantom wallet
- Added development mode for easier debugging
- Improved error handling and logging
- Added simple test pages for wallet connection debugging

### Version 1.1

- Added QR code and browser extension wallet connection support
- Added web server component for Solana wallet authentication
- Improved code structure in command classes
- Improved logging with level checks for better performance
- Added proper null checks and error handling
- Fixed potential memory leaks in database connections
- Improved wallet validation for Phantom wallets

### Version 1.0

- Initial release
- Basic wallet connection functionality
- SQLite database integration
- Login and registration commands
- Basic security features

## Development

### Development Requirements

- JDK 8 or higher
- Maven
- Node.js and npm
- Java IDE (IntelliJ IDEA, Eclipse)

### Compiling the Plugin

```bash
mvn clean package
```

### Running the Web Server in Development Mode

```bash
cd web-server
npm run dev
```

### Source Code Structure

- `src/main/java/com/nftlogin/walletlogin/` - Java source code for the plugin
  - `commands/` - In-game commands
  - `database/` - Database handling
  - `session/` - Session management
  - `utils/` - Utilities
- `web-server/` - Web server source code
  - `public/` - Static files (HTML, CSS, JS)
  - `server.js` - Express server source code

## Contributing

Contributions are always welcome! Please follow these steps:

1. Fork the project
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

If you have any questions or suggestions, please open an issue on GitHub.

---

Developed for the Solana Hackathon 2025.
