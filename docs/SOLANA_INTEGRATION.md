# Solana Integration Guide

This guide explains how to set up and configure the Solana blockchain integration for the SolanaLogin plugin.

## Overview

SolanaLogin now supports storing wallet links and verification status on the Solana blockchain, providing:

- Improved security through decentralized storage
- Immutable record of wallet connections
- Transparent verification process
- Fallback mechanisms for reliability

## Storage Modes

The plugin supports three storage modes:

1. **SQL Mode**: All data is stored in the SQL database (traditional approach)
2. **Solana Mode**: Wallet links are stored on the Solana blockchain, other data in SQL
3. **Hybrid Mode**: Combines both approaches with automatic fallback

## Configuration

### Basic Solana Settings

Edit your `config.yml` file to configure Solana integration:

```yaml
# Solana Settings
solana:
  enabled: true                                    # Enable/disable Solana integration
  network: "devnet"                                # mainnet, testnet, or devnet
  rpc-url: "https://api.devnet.solana.com"         # RPC URL for Solana network
  program-id: "YOUR_PROGRAM_ID"                    # Program ID of the deployed Solana program
  storage-mode: "hybrid"                           # sql, solana, or hybrid
  verification-message: "I confirm that I own this wallet and authorize its use on the Minecraft server."

# Storage Configuration
storage:
  wallet-links: "solana"                           # Store wallet links on Solana
  user-data: "sql"                                 # Store user data in SQL
  sessions: "sql"                                  # Store sessions in SQL
```

### Deploying the Solana Program

1. **Prerequisites**:
   - [Rust](https://www.rust-lang.org/tools/install)
   - [Solana CLI](https://docs.solana.com/cli/install-solana-cli-tools)
   - [Anchor](https://project-serum.github.io/anchor/getting-started/installation.html)
   - [Node.js](https://nodejs.org/en/download/)
   - [Yarn](https://yarnpkg.com/getting-started/install)

2. **Set up a Solana wallet**:
   ```bash
   solana-keygen new --outfile ~/.config/solana/id.json
   ```

3. **Configure Solana CLI for devnet**:
   ```bash
   solana config set --url https://api.devnet.solana.com
   ```

4. **Fund your wallet**:
   ```bash
   solana airdrop 2
   ```

5. **Build and deploy the program**:
   ```bash
   cd web-server/solana-program
   yarn install
   yarn build
   yarn deploy
   ```

6. **Update your config.yml**:
   - Copy the Program ID from the deployment output
   - Update the `solana.program-id` field in your config.yml

## Testing the Integration

1. **Start the web server**:
   ```bash
   ./start-web-server.sh
   ```

2. **Start your Minecraft server**:
   ```bash
   java -jar spigot.jar
   ```

3. **Connect a wallet**:
   - Join the server
   - Register and login
   - Use `/connectwallet` to connect your Solana wallet
   - The connection will be stored on the Solana blockchain

4. **Verify the connection**:
   - Use `/walletinfo` to check your wallet status
   - The plugin will retrieve the data from the Solana blockchain

## Troubleshooting

### Common Issues

1. **"Failed to initialize Solana client"**:
   - Check that your RPC URL is correct
   - Ensure your Program ID is valid
   - Verify that the Solana network is accessible

2. **"Error connecting wallet on Solana"**:
   - Check that your wallet has enough SOL for transaction fees
   - Verify that the Program ID in config.yml matches the deployed program
   - Check the web server logs for detailed error messages

3. **"No wallet connection found on Solana"**:
   - If using hybrid mode, the plugin will automatically fall back to SQL
   - Check that the wallet was successfully connected
   - Verify that the Solana program is deployed correctly

### Viewing Solana Transactions

You can view your transactions on the Solana blockchain using a block explorer:

- [Solana Explorer](https://explorer.solana.com/?cluster=devnet) (for devnet)
- [Solscan](https://solscan.io) (for mainnet)

Enter your wallet address or transaction ID to see the details.

## Advanced Configuration

### Performance Tuning

Add these options to your config.yml for performance optimization:

```yaml
# Performance Configuration
performance:
  cache-enabled: true                              # Enable caching of Solana data
  cache-ttl: 300                                   # Cache time-to-live in seconds
  retry-attempts: 3                                # Number of retry attempts for Solana transactions
  fallback-to-sql: true                            # Automatically fall back to SQL if Solana fails
```

### Security Considerations

- **Program Authority**: The program authority controls who can modify the Solana program
- **Transaction Signing**: All transactions are signed by the server's wallet
- **Verification**: Wallet verification requires a signature from the wallet owner

## Migrating Existing Data

To migrate existing wallet links from SQL to Solana:

1. Set `solana.storage-mode` to `hybrid`
2. Set `storage.wallet-links` to `solana`
3. Restart the server
4. The plugin will automatically use Solana for new connections
5. Existing connections will be read from SQL until they are updated

## Development Resources

- [Solana Documentation](https://docs.solana.com/)
- [Anchor Framework](https://project-serum.github.io/anchor/)
- [Solana Web3.js](https://solana-labs.github.io/solana-web3.js/)
- [Phantom Wallet Documentation](https://docs.phantom.app/)
