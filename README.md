# SolanaLogin Plugin

A Minecraft Spigot plugin that integrates Solana wallet authentication with Minecraft login. This plugin allows players to connect their Solana wallets to their Minecraft accounts and supports both premium and cracked Minecraft accounts.

## Features

- Authentication system for both premium and cracked Minecraft accounts
- Secure password storage with PBKDF2 hashing
- Connect Solana wallets to Minecraft accounts
- Wallet verification system
- MySQL database integration for storing player and wallet information
- Session management with timeout
- Configurable messages and settings

## Installation

1. Download the latest release JAR file
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in the `config.yml` file

## Configuration

The plugin's configuration file (`config.yml`) allows you to customize various settings:

```yaml
# Database Configuration
database:
  host: localhost
  port: 3306
  database: minecraft
  username: root
  password: password
  table-prefix: walletlogin_

# Plugin Settings
settings:
  require-login: true  # If true, players must login to play
  require-wallet-login: false  # If true, players must connect a wallet to play
  login-timeout: 60  # Time in seconds for players to login after joining
  session-timeout: 1440  # Time in minutes for session to expire (24 hours)
  max-login-attempts: 5  # Maximum number of login attempts before timeout
  login-attempt-timeout: 10  # Time in minutes for login attempt timeout
  register-ip-limit: 3  # Maximum number of accounts per IP
  wallet-validation: true  # Whether to validate wallet addresses format
  solana-only: true  # Only allow Solana wallets

# Authentication Settings
auth:
  min-password-length: 6  # Minimum password length
  max-password-length: 32  # Maximum password length
  hash-algorithm: "PBKDF2WithHmacSHA256"  # Password hashing algorithm
  hash-iterations: 65536  # Number of iterations for password hashing
  hash-key-length: 256  # Key length for password hashing
  salt-length: 16  # Salt length for password hashing

# Solana Settings
solana:
  network: "mainnet"  # mainnet, testnet, or devnet
  rpc-url: "https://api.mainnet-beta.solana.com"  # RPC URL for Solana network
  verification-message: "I confirm that I own this wallet and authorize its use on the Minecraft server."  # Message to sign for verification

# Messages
messages:
  prefix: "&8[&6SolanaLogin&8] &r"
  # Login/Register Messages
  login-required: "&cPlease login with /login <password>"
  register-required: "&cPlease register with /register <password> <confirmPassword>"
  login-success: "&aYou have successfully logged in!"
  login-fail: "&cIncorrect password! Attempts remaining: %attempts%"
  login-timeout: "&cYou took too long to login. Please reconnect."
  login-attempts-exceeded: "&cToo many failed login attempts. Please try again later."
  register-success: "&aYou have successfully registered! Please login with /login <password>"
  register-fail: "&cRegistration failed. Please try again."
  register-password-mismatch: "&cPasswords do not match!"
  register-password-too-short: "&cPassword is too short! Minimum length: %length%"
  register-password-too-long: "&cPassword is too long! Maximum length: %length%"
  register-ip-limit: "&cYou have reached the maximum number of accounts for your IP!"
  already-logged-in: "&cYou are already logged in!"
  already-registered: "&cYou are already registered!"
  not-logged-in: "&cYou must be logged in to use this command!"

  # Wallet Messages
  wallet-connected: "&aYour Solana wallet has been successfully connected!"
  wallet-disconnected: "&cYour Solana wallet has been disconnected."
  wallet-required: "&cYou need to connect a Solana wallet to play on this server. Use /connectwallet <address>"
  invalid-wallet: "&cThe wallet address you provided is not a valid Solana address."
  already-connected: "&cYou already have a wallet connected. Use /disconnectwallet first."
  not-connected: "&cYou don't have a wallet connected."
  wallet-info: "&aYour connected Solana wallet is: &6%wallet%"
  wallet-verification-required: "&cYou need to verify your wallet ownership. Please check the website or use /verifycode <code>"
  wallet-verification-success: "&aYour wallet has been successfully verified!"
  wallet-verification-fail: "&cWallet verification failed. Please try again."
  wallet-verification-pending: "&eYour wallet verification is pending. Please complete the verification process."
```

## Commands

### Authentication Commands

- `/register <password> <confirmPassword>` - Register an account
- `/login <password>` - Login to your account
- `/changepassword <oldPassword> <newPassword> <confirmNewPassword>` - Change your password
- `/logout` - Logout from your account

### Wallet Commands

- `/connectwallet <wallet_address>` - Connect your Solana wallet to your Minecraft account
- `/disconnectwallet` - Disconnect your Solana wallet from your Minecraft account
- `/walletinfo` - View your Solana wallet information
- `/verifycode <code>` - Verify your wallet with a verification code

### Admin Commands

- `/solanalogin reload` - Reload the plugin configuration
- `/solanalogin info` - View plugin information

## Permissions

### Authentication Permissions

- `solanalogin.register` - Allows players to register an account (default: true)
- `solanalogin.login` - Allows players to login to their account (default: true)
- `solanalogin.changepassword` - Allows players to change their password (default: true)
- `solanalogin.logout` - Allows players to logout from their account (default: true)

### Wallet Permissions

- `solanalogin.wallet.connect` - Allows players to connect their Solana wallet (default: true)
- `solanalogin.wallet.disconnect` - Allows players to disconnect their Solana wallet (default: true)
- `solanalogin.wallet.info` - Allows players to view their Solana wallet information (default: true)
- `solanalogin.wallet.verify` - Allows players to verify their Solana wallet (default: true)

### Admin Permissions

- `solanalogin.admin` - Allows access to admin commands (default: op)

## Building from Source

1. Clone the repository
2. Build using Maven:

   ```bash
   mvn clean package
   ```

3. The compiled JAR will be in the `target` directory

## Future Enhancements

- Add support for NFT verification
- Implement wallet signature verification for enhanced security
- Add web interface for wallet connection
- Integration with Solana NFT marketplaces

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

If you have any questions or need help, please feel free to open an issue or contact us at [NFTLogin](https://nftlogin.com).
