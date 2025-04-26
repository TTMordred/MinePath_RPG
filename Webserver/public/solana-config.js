// Solana configuration
const SOLANA_CONFIG = {
  cluster: 'devnet',
  clusterApiUrl: 'https://api.devnet.solana.com',
  explorerUrl: 'https://explorer.solana.com/?cluster=devnet'
};

// Export for use in other files
if (typeof module !== 'undefined') {
  module.exports = SOLANA_CONFIG;
}
