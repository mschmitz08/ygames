@echo off
echo Removing Y! Games launcher protocol for this Windows user...
reg delete "HKCU\Software\Classes\nygames" /f
echo.
echo Removed.
echo.
pause
