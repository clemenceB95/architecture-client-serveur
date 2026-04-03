@echo off
setlocal
call "%~dp0mvn-local.cmd" package %*
exit /b %ERRORLEVEL%
