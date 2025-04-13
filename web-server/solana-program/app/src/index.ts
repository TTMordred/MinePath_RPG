import * as anchor from '@project-serum/anchor';
import { Program } from '@project-serum/anchor';
import { PublicKey, Connection, Keypair } from '@solana/web3.js';
import { MinecraftAuth } from '../target/types/minecraft_auth';

export class MinecraftAuthClient {
  program: Program<MinecraftAuth>;
  connection: Connection;
  wallet: anchor.Wallet;

  constructor(
    connection: Connection,
    wallet: anchor.Wallet,
    programId: PublicKey
  ) {
    this.connection = connection;
    this.wallet = wallet;

    // Create the provider
    const provider = new anchor.AnchorProvider(
      connection,
      wallet,
      { commitment: 'confirmed' }
    );

    // Create the program
    this.program = new Program(
      require('../target/idl/minecraft_auth.json'),
      programId,
      provider
    ) as Program<MinecraftAuth>;
  }

  /**
   * Find the PDA for a Minecraft account
   * @param minecraftUuid The Minecraft UUID
   * @returns The PDA and bump seed
   */
  async findMinecraftAccountPDA(minecraftUuid: string): Promise<[PublicKey, number]> {
    return await PublicKey.findProgramAddress(
      [
        Buffer.from('minecraft-account'),
        Buffer.from(minecraftUuid)
      ],
      this.program.programId
    );
  }

  /**
   * Connect a wallet to a Minecraft account
   * @param minecraftUuid The Minecraft UUID
   * @returns The transaction signature
   */
  async connectWallet(minecraftUuid: string): Promise<string> {
    const [minecraftAccountPDA] = await this.findMinecraftAccountPDA(minecraftUuid);

    const tx = await this.program.methods
      .connectWallet(minecraftUuid)
      .accounts({
        minecraftAccount: minecraftAccountPDA,
        authority: this.wallet.publicKey,
        systemProgram: anchor.web3.SystemProgram.programId,
      })
      .rpc();

    return tx;
  }

  /**
   * Disconnect a wallet from a Minecraft account
   * @param minecraftUuid The Minecraft UUID
   * @returns The transaction signature
   */
  async disconnectWallet(minecraftUuid: string): Promise<string> {
    const [minecraftAccountPDA] = await this.findMinecraftAccountPDA(minecraftUuid);

    const tx = await this.program.methods
      .disconnectWallet()
      .accounts({
        minecraftAccount: minecraftAccountPDA,
        authority: this.wallet.publicKey,
      })
      .rpc();

    return tx;
  }

  /**
   * Verify a wallet for a Minecraft account
   * @param minecraftUuid The Minecraft UUID
   * @returns The transaction signature
   */
  async verifyWallet(minecraftUuid: string): Promise<string> {
    const [minecraftAccountPDA] = await this.findMinecraftAccountPDA(minecraftUuid);

    const tx = await this.program.methods
      .verifyWallet()
      .accounts({
        minecraftAccount: minecraftAccountPDA,
        authority: this.wallet.publicKey,
      })
      .rpc();

    return tx;
  }

  /**
   * Get a Minecraft account
   * @param minecraftUuid The Minecraft UUID
   * @returns The Minecraft account data
   */
  async getMinecraftAccount(minecraftUuid: string): Promise<any> {
    const [minecraftAccountPDA] = await this.findMinecraftAccountPDA(minecraftUuid);

    try {
      const account = await this.program.account.minecraftAccount.fetch(minecraftAccountPDA);
      return {
        minecraftUuid: account.minecraftUuid,
        walletAddress: account.walletAddress.toString(),
        isVerified: account.isVerified,
        verificationTime: account.verificationTime.toNumber(),
        authority: account.authority.toString(),
      };
    } catch (e) {
      console.error('Error fetching Minecraft account:', e);
      return null;
    }
  }
}

// Example usage
async function main() {
  // Connect to the Solana devnet
  const connection = new Connection('https://api.devnet.solana.com', 'confirmed');
  
  // Create a wallet from a keypair
  const wallet = new anchor.Wallet(Keypair.generate());
  
  // Program ID
  const programId = new PublicKey('Fg6PaFpoGXkYsidMpWTK6W2BeZ7FEfcYkg476zPFsLnS');
  
  // Create the client
  const client = new MinecraftAuthClient(connection, wallet, programId);
  
  // Example Minecraft UUID
  const minecraftUuid = '00000000-0000-0000-0000-000000000000';
  
  // Connect a wallet
  const tx = await client.connectWallet(minecraftUuid);
  console.log('Connected wallet:', tx);
  
  // Get the Minecraft account
  const account = await client.getMinecraftAccount(minecraftUuid);
  console.log('Minecraft account:', account);
}

// Uncomment to run the example
// main().catch(console.error);
