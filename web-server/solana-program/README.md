# Minecraft Authentication Solana Program

This is a Solana Program for storing Minecraft account and wallet links on the Solana blockchain.

## Overview

The Minecraft Authentication Solana Program allows:

- Connecting a Solana wallet to a Minecraft UUID
- Verifying wallet ownership
- Disconnecting a wallet from a Minecraft UUID
- Querying wallet information for a Minecraft UUID

## Development Setup

### Prerequisites

- [Rust](https://www.rust-lang.org/tools/install)
- [Solana CLI](https://docs.solana.com/cli/install-solana-cli-tools)
- [Anchor](https://project-serum.github.io/anchor/getting-started/installation.html)
- [Node.js](https://nodejs.org/en/download/)
- [Yarn](https://yarnpkg.com/getting-started/install)

### Build

```bash
# Install dependencies
yarn install

# Build the program and TypeScript client
yarn build
```

### Deploy

```bash
# Deploy to Solana devnet
yarn deploy
```

### Test

```bash
# Run tests
yarn test
```

## Program Structure

### Accounts

- `MinecraftAccount`: Stores the link between a Minecraft UUID and a Solana wallet

### Instructions

- `connectWallet`: Connect a wallet to a Minecraft UUID
- `disconnectWallet`: Disconnect a wallet from a Minecraft UUID
- `verifyWallet`: Verify wallet ownership

## TypeScript Client

The TypeScript client provides a simple interface for interacting with the Solana Program:

```typescript
import { MinecraftAuthClient } from 'minecraft-auth';

// Create a client
const client = new MinecraftAuthClient(connection, wallet, programId);

// Connect a wallet
await client.connectWallet('minecraft-uuid');

// Verify a wallet
await client.verifyWallet('minecraft-uuid');

// Get a Minecraft account
const account = await client.getMinecraftAccount('minecraft-uuid');

// Disconnect a wallet
await client.disconnectWallet('minecraft-uuid');
```
