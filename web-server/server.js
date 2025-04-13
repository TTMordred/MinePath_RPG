const express = require('express');
const cors = require('cors');
const path = require('path');
const QRCode = require('qrcode');
const nacl = require('tweetnacl');
const bs58 = require('bs58');
const { PublicKey } = require('@solana/web3.js');
const fs = require('fs');

// Set development mode for testing
process.env.NODE_ENV = 'development';

const app = express();
const PORT = process.env.PORT || 3000;

// Solana configuration - disabled for now
// In the future, we'll implement Solana blockchain storage

// In-memory storage for sessions and nonces
// In a production environment, use Redis or a database
const sessions = new Map();

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Routes
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Login page
app.get('/login', (req, res) => {
  const { session, nonce, player, qr } = req.query;
  console.log('Login request received:', { session, nonce, player, qr });

  if (!session || !nonce || !player) {
    console.error('Missing required parameters for login:', { session: !!session, nonce: !!nonce, player: !!player });
    // Instead of returning error, create a demo session for testing
    if (process.env.NODE_ENV !== 'production') {
      const demoSession = 'demo-' + Date.now();
      const demoNonce = 'nonce-' + Date.now();
      const demoPlayer = player || 'TestPlayer';

      console.log('Creating demo session for testing:', { demoSession, demoNonce, demoPlayer });

      // Store demo session data
      sessions.set(demoSession, {
        nonce: demoNonce,
        player: demoPlayer,
        connected: false,
        createdAt: Date.now(),
        isDemo: true
      });

      // Redirect to login page with demo parameters
      return res.redirect(`/login?session=${demoSession}&nonce=${demoNonce}&player=${demoPlayer}${qr === 'true' ? '&qr=true' : ''}`);
    }

    return res.status(400).send('Missing required parameters');
  }

  // Store session data
  sessions.set(session, {
    nonce,
    player,
    connected: false,
    createdAt: Date.now()
  });

  // If QR code is requested, show QR page
  if (qr === 'true') {
    return res.sendFile(path.join(__dirname, 'public', 'qr.html'));
  }

  // Otherwise show login page
  res.sendFile(path.join(__dirname, 'public', 'login.html'));
});

// Get session data
app.get('/api/session/:sessionId', (req, res) => {
  const { sessionId } = req.params;
  const session = sessions.get(sessionId);

  if (!session) {
    return res.status(404).json({ error: 'Session not found' });
  }

  // Don't expose the nonce
  const { nonce, ...sessionData } = session;

  res.json(sessionData);
});

// Get nonce for a session
app.get('/api/nonce/:sessionId', (req, res) => {
  const { sessionId } = req.params;
  const session = sessions.get(sessionId);

  if (!session) {
    return res.status(404).json({ error: 'Session not found' });
  }

  res.json({ nonce: session.nonce });
});

// Generate QR code
app.get('/api/qr', (req, res) => {
  const { session, nonce, player } = req.query;
  console.log('QR code request received:', { session, nonce, player });

  if (!session || !nonce || !player) {
    console.error('Missing required parameters for QR code:', { session: !!session, nonce: !!nonce, player: !!player });

    // For testing, create a demo session if parameters are missing
    if (process.env.NODE_ENV !== 'production') {
      const demoSession = session || ('demo-' + Date.now());
      const demoNonce = nonce || ('nonce-' + Date.now());
      const demoPlayer = player || 'TestPlayer';

      console.log('Creating demo session for QR code:', { demoSession, demoNonce, demoPlayer });

      // Store demo session
      sessions.set(demoSession, {
        nonce: demoNonce,
        player: demoPlayer,
        connected: false,
        createdAt: Date.now(),
        isDemo: true
      });

      // Continue with the demo session
      req.query.session = demoSession;
      req.query.nonce = demoNonce;
      req.query.player = demoPlayer;
    } else {
      return res.status(400).send('Missing required parameters');
    }
  }

  // Get session from updated query params
  const sessionId = req.query.session;

  // Create deep link for Phantom wallet
  const redirectUrl = `${req.protocol}://${req.get('host')}/phantom-redirect?session=${sessionId}`;

  // Use the official Phantom mobile deep link format
  // Documentation: https://docs.phantom.app/integrating/deeplinks-ios-and-android
  // Using devnet for testing
  const cluster = 'devnet';

  // Try a simpler deep link format for better compatibility
  // const deepLink = `https://phantom.app/ul/v1/connect?app_url=${encodeURIComponent(appUrl)}&dapp_encryption_public_key=phantom&redirect_url=${encodeURIComponent(redirectUrl)}&cluster=${cluster}`;

  // Alternative format focusing on just the essential parameters
  const deepLink = `https://phantom.app/ul/v1/connect?cluster=${cluster}&redirect_url=${encodeURIComponent(redirectUrl)}`;

  console.log('Generated deep link:', deepLink);

  // Generate QR code
  QRCode.toDataURL(deepLink, (err, url) => {
    if (err) {
      return res.status(500).json({ error: 'Failed to generate QR code' });
    }

    res.json({ qrCode: url, deepLink });
  });
});

