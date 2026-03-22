@echo off
setlocal
set "BASE=%~dp0"

echo Installing Y! Games launcher protocol for this Windows user...

reg add "HKCU\Software\Classes\nygames" /ve /d "URL:Y! Games Launcher Protocol" /f >nul
reg add "HKCU\Software\Classes\nygames" /v "URL Protocol" /d "" /f >nul
reg add "HKCU\Software\Classes\nygames\DefaultIcon" /ve /d "\"%SystemRoot%\System32\shell32.dll\",13" /f >nul
reg add "HKCU\Software\Classes\nygames\shell\open\command" /ve /d "wscript.exe \"%BASE%ygames_launcher.vbs\" \"%%1\"" /f >nul

echo.
echo Installed.
echo.
echo The website can now hand off nygames:// links to this launcher.
echo Keep this launcher folder where it is so the protocol registration stays valid.
echo.
pause
