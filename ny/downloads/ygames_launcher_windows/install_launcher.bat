@echo off
setlocal
set "BASE=%~dp0"
set "TARGET=%LocalAppData%\YGamesLauncher"

echo Installing Y! Games launcher protocol for this Windows user...
echo Copying launcher files to:
echo %TARGET%
echo.

if not exist "%TARGET%" mkdir "%TARGET%"
xcopy "%BASE%*" "%TARGET%\" /E /I /Y >nul

if not exist "%TARGET%\app\newyahoo\client.jar" (
echo.
echo Warning: the launcher package copy did not include app\newyahoo\client.jar.
echo Re-extract the ZIP fully and run this installer again.
echo.
pause
exit /b 1
)

if not exist "%TARGET%\app\newyahoo\yog" (
echo.
echo Warning: the launcher package copy did not include app\newyahoo\yog.
echo Re-extract the ZIP fully and run this installer again.
echo.
pause
exit /b 1
)

reg add "HKCU\Software\Classes\nygames" /ve /d "URL:Y! Games Launcher Protocol" /f >nul
reg add "HKCU\Software\Classes\nygames" /v "URL Protocol" /d "" /f >nul
reg add "HKCU\Software\Classes\nygames\DefaultIcon" /ve /d "\"%SystemRoot%\System32\shell32.dll\",13" /f >nul
reg add "HKCU\Software\Classes\nygames\shell\open\command" /ve /d "wscript.exe \"%TARGET%\ygames_launcher.vbs\" \"%%1\"" /f >nul

echo.
echo Installed.
echo.
echo The website can now hand off nygames:// links to this launcher.
echo Future reinstalls will refresh this stable launcher folder:
echo %TARGET%
echo Existing site-specific bundles under %TARGET%\sites are preserved.
echo.
pause
