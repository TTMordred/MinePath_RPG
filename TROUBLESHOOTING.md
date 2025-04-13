# SolanaLogin Troubleshooting Guide

This document provides solutions for common issues you might encounter when using the SolanaLogin plugin.

## Table of Contents

1. [Installation Issues](#installation-issues)
2. [Plugin Startup Issues](#plugin-startup-issues)
3. [Database Connection Issues](#database-connection-issues)
4. [Web Server Issues](#web-server-issues)
5. [Wallet Connection Issues](#wallet-connection-issues)
6. [Solana Integration Issues](#solana-integration-issues)
7. [Authentication Issues](#authentication-issues)
8. [Performance Issues](#performance-issues)
9. [Debugging Tools](#debugging-tools)

## Installation Issues

### Plugin Won't Load

**Symptoms:**

- Plugin is not listed when using `/plugins` command
- Error messages in the server console during startup

**Solutions:**

1. Verify that you're using a compatible Minecraft server version (1.18+)
2. Check that the JAR file is in the correct `plugins` directory
3. Ensure you have the correct Java version (Java 8 or higher)
4. Check server logs for specific error messages

### Web Server Won't Start

**Symptoms:**

- Error messages when running `npm start`
- Web server doesn't respond to requests

**Solutions:**

1. Ensure Node.js 14+ is installed (`node -v` to check)
2. Verify all dependencies are installed (`npm install`)
3. Check if port 3000 (or your configured port) is already in use
4. Look for specific error messages in the console

## Plugin Startup Issues

### Plugin Disables Itself

**Symptoms:**

- Plugin starts but then disables itself
- Error message about database connection

**Solutions:**

1. Check database configuration in `config.yml`
2. Ensure MySQL server is running
3. Verify database credentials are correct
4. Check if the database and tables exist

### Configuration Errors

**Symptoms:**

- Error messages about invalid configuration
- Plugin fails to load certain features

**Solutions:**

1. Reset to default configuration by deleting `config.yml` and restarting
2. Check for syntax errors in your YAML configuration
3. Ensure all required fields are present
4. Validate URLs and file paths

## Database Connection Issues

### Cannot Connect to Database

**Symptoms:**

- Error message: "Failed to initialize database"
- Plugin disables itself during startup

**Solutions:**

1. Verify MySQL server is running
2. Check database credentials in `config.yml`
3. Ensure the database exists and is accessible
4. Try connecting to the database manually to test credentials

```sql
mysql -u username -p
```

### Table Creation Fails

**Symptoms:**

- Error about creating tables
- Database connects but plugin still fails

**Solutions:**

1. Ensure the database user has CREATE TABLE permissions
2. Check if tables already exist but are incompatible
3. Try manually creating the tables using the schema in the documentation

## Web Server Issues

### Connection Refused

**Symptoms:**

- Error connecting to web server from Minecraft
- "Connection refused" errors

**Solutions:**

1. Ensure web server is running (`npm start` in the `web-server` directory)
2. Check that the port is correct in both web server and plugin config
3. Verify firewall settings allow connections to the port
4. Try accessing the web server directly in a browser

### CORS Errors

**Symptoms:**

- Web page loads but wallet connection fails
- Console errors about CORS policy

**Solutions:**

1. Ensure the `web-server.url` in `config.yml` matches the actual URL
2. Access the web server using the same hostname as configured
3. If using a custom domain, ensure it's properly configured in the web server

## Wallet Connection Issues

### QR Code Not Working

**Symptoms:**

- QR code displays but scanning doesn't connect
- Mobile app opens but connection fails

**Solutions:**

1. Ensure you're using the latest version of the Phantom app
2. Check that your phone has internet access
3. Verify the web server is accessible from your phone
4. Try using the browser extension method instead

### Phantom Extension Not Detected

**Symptoms:**

- "Phantom extension not found" error
- Connection button doesn't work

**Solutions:**

1. Install the Phantom browser extension
2. Ensure the extension is enabled
3. Log in to your Phantom wallet
4. Try refreshing the page or using a different browser

### Signature Verification Fails

**Symptoms:**

- "Invalid signature" error
- Connection process starts but fails to complete

**Solutions:**

1. Ensure you're connected to the correct Solana network (devnet)
2. Try the Simple Connect test page to verify signature generation
3. Check console logs for detailed error messages
4. Clear browser cache and try again

## Solana Integration Issues

### Solana Program Deployment Fails

**Symptoms:**

- Error messages during program deployment
- "Program ID not found" errors

**Solutions:**

1. Ensure you have the Solana CLI tools installed correctly
2. Verify you have enough SOL in your wallet for deployment
3. Check that you're using the correct Solana network (devnet/mainnet)
4. Try running `solana logs` to see detailed error messages

### Wallet Data Not Storing on Solana

**Symptoms:**

- Wallet connections work but aren't stored on Solana
- "Error connecting wallet on Solana" messages in logs

**Solutions:**

1. Verify that `solana.enabled` is set to `true` in config.yml
2. Check that `solana.program-id` matches your deployed program
3. Ensure `storage.wallet-links` is set to `solana` or `hybrid`
4. Check the web server logs for detailed error messages
5. Verify the Solana RPC endpoint is accessible

### Solana Transaction Errors

**Symptoms:**

- "Transaction simulation failed" errors
- "Blockhash not found" errors
- Transactions time out

**Solutions:**

1. Check that your wallet has enough SOL for transaction fees
2. Verify the RPC endpoint is responsive
3. Increase `performance.retry-attempts` in config.yml
4. Try a different RPC endpoint
5. Check if the Solana network is experiencing high load

### Solana Client Initialization Fails

**Symptoms:**

- "Failed to initialize Solana client" error in logs
- Plugin falls back to SQL storage despite Solana being enabled

**Solutions:**

1. Verify the Solana Program client is built correctly
2. Check that the web server has access to the Solana network
3. Ensure all dependencies are installed
4. Try rebuilding the Solana Program client:
   ```bash
   cd web-server/solana-program
   yarn install
   yarn build
   ```

### Data Inconsistency Between SQL and Solana

**Symptoms:**

- Different wallet information shown in different contexts
- Wallet appears connected in-game but not on the web interface

**Solutions:**

1. Set `storage-mode` to `hybrid` to use both storage systems
2. Enable `fallback-to-sql` in performance settings
3. Restart both the web server and Minecraft server
4. Reconnect your wallet to update both storage systems

## Authentication Issues

### Login Command Not Working

**Symptoms:**

- `/login` command doesn't authenticate the player
- Error message about invalid credentials

**Solutions:**

1. Ensure you've registered first with `/register <password> <confirmPassword>`
2. Check that you're using the correct password
3. Verify that your session hasn't expired
4. Try reconnecting to the server

### Wallet Verification Required

**Symptoms:**

- Message about wallet verification being required
- Cannot play despite successful login

**Solutions:**

1. Connect your wallet using `/connectwallet` or `/connectwallet qr`
2. Follow the on-screen instructions to complete the connection
3. Check if the server requires wallet connection (`require-wallet-login: true`)
4. Verify your wallet is connected to the correct Solana network

## Performance Issues

### Slow Login Process

**Symptoms:**

- Login takes a long time to complete
- Server lag during authentication

**Solutions:**

1. Check server performance and resources
2. Optimize database queries if you've modified the plugin
3. Ensure the web server has adequate resources
4. Consider increasing timeout values in the configuration

### Memory Leaks

**Symptoms:**

- Server memory usage increases over time
- Performance degrades after extended use

**Solutions:**

1. Ensure you're using the latest version of the plugin
2. Restart the server periodically
3. Monitor memory usage to identify patterns
4. Check for plugin conflicts

## Debugging Tools

### Test Pages

The web server includes several test pages to help debug wallet connection issues:

1. **Test Flow**: `http://localhost:3000/test-flow.html`
   - Tests the complete wallet connection flow
   - Useful for verifying the entire process works

2. **Simple Connect**: `http://localhost:3000/simple-connect.html`
   - Tests basic connection with Phantom extension
   - Useful for isolating signature issues

3. **Simple QR**: `http://localhost:3000/simple-qr.html`
   - Tests QR code generation and scanning
   - Useful for debugging mobile connection issues

4. **Simple Redirect**: `http://localhost:3000/simple-redirect.html`
   - Tests Phantom redirect handling
   - Useful for debugging URL parameter issues

### Server Logs

1. **Minecraft Server Logs**: Check `logs/latest.log` for plugin-related errors
2. **Web Server Logs**: Check the console where you started the web server
3. **Browser Console**: Open browser developer tools (F12) to check for JavaScript errors

### Development Mode

The web server includes a development mode that makes debugging easier:

```javascript
// Set development mode for testing
process.env.NODE_ENV = 'development';
```

In development mode:

- Missing parameters will be auto-filled with test values
- Signature verification is more lenient
- More detailed logging is enabled

To run in production mode, set `NODE_ENV=production` in your environment variables.

## Still Having Issues?

If you've tried the solutions above and are still experiencing problems:

1. Check the GitHub Issues page for similar problems and solutions
2. Create a new issue with detailed information about your problem
3. Include relevant logs, configuration, and steps to reproduce the issue
4. Specify your server version, Java version, and plugin version
