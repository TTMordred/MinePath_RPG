import { Buffer } from 'buffer';
;(globalThis as any).Buffer = Buffer;
import { Connection, PublicKey, Transaction, clusterApiUrl } from '@solana/web3.js';
import { createTransferCheckedInstruction, getAssociatedTokenAddress, createAssociatedTokenAccountInstruction} from '@solana/spl-token';

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
  throw new Error('Phantom Wallet not found. Please install it.');
}

/**
 * Send an SPL token transfer transaction using Phantom for signing.
 * @param senderATA - Associated Token Account address of the sender
 * @param receiverWallet - Associated Token Account address of the receiver
 * @param amount - Token amount in human-readable units (e.g., 1.5)
 * @param mintAddress - The SPL token mint address
 * @param decimals - Number of decimals the token uses
 * @returns Transaction signature (txid)
 */
export async function sendToken(
  senderATA: string,
  receiverWallet: string,
  amount: number,
  mintAddress: string,
  decimals: number
): Promise<string> {
  console.log('🔥 sendToken()', { senderATA, receiverWallet, amount, mintAddress, decimals });
  // Connect to Phantom
  try{const provider = getProvider();
    await provider.connect();
  
    // Prepare public keys
    const walletPubkey = new PublicKey(provider.publicKey.toString());
    console.log('[sendToken] connected wallet', walletPubkey.toString());;
    const fromTokenAccount = new PublicKey(senderATA);
    const toPubKey = new PublicKey(receiverWallet);
    const mintPubkey = new PublicKey(mintAddress);
    const ata = await getAssociatedTokenAddress(mintPubkey, toPubKey);  
    console.log('▶️ derived ATA:', ata.toBase58());
    // Check if the receiver's ATA already exists
    const ataInfo = await connection.getAccountInfo(ata, 'confirmed'); 
    const ixns = [];  
    if (ata === null){
      ixns.push(
        createAssociatedTokenAccountInstruction(  
        walletPubkey,     // who pays the rent + fees  
        ata,             // the ATA to (maybe) create  
        toPubKey,     // who'll own this ATA  
        mintPubkey       // which token mint  
      ));
    } 
    // Verify the sender ATA belongs to the connected wallet
    const parsedInfo = await connection.getParsedAccountInfo(fromTokenAccount);
    if (!parsedInfo.value) {
      throw new Error(`Sender ATA ${senderATA} not found on chain.`);
    }
    // @ts-ignore: parsing raw account data
    const owner = (parsedInfo.value.data as any).parsed.info.owner;
    if (owner !== walletPubkey.toString()) {
      throw new Error('Sender ATA does not match connected wallet public key.');
    }
  
    // Convert amount to smallest unit
    const rawAmount = BigInt(Math.round(amount * Math.pow(10, decimals)));

    // Create the transfer instruction (checked ensures correct decimals)
    ixns.push(
      createTransferCheckedInstruction(
        fromTokenAccount,
        mintPubkey,
        ata,
        walletPubkey,
        rawAmount,
        decimals
      )
    );
    // Build transaction
    const transaction = new Transaction().add(...ixns);
    transaction.feePayer = walletPubkey;
    const {blockhash}= await connection.getLatestBlockhash('finalized');
    transaction.recentBlockhash = blockhash;
    console.log('[sendToken] set blockhash', blockhash);
    // Request Phantom to sign the transaction
    let result;
    try {
      // this pops Phantom, signs, and sends in one step
      result = await provider.signAndSendTransaction(transaction);
    } catch (err: any) {
      console.error('[sendToken] signAndSendTransaction error', err);
      throw new Error('Wallet failed to sign & send: ' + (err.message || err));
    }
  
    const signature = result.signature;
    console.log('[sendToken] signature:', signature);
  
    // Optionally confirm it
    await connection.confirmTransaction(signature, 'confirmed');
    console.log('[sendToken] confirmed:', signature);
  
    return signature;
  }catch (err: any) {
    console.error('[sendToken] ERROR', err);
    // rethrow so the caller can see it
    throw err;
  }
}