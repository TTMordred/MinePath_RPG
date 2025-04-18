@echo off
echo Running WalletValidator test...
echo.

REM Create directories if they don't exist
if not exist "target\classes\com\nftlogin\walletlogin\utils" (
    mkdir "target\classes\com\nftlogin\walletlogin\utils"
)

if not exist "target\test-classes\com\nftlogin\walletlogin\utils" (
    mkdir "target\test-classes\com\nftlogin\walletlogin\utils"
)

REM Compile the classes
echo Compiling WalletValidator class...
javac -d target\classes src\main\java\com\nftlogin\walletlogin\utils\WalletValidator.java

echo Compiling WalletValidatorTest class...
javac -cp target\classes -d target\test-classes src\test\java\com\nftlogin\walletlogin\utils\WalletValidatorTest.java

echo.
echo Running test...
echo.
java -cp "target\classes;target\test-classes" com.nftlogin.walletlogin.utils.WalletValidatorTest

echo.
echo Test completed.
pause
