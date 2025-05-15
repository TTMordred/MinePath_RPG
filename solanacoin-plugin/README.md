# 🌟 MinePathCoinPlugin (v2) 🌟

The ultimate bridge between **Minecraft** and **Solana**—now with a unified `/token…` command set!

---

## 🎯 Table of Contents

1. [Introduction](#introduction)  
2. [Features](#features)  
3. [Requirements](#requirements)  
4. [Installation](#installation)  
5. [Configuration](#configuration)  
6. [Usage & Commands](#usage--commands)  
7. [Troubleshooting](#troubleshooting)  
8. [Credits](#credits)  
9. [License](#license)  

---

## 🧩 Introduction

MinePathCoinPlugin lets you sync balances with Solana, authorize accounts, burn tokens, and claim on-chain deposits.  
Version 2 adds a `/token…` prefix for consistency and requires a companion Node.js service for Solana RPC handling.

---

## ✨ Features

- **Balance tracking** – keep player balances synced with Solana  
- **Account authorization** – link Minecraft UUIDs to Solana wallets (`/tokenauthorize`, admin-only)  
- **Token burning** – permanently remove tokens from treasury (`/tokenburn`, admin-only)  
- **On-chain claim** – credit external deposits to players (`/tokenclaim`, admin-only)  
- **Database support** – SQLite, MySQL, PostgreSQL via JDBC  
- **Vault integration** – optional permissions control  

---

## 📋 Requirements

- **Java 17** (OpenJDK or Oracle)  
- **Spigot/Bukkit API** (1.16+ tested)  
- **Node.js & npm** (v14+)  
- **Vault + Permissions Plugin** (e.g. LuckPerms) — optional  

---

## 🚀 Installation

1. **Clone & build the plugin**  
   ```bash
   git clone https://github.com/HungPhan-0612/Minecraft-Solana-Coin-Plugin.git
   cd Minecraft-Solana-Coin-Plugin
   ./gradlew clean shadowJar     # Windows: gradlew.bat clean shadowJar
   
2. **Deploy the JAR**

- copy build/libs/MinePathCoinPlugin.jar → <your-server>/plugins/

3. **Set up the Solana service**
   ```bash
    cd server_handle_solanacoin  # located in the repo root
    npm install
    npm run start
    Restart your Minecraft server
    A default plugins/MinePath/config.yml will be generated.


---

## ⚙️ Configuration

Edit `plugins/MinePath/config.yml`:

    ```yaml
    enabled: true
    vaultEnabled: true         # set false to disable Vault integration
    dbType: mysql              # sqlite | mysql | postgresql
    sqliteLocation: plugins/MinePath/MinePath.db
    dbHost: localhost
    dbPort: 3306
    dbName: minecraft
    dbTable: balance
    dbUsername: root
    dbPassword: 
    dbUseSSL: false
    
    # Solana settings
    tokenMint: "8ukoz8y6bJxpjUVSE3bEDbGjwyStqXBQZiSyxjhjNx1g"
    signer: "<BASE58_PRIVATE_KEY>"
    publicKey: "<BASE58_PUBLIC_KEY>"
    rpcURL: "https://api.devnet.solana.com"
    
    currencySymbol: "MINECRAFT"
    startingBalance: 0.0
    minimumExport: 0.5
    requestLimitPerSecond: 1
    
    # Companion service
    linkweb: "<LINK TO THE HANDLE SERVER>"


---

## 🚩 Usage & Commands

### User Commands

| Command             | Description                                                           |
|---------------------|-----------------------------------------------------------------------|
| `/tokenauthorize`   | Link your Minecraft UUID to a session Solana wallet (must be authorized).     |
| `/tokenbalance`     | Show your own (or, when run from console, the server’s) token balance.|

### Admin Commands

| Command                                             | Description                                                                                                                              |
|-----------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| `/tokenadmin balance [player]`                      | Get the balance of a specific player or the admin themselve.                                                                                      |
| `/tokenadmin add <player> <amount>`                 | Add tokens to a player’s balance.                                                                                                        |
| `/tokenadmin destroydb confirm`                     | Permanently delete the plugin database (requires `confirm`).                                                                             |
| `/tokenadmin reload`                                | Reload the plugin’s configuration.                                                                                                       |
| `/tokenlbh`                                         | Display the latest Solana blockhash fetched from RPC.                                                                                    |
| `/tokendb <connect\|disconnect\|status>`            | Connect to, disconnect from, or view status of the configured database.                                                                  |
| `/tokenburn <amount>`                               | Burn an amount tokens from the user Associated Token Account (requires `server_handle_solanacoin` service running and prior authorization). **For other-plugin integration.** |
| `/tokenclaim <amount>`                         | Credit tokens to the user Associated Token Account (requires `server_handle_solanacoin` service running and prior authorization). **For other-plugin integration.** |

> **Note:**  
> - Destructive operation (`destroydb`) require the `confirm` argument.  
> - The `/tokenburn` and `/tokenclaim` commands are intended for use by external plugins once a user has been authorized.

---

## 🛠️ Troubleshooting

| Issue                             | Solution                                                      |
|-----------------------------------|---------------------------------------------------------------|
| Plugin fails to enable            | Check that `signer`, `publicKey`, and `tokenMint` are set     |
| DB connection errors              | Verify JDBC URL, credentials; use `/tokendb status`           |
| RPC requests hanging              | Ensure `server_handle_solanacoin` is running and healthy      |
| Vault permissions not applied     | Install Vault + a permissions plugin, set `vaultEnabled: true`|
