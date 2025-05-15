// public/send_sol.ts

import { Buffer } from 'buffer';
;(globalThis as any).Buffer = Buffer;

import {
  Connection,
  PublicKey,
  Transaction,
  SystemProgram,
  LAMPORTS_PER_SOL,
  clusterApiUrl
} from '@solana/web3.js';

// Initialize connection (pick your cluster)
const connection = new Connection("https://api.devnet.solana.com", "confirmed");

// Helper to get the Phantom provider
function getProvider(): any {
  if ('solana' in window) {
    const provider = (window as any).solana;
    if (provider.isPhantom) return provider;
  }
  throw new Error('Phantom Wallet not found. Please install it.');
}

/**
 * Send native SOL from the connected wallet to a recipient.
 *
 * @param receiverWallet - The recipient’s public key (base58 string)
 * @param amountSol      - Amount in SOL (e.g. 0.5)
 * @returns the transaction signature (txid)
 */
export async function sendSol(
  receiverWallet: string,
  amountSol: number
): Promise<string> {
  console.log('🔥 sendSol()', { receiverWallet, amountSol });
  const provider = getProvider();
  await provider.connect();
  const walletPubkey = new PublicKey(provider.publicKey.toString());
  console.log('[sendSol] connected wallet', walletPubkey.toBase58());

  // Convert SOL to lamports
  const lamports = BigInt(Math.round(amountSol * LAMPORTS_PER_SOL));
  if (lamports <= 0n) {
    throw new Error('Amount must be > 0');
  }

  // Build the SystemProgram transfer instruction
  const toPubkey = new PublicKey(receiverWallet);
  const transferIx = SystemProgram.transfer({
    fromPubkey: walletPubkey,
    toPubkey,
    lamports
  });

  // Build transaction
  const tx = new Transaction().add(transferIx);
  tx.feePayer = walletPubkey;
  const { blockhash } = await connection.getLatestBlockhash('finalized');
  tx.recentBlockhash = blockhash;
  console.log('[sendSol] set blockhash', blockhash);

  // Sign & send in one step
  let result;
  try {
    result = await provider.signAndSendTransaction(tx);
  } catch (err: any) {
    console.error('[sendSol] signAndSendTransaction error', err);
    throw new Error('Wallet failed to sign & send: ' + (err.message || err));
  }

  const signature = result.signature;
  console.log('[sendSol] signature:', signature);

  // Confirm
  await connection.confirmTransaction(signature, 'confirmed');
  console.log('[sendSol] confirmed:', signature);

  return signature;
}
