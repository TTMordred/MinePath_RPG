@echo off
echo Running WalletValidator test...
echo.

if not exist "target\classes\com\nftlogin\walletlogin\utils\WalletValidator.class" (
    echo Compiling WalletValidator class...
    if not exist "target\classes\com\nftlogin\walletlogin\utils" mkdir "target\classes\com\nftlogin\walletlogin\utils"
    javac -d target\classes src\main\java\com\nftlogin\walletlogin\utils\WalletValidator.java
)

if not exist "target\test-classes\com\nftlogin\walletlogin\utils" mkdir "target\test-classes\com\nftlogin\walletlogin\utils"
javac -cp target\classes -d target\test-classes src\test\java\com\nftlogin\walletlogin\utils\WalletValidatorTest.java

echo.
echo Running test...
echo.
java -cp target\classes;target\test-classes com.nftlogin.walletlogin.utils.WalletValidatorTest

echo.
echo Test completed.
pause