// Verify wallet signature
app.post('/api/verify', (req, res) => {
  // Use let instead of const for variables that might be modified
  let { session, publicKey, signature, message } = req.body;
  console.log('Verify request received:', {
    session,
    publicKey,
    signature: signature?.substring(0, 20) + '...',
    messageLength: message?.length || 0
  });
  console.log('Request body:', JSON.stringify(req.body));

  // Check for empty signature
  if (signature === '') {
    console.error('Empty signature received');
    return res.status(400).json({ error: 'Empty signature' });
  }

  if (!session || !publicKey || !signature || !message) {
    console.error('Missing required parameters:', {
      session: !!session,
      publicKey: !!publicKey,
      signature: !!signature,
      message: !!message
    });

    // For testing, allow verification without all parameters
    if (process.env.NODE_ENV !== 'production' && session && publicKey) {
      console.log('Allowing verification with missing parameters for testing');

      // If signature is missing or empty, create a dummy one for testing
      if (!signature) {
        req.body.signature = 'dummySignatureForTesting';
        console.log('Created dummy signature for testing');
      }

      // If message is missing, create a dummy one
      if (!message) {
        req.body.message = `Verify wallet ownership for Minecraft login. Session: ${session}. Nonce: test-nonce`;
        console.log('Created dummy message:', req.body.message);
      }

      // Update local variables
      signature = req.body.signature;
      message = req.body.message;
    } else {
      return res.status(400).json({ error: 'Missing required parameters' });
    }
  }

  let sessionData = sessions.get(session);
  if (!sessionData) {
    console.error('Session not found:', session);

    // For testing, create a demo session if it doesn't exist
    if (process.env.NODE_ENV !== 'production') {
      console.log('Creating demo session for verification:', session);
      sessionData = {
        nonce: 'demo-nonce-' + Date.now(),
        player: 'TestPlayer',
        connected: false,
        createdAt: Date.now(),
        isDemo: true
      };
      sessions.set(session, sessionData);
    } else {
      return res.status(404).json({ error: 'Session not found' });
    }
  }

  console.log('Session data:', { ...sessionData, nonce: sessionData.nonce?.substring(0, 8) + '...' });

  try {
    // Verify signature
    const messageBytes = new TextEncoder().encode(message);
    let signatureBytes;

    // Check signature format and decode
    try {
      // Try bs58 first
      try {
        signatureBytes = bs58.decode(signature);
        console.log('Successfully decoded signature with bs58');
      } catch (bs58Error) {
        // Fallback to base64
        console.log('Falling back to base64 decoding for signature');
        try {
          const base64Decoded = Buffer.from(signature, 'base64');
          signatureBytes = new Uint8Array(base64Decoded);
          console.log('Successfully decoded signature with base64');
        } catch (base64Error) {
          // Fallback to hex
          console.log('Falling back to hex decoding for signature');
          try {
            // Convert hex string to Uint8Array
            if (signature.length % 2 !== 0) {
              throw new Error('Invalid hex string length');
            }
            const hexBytes = [];
            for (let i = 0; i < signature.length; i += 2) {
              hexBytes.push(parseInt(signature.substr(i, 2), 16));
            }
            signatureBytes = new Uint8Array(hexBytes);
            console.log('Successfully decoded signature with hex');
          } catch (hexError) {
            console.error('Failed to decode signature with any method:', { bs58Error, base64Error, hexError });
            return res.status(400).json({ error: 'Invalid signature format' });
          }
        }
      }
    } catch (error) {
      console.error('Unexpected error decoding signature:', error);
      return res.status(400).json({ error: 'Invalid signature format: ' + error.message });
    }

    // Validate public key
    let publicKeyObj;
    try {
      publicKeyObj = new PublicKey(publicKey);
      console.log('Valid Solana public key:', publicKey);
    } catch (pkError) {
      console.error('Invalid public key:', pkError);
      return res.status(400).json({ error: 'Invalid public key format' });
    }

    const publicKeyBytes = publicKeyObj.toBytes();

    // Verify signature
    console.log('Verifying signature...');
    const verified = nacl.sign.detached.verify(
      messageBytes,
      signatureBytes,
      publicKeyBytes
    );

    console.log('Signature verification result:', verified);

    if (verified) {
      // Update session
      sessionData.connected = true;
      sessionData.walletAddress = publicKey;
      sessionData.verifiedAt = Date.now();
      sessions.set(session, sessionData);

      // Solana blockchain storage is disabled for now
      // In the future, we'll implement storing wallet connections on Solana

      console.log('Session updated successfully, wallet connected');
      return res.json({ success: true });
    }

    console.error('Invalid signature verification');
    res.status(400).json({ error: 'Invalid signature' });
  } catch (error) {
    console.error('Error verifying signature:', error);
    res.status(500).json({ error: 'Failed to verify signature: ' + error.message });
  }
});

// Phantom redirect handler
app.get('/phantom-redirect', (req, res) => {
  const { session } = req.query;
  console.log('Phantom redirect received:', { session, query: req.query });

  if (!session) {
    console.error('Missing session parameter in redirect');
    return res.status(400).send('Missing session parameter');
  }

  // Use the simple redirect page for testing
  res.sendFile(path.join(__dirname, 'public', 'simple-redirect.html'));
});

// Check connection status
app.get('/status', (req, res) => {
  const { session } = req.query;

  if (!session) {
    return res.status(400).json({ error: 'Missing session parameter' });
  }

  const sessionData = sessions.get(session);
  if (!sessionData) {
    return res.status(404).json({ error: 'Session not found' });
  }

  res.json({
    connected: sessionData.connected,
    walletAddress: sessionData.walletAddress,
    player: sessionData.player
  });
});

// Clean up expired sessions every 5 minutes
setInterval(() => {
  const now = Date.now();
  const expirationTime = 5 * 60 * 1000; // 5 minutes

  for (const [sessionId, session] of sessions.entries()) {
    if (now - session.createdAt > expirationTime) {
      sessions.delete(sessionId);
    }
  }
}, 60 * 1000);

// Start server
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
