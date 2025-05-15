// server.js
// Refactored for Netlify Functions + local testing

const express      = require('express');
const cors         = require('cors');
const path         = require('path');
const QRCode       = require('qrcode');
const nacl         = require('tweetnacl');
const bs58         = require('bs58');
const { PublicKey }= require('@solana/web3.js');
const serverless   = require('serverless-http');

// Environment flag
typeof process.env.NODE_ENV === 'string' || (process.env.NODE_ENV = 'development');
const isProd = process.env.NODE_ENV === 'production';
const PORT   = process.env.PORT || 3000;

// In-memory session store (for demo; swap for Redis in prod)
const sessions = new Map();

// Express setup
const app = express();
app.use(cors({ origin: [ 'https://minepath-api.netlify.app', 'http://localhost:3000' ], credentials: true }));
app.use(express.json());

// Serve static locally only
if (!isProd) {
  app.use(express.static(path.join(__dirname, 'public')));
}

// -- LOGIN endpoint: store session, then redirect to static login page --
app.get('/login', (req, res) => {
  let { session, nonce, player, qr } = req.query;
  console.log('Login request:', { session, nonce, player, qr });

  // Demo session in dev
  if (!session || !nonce || !player) {
    if (!isProd) {
      session = session || `demo-${Date.now()}`;
      nonce   = nonce   || `nonce-${Date.now()}`;
      player  = player  || 'TestPlayer';
      sessions.set(session, { nonce, player, connected: false, createdAt: Date.now(), isDemo: true });
      // redirect back into /login with params
      return res.redirect(`/login?session=${session}&nonce=${nonce}&player=${player}${qr==='true'? '&qr=true':''}`);
    }
    return res.status(400).send('Missing required parameters');
  }

  // store session
  sessions.set(session, { nonce, player, connected: false, createdAt: Date.now() });

  // build static page URL
  const page = qr==='true' ? 'qr.html' : 'login.html';
  const query = `?session=${encodeURIComponent(session)}&nonce=${encodeURIComponent(nonce)}&player=${encodeURIComponent(player)}` + (qr==='true'? '&qr=true':'');
  const redirectUrl = `${isProd ? '' : ''}/${page}${query}`;

  return res.redirect(redirectUrl);
});

// -- QR generation endpoint --
app.get('/api/qr', (req, res) => {
  let { session, nonce, player } = req.query;
  console.log('QR request:', { session, nonce, player });

  if (!session || !nonce || !player) {
    if (!isProd) {
      session = session || `demo-${Date.now()}`;
      nonce   = nonce   || `nonce-${Date.now()}`;
      player  = player  || 'TestPlayer';
      sessions.set(session, { nonce, player, connected: false, createdAt: Date.now(), isDemo: true });
    } else {
      return res.status(400).send('Missing parameters');
    }
  }

  const cluster     = 'devnet';
  const redirectUrl = `https://minepath-api.netlify.app/phantom-redirect?session=${encodeURIComponent(session)}`;
  const deepLink    = `https://phantom.app/ul/v1/connect?cluster=${cluster}&redirect_url=${encodeURIComponent(redirectUrl)}`;

  QRCode.toDataURL(deepLink, (err, url) => {
    if (err) return res.status(500).json({ error: 'QR generation failed' });
    res.json({ qrCode: url, deepLink });
  });
});

// -- Verify signature endpoint --
app.post('/api/verify', (req, res) => {
  let { session, publicKey, signature, message } = req.body;
  console.log('Verify request:', { session, publicKey, signature: signature?.slice(0,10), msgLen: message?.length });

  if (!session || !publicKey || !signature || !message) {
    if (!isProd && session && publicKey) {
      signature = signature || 'dummySig';
      message   = message   || `Verify ${session}`;
    } else {
      return res.status(400).json({ error: 'Missing parameters' });
    }
  }

  let data = sessions.get(session);
  if (!data) {
    if (!isProd) {
      data = { nonce: 'demo', player: 'TestPlayer', connected: false, createdAt: Date.now(), isDemo: true };
      sessions.set(session, data);
    } else {
      return res.status(404).json({ error: 'Session not found' });
    }
  }

  try {
    let sigBytes;
    try { sigBytes = bs58.decode(signature); } catch { sigBytes = Buffer.from(signature, 'base64'); }
    const msgBytes = new TextEncoder().encode(message);
    const pkObj    = new PublicKey(publicKey);
    const ok       = nacl.sign.detached.verify(msgBytes, sigBytes, pkObj.toBytes());

    if (!ok) return res.status(400).json({ error: 'Invalid signature' });

    data.connected     = true;
    data.walletAddress = publicKey;
    data.verifiedAt    = Date.now();
    sessions.set(session, data);

    return res.json({ success: true });
  } catch (e) {
    console.error('Verification error:', e);
    return res.status(500).json({ error: e.message });
  }
});

// -- Session info endpoint --
app.get('/api/session/:id', (req, res) => {
  const data = sessions.get(req.params.id);
  if (!data) return res.status(404).json({ error: 'Not found' });
  const { nonce, ...rest } = data;
  res.json(rest);
});

// -- Nonce endpoint --
app.get('/api/nonce/:id', (req, res) => {
  const data = sessions.get(req.params.id);
  if (!data) return res.status(404).json({ error: 'Not found' });
  res.json({ nonce: data.nonce });
});

// -- Phantom redirect page (static) --
app.get('/phantom-redirect', (req, res) => {
  const { session } = req.query;
  if (!session) return res.status(400).send('Missing session');
  // redirect to static simple-redirect.html with query
  return res.redirect(`/simple-redirect.html?session=${encodeURIComponent(session)}`);
});

// -- Connection status endpoint --
app.get('/status', (req, res) => {
  const { session } = req.query;
  if (!session) return res.status(400).json({ error: 'Missing session' });
  const data = sessions.get(session);
  if (!data) return res.status(404).json({ error: 'Not found' });
  res.json({ connected: data.connected, walletAddress: data.walletAddress, player: data.player });
});

// -- Cleanup expired sessions --
setInterval(() => {
  const now = Date.now();
  for (const [id, s] of sessions) {
    if (now - s.createdAt > 5*60*1000) sessions.delete(id);
  }
}, 60*1000);

// Local server start
if (!isProd) {
  app.listen(PORT, () => console.log(`Local on http://localhost:${PORT}`));
}

// Export for Netlify Functions
module.exports = app;
module.exports.handler = serverless(app);
