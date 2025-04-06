# Installation Guide for WalletLogin Plugin

This guide will help you set up and install the WalletLogin plugin for your Minecraft Spigot server.

## Prerequisites

1. **Java Development Kit (JDK) 8 or higher**
   - Download from: [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://adoptopenjdk.net/)

2. **Maven**
   - Download from: [Maven Official Site](https://maven.apache.org/download.cgi)
   - Installation guide: [Maven Installation](https://maven.apache.org/install.html)

3. **MySQL Database**
   - Download from: [MySQL](https://dev.mysql.com/downloads/mysql/)
   - Or use a hosted MySQL service

4. **Spigot Server**
   - Set up a Spigot server using [BuildTools](https://www.spigotmc.org/wiki/buildtools/) or download a pre-built version

## Building the Plugin

### Option 1: Using Maven (Recommended)

1. Open a command prompt or terminal
2. Navigate to the plugin directory (where the `pom.xml` file is located)
3. Run the following command:
   ```
   mvn clean package
   ```
4. The compiled plugin JAR file will be in the `target` directory

### Option 2: Using an IDE

1. Import the project into your IDE (IntelliJ IDEA, Eclipse, etc.)
2. Build the project using the IDE's build tools
3. The compiled JAR file will be in the `target` directory

## Database Setup

1. Create a MySQL database for the plugin:
   ```sql
   CREATE DATABASE minecraft;
   ```

2. Create a user with appropriate permissions:
   ```sql
   CREATE USER 'minecraft'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON minecraft.* TO 'minecraft'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. Update the plugin's `config.yml` file with your database credentials

## Installing the Plugin

1. Copy the compiled JAR file (`WalletLogin-1.0-SNAPSHOT.jar`) from the `target` directory to your server's `plugins` folder
2. Restart your Spigot server
3. The plugin will generate a default configuration file in the `plugins/WalletLogin` directory
4. Edit the configuration file as needed and restart the server again

## Verifying Installation

1. Start your Minecraft server
2. Check the server console for messages indicating that the WalletLogin plugin has been enabled
3. Join the server and try the plugin commands:
   - `/connectwallet <wallet_address>`
   - `/disconnectwallet`
   - `/walletinfo`

## Troubleshooting

- **Database Connection Issues**: Ensure your MySQL server is running and accessible, and that the credentials in the config file are correct
- **Plugin Not Loading**: Check the server logs for any error messages related to the plugin
- **Command Not Found**: Make sure the plugin is properly installed and enabled in the server

## Support

If you encounter any issues or have questions, please:
1. Check the README.md file for documentation
2. Review the server logs for error messages
3. Contact the plugin developer for support
