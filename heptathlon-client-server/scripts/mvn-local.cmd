@echo off
setlocal

set "MAVEN_CMD=C:\Users\Pc\AppData\Local\Programs\IntelliJ IDEA Ultimate\plugins\maven\lib\maven3\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
    echo Maven introuvable au chemin configure :
    echo %MAVEN_CMD%
    exit /b 1
)

call "%MAVEN_CMD%" %*
exit /b %ERRORLEVEL%
