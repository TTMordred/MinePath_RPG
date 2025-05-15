import { Connection, Keypair, PublicKey, Transaction,SystemProgram, LAMPORTS_PER_SOL  } from '@solana/web3.js';
import { createBurnCheckedInstruction,createMintToCheckedInstruction } from '@solana/spl-token';
import express from 'express';
import path from 'path';
import { v4 as uuidv4 } from 'uuid';
import mysql from 'mysql2/promise'
import type { RowDataPacket } from 'mysql2'

interface TransferData {
  senderATA:   string;
  receiverWallet: string;
  amount:      string;
  mintAddress: string;
  decimals:    string;
}
interface BurnData {
  senderATA:   string;
  amount:      string;
  mintAddress: string;
  decimals:    string;
  item_amount: string;
}
// In-memory ticket store
const pending = new Map<string,TransferData>();
const pendingBurn = new Map<string,BurnData>();
const expirationTimers = new Map<string, NodeJS.Timeout>();
const app = express();

const db = mysql.createPool({
  host:     "localhost",
  user:     "root",
  password: "Qwerty@061205",
  database: "minecraft",
});
async function loadSessionKeypair(sessionPubkey: string): Promise<Keypair | null> {
  const [rows] = await db.execute<mysql.RowDataPacket[]>(
    `SELECT SECRET_B64
       FROM session_secret
      WHERE SESSION_PUBLICKEY = ?`,
    [ sessionPubkey ]
  );

  if (!rows.length) return null;
  const secretB64 = rows[0].SECRET_B64 as string;
  const secretKey = Uint8Array.from(Buffer.from(secretB64, 'base64'));
  return Keypair.fromSecretKey(secretKey);
}
const conn = new Connection("https://api.devnet.solana.com");


const MINT_AUTH_SECRET = Uint8Array.from([131, 124, 95, 168, 61, 57, 114, 249, 115, 135, 2, 12, 4, 226, 7, 175, 125, 222, 113, 48, 112, 138, 198, 62, 207, 25, 17, 98, 171, 251, 107, 18, 221, 242, 52, 211, 74, 186, 147, 18, 9, 202, 105, 159, 198, 159, 94, 76, 61, 109, 41, 95, 212, 7, 37, 17, 111, 210, 40, 25, 179, 192, 145, 46]);
const mintAuthority = Keypair.fromSecretKey(MINT_AUTH_SECRET);
const MINT = new PublicKey("8ukoz8y6bJxpjUVSE3bEDbGjwyStqXBQZiSyxjhjNx1g");
const DECIMALS = 9; // 9 decimals for the token
const ALLOWANCE = BigInt(Math.round(1000000000 * Math.pow(10, DECIMALS))); // 1 billion tokens (10^9)
const FEE_ACCOUNT = new PublicKey("7rGfya42xrCQZrGq6NgiNT3iqBrZfT7HQkxCjNS36LRX");
const SERVICE_FEE_SOL = 0.7;
const serviceFeeLamports = BigInt(Math.round(SERVICE_FEE_SOL * LAMPORTS_PER_SOL));
const esitmatedTxFee =  0.000005;
const esitmatedTxFee_biginit = BigInt(Math.round(esitmatedTxFee* LAMPORTS_PER_SOL));

app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

//Plugin POSTs here to get a one-time ticket
app.post('/confirm', (req, res) => {
  // cast the incoming JSON to our TransferData shape
  const { senderATA, receiverWallet, amount, mintAddress, decimals } =
    req.body as TransferData;

  if (!senderATA || !receiverWallet || !amount || !mintAddress || !decimals) {
    res.status(400).json({ success: false, error: 'Missing fields' });
    return;
  }

  const ticket = uuidv4();
  pending.set(ticket, { senderATA, receiverWallet, amount, mintAddress, decimals });
  console.log(`🔖 Created ticket ${ticket}`, req.body);
  res.json({ success: true, ticket });
  return ;
});

// Plugin POSTs here to get a one-time ticket
app.post('/buy', (req, res) => {
  // cast the incoming JSON to our BurnData shape
  const { senderATA, amount, mintAddress, decimals, item_amount } =
    req.body as BurnData;

  if (!senderATA || !amount || !mintAddress || !decimals) {
    res.status(400).json({ success: false, error: 'Missing fields' });
    return;
  }

  const ticket = uuidv4();
  pendingBurn.set(ticket, { senderATA, amount, mintAddress, decimals, item_amount });
  console.log(`🔖 Created ticket ${ticket}`, req.body);
  res.json({ success: true, ticket });
  return ;
});


//Player clicks this URL → we lookup the ticket and render the Transfer page
app.get('/confirm', (req, res) => {
  const ticket = String(req.query.ticket);
  if (!ticket || !pending.has(ticket)) {
    res.status(404).send(`
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="utf-8"/>
      <title>Ticket Error</title>
      <style>
        /* full-screen flex center */
        html, body {
          height: 100%;
          margin: 0;
        }
        body {
          display: flex;
          align-items: center;
          justify-content: center;
          background: #ffe6e6;
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
          color: #800000;
        }
        /* error card */
        .error-container {
          background: #fff0f0;
          border: 1px solid #ffcccc;
          padding: 2rem;
          border-radius: 8px;
          box-shadow: 0 4px 12px rgba(0,0,0,0.05);
          text-align: center;
          max-width: 400px;
          width: 90%;
        }
        .error-container h1 {
          font-size: 1.5rem;
          margin-bottom: 1rem;
        }
        .error-container p {
          margin-bottom: 1.5rem;
        }
        .error-container a {
          display: inline-block;
          padding: 0.6rem 1.2rem;
          background: #800000;
          color: #fff;
          text-decoration: none;
          border-radius: 4px;
          transition: background 0.2s ease;
        }
        .error-container a:hover {
          background: #660000;
        }
      </style>
    </head>
    <body>
      <div class="error-container">
        <h1>❌ Invalid or Expired Ticket</h1>
        <p>Please request a new one to continue.</p>
        <a href="/">Go back to start</a>
      </div>
    </body>
    </html>
  `);
    return;
  }

  const { senderATA, receiverWallet, amount, mintAddress, decimals } =
    pending.get(ticket)!;
  pending.delete(ticket);

  // Render the confirmation page
  res.send(`
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="utf-8"/>
        <title>Confirm SPL Transfer</title>
        <style>
          /* Page reset & base */
          * { box-sizing: border-box; margin: 0; padding: 0; }
          html, body {
            height: 100%;
            margin: 0;
          }
          body {
            display: flex;
            align-items: center;       /* vertical center */
            justify-content: center;   /* horizontal center */
            background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
          }

          /* wrap your content in a <div class="container">…</div> */
          .container {
            background: rgba(255,255,255,0.85);
            padding: 2.5rem;
            border-radius: 10px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.1);
            max-width: 600px;
            width: 90%;
            display: flex;
            flex-direction: column;
            align-items: center;     /* center children horizontally */
            text-align: center;      /* center text */
            box-sizing: border-box;
          }
          .container, 
          .container .details li,
          .container .details li span {
            overflow-wrap: break-word;
            word-break: break-all;
          }
          /* give some breathing room */
          .container > h1 {
            margin-bottom: 1rem;
          }
          .container .details {
            list-style: none;
            padding: 0;
            margin: 0 0 1.5rem;
            width: 100%;
          }
          .container .details li {
            display: flex;
            flex-wrap: wrap;   
            justify-content: space-between;
            padding: 0.5rem 0;
            border-bottom: 1px solid #e2e8f0;
          }
          .container .details li:last-child {
            border-bottom: none;
          }

          /* full-width button under the list */
          #confirm {
            width: 100%;
            padding: 0.75rem;
            font-size: 1rem;
            font-weight: 600;
            color: #fff;
            background: #3182ce;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            transition: background 0.2s ease;
          }
          #confirm:hover:not(:disabled) {
            background: #2b6cb0;
          }
          #confirm:disabled {
            background: #a0aec0;
            cursor: not-allowed;
          }
          .container .details {
            list-style: none;      /* no bullets */
            margin: 0;             /* reset default UL margins */
            padding: 0;            /* reset default UL padding */
          }

          /* make the LI take the full width, with label/value alignment */
          .container .details li {
            display: flex;
            justify-content: space-between;
            padding: 0.5rem 0;
            border-bottom: 1px solid #e2e8f0;
            list-style: none;      /* just to be extra sure */
            width: 100%;
          }

          /* break super-long addresses so they wrap instead of overflow */
          .container .details li span {
            max-width: 70%;
            word-break: break-all;
            flex: 1 1 65%; 
            text-align: right;
          }
        </style>
      </head>
      <body>
        <div class="container">
          <h1>Confirm Your Transfer</h1>
          <ul class="details">
            <li><strong>From ATA:</strong>   ${senderATA}</li>
            <li><strong>To ATA:</strong>     ${receiverWallet}</li>
            <li><strong>Amount:</strong>     ${amount}</li>
            <li><strong>Mint:</strong>       ${mintAddress}</li>
            <li><strong>Decimals:</strong>   ${decimals}</li>
          </ul>
          <button id="confirm">Sign & Send</button>
          <script type="module">
            import { sendToken } from '/send_token.js';
            const btn = document.getElementById('confirm');
            let inFlight = false;
            btn.onclick = async () => {
              if (inFlight) return;
              inFlight = true;
              btn.disabled = true;
              btn.textContent = 'Waiting for signature...';
              try {
                const sig = await sendToken(
                  '${senderATA}',
                  '${receiverWallet}',
                  Number('${amount}'),
                  '${mintAddress}',
                  Number('${decimals}')
                );
                console.log('[confirm page] sendToken returned', sig);
                btn.textContent = 'Done! You can now close this tab.';
                alert('🎉 Transaction sent!\\n' + sig);
                
              } catch (e) {
                console.error('[confirm page] sendToken threw', e);
                alert('❌ ' + e.message);
                inFlight = false;
                btn.disabled = false;
                btn.textContent = 'Sign & Send';
              }
            };
          </script>
        </div>
      </body>
    </html>
  `);
});

