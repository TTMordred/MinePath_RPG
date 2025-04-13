# SolanaLogin Demo Server Setup Guide

This guide provides instructions for setting up a demo server for the SolanaLogin plugin, allowing users to test the functionality without installing it on their own server.

## Table of Contents

1. [Demo Server Requirements](#demo-server-requirements)
2. [Installation Steps](#installation-steps)
3. [Configuration](#configuration)
4. [Security Considerations](#security-considerations)
5. [Maintenance](#maintenance)
6. [Connecting to the Demo Server](#connecting-to-the-demo-server)

## Demo Server Requirements

To set up a demo server for SolanaLogin, you'll need:

- A VPS or dedicated server with at least 2GB RAM
- Ubuntu 20.04 LTS or later (recommended)
- Java 11 or higher
- Node.js 14 or higher
- MySQL or MariaDB
- A domain name (optional but recommended)
- Basic knowledge of Linux server administration

## Installation Steps

### Step 1: Set Up the Server

1. Update your server:
   ```bash
   sudo apt update
   sudo apt upgrade -y
   ```

2. Install Java:
   ```bash
   sudo apt install openjdk-17-jre-headless -y
   ```

3. Install Node.js:
   ```bash
   curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
   sudo apt install nodejs -y
   ```

4. Install MySQL:
   ```bash
   sudo apt install mysql-server -y
   sudo mysql_secure_installation
   ```

### Step 2: Set Up the Minecraft Server

1. Create a directory for the server:
   ```bash
   mkdir -p ~/minecraft-demo
   cd ~/minecraft-demo
   ```

2. Download Paper (or your preferred server software):
   ```bash
   wget https://papermc.io/api/v2/projects/paper/versions/1.19.4/builds/550/downloads/paper-1.19.4-550.jar -O paper.jar
   ```

3. Create a start script:
   ```bash
   cat > start.sh << 'EOF'
   #!/bin/bash
   java -Xms1G -Xmx2G -jar paper.jar nogui
   EOF
   chmod +x start.sh
   ```

4. Start the server once to generate files:
   ```bash
   ./start.sh
   ```

5. Accept the EULA:
   ```bash
   echo "eula=true" > eula.txt
   ```

### Step 3: Install the SolanaLogin Plugin

1. Create a plugins directory:
   ```bash
   mkdir -p plugins
   ```

2. Download the SolanaLogin plugin:
   ```bash
   wget https://github.com/yourusername/SolanaLogin/releases/download/v1.3/SolanaLogin-1.3.jar -O plugins/SolanaLogin.jar
   ```

3. Start the server again to generate plugin configuration:
   ```bash
   ./start.sh
   ```

4. Stop the server (CTRL+C)

### Step 4: Set Up the Web Server

1. Copy the web-server directory:
   ```bash
   cp -r plugins/SolanaLogin/web-server ~/web-server
   cd ~/web-server
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create a start script:
   ```bash
   cat > start.sh << 'EOF'
   #!/bin/bash
   export PORT=3000
   export NODE_ENV=production
   node server.js
   EOF
   chmod +x start.sh
   ```

### Step 5: Set Up Database

1. Create a database and user:
   ```bash
   sudo mysql -e "CREATE DATABASE minecraft;"
   sudo mysql -e "CREATE USER 'minecraft'@'localhost' IDENTIFIED BY 'your_secure_password';"
   sudo mysql -e "GRANT ALL PRIVILEGES ON minecraft.* TO 'minecraft'@'localhost';"
   sudo mysql -e "FLUSH PRIVILEGES;"
   ```

## Configuration

### Configure the Plugin

Edit the plugin configuration file:

```bash
nano ~/minecraft-demo/plugins/SolanaLogin/config.yml
```

Update the following settings:

```yaml
# Database settings
database:
  host: localhost
  port: 3306
  database: minecraft
  username: minecraft
  password: your_secure_password
  table-prefix: walletlogin_

# Web server settings
web-server:
  enabled: true
  url: "http://your-server-ip:3000"  # Change to your server's IP or domain
  port: 3000
  qr-code-timeout: 300
  check-interval: 5

# Solana settings
solana:
  network: "devnet"
  rpc-url: "https://api.devnet.solana.com"
```

### Configure the Web Server

If you're using a domain name, you may want to set up Nginx as a reverse proxy:

1. Install Nginx:
   ```bash
   sudo apt install nginx -y
   ```

2. Create a configuration file:
   ```bash
   sudo nano /etc/nginx/sites-available/solana-login
   ```

3. Add the following configuration:
   ```
   server {
       listen 80;
       server_name demo.solanalogin.com;  # Change to your domain

       location / {
           proxy_pass http://localhost:3000;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
       }
   }
   ```

4. Enable the site:
   ```bash
   sudo ln -s /etc/nginx/sites-available/solana-login /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   ```

5. Update the plugin configuration to use your domain:
   ```yaml
   web-server:
     url: "https://demo.solanalogin.com"  # Use your domain
   ```

## Security Considerations

For a public demo server, consider the following security measures:

1. **Firewall Configuration**:
   ```bash
   sudo apt install ufw -y
   sudo ufw allow 22/tcp
   sudo ufw allow 25565/tcp
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   sudo ufw enable
   ```

2. **SSL/TLS for the Web Server**:
   ```bash
   sudo apt install certbot python3-certbot-nginx -y
   sudo certbot --nginx -d demo.solanalogin.com
   ```

3. **Regular Backups**:
   ```bash
   # Create a backup script
   cat > ~/backup.sh << 'EOF'
   #!/bin/bash
   DATE=$(date +%Y-%m-%d)
   mkdir -p ~/backups
   cd ~/minecraft-demo
   tar -czf ~/backups/minecraft-$DATE.tar.gz plugins/SolanaLogin
   mysqldump -u minecraft -p'your_secure_password' minecraft > ~/backups/database-$DATE.sql
   EOF
   chmod +x ~/backup.sh
   
   # Add to crontab
   (crontab -l 2>/dev/null; echo "0 0 * * * ~/backup.sh") | crontab -
   ```

4. **Rate Limiting**:
   Add rate limiting to Nginx to prevent abuse:
   ```
   # Add to your Nginx configuration
   limit_req_zone $binary_remote_addr zone=login:10m rate=5r/m;
   
   location /api/ {
       limit_req zone=login burst=10 nodelay;
       proxy_pass http://localhost:3000;
       # ... other proxy settings
   }
   ```

## Maintenance

### Automatic Server Restart

Set up a systemd service for both the Minecraft server and web server:

1. Minecraft Server Service:
   ```bash
   sudo nano /etc/systemd/system/minecraft-demo.service
   ```
   
   Add:
   ```
   [Unit]
   Description=Minecraft Demo Server
   After=network.target
   
   [Service]
   User=your_username
   WorkingDirectory=/home/your_username/minecraft-demo
   ExecStart=/home/your_username/minecraft-demo/start.sh
   Restart=on-failure
   
   [Install]
   WantedBy=multi-user.target
   ```

2. Web Server Service:
   ```bash
   sudo nano /etc/systemd/system/solana-login-web.service
   ```
   
   Add:
   ```
   [Unit]
   Description=SolanaLogin Web Server
   After=network.target
   
   [Service]
   User=your_username
   WorkingDirectory=/home/your_username/web-server
   ExecStart=/home/your_username/web-server/start.sh
   Restart=on-failure
   
   [Install]
   WantedBy=multi-user.target
   ```

3. Enable and start the services:
   ```bash
   sudo systemctl enable minecraft-demo.service
   sudo systemctl start minecraft-demo.service
   sudo systemctl enable solana-login-web.service
   sudo systemctl start solana-login-web.service
   ```

### Monitoring

Set up basic monitoring with a simple script:

```bash
cat > ~/monitor.sh << 'EOF'
#!/bin/bash
# Check if Minecraft server is running
if ! systemctl is-active --quiet minecraft-demo.service; then
  echo "Minecraft server is down, restarting..."
  sudo systemctl restart minecraft-demo.service
fi

# Check if web server is running
if ! systemctl is-active --quiet solana-login-web.service; then
  echo "Web server is down, restarting..."
  sudo systemctl restart solana-login-web.service
fi

# Check if web server is responding
if ! curl -s http://localhost:3000 > /dev/null; then
  echo "Web server is not responding, restarting..."
  sudo systemctl restart solana-login-web.service
fi
EOF
chmod +x ~/monitor.sh

# Add to crontab to run every 5 minutes
(crontab -l 2>/dev/null; echo "*/5 * * * * ~/monitor.sh") | crontab -
```

## Connecting to the Demo Server

Provide the following information to users who want to connect to your demo server:

1. **Server Address**: `demo.solanalogin.com` (or your server IP)
2. **Minecraft Version**: 1.19.4 (or your server version)
3. **Web Interface**: `https://demo.solanalogin.com` (or your web server URL)

### Instructions for Users

1. Launch Minecraft and add the server with the address `demo.solanalogin.com`
2. Connect to the server
3. Register with `/register <password> <confirmPassword>`
4. Login with `/login <password>`
5. Connect your Solana wallet with `/connectwallet` or `/connectwallet qr`
6. Follow the on-screen instructions to complete the wallet connection

### Demo Server Rules

Consider adding the following rules to your demo server:

1. This is a demo server for testing the SolanaLogin plugin
2. All data may be reset periodically
3. Use a test wallet with the Solana devnet network
4. Do not use real passwords or wallets with real funds
5. Be respectful to other users
6. Report any issues or bugs to the GitHub repository

---

**Note**: Remember to replace placeholder values like `your_username`, `your_secure_password`, and `demo.solanalogin.com` with your actual values.
