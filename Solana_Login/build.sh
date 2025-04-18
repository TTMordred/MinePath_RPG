#!/bin/bash

echo "Building WalletLogin plugin..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    echo "Visit https://maven.apache.org/install.html for installation instructions."
    exit 1
fi

# Build the plugin
mvn clean package

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful! The plugin JAR file is in the target directory."
    echo "Copy the JAR file to your Spigot server's plugins folder to install it."
    echo ""
    
    # Check if the JAR file exists
    JAR_FILE=$(find target -name "WalletLogin-*.jar" | head -n 1)
    if [ -n "$JAR_FILE" ]; then
        echo "Plugin JAR: $JAR_FILE"
        echo "File size: $(du -h "$JAR_FILE" | cut -f1)"
    fi
else
    echo ""
    echo "Build failed. Please check the error messages above."
fi
