# SolanaLogin Web Server

This is the web server component for the SolanaLogin Minecraft plugin. It handles wallet authentication via QR code and browser extension.

## Setup

1. Install dependencies:
   ```bash
   npm install
   ```

2. Start the server:
   ```bash
   npm start
   ```

The server will run on port 3000 by default. You can change this by setting the `PORT` environment variable.

## API Endpoints

- `GET /login` - Login page
- `GET /api/qr` - Generate QR code for mobile wallet
- `POST /api/verify` - Verify wallet signature
- `GET /status` - Check connection status

## Configuration

Make sure the URL in the Minecraft plugin's `config.yml` matches the URL where this web server is running:

```yaml
web-server:
  enabled: true
  url: "http://localhost:3000"  # Change this to your server's URL
  port: 3000
  qr-code-timeout: 300
  check-interval: 5
```