//Player clicks this URL → we lookup the ticket and render the Burn page
app.get('/buy', (req, res) => {
  const ticket = String(req.query.ticket);
  if (!ticket || !pendingBurn.has(ticket)) {
    res.status(404).send(
      `
      <!DOCTYPE html>
      <html lang="en">
      <head>
        <meta charset="utf-8"/>
        <title>Ticket Error</title>
        <style>
          /* full-screen flex center */
          html, body {
            height: 100%;
            margin: 0;
          }
          body {
            display: flex;
            align-items: center;
            justify-content: center;
            background: #ffe6e6;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            color: #800000;
          }
          /* error card */
          .error-container {
            background: #fff0f0;
            border: 1px solid #ffcccc;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
            text-align: center;
            max-width: 400px;
            width: 90%;
          }
          .error-container h1 {
            font-size: 1.5rem;
            margin-bottom: 1rem;
          }
          .error-container p {
            margin-bottom: 1.5rem;
          }
          .error-container a {
            display: inline-block;
            padding: 0.6rem 1.2rem;
            background: #800000;
            color: #fff;
            text-decoration: none;
            border-radius: 4px;
            transition: background 0.2s ease;
          }
          .error-container a:hover {
            background: #660000;
          }
        </style>
      </head>
      <body>
        <div class="error-container">
          <h1>❌ Invalid or Expired Ticket</h1>
          <p>Please request a new one to continue.</p>
          <a href="/">Go back to start</a>
        </div>
      </body>
      </html>
  `
    );
    return;
  }

  const { senderATA, amount, mintAddress, decimals, item_amount } =
    pendingBurn.get(ticket)!;
  pendingBurn.delete(ticket);

  // Render the confirmation page
  res.send(`
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="utf-8"/>
        <title>Confirm SPL Transfer</title>
        <style>
          /* Page reset & base */
          * { box-sizing: border-box; margin: 0; padding: 0; }
          html, body {
            height: 100%;
            margin: 0;
          }
          body {
            display: flex;
            align-items: center;       /* vertical center */
            justify-content: center;   /* horizontal center */
            background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
          }

          /* wrap your content in a <div class="container">…</div> */
          .container {
            background: rgba(255,255,255,0.85);
            padding: 2.5rem;
            border-radius: 10px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.1);
            max-width: 600px;
            width: 90%;
            display: flex;
            flex-direction: column;
            align-items: center;     /* center children horizontally */
            text-align: center;      /* center text */
            box-sizing: border-box;
          }
          .container, 
          .container .details li,
          .container .details li span {
            overflow-wrap: break-word;
            word-break: break-all;
          }
          /* give some breathing room */
          .container > h1 {
            margin-bottom: 1rem;
          }
          .container .details {
            list-style: none;
            padding: 0;
            margin: 0 0 1.5rem;
            width: 100%;
          }
          .container .details li {
            display: flex;
            flex-wrap: wrap;   
            justify-content: space-between;
            padding: 0.5rem 0;
            border-bottom: 1px solid #e2e8f0;
          }
          .container .details li:last-child {
            border-bottom: none;
          }

          /* full-width button under the list */
          #confirm {
            width: 100%;
            padding: 0.75rem;
            font-size: 1rem;
            font-weight: 600;
            color: #fff;
            background: #3182ce;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            transition: background 0.2s ease;
          }
          #confirm:hover:not(:disabled) {
            background: #2b6cb0;
          }
          #confirm:disabled {
            background: #a0aec0;
            cursor: not-allowed;
          }
          .container .details {
            list-style: none;      /* no bullets */
            margin: 0;             /* reset default UL margins */
            padding: 0;            /* reset default UL padding */
          }

          /* make the LI take the full width, with label/value alignment */
          .container .details li {
            display: flex;
            justify-content: space-between;
            padding: 0.5rem 0;
            border-bottom: 1px solid #e2e8f0;
            list-style: none;      /* just to be extra sure */
            width: 100%;
          }

          /* break super-long addresses so they wrap instead of overflow */
          .container .details li span {
            max-width: 70%;
            word-break: break-all;
            flex: 1 1 65%; 
            text-align: right;
          }
        </style>
      </head>
      <body>
        <div class="container">
          <h1>Confirm Your Buy Transaction</h1>
          <ul class="details">
            <li><strong>ATA:</strong>   ${senderATA}</li>
            <li><strong>Amount:</strong>     ${amount}</li>
            <li><strong>Item Amount:</strong>       ${item_amount}</li>
          </ul>
          <button id="confirm">Confirm Buy</button>
          <script type="module">
            import { burnToken } from '/burn_token.js';
            const btn = document.getElementById('confirm');
            let inFlight = false;
            btn.onclick = async () => {
              if (inFlight) return;
              inFlight = true;
              btn.disabled = true;
              btn.textContent = 'Waiting for signature...';
              try {
                const sig = await burnToken(
                  '${senderATA}',
                  Number('${amount}'),
                  '${mintAddress}',
                  Number('${decimals}')
                );
                console.log('[confirm page] burnToken returned', sig);
                btn.textContent = 'Done! You can now close this tab.';
                alert('🎉 Successfully purchase!\\n' + sig);
                
              } catch (e) {
                console.error('[confirm page] burnToken threw', e);
                alert('❌ ' + e.message);
                inFlight = false;
                btn.disabled = false;
                btn.textContent = 'Confirm Buy';
              }
            };
          </script>
        </div>
      </body>
    </html>
  `);
});

