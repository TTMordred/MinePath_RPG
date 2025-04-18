# SolanaLogin Development Guide

This document provides information for developers who want to contribute to or extend the SolanaLogin plugin.

## Table of Contents

1. [Project Structure](#project-structure)
2. [Development Environment Setup](#development-environment-setup)
3. [Building the Project](#building-the-project)
4. [Testing](#testing)
5. [Code Style and Conventions](#code-style-and-conventions)
6. [Architecture Overview](#architecture-overview)
7. [Adding New Features](#adding-new-features)
8. [Common Development Tasks](#common-development-tasks)
9. [Contributing Guidelines](#contributing-guidelines)

## Project Structure

The project is divided into two main components:

### Minecraft Plugin (`src/` directory)

```
src/main/java/com/nftlogin/walletlogin/
├── SolanaLogin.java              # Main plugin class
├── commands/                     # Command handlers
│   ├── AdminCommand.java
│   ├── ChangePasswordCommand.java
│   ├── ConnectWalletCommand.java
│   ├── DisconnectWalletCommand.java
│   ├── LoginCommand.java
│   ├── LogoutCommand.java
│   ├── RegisterCommand.java
│   ├── VerifyCodeCommand.java
│   └── WalletInfoCommand.java
├── database/                     # Database management
│   ├── DatabaseManager.java
│   └── SQLQueries.java
├── listeners/                    # Event listeners
│   └── PlayerLoginListener.java
├── session/                      # Session management
│   └── SessionManager.java
└── utils/                        # Utility classes
    ├── PasswordUtils.java
    └── WalletValidator.java
```

### Web Server (`web-server/` directory)

```
web-server/
├── server.js                     # Main server file
├── package.json                  # Node.js dependencies
├── public/                       # Static files
│   ├── css/
│   │   └── style.css
│   ├── js/
│   │   └── utils.js
│   ├── index.html
│   ├── login.html
│   ├── qr.html
│   ├── redirect.html
│   ├── simple-connect.html
│   ├── simple-qr.html
│   ├── simple-redirect.html
│   └── test-flow.html
└── README.md
```

## Development Environment Setup

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Maven
- Node.js 14 or higher
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code)
- MySQL database server

### Setting Up the Development Environment

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/SolanaLogin.git
   cd SolanaLogin
   ```

2. **Set up the database**

   ```sql
   CREATE DATABASE minecraft;
   CREATE USER 'minecraft'@'localhost' IDENTIFIED BY 'yourpassword';
   GRANT ALL PRIVILEGES ON minecraft.* TO 'minecraft'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Configure the IDE**

   For IntelliJ IDEA:
   - Open the project
   - Import as Maven project
   - Set up the JDK
   - Install the Minecraft Development plugin (optional but recommended)

   For Eclipse:
   - Import as Maven project
   - Set up the JDK
   - Configure build path if needed

4. **Set up the web server**

   ```bash
   cd web-server
   npm install
   ```

## Building the Project

### Building the Plugin

```bash
# From the project root
mvn clean package
```

This will generate a JAR file in the `target` directory.

### Running the Web Server

```bash
# From the web-server directory
npm start
```

For development with auto-reload:

```bash
npm run dev
```

## Testing

### Running Tests

```bash
# From the project root
mvn test
```

Or use the provided scripts:

```bash
# On Windows
run-test.bat

# On Linux/Mac
./run-test.sh
```

### Testing the Plugin

1. Set up a test server
2. Copy the generated JAR file to the server's `plugins` folder
3. Start the server
4. Test the functionality in-game

### Testing the Web Server

1. Start the web server
2. Access the test pages:
   - `http://localhost:3000/test-flow.html`
   - `http://localhost:3000/simple-connect.html`
   - `http://localhost:3000/simple-qr.html`
   - `http://localhost:3000/simple-redirect.html`

## Code Style and Conventions

### Java Code Style

- Use standard Java naming conventions
- Classes: PascalCase
- Methods and variables: camelCase
- Constants: UPPER_SNAKE_CASE
- Package names: lowercase
- Use meaningful names
- Add JavaDoc comments for public methods
- Keep methods small and focused
- Follow the single responsibility principle

### JavaScript Code Style

- Use ES6+ features
- Variables: camelCase
- Constants: UPPER_SNAKE_CASE or camelCase
- Functions: camelCase
- Add comments for complex logic
- Use async/await for asynchronous code
- Organize code into logical modules

## Architecture Overview

### Plugin Architecture

The plugin follows a modular architecture:

1. **Main Plugin Class (`SolanaLogin.java`)**
   - Initializes the plugin
   - Registers commands and event listeners
   - Manages plugin lifecycle

2. **Command Handlers (`commands/` package)**
   - Each command has its own class
   - Handles command execution and permissions
   - Interacts with the database and session manager

3. **Database Management (`database/` package)**
   - Manages database connections
   - Handles data storage and retrieval
   - Provides an abstraction layer for SQL operations

4. **Session Management (`session/` package)**
   - Manages player sessions
   - Handles authentication state
   - Stores temporary data like verification codes

5. **Event Listeners (`listeners/` package)**
   - Listens for Bukkit events
   - Handles player join/quit events
   - Enforces authentication requirements

6. **Utilities (`utils/` package)**
   - Provides helper methods
   - Handles password hashing and verification
   - Validates wallet addresses

### Web Server Architecture

The web server follows a simple Express.js architecture:

1. **Main Server (`server.js`)**
   - Sets up Express server
   - Defines routes and middleware
   - Handles API endpoints

2. **Static Files (`public/` directory)**
   - HTML pages for user interface
   - CSS for styling
   - JavaScript for client-side logic

3. **Client-Side Logic (`public/js/utils.js`)**
   - Handles wallet connections
   - Manages signature verification
   - Provides UI utilities

## Adding New Features

### Adding a New Command

1. Create a new class in the `commands` package
2. Implement the `CommandExecutor` interface
3. Register the command in `SolanaLogin.java`
4. Add the command to `plugin.yml`

Example:

```java
package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NewCommand implements CommandExecutor {

    private final SolanaLogin plugin;

    public NewCommand(SolanaLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.formatMessage("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;
        // Command logic here

        return true;
    }
}
```

Then in `SolanaLogin.java`:

```java
getCommand("newcommand").setExecutor(new NewCommand(this));
```

And in `plugin.yml`:

```yaml
commands:
  newcommand:
    description: Description of the new command
    usage: /newcommand
    permission: solanalogin.newcommand
```

### Adding a New Web API Endpoint

1. Add the route to `server.js`
2. Implement the endpoint logic
3. Test the endpoint

Example:

```javascript
// Add a new API endpoint
app.get('/api/new-endpoint', (req, res) => {
  // Endpoint logic here
  res.json({ success: true, data: 'Some data' });
});
```

## Common Development Tasks

### Adding Database Fields

1. Update the table creation SQL in `DatabaseManager.java`
2. Add methods to access the new fields
3. Update any related commands or listeners

Example:

```java
// In SQLQueries.java
public static final String CREATE_PLAYERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "players ("
        + "uuid VARCHAR(36) PRIMARY KEY, "
        + "username VARCHAR(16) NOT NULL, "
        + "password VARCHAR(255) NOT NULL, "
        + "ip VARCHAR(45), "
        + "last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
        + "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
        + "new_field VARCHAR(255)" // Add new field
        + ")";

// In DatabaseManager.java
public void setNewField(UUID playerUuid, String value) {
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement("UPDATE " + SQLQueries.TABLE_PREFIX + "players SET new_field = ? WHERE uuid = ?")) {
        stmt.setString(1, value);
        stmt.setString(2, playerUuid.toString());
        stmt.executeUpdate();
    } catch (SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to update new field", e);
    }
}

public Optional<String> getNewField(UUID playerUuid) {
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT new_field FROM " + SQLQueries.TABLE_PREFIX + "players WHERE uuid = ?")) {
        stmt.setString(1, playerUuid.toString());
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return Optional.ofNullable(rs.getString("new_field"));
        }
    } catch (SQLException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to get new field", e);
    }
    return Optional.empty();
}
```

### Modifying the Web Interface

1. Edit the HTML files in the `web-server/public` directory
2. Update the CSS in `web-server/public/css/style.css`
3. Modify client-side logic in `web-server/public/js/utils.js`
4. Test the changes

## Contributing Guidelines

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
   - Follow the code style guidelines
   - Add tests for new functionality
   - Update documentation as needed
4. **Commit your changes**
   ```bash
   git commit -m "Add feature: your feature description"
   ```
5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```
6. **Create a pull request**
   - Provide a clear description of the changes
   - Reference any related issues
   - Wait for review and address any feedback

### Commit Message Guidelines

- Use the present tense ("Add feature" not "Added feature")
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
- Limit the first line to 72 characters or less
- Reference issues and pull requests liberally after the first line

### Code Review Process

- All code changes require a review before merging
- Address all review comments
- Make sure all tests pass
- Ensure documentation is updated

## Additional Resources

- [Bukkit API Documentation](https://hub.spigotmc.org/javadocs/bukkit/)
- [Spigot Plugin Development Guide](https://www.spigotmc.org/wiki/spigot-plugin-development/)
- [Express.js Documentation](https://expressjs.com/)
- [Solana Web3.js Documentation](https://solana-labs.github.io/solana-web3.js/)
- [Phantom Wallet Documentation](https://docs.phantom.app/)
