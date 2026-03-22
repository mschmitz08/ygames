@echo off
setlocal
set "TARGET=%LocalAppData%\YGamesLauncher"
echo Removing Y! Games launcher protocol for this Windows user...
reg delete "HKCU\Software\Classes\nygames" /f
if exist "%TARGET%" rmdir /S /Q "%TARGET%"
echo.
echo Removed.
echo.
pause