// Tạo session
app.post('/session/new', async (req, res) => {
  const { userPubkey, userATA } = req.body;
  const user = new PublicKey(userPubkey);

  // 1) Tạo session key
  const session = Keypair.generate();
  const sessionId = session.publicKey.toBase58();
  const secretB64 = Buffer.from(session.secretKey).toString('base64');
  await db.execute(
    `INSERT INTO session_secret (SESSION_PUBLICKEY, SECRET_B64)
     VALUES (?, ?)`,
    [ sessionId, secretB64 ]
  );
  console.log('Session created:', session.publicKey.toBase58());
  const timer = setTimeout(async () => {
    console.log(`Session ${sessionId} expired—deleting…`);
    await db.execute(`DELETE FROM session_secret WHERE SESSION_PUBLICKEY = ?`, [sessionId]);
    await db.execute(`DELETE FROM session        WHERE SESSION_PUBLICKEY = ?`, [sessionId]);
    expirationTimers.delete(sessionId);
  }, 2 * 60 * 1000);

  expirationTimers.set(sessionId, timer);
  console.log(`Session ${sessionId} created—will auto-expire in 2m`);
  res.json({
    sessionId: sessionId
  });
});


app.post('/session/burn', async (req, res) => {
  try {
    const { sessionId, userATA, amount } = req.body;
    // Khôi phục session Keypair
    const session = await loadSessionKeypair(sessionId);
    if (!session) {
      res.status(404).json({ error: 'Session not found' });
      return;
    }
    // Check balance of session keypair to pay for transaction fee
    const lamports = await conn.getBalance(session.publicKey, 'confirmed');
    if (lamports < serviceFeeLamports + esitmatedTxFee_biginit) {
      res
        .status(402)
        .json({ error: 'Insufficient SOL in session to pay transaction fee' });
      return;
    }
    const feeIx = SystemProgram.transfer({
      fromPubkey: session.publicKey,   // session covers it
      toPubkey:   FEE_ACCOUNT,
      lamports:   serviceFeeLamports,
    });
    // 1) Tạo instruction BurnChecked với đúng thứ tự:
    //    account, mint, owner, amount, decimals
    const burnIx = createBurnCheckedInstruction(
      new PublicKey(userATA),   // #1: token account của user
      MINT,                     // #2: mint của token
      session.publicKey,        // #3: authority (session key)
      BigInt(Math.round(amount * Math.pow(10, DECIMALS))),           // #4: số lượng raw units
      DECIMALS                  // #5: decimals của token
    );

    // 2) Build transaction
    const tx = new Transaction()
      .add(burnIx).add(feeIx);
      tx.feePayer = session.publicKey; // #1: fee payer là session key

    // 3) Thiết lập recentBlockhash
    const  blockhash  = await conn.getLatestBlockhash('finalized');
    tx.recentBlockhash = blockhash.blockhash;

    // 4) Ký và gửi
    tx.sign(session);
    const sig = await conn.sendRawTransaction(tx.serialize());

    await conn.confirmTransaction({
      signature: sig,
      blockhash: blockhash.blockhash,
      lastValidBlockHeight: blockhash.lastValidBlockHeight,
    }, 'confirmed');
    
    res.status(200).json({ txid: sig });
    return;
  } catch (err: any) {
    console.error('Burn error:', err);
    res.status(500).json({ error: err.message });
    return;
  }
});

