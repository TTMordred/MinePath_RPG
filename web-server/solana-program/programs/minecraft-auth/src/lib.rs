use anchor_lang::prelude::*;

declare_id!("Fg6PaFpoGXkYsidMpWTK6W2BeZ7FEfcYkg476zPFsLnS");

#[program]
pub mod minecraft_auth {
    use super::*;

    pub fn initialize(ctx: Context<Initialize>) -> Result<()> {
        Ok(())
    }

    pub fn connect_wallet(ctx: Context<ConnectWallet>, minecraft_uuid: String) -> Result<()> {
        let account = &mut ctx.accounts.minecraft_account;
        let authority = &ctx.accounts.authority;

        account.minecraft_uuid = minecraft_uuid;
        account.wallet_address = authority.key();
        account.is_verified = false;
        account.verification_time = 0;
        account.authority = authority.key();

        Ok(())
    }

    pub fn disconnect_wallet(ctx: Context<DisconnectWallet>) -> Result<()> {
        // The account will be closed and rent returned to the authority
        Ok(())
    }

    pub fn verify_wallet(ctx: Context<VerifyWallet>) -> Result<()> {
        let account = &mut ctx.accounts.minecraft_account;
        
        account.is_verified = true;
        account.verification_time = Clock::get()?.unix_timestamp;

        Ok(())
    }
}

#[derive(Accounts)]
pub struct Initialize {}

#[derive(Accounts)]
#[instruction(minecraft_uuid: String)]
pub struct ConnectWallet<'info> {
    #[account(
        init,
        payer = authority,
        space = 8 + 36 + 32 + 1 + 8 + 32,
        seeds = [b"minecraft-account", minecraft_uuid.as_bytes()],
        bump
    )]
    pub minecraft_account: Account<'info, MinecraftAccount>,
    
    #[account(mut)]
    pub authority: Signer<'info>,
    
    pub system_program: Program<'info, System>,
}

#[derive(Accounts)]
pub struct DisconnectWallet<'info> {
    #[account(
        mut,
        close = authority,
        has_one = authority,
    )]
    pub minecraft_account: Account<'info, MinecraftAccount>,
    
    #[account(mut)]
    pub authority: Signer<'info>,
}

#[derive(Accounts)]
pub struct VerifyWallet<'info> {
    #[account(
        mut,
        has_one = authority,
    )]
    pub minecraft_account: Account<'info, MinecraftAccount>,
    
    pub authority: Signer<'info>,
}

#[account]
pub struct MinecraftAccount {
    pub minecraft_uuid: String,      // UUID of the Minecraft player
    pub wallet_address: Pubkey,      // Solana wallet address
    pub is_verified: bool,           // Whether the wallet is verified
    pub verification_time: i64,      // When the wallet was verified
    pub authority: Pubkey,           // Authority that can modify this account
}
