@echo off
setlocal

for %%I in ("%~dp0..\..") do set "REPO_ROOT=%%~fI"
set "SCRIPT_DIR=%~dp0"
set "LOCAL_CONFIG=%SCRIPT_DIR%deploy.local.cmd"

if not exist "%LOCAL_CONFIG%" (
    echo Missing local config: "%LOCAL_CONFIG%"
    echo Copy deploy.local.example.cmd to deploy.local.cmd and update the path values first.
    exit /b 1
)

call "%LOCAL_CONFIG%"

if not defined TOMCAT_ROOT (
    echo TOMCAT_ROOT is not set.
    echo Update "%LOCAL_CONFIG%" with your local Tomcat install path.
    exit /b 1
)

set "NEWHYAHOO_SRC=%REPO_ROOT%\newyahoo\WEB-INF\src"
set "NEWHYAHOO_CLASSES=%REPO_ROOT%\newyahoo\WEB-INF\classes"
set "NEWHYAHOO_ROOT=%REPO_ROOT%\newyahoo"
set "NY_SRC=%REPO_ROOT%\ny\WEB-INF\src"
set "NY_DOWNLOADS=%REPO_ROOT%\ny\downloads"
set "LAUNCHER_STAGING=%NY_DOWNLOADS%\ygames_launcher_windows"
set "LAUNCHER_CLIENT=%LAUNCHER_STAGING%\app\newyahoo\client.jar"
set "SERVLET_JAR=%TOMCAT_ROOT%\lib\servlet-api.jar"
set "WEBAPPS_ROOT=%TOMCAT_ROOT%\webapps"
set "WORK_ROOT=%TOMCAT_ROOT%\work\Catalina\localhost"
set "LAUNCHER_VERSION="

echo Refreshing Java source lists...
cd /d "%NEWHYAHOO_SRC%"
dir /s /b *.java > sources.txt

cd /d "%NY_SRC%"
dir /s /b *.java > sources.txt

echo Compiling newyahoo...
cd /d "%NEWHYAHOO_SRC%"
javac -cp "..\classes;..\lib\*;..\..\..\lib\*;%SERVLET_JAR%" -d ..\classes @sources.txt
if errorlevel 1 exit /b %errorlevel%

echo Rebuilding client.jar...
cd /d "%NEWHYAHOO_ROOT%"
jar cf client.jar -C WEB-INF\classes . -C . yog
if errorlevel 1 exit /b %errorlevel%

for /f %%V in ('powershell -NoProfile -Command "$value = (Get-Content '%REPO_ROOT%\ny\downloads\ygames_launcher_windows\launcher_version.txt' -Raw).Trim(); if ($value) { $value }"') do set "LAUNCHER_VERSION=%%V"
if not defined LAUNCHER_VERSION (
    echo Could not determine launcher version from "%REPO_ROOT%\ny\downloads\ygames_launcher_windows\launcher_version.txt".
    exit /b 1
)
echo Launcher version is %LAUNCHER_VERSION%

echo Refreshing launcher package...
copy /Y "%NEWHYAHOO_ROOT%\client.jar" "%LAUNCHER_CLIENT%"
if exist "%NY_DOWNLOADS%\ygames_launcher_windows_%LAUNCHER_VERSION%.zip" del /Q "%NY_DOWNLOADS%\ygames_launcher_windows_%LAUNCHER_VERSION%.zip"
if exist "%NY_DOWNLOADS%\ygames_launcher_windows.zip" del /Q "%NY_DOWNLOADS%\ygames_launcher_windows.zip"
powershell -NoProfile -Command "Compress-Archive -Path '%LAUNCHER_STAGING%\*' -DestinationPath '%NY_DOWNLOADS%\ygames_launcher_windows_%LAUNCHER_VERSION%.zip'"
if errorlevel 1 exit /b %errorlevel%
copy /Y "%NY_DOWNLOADS%\ygames_launcher_windows_%LAUNCHER_VERSION%.zip" "%NY_DOWNLOADS%\ygames_launcher_windows.zip"

echo Compiling ny...
cd /d "%NY_SRC%"
javac -cp "..\classes;..\lib\*;..\..\..\lib\*;%SERVLET_JAR%" -d ..\classes @sources.txt
if errorlevel 1 exit /b %errorlevel%

echo Restarting Tomcat and redeploying webapps...
cd /d "%TOMCAT_ROOT%\bin"
call shutdown.bat
rmdir /S /Q "%WEBAPPS_ROOT%\ny"
rmdir /S /Q "%WEBAPPS_ROOT%\newyahoo"
rmdir /S /Q "%WORK_ROOT%\ny"
rmdir /S /Q "%WORK_ROOT%\newyahoo"
robocopy "%REPO_ROOT%\ny" "%WEBAPPS_ROOT%\ny" /MIR
robocopy "%REPO_ROOT%\newyahoo" "%WEBAPPS_ROOT%\newyahoo" /MIR
call startup.bat

echo Redeploy complete.
endlocal
