@echo off
setlocal
call "%~dp0mvn-local.cmd" site %*
exit /b %ERRORLEVEL%