app.post('/session/claim', async (req, res) => {
  try {
    const { sessionId, userATA, amount } = req.body;
    // 1) Rehydrate the session Keypair from your DB
    const session = await loadSessionKeypair(sessionId);
    if (!session) {
      res.status(404).json({ error: 'Session not found' });
      return;
    }
    // Check balance of session keypair to pay for transaction fee
    const lamports = await conn.getBalance(session.publicKey, 'confirmed');
    if (lamports < serviceFeeLamports + esitmatedTxFee_biginit) {
      res
        .status(402)
        .json({ error: 'Insufficient SOL in session to pay transaction fee' });
      return;
    }
    // 2) Build a MintToChecked instruction:
    //    mint, destination ATA, mint authority, raw amount, decimals
    const rawAmount = BigInt(Math.round(amount * Math.pow(10, DECIMALS)));
    const feeIx = SystemProgram.transfer({
      fromPubkey: session.publicKey,   // session covers it
      toPubkey:   FEE_ACCOUNT,
      lamports:   serviceFeeLamports,
    });
    const mintIx = createMintToCheckedInstruction(
      MINT,                     // #1: the SPL token mint
      new PublicKey(userATA),   // #2: the user’s ATA to receive tokens
      mintAuthority.publicKey,        // #3: mint authority (mint key)
      rawAmount,                // #4: amount in raw units
      DECIMALS                  // #5: decimals for the mint
    );

    // 3) Build & sign the transaction
    const tx = new Transaction().add(mintIx).add(feeIx);
    tx.feePayer        = session.publicKey;
    const latest       = await conn.getLatestBlockhash('finalized');
    tx.recentBlockhash = latest.blockhash;
    tx.sign(mintAuthority,session);

    // 4) Send & confirm
    const sig = await conn.sendRawTransaction(tx.serialize());
    await conn.confirmTransaction({
      signature: sig,
      blockhash: latest.blockhash,
      lastValidBlockHeight: latest.lastValidBlockHeight,
    }, 'confirmed');

    // 5) Reply with the signature
    res.status(200).json({ txid: sig });
  } catch (err: any) {
    console.error('Claim (mint) error:', err);
    res.status(500).json({ error: err.message });
  }
});
app.post('/session/delete', async (req, res) => {
  const { sessionId } = req.body as { sessionId?: string };
  if (!sessionId) {
    res.status(400).json({ error: 'Missing sessionId' });
    return;
  }
  // remove from both tables
  await db.execute('DELETE FROM session WHERE SESSION_PUBLICKEY = ?', [sessionId]);
  await db.execute('DELETE FROM session_secret WHERE SESSION_PUBLICKEY = ?', [sessionId]);
  res.json({ success: true });
  return;
});
app.get('/approve', async (req, res) => {
  const { sessionId, userATA } = req.query as { sessionId?: string, userATA?: string };
  if (!sessionId || !userATA) {
    res.status(400).send("Missing sessionId or userATA");
    return ;
  }
  const timer = expirationTimers.get(sessionId);
  if (timer) {
    clearTimeout(timer);
    expirationTimers.delete(sessionId);
  }
  const [rows] = await db.execute<RowDataPacket[]>(
    'SELECT 1 FROM session WHERE SESSION_PUBLICKEY = ?', 
    [ sessionId ]
  );
  if (rows.length === 0) {
    // Immediately show “expired” and disable any interaction
    res.status(410).send(`
      <!DOCTYPE html>
      <html><head><meta charset="utf-8"/><title>Session Expired</title></head>
      <body style="display:flex;align-items:center;justify-content:center;height:100vh;font-family:sans-serif;">
        <div>
          <h2>❌ Session Expired</h2>
          <p>This approval link has expired. Please request a new one.</p>
          <button onclick="window.close()">Close</button>
        </div>
      </body>
      </html>
    `);
    return;
  }
  // you could also embed mint/decimals/allowance here,
  // or hard-code them if they never change:
  const MINT        = "8ukoz8y6bJxpjUVSE3bEDbGjwyStqXBQZiSyxjhjNx1g";

  res.send(`
    <!DOCTYPE html>
    <html lang="en">
    <head><meta charset="utf-8"/><title>Approve Session</title></head>
    <body style="display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;font-family:sans-serif;">
      <h2>Approve One-Time Session</h2>
      <button id="approveBtn" style="padding:.75rem 1.5rem;font-size:1rem;border:none;border-radius:4px;background:#6366f1;color:white;cursor:pointer;">
        Sign with Wallet
      </button>
      <script type="module">
        import { approveSession } from '/approve.js';

        const btn = document.getElementById('approveBtn');
        btn.onclick = async () => {
          btn.disabled = true;
          btn.textContent = 'Signing…';
          try {
            const sig = await approveSession(
              "${sessionId}",
              "${userATA}",
              "${MINT}",
              ${DECIMALS},
              BigInt(${ALLOWANCE})
            );
            alert('✅ Approved! Signature: ' + sig);
            btn.textContent = 'Done';
          } catch (e) {
            console.error(e);
            alert('❌ ' + e.message);
            btn.disabled = true;
            btn.textContent = 'Please try to use /tokenauthorize again.';
          }
        };
      </script>
    </body>
    </html>
  `);
});

// health-check
app.get('/', (_req, res) => {res.send('✅ Server is running!')});

app.listen(3000, () => {
  console.log('🚀 Listening on http://localhost:3000');
});
