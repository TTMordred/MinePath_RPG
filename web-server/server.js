const express = require('express');
const cors = require('cors');
const path = require('path');
const QRCode = require('qrcode');
const nacl = require('tweetnacl');
const bs58 = require('bs58');
const { PublicKey } = require('@solana/web3.js');

const app = express();
const PORT = process.env.PORT || 3000;

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
  
  if (!session || !nonce || !player) {
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

// Generate QR code
app.get('/api/qr', (req, res) => {
  const { session, nonce, player } = req.query;
  
  if (!session || !nonce || !player) {
    return res.status(400).send('Missing required parameters');
  }
  
  // Create deep link for Phantom wallet
  const redirectUrl = `${req.protocol}://${req.get('host')}/phantom-redirect?session=${session}`;
  const deepLink = `https://phantom.app/ul/v1/connect?app_url=${encodeURIComponent(req.protocol + '://' + req.get('host'))}&redirect_link=${encodeURIComponent(redirectUrl)}&cluster=mainnet`;
  
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
  const { session, publicKey, signature, message } = req.body;
  
  if (!session || !publicKey || !signature || !message) {
    return res.status(400).json({ error: 'Missing required parameters' });
  }
  
  const sessionData = sessions.get(session);
  if (!sessionData) {
    return res.status(404).json({ error: 'Session not found' });
  }
  
  try {
    // Verify signature
    const messageBytes = new TextEncoder().encode(message);
    const signatureBytes = bs58.decode(signature);
    const publicKeyBytes = new PublicKey(publicKey).toBytes();
    
    const verified = nacl.sign.detached.verify(
      messageBytes,
      signatureBytes,
      publicKeyBytes
    );
    
    if (verified) {
      // Update session
      sessionData.connected = true;
      sessionData.walletAddress = publicKey;
      sessionData.verifiedAt = Date.now();
      sessions.set(session, sessionData);
      
      return res.json({ success: true });
    }
    
    res.status(400).json({ error: 'Invalid signature' });
  } catch (error) {
    console.error('Error verifying signature:', error);
    res.status(500).json({ error: 'Failed to verify signature' });
  }
});

// Phantom redirect handler
app.get('/phantom-redirect', (req, res) => {
  const { session } = req.query;
  
  if (!session) {
    return res.status(400).send('Missing session parameter');
  }
  
  res.sendFile(path.join(__dirname, 'public', 'redirect.html'));
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
