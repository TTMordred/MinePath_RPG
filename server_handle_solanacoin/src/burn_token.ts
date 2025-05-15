import { Buffer } from 'buffer';
;(globalThis as any).Buffer = Buffer;
import { Connection, PublicKey, Transaction } from '@solana/web3.js';
import { createBurnCheckedInstruction } from '@solana/spl-token';

// Initialize connection to the cluster (devnet/testnet/mainnet)
const connection = new Connection("https://api.devnet.solana.com");

// Helper to get the Phantom provider from the window object
function getProvider(): any {
  if ('solana' in window) {
    const provider = (window as any).solana;
    if (provider.isPhantom) {
      return provider;
    }
  }
  throw new Error('Phantom provider not found');
}

/**
 * Burns SPL tokens from the user's associated token account.
 *
 * @param ownerATA   - The user's Associated Token Account to burn from
 * @param amount     - Amount of tokens to burn (in UI units, e.g. 1.5)
 * @param mintAddress- The SPL token mint address
 * @param decimals   - The mint's decimals, needed to scale the amount
 * @returns          - The transaction signature
 */
export async function burnToken(
  ownerATA: string,
  amount: number,
  mintAddress: string,
  decimals: number
): Promise<string> {
  try {
    // 1) Connect to wallet
    const provider = getProvider();
    await provider.connect();
    const ownerPublicKey: PublicKey = provider.publicKey;

    // 2) Build burn instruction
    const tokenAccount = new PublicKey(ownerATA);
    const mintPubkey   = new PublicKey(mintAddress);

    const burnIx = createBurnCheckedInstruction(
      tokenAccount,
      mintPubkey,
      ownerPublicKey,
      amount * Math.pow(10, decimals),
      decimals
    );

    // 3) Assemble transaction
    const transaction = new Transaction().add(burnIx);
    transaction.feePayer = ownerPublicKey;
    const { blockhash } = await connection.getLatestBlockhash('finalized');
    transaction.recentBlockhash = blockhash;

    // 4) Sign & send
    let result;
    try {
      // this pops Phantom, signs, and sends in one step
      result = await provider.signAndSendTransaction(transaction);
    } catch (err: any) {
      console.error('[burnToken] signAndSendTransaction error', err);
      throw new Error('Wallet failed to sign & send: ' + (err.message || err));
    }
  
    const signature = result.signature;
    console.log('[burnToken] signature:', signature);
  
    // Optionally confirm it
    await connection.confirmTransaction(signature, 'confirmed');
    console.log('[burnToken] confirmed:', signature);
  
    return signature;
  } catch (err: any) {
    console.error('[burnToken] ERROR', err);
    throw err;
  }
}
