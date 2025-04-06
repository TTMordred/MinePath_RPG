# WalletLogin

A Minecraft Spigot plugin that integrates wallet authentication with Minecraft login. This plugin allows players to connect their blockchain wallets (Ethereum, Solana, etc.) to their Minecraft accounts.

## Features

- Connect blockchain wallets to Minecraft accounts
- Optional wallet login requirement
- MySQL database integration for storing wallet information
- Support for multiple blockchain wallet formats (Ethereum, Solana)
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
  require-wallet-login: false  # If true, players must connect a wallet to play
  login-timeout: 60  # Time in seconds for players to connect their wallet after joining
  wallet-validation: true  # Whether to validate wallet addresses format

# Messages
messages:
  prefix: "&8[&6WalletLogin&8] &r"
  wallet-connected: "&aYour wallet has been successfully connected!"
  wallet-disconnected: "&cYour wallet has been disconnected."
  wallet-required: "&cYou need to connect a wallet to play on this server. Use /connectwallet <address>"
  invalid-wallet: "&cThe wallet address you provided is invalid."
  already-connected: "&cYou already have a wallet connected. Use /disconnectwallet first."
  not-connected: "&cYou don't have a wallet connected."
  wallet-info: "&aYour connected wallet is: &6%wallet%"
```

## Commands

- `/connectwallet <wallet_address>` - Connect a wallet to your Minecraft account
- `/disconnectwallet` - Disconnect your wallet from your Minecraft account
- `/walletinfo` - View your connected wallet information

## Permissions

- `walletlogin.connect` - Allows players to connect their wallet (default: true)
- `walletlogin.disconnect` - Allows players to disconnect their wallet (default: true)
- `walletlogin.info` - Allows players to view their wallet information (default: true)

## Building from Source

1. Clone the repository
2. Build using Maven:
   ```
   mvn clean package
   ```
3. The compiled JAR will be in the `target` directory

## Future Enhancements

- Add support for NFT verification
- Implement wallet signature verification for enhanced security
- Add web interface for wallet connection
- Support for more blockchain networks

## License

This project is licensed under the MIT License - see the LICENSE file for details.
