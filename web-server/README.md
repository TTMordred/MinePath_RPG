# SolanaLogin Web Server

This is the web server component of the SolanaLogin project, handling Solana wallet authentication for the Minecraft plugin.

## Features

- Solana wallet authentication through blockchain signatures
- Support for Phantom browser extension connections
- QR code generation and handling for Phantom mobile app
- Integration with Solana devnet network
- RESTful API for interaction with the Minecraft plugin

## Installation

1. Install Node.js (version 14 or higher)
2. Install dependencies:

   ```bash
   npm install
   ```

3. Start the server:

   ```bash
   npm start
   ```

The server will run on port 3000 by default. You can change this by setting the `PORT` environment variable.

## API Endpoints

- `GET /login` - Login page
- `GET /api/qr` - Generate QR code for mobile connection
- `POST /api/verify` - Verify wallet signature
- `GET /status` - Check connection status
- `GET /phantom-redirect` - Handle redirect from Phantom

## Testing and Debug Pages

The web server includes several test pages to help debug wallet connection issues:

1. **Test Flow**: `/test-flow.html`
   Access [http://localhost:3000/test-flow.html](http://localhost:3000/test-flow.html)
   - Test the complete wallet connection flow
   - Create session, connect wallet, and verify connection

2. **Simple Connect**: `/simple-connect.html`
   - Test basic connection with Phantom extension
   - Test message signing

3. **Simple QR**: `/simple-qr.html`
   - Test QR code generation and scanning
   - Test deep linking for Phantom mobile

4. **Simple Redirect**: `/simple-redirect.html`
   - Test Phantom redirect handling
   - Test URL parameters

## Configuration

Make sure the URL in the Minecraft plugin's `config.yml` file matches the URL where this web server is running:

```yaml
web-server:
  enabled: true
  url: "http://localhost:3000"  # Change to your server's URL
  port: 3000
  qr-code-timeout: 300
  check-interval: 5
```

## Development Mode

The server includes a development mode that makes debugging easier:

```javascript
// Set development mode for testing
process.env.NODE_ENV = 'development';
```

In development mode:

- Missing parameters will be auto-filled with test values
- Signature verification is more lenient
- More detailed logging is enabled

To run in production mode, set `NODE_ENV=production` in your environment variables.

## Latest Updates (Version 1.3)

- Optimized code structure with utility functions
- Improved signature handling from Phantom wallet
- Fixed "Empty signature" error when connecting from Minecraft
- Added multiple fallback methods for signature handling
- Improved UI and user experience
- Added more detailed logging for easier debugging

## Previous Updates (Version 1.2)

- Switched to Solana devnet network
- Improved Phantom wallet connection handling
- Fixed "Missing required parameters" error during signature verification
- Simplified deep link format for Phantom wallet
- Added development mode for easier debugging
- Improved error handling and logging
- Added simple test pages for wallet connection debugging

## Troubleshooting

### Common Issues

- **"Missing required parameters" error**: Ensure the login URL contains all required parameters: session, nonce, and player.
- **Cannot connect to Phantom**: Check that the Phantom extension is installed and logged in.
- **QR code not working**: Make sure the Phantom app is updated to the latest version.
- **Signature verification fails**: Try using the Simple Connect test page to verify that signature generation works correctly.

### Debugging Tips

1. Check the console logs for detailed error messages
2. Use the test pages to isolate specific components of the authentication flow
3. Ensure your Phantom wallet is connected to the correct Solana network (devnet)
4. Verify that CORS is properly configured if accessing from a different domain
