// server.js

const express   = require('express');
const cors      = require('cors');
const path      = require('path');
const QRCode    = require('qrcode');
const nacl      = require('tweetnacl');
const bs58      = require('bs58');
const { PublicKey } = require('@solana/web3.js');

const app  = express();
const PORT = process.env.PORT || 3000;

// In-memory storage for sessions and nonces
// (use Redis or a real database in production)
const sessions = new Map();

// ─── Middleware ────────────────────────────────────────────────────────────────
// only allow your front‑end domain to talk to these APIs
app.use(cors({ origin: 'https://minepath.vercel.app' }));
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// ─── Routes ────────────────────────────────────────────────────────────────────

// Home / landing page
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Login page (with optional QR view)
app.get('/login', (req, res) => {
  let { session, nonce, player, qr } = req.query;
  console.log('Login request:', { session, nonce, player, qr });

  // If missing params, create a demo session in non‑prod
  if ((!session || !nonce || !player) && process.env.NODE_ENV !== 'production') {
    session = session || `demo-${Date.now()}`;
    nonce   = nonce   || `nonce-${Date.now()}`;
    player  = player  || 'TestPlayer';

    sessions.set(session, { nonce, player, connected: false, createdAt: Date.now(), isDemo: true });
    return res.redirect(`/login?session=${session}&nonce=${nonce}&player=${player}${qr === 'true' ? '&qr=true' : ''}`);
  }

  if (!session || !nonce || !player) {
    return res.status(400).send('Missing required parameters');
  }

  sessions.set(session, { nonce, player, connected: false, createdAt: Date.now() });

  if (qr === 'true') {
    return res.sendFile(path.join(__dirname, 'public', 'qr.html'));
  }
  res.sendFile(path.join(__dirname, 'public', 'login.html'));
});

// Fetch session info (hides nonce)
app.get('/api/session/:sessionId', (req, res) => {
  const session = sessions.get(req.params.sessionId);
  if (!session) return res.status(404).json({ error: 'Session not found' });
  const { nonce, ...data } = session;
  res.json(data);
});

// Fetch nonce only
app.get('/api/nonce/:sessionId', (req, res) => {
  const session = sessions.get(req.params.sessionId);
  if (!session) return res.status(404).json({ error: 'Session not found' });
  res.json({ nonce: session.nonce });
});

// Generate a Phantom deep‑link QR code
app.get('/api/qr', (req, res) => {
  let { session, nonce, player } = req.query;

  // demo fallback in non‑prod
  if ((!session || !nonce || !player) && process.env.NODE_ENV !== 'production') {
    session = session || `demo-${Date.now()}`;
    nonce   = nonce   || `nonce-${Date.now()}`;
    player  = player  || 'TestPlayer';
    sessions.set(session, { nonce, player, connected: false, createdAt: Date.now(), isDemo: true });
  }
  if (!session || !nonce || !player) {
    return res.status(400).send('Missing required parameters');
  }

  const redirectUrl = `${req.protocol}://${req.get('host')}/phantom-redirect?session=${session}`;
  const cluster     = 'devnet';
  const deepLink    = `https://phantom.app/ul/v1/connect?cluster=${cluster}&redirect_url=${encodeURIComponent(redirectUrl)}`;

  QRCode.toDataURL(deepLink, (err, url) => {
    if (err) return res.status(500).json({ error: 'Failed to generate QR code' });
    res.json({ qrCode: url, deepLink });
  });
});

// Verify signature from the wallet
app.post('/api/verify', (req, res) => {
  let { session, publicKey, signature, message } = req.body;
  console.log('Verify request:', { session, publicKey });

  if (!session || !publicKey) {
    return res.status(400).json({ error: 'Missing session or publicKey' });
  }

  // ensure session exists
  if (!sessions.has(session) && process.env.NODE_ENV !== 'production') {
    sessions.set(session, {
      nonce:     `demo-nonce-${Date.now()}`,
      player:    'TestPlayer',
      connected: false,
      createdAt: Date.now(),
      isDemo:    true
    });
  }
  const sessionData = sessions.get(session);
  if (!sessionData) return res.status(404).json({ error: 'Session not found' });

  // decode signature (bs58 → base64 → hex fallback)
  let sigBytes;
  try {
    sigBytes = bs58.decode(signature);
  } catch {
    try {
      sigBytes = Uint8Array.from(Buffer.from(signature, 'base64'));
    } catch {
      const hex = signature.replace(/^0x/, '');
      const arr = [];
      for (let i = 0; i < hex.length; i += 2) {
        arr.push(parseInt(hex.substr(i, 2), 16));
      }
      sigBytes = new Uint8Array(arr);
    }
  }

  // verify
  const msgBytes = new TextEncoder().encode(message);
  const pubKey   = new PublicKey(publicKey).toBytes();
  const ok       = nacl.sign.detached.verify(msgBytes, sigBytes, pubKey);

  if (!ok) {
    return res.status(400).json({ error: 'Invalid signature' });
  }

  // success → mark connected
  sessionData.connected     = true;
  sessionData.walletAddress = publicKey;
  sessions.set(session, sessionData);
  res.json({ success: true });
});

// Phantom mobile redirect endpoint
app.get('/phantom-redirect', (req, res) => {
  const { session } = req.query;
  if (!session) return res.status(400).send('Missing session');
  res.sendFile(path.join(__dirname, 'public', 'simple-redirect.html'));
});

// Check current connection status
app.get('/status', (req, res) => {
  const session = sessions.get(req.query.session);
  if (!session) return res.status(404).json({ error: 'Session not found' });
  res.json({
    connected:     session.connected,
    walletAddress: session.walletAddress,
    player:        session.player
  });
});

// cleanup expired sessions every 5 minutes
setInterval(() => {
  const now = Date.now();
  for (const [id, s] of sessions.entries()) {
    if (now - s.createdAt > 5 * 60 * 1000) sessions.delete(id);
  }
}, 60 * 1000);

// ─── Local listen for development ───────────────────────────────────────────────
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`🟢 Local server listening on http://localhost:${PORT}`);
  });
}

// ─── Export for Vercel ───────────────────────────────────────────────────────────
module.exports = app;
