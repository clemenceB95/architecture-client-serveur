@echo off
setlocal
call "%~dp0mvn-local.cmd" test %*
exit /b %ERRORLEVEL%
