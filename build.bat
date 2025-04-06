@echo off
echo Building WalletLogin plugin...
call mvn clean package
echo.
echo Build complete! The plugin JAR file is in the target directory.
echo.
pause
