@echo off
echo ===================================
echo Building NFT Plugin for Minecraft
echo ===================================

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Maven is not installed or not in PATH.
    echo Please install Maven from https://maven.apache.org/download.cgi
    echo and add it to your PATH.
    exit /b 1
)

echo Building plugin with Maven...
call mvn clean package

if %ERRORLEVEL% neq 0 (
    echo Build failed. Please check the errors above.
    exit /b 1
)

echo.
echo Build successful!
echo.
echo The plugin JAR file is located in the target directory.
echo Copy the JAR file to your server's plugins folder to install.
echo.
echo Don't forget to install the SolanaLogin plugin as well!
echo.

REM Copy the JAR file to a more accessible location
echo Copying JAR file to the root directory...
copy /Y target\nft-plugin-1.0-SNAPSHOT.jar NFTPlugin.jar

echo Done!
pause
