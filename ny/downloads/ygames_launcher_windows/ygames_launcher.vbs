Option Explicit

Dim shell
Dim fso
Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

Dim baseDir
baseDir = fso.GetParentFolderName(WScript.ScriptFullName)

Dim templateAppDir
templateAppDir = fso.BuildPath(baseDir, "app\newyahoo")

Dim sitesRootDir
sitesRootDir = fso.BuildPath(baseDir, "sites")
EnsureFolder sitesRootDir

Dim appDir
appDir = templateAppDir

Dim launchDir
launchDir = fso.BuildPath(baseDir, "launches")
EnsureFolder launchDir

Dim game
Dim room
Dim host
Dim port
Dim accountMode
Dim launcherVersion
Dim webBase
Dim installedLauncherVersion
Dim appletWidth
Dim appletHeight
Dim openSettings
Dim widthSpecified
Dim heightSpecified
Dim expectedClientHash
Dim stableInstallDir
Dim siteId
Dim siteRootDir

game = "pool"
room = "corner_pocket"
host = "127.0.0.1"
port = "11998"
accountMode = ""
launcherVersion = ""
webBase = "http://127.0.0.1:8080/ny"
installedLauncherVersion = "0.7.3"
appletWidth = "1400"
appletHeight = "900"
openSettings = False
widthSpecified = False
heightSpecified = False
expectedClientHash = ""
stableInstallDir = shell.ExpandEnvironmentStrings("%LocalAppData%\YGamesLauncher")
siteId = ""
siteRootDir = baseDir

ParseArguments
ConfigureSitePaths
EnsureSiteBundle
LoadLauncherSettings

If ShouldWarnPortableLaunch() Then
    ShowPortableLaunchWarning
End If

If openSettings Then
    ConfigureLauncherSettings
    WScript.Quit 0
End If

If expectedClientHash <> "" Then
    Dim localClientHash
    localClientHash = GetFileSha256(fso.BuildPath(appDir, "client.jar"))
    If siteId <> "" And LCase(localClientHash) <> LCase(expectedClientHash) Then
        Dim templateClientHash
        templateClientHash = GetFileSha256(fso.BuildPath(templateAppDir, "client.jar"))
        If templateClientHash <> "" And LCase(templateClientHash) = LCase(expectedClientHash) Then
            CopyFolderContents templateAppDir, appDir
            localClientHash = GetFileSha256(fso.BuildPath(appDir, "client.jar"))
        End If
    End If
    If localClientHash = "" Or LCase(localClientHash) <> LCase(expectedClientHash) Then
        ShowClientMismatch expectedClientHash, localClientHash
        WScript.Quit 1
    End If
End If

If launcherVersion <> "" Then
    If CompareVersions(installedLauncherVersion, launcherVersion) < 0 Then
        ShowUpdateRequired launcherVersion
        WScript.Quit 1
    End If
End If

Dim appletViewerPath
appletViewerPath = FindAppletViewer()
If appletViewerPath = "" Then
    ShowRequirementError
    WScript.Quit 1
End If

If Not fso.FileExists(fso.BuildPath(appDir, "client.jar")) Then
    MsgBox "The launcher could not find client.jar in:" & vbCrLf & appDir & vbCrLf & vbCrLf & _
        "Reinstall the launcher package or copy the game files into the app folder.", vbExclamation, "Y! Games Launcher"
    WScript.Quit 1
End If

If Not fso.FolderExists(fso.BuildPath(appDir, "yog")) Then
    MsgBox "The launcher could not find the yog resource folder in:" & vbCrLf & appDir & vbCrLf & vbCrLf & _
        "Reinstall the launcher package or copy the game files into the app folder.", vbExclamation, "Y! Games Launcher"
    WScript.Quit 1
End If

Dim policyPath
Dim htmlPath
policyPath = fso.BuildPath(launchDir, "launch.policy")
htmlPath = fso.BuildPath(launchDir, CleanFileName(game & "_" & room) & ".html")

WritePolicyFile policyPath
WriteAppletHtml htmlPath

Dim command
command = Quote(appletViewerPath) & " -J-Djava.security.policy=" & Quote(policyPath) _
    & " -J-Dsun.java2d.dpiaware=false " & Quote(htmlPath)
shell.CurrentDirectory = appDir
shell.Run command, 0, False

Sub ParseArguments()
    If WScript.Arguments.Count = 0 Then
        Exit Sub
    End If

    Dim firstArg
    firstArg = WScript.Arguments(0)
    If LCase(Left(firstArg, 10)) = "nygames://" Then
        ParseUri firstArg
    Else
        ParseNamedArgs
    End If
End Sub

Sub ParseNamedArgs()
    Dim i
    i = 0
    Do While i < WScript.Arguments.Count
        Dim name
        Dim value
        name = LCase(WScript.Arguments(i))
        value = ""
        If i + 1 < WScript.Arguments.Count Then
            value = WScript.Arguments(i + 1)
        End If

        If name = "--game" Then
            game = value
            i = i + 2
        ElseIf name = "--room" Then
            room = value
            i = i + 2
        ElseIf name = "--host" Then
            host = value
            i = i + 2
        ElseIf name = "--port" Then
            port = value
            i = i + 2
        ElseIf name = "--account_mode" Then
            accountMode = value
            i = i + 2
        ElseIf name = "--launcher_version" Then
            launcherVersion = value
            i = i + 2
        ElseIf name = "--webbase" Then
            webBase = value
            i = i + 2
        ElseIf name = "--width" Then
            If IsNumeric(value) Then
                appletWidth = CStr(CLng(value))
                widthSpecified = True
            End If
            i = i + 2
        ElseIf name = "--height" Then
            If IsNumeric(value) Then
                appletHeight = CStr(CLng(value))
                heightSpecified = True
            End If
            i = i + 2
        ElseIf name = "--settings" Then
            openSettings = True
            i = i + 1
        ElseIf name = "--expected_client_hash" Then
            expectedClientHash = value
            i = i + 2
        ElseIf name = "--site_id" Then
            siteId = value
            i = i + 2
        Else
            i = i + 1
        End If
    Loop
End Sub

Sub ParseUri(uri)
    Dim queryPos
    queryPos = InStr(uri, "?")
    If queryPos <= 0 Then
        Exit Sub
    End If

    Dim query
    query = Mid(uri, queryPos + 1)
    Dim parts
    Dim part
    parts = Split(query, "&")
    For Each part In parts
        Dim eqPos
        eqPos = InStr(part, "=")
        If eqPos > 0 Then
            Dim key
            Dim value
            key = LCase(Left(part, eqPos - 1))
            value = UrlDecode(Mid(part, eqPos + 1))
            If key = "game" Then
                game = value
            ElseIf key = "room" Then
                room = value
            ElseIf key = "host" Then
                host = value
            ElseIf key = "port" Then
                port = value
            ElseIf key = "account_mode" Then
                accountMode = value
            ElseIf key = "launcher_version" Then
                launcherVersion = value
            ElseIf key = "webbase" Then
                webBase = value
            ElseIf key = "width" Then
                If IsNumeric(value) Then
                    appletWidth = CStr(CLng(value))
                    widthSpecified = True
                End If
            ElseIf key = "height" Then
                If IsNumeric(value) Then
                    appletHeight = CStr(CLng(value))
                    heightSpecified = True
                End If
            ElseIf key = "action" And LCase(value) = "settings" Then
                openSettings = True
            ElseIf key = "expected_client_hash" Then
                expectedClientHash = value
            ElseIf key = "site_id" Then
                siteId = value
            End If
        End If
    Next
End Sub

Sub ConfigureSitePaths()
    If siteId = "" Then
        launchDir = fso.BuildPath(baseDir, "launches")
        EnsureFolder launchDir
        appDir = templateAppDir
        siteRootDir = baseDir
        Exit Sub
    End If

    siteId = CleanFileName(siteId)
    siteRootDir = fso.BuildPath(sitesRootDir, siteId)
    EnsureFolder siteRootDir
    EnsureFolder fso.BuildPath(siteRootDir, "app")
    appDir = fso.BuildPath(siteRootDir, "app\newyahoo")
    launchDir = fso.BuildPath(siteRootDir, "launches")
    EnsureFolder launchDir
End Sub

Sub EnsureSiteBundle()
    If siteId = "" Then
        Exit Sub
    End If

    Dim siteClientJar
    Dim siteYogDir
    siteClientJar = fso.BuildPath(appDir, "client.jar")
    siteYogDir = fso.BuildPath(appDir, "yog")

    If fso.FileExists(siteClientJar) And fso.FolderExists(siteYogDir) Then
        Exit Sub
    End If

    If Not fso.FolderExists(templateAppDir) Then
        Exit Sub
    End If

    CopyFolderContents templateAppDir, appDir
End Sub

Function ShouldWarnPortableLaunch()
    Dim lowerBase
    lowerBase = LCase(baseDir)
    ShouldWarnPortableLaunch = False
    If LCase(baseDir) = LCase(stableInstallDir) Then
        Exit Function
    End If
    If InStr(lowerBase, "\downloads\") > 0 Or InStr(lowerBase, "\temp\") > 0 _
            Or InStr(lowerBase, "\temporary") > 0 Then
        ShouldWarnPortableLaunch = True
    End If
End Function

Sub ShowPortableLaunchWarning()
    MsgBox "This launcher appears to be running from:" & vbCrLf & baseDir & vbCrLf & vbCrLf & _
        "That often means it was opened from Downloads or a temporary extracted folder." & vbCrLf & _
        "For reliable updates, run install_launcher.bat so Windows uses the stable launcher folder:" & vbCrLf & _
        stableInstallDir, vbExclamation, "Y! Games Launcher Install Reminder"
End Sub

Function FindAppletViewer()
    Dim bundledPath
    bundledPath = fso.BuildPath(baseDir, "runtime\bin\appletviewer.exe")
    If fso.FileExists(bundledPath) Then
        FindAppletViewer = bundledPath
        Exit Function
    End If

    Dim javaHome
    javaHome = shell.ExpandEnvironmentStrings("%JAVA_HOME%")
    If javaHome <> "%JAVA_HOME%" Then
        Dim javaHomeAppletViewer
        javaHomeAppletViewer = fso.BuildPath(javaHome, "bin\appletviewer.exe")
        If fso.FileExists(javaHomeAppletViewer) Then
            FindAppletViewer = javaHomeAppletViewer
            Exit Function
        End If
    End If

    Dim discoveredPath
    discoveredPath = FindAppletViewerInKnownLocations()
    If discoveredPath <> "" Then
        FindAppletViewer = discoveredPath
        Exit Function
    End If

    Dim exec
    On Error Resume Next
    Set exec = shell.Exec("cmd /c where appletviewer")
    If Err.Number = 0 Then
        Dim output
        output = ""
        Do While Not exec.StdOut.AtEndOfStream
            output = Trim(exec.StdOut.ReadLine)
            If output <> "" And fso.FileExists(output) Then
                FindAppletViewer = output
                Exit Function
            End If
        Loop
    End If
    On Error GoTo 0

    FindAppletViewer = ""
End Function

Function FindAppletViewerInKnownLocations()
    Dim searchRoots
    searchRoots = Array( _
        "C:\Program Files\Java", _
        "C:\Program Files (x86)\Java", _
        "C:\Program Files\Eclipse Adoptium", _
        "C:\Program Files\AdoptOpenJDK", _
        "C:\Program Files\BellSoft" _
    )

    Dim i
    For i = 0 To UBound(searchRoots)
        Dim candidate
        candidate = FindAppletViewerUnderRoot(searchRoots(i))
        If candidate <> "" Then
            FindAppletViewerInKnownLocations = candidate
            Exit Function
        End If
    Next

    FindAppletViewerInKnownLocations = ""
End Function

Function FindAppletViewerUnderRoot(rootPath)
    If Not fso.FolderExists(rootPath) Then
        FindAppletViewerUnderRoot = ""
        Exit Function
    End If

    Dim rootFolder
    Set rootFolder = fso.GetFolder(rootPath)

    Dim subFolder
    For Each subFolder In rootFolder.SubFolders
        Dim directCandidate
        directCandidate = fso.BuildPath(subFolder.Path, "bin\appletviewer.exe")
        If fso.FileExists(directCandidate) Then
            FindAppletViewerUnderRoot = directCandidate
            Exit Function
        End If
    Next

    FindAppletViewerUnderRoot = ""
End Function

Function GetAppletViewerDiagnostics()
    Dim report
    report = "Y! Games Launcher diagnostics" & vbCrLf
    report = report & "Time: " & Now & vbCrLf
    report = report & "Launcher folder: " & baseDir & vbCrLf
    report = report & "Stable install folder: " & stableInstallDir & vbCrLf
    report = report & "Site id: " & siteId & vbCrLf
    report = report & "Site root folder: " & siteRootDir & vbCrLf
    report = report & "Game folder: " & appDir & vbCrLf & vbCrLf

    Dim javaHome
    javaHome = shell.ExpandEnvironmentStrings("%JAVA_HOME%")
    report = report & "JAVA_HOME: " & javaHome & vbCrLf
    If javaHome <> "%JAVA_HOME%" Then
        report = report & "JAVA_HOME appletviewer: " & CheckCandidatePath(fso.BuildPath(javaHome, "bin\appletviewer.exe")) & vbCrLf
    Else
        report = report & "JAVA_HOME appletviewer: JAVA_HOME is not set" & vbCrLf
    End If

    report = report & "Bundled runtime appletviewer: " & CheckCandidatePath(fso.BuildPath(baseDir, "runtime\bin\appletviewer.exe")) & vbCrLf & vbCrLf
    report = report & "Known install folders:" & vbCrLf
    report = report & DescribeKnownLocationChecks()
    report = report & vbCrLf & "PATH lookup (where appletviewer):" & vbCrLf
    report = report & DescribeWhereLookup()
    report = report & vbCrLf & "Current request:" & vbCrLf
    report = report & "installed_launcher_version=" & installedLauncherVersion & vbCrLf
    report = report & "expected_client_hash=" & expectedClientHash & vbCrLf
    report = report & "local_client_hash=" & GetFileSha256(fso.BuildPath(appDir, "client.jar")) & vbCrLf
    report = report & "game=" & game & vbCrLf
    report = report & "room=" & room & vbCrLf
    report = report & "host=" & host & vbCrLf
    report = report & "port=" & port & vbCrLf
    report = report & "applet_width=" & appletWidth & vbCrLf
    report = report & "applet_height=" & appletHeight & vbCrLf
    report = report & "account_mode=" & accountMode & vbCrLf
    report = report & "launcher_version=" & launcherVersion & vbCrLf
    report = report & "webbase=" & webBase & vbCrLf

    GetAppletViewerDiagnostics = report
End Function

Function GetFileSha256(filePath)
    If Not fso.FileExists(filePath) Then
        GetFileSha256 = ""
        Exit Function
    End If

    Dim exec
    Dim report
    report = ""
    On Error Resume Next
    Set exec = shell.Exec("cmd /c certutil -hashfile " & Quote(filePath) & " SHA256")
    If Err.Number <> 0 Then
        Err.Clear
        On Error GoTo 0
        GetFileSha256 = ""
        Exit Function
    End If
    On Error GoTo 0

    Do While Not exec.StdOut.AtEndOfStream
        Dim line
        line = Trim(exec.StdOut.ReadLine)
        If line <> "" And InStr(line, "SHA256") = 0 And InStr(line, "CertUtil:") = 0 Then
            report = report & Replace(line, " ", "")
        End If
    Loop
    GetFileSha256 = LCase(report)
End Function

Sub CopyFolderContents(sourcePath, targetPath)
    If Not fso.FolderExists(sourcePath) Then
        Exit Sub
    End If

    If Not fso.FolderExists(targetPath) Then
        fso.CreateFolder targetPath
    End If

    Dim sourceFolder
    Set sourceFolder = fso.GetFolder(sourcePath)

    Dim file
    For Each file In sourceFolder.Files
        fso.CopyFile file.Path, fso.BuildPath(targetPath, file.Name), True
    Next

    Dim subFolder
    For Each subFolder In sourceFolder.SubFolders
        CopyFolderContents subFolder.Path, fso.BuildPath(targetPath, subFolder.Name)
    Next
End Sub

Sub LoadLauncherSettings()
    Dim settingsPath
    settingsPath = fso.BuildPath(siteRootDir, "launcher_settings.ini")
    If Not fso.FileExists(settingsPath) Then
        Exit Sub
    End If

    Dim file
    Set file = fso.OpenTextFile(settingsPath, 1)
    Do While Not file.AtEndOfStream
        Dim line
        line = Trim(file.ReadLine)
        If line <> "" And Left(line, 1) <> "#" And Left(line, 1) <> ";" Then
            Dim eqPos
            eqPos = InStr(line, "=")
            If eqPos > 0 Then
                Dim key
                Dim value
                key = LCase(Trim(Left(line, eqPos - 1)))
                value = Trim(Mid(line, eqPos + 1))
                If key = "width" And IsNumeric(value) And Not widthSpecified Then
                    appletWidth = CStr(CLng(value))
                ElseIf key = "height" And IsNumeric(value) And Not heightSpecified Then
                    appletHeight = CStr(CLng(value))
                End If
            End If
        End If
    Loop
    file.Close
End Sub

Sub ConfigureLauncherSettings()
    Dim widthValue
    Dim heightValue

    widthValue = InputBox( _
        "Enter the launcher width in pixels." & vbCrLf & vbCrLf & _
        "Current width: " & appletWidth, _
        "Y! Games Launcher Settings", appletWidth)
    If widthValue = "" Then
        Exit Sub
    End If
    If Not IsNumeric(widthValue) Then
        MsgBox "Width must be a number.", vbExclamation, "Y! Games Launcher Settings"
        Exit Sub
    End If

    heightValue = InputBox( _
        "Enter the launcher height in pixels." & vbCrLf & vbCrLf & _
        "Current height: " & appletHeight, _
        "Y! Games Launcher Settings", appletHeight)
    If heightValue = "" Then
        Exit Sub
    End If
    If Not IsNumeric(heightValue) Then
        MsgBox "Height must be a number.", vbExclamation, "Y! Games Launcher Settings"
        Exit Sub
    End If

    appletWidth = CStr(CLng(widthValue))
    appletHeight = CStr(CLng(heightValue))
    SaveLauncherSettings

    MsgBox "Launcher settings saved." & vbCrLf & vbCrLf & _
        "Width: " & appletWidth & vbCrLf & _
        "Height: " & appletHeight, vbInformation, "Y! Games Launcher Settings"
End Sub

Sub SaveLauncherSettings()
    Dim settingsPath
    Dim file
    settingsPath = fso.BuildPath(siteRootDir, "launcher_settings.ini")
    Set file = fso.CreateTextFile(settingsPath, True)
    file.WriteLine "# Y! Games Launcher window size"
    file.WriteLine "width=" & appletWidth
    file.WriteLine "height=" & appletHeight
    file.Close
End Sub

Function CompareVersions(leftValue, rightValue)
    Dim leftParts
    Dim rightParts
    leftParts = Split(leftValue, ".")
    rightParts = Split(rightValue, ".")

    Dim maxIndex
    maxIndex = UBound(leftParts)
    If UBound(rightParts) > maxIndex Then
        maxIndex = UBound(rightParts)
    End If

    Dim i
    For i = 0 To maxIndex
        Dim leftPart
        Dim rightPart
        leftPart = 0
        rightPart = 0
        If i <= UBound(leftParts) And IsNumeric(leftParts(i)) Then
            leftPart = CLng(leftParts(i))
        End If
        If i <= UBound(rightParts) And IsNumeric(rightParts(i)) Then
            rightPart = CLng(rightParts(i))
        End If
        If leftPart < rightPart Then
            CompareVersions = -1
            Exit Function
        End If
        If leftPart > rightPart Then
            CompareVersions = 1
            Exit Function
        End If
    Next

    CompareVersions = 0
End Function

Function DescribeKnownLocationChecks()
    Dim searchRoots
    searchRoots = Array( _
        "C:\Program Files\Java", _
        "C:\Program Files (x86)\Java", _
        "C:\Program Files\Eclipse Adoptium", _
        "C:\Program Files\AdoptOpenJDK", _
        "C:\Program Files\BellSoft" _
    )

    Dim report
    Dim i
    report = ""
    For i = 0 To UBound(searchRoots)
        report = report & DescribeRoot(searchRoots(i))
    Next
    DescribeKnownLocationChecks = report
End Function

Function DescribeRoot(rootPath)
    Dim report
    report = ""
    If Not fso.FolderExists(rootPath) Then
        DescribeRoot = "- " & rootPath & " (missing)" & vbCrLf
        Exit Function
    End If

    report = "- " & rootPath & vbCrLf
    Dim rootFolder
    Set rootFolder = fso.GetFolder(rootPath)

    Dim subFolder
    For Each subFolder In rootFolder.SubFolders
        report = report & "    " & CheckCandidatePath(fso.BuildPath(subFolder.Path, "bin\appletviewer.exe")) & vbCrLf
    Next

    DescribeRoot = report
End Function

Function CheckCandidatePath(candidatePath)
    If fso.FileExists(candidatePath) Then
        CheckCandidatePath = candidatePath & " [FOUND]"
    Else
        CheckCandidatePath = candidatePath & " [missing]"
    End If
End Function

Function DescribeWhereLookup()
    Dim exec
    Dim report
    report = ""
    On Error Resume Next
    Set exec = shell.Exec("cmd /c where appletviewer")
    If Err.Number <> 0 Then
        DescribeWhereLookup = "Unable to run PATH lookup: " & Err.Description & vbCrLf
        Err.Clear
        On Error GoTo 0
        Exit Function
    End If

    Do While Not exec.StdOut.AtEndOfStream
        report = report & "    " & Trim(exec.StdOut.ReadLine) & vbCrLf
    Loop
    Do While Not exec.StdErr.AtEndOfStream
        report = report & "    " & Trim(exec.StdErr.ReadLine) & vbCrLf
    Loop
    On Error GoTo 0

    If report = "" Then
        report = "    No appletviewer.exe found on PATH" & vbCrLf
    End If
    DescribeWhereLookup = report
End Function

Sub ShowRequirementError()
    Dim message
    message = "This launcher needs Java 8 AppletViewer support before it can start the game." & vbCrLf & vbCrLf & _
        "What the launcher checked:" & vbCrLf & _
        "1. Bundled runtime in the launcher package" & vbCrLf & _
        "2. JAVA_HOME\bin\appletviewer.exe" & vbCrLf & _
        "3. Standard Java install folders under Program Files" & vbCrLf & _
        "4. appletviewer on your PATH" & vbCrLf & vbCrLf & _
        "What to do next:" & vbCrLf & _
        "- Install a Java 8 JDK that includes appletviewer." & vbCrLf & _
        "  Recommended download: https://adoptium.net/temurin/releases/?version=8" & vbCrLf & _
        "- After installing, open a new browser window and try Launch Again." & vbCrLf & _
        "- Or place an approved bundled Java 8 runtime under the launcher's runtime folder." & vbCrLf & vbCrLf & _
        "Launcher folder:" & vbCrLf & baseDir & vbCrLf & vbCrLf & _
        "Click Yes to open a diagnostics report."

    Dim response
    response = MsgBox(message, vbExclamation + vbYesNo, "Y! Games Launcher Requirements")
    If response = vbYes Then
        OpenDiagnosticsReport
    End If
End Sub

Sub ShowUpdateRequired(requiredVersion)
    Dim message
    message = "This launcher is out of date for the website you just opened." & vbCrLf & vbCrLf & _
        "Installed launcher version: " & installedLauncherVersion & vbCrLf & _
        "Website expects version: " & requiredVersion & vbCrLf & vbCrLf & _
        "Please download the newer launcher package from the site and run install_launcher.bat again." & vbCrLf & vbCrLf & _
        "Launcher folder:" & vbCrLf & baseDir
    MsgBox message, vbExclamation, "Y! Games Launcher Update Needed"
End Sub

Sub ShowClientMismatch(requiredHash, localHash)
    Dim message
    If localHash = "" Then
        localHash = "(unable to read local client.jar hash)"
    End If
    message = "This launcher's bundled game files do not match what the website expects." & vbCrLf & vbCrLf & _
        "Expected client hash: " & requiredHash & vbCrLf & _
        "Local client hash: " & localHash & vbCrLf & vbCrLf & _
        "Please download the latest launcher package and run install_launcher.bat again." & vbCrLf & vbCrLf & _
        "Launcher folder:" & vbCrLf & baseDir
    MsgBox message, vbExclamation, "Y! Games Launcher Update Needed"
End Sub

Sub OpenDiagnosticsReport()
    Dim reportPath
    Dim file
    reportPath = fso.BuildPath(launchDir, "launcher_diagnostics.txt")
    Set file = fso.CreateTextFile(reportPath, True)
    file.Write GetAppletViewerDiagnostics()
    file.Close
    shell.Run "notepad.exe " & Quote(reportPath), 1, False
End Sub

Sub WritePolicyFile(policyPath)
    Dim file
    Set file = fso.CreateTextFile(policyPath, True)
    file.WriteLine "grant {"
    file.WriteLine "    permission java.security.AllPermission;"
    file.WriteLine "};"
    file.Close
End Sub

Sub WriteAppletHtml(htmlPath)
    Dim appletCode
    Dim defaultPort
    Dim dictPath
    Dim dictFilePath

    If LCase(game) = "checkers" Then
        appletCode = "y.k.YahooCheckers"
        defaultPort = "11999"
        dictFilePath = fso.BuildPath(appDir, "yog\y\k\us-t4.ldict")
        If room = "" Then
            room = "badger_bridge"
        End If
    Else
        appletCode = "y.po.YahooPool"
        defaultPort = "11998"
        dictFilePath = fso.BuildPath(appDir, "yog\y\po\us-ti.ldict")
        If room = "" Then
            room = "corner_pocket"
        End If
    End If

    If port = "" Then
        port = defaultPort
    End If

    Dim file
    Set file = fso.CreateTextFile(htmlPath, True)
    file.WriteLine "<html>"
    file.WriteLine "  <body>"
    file.WriteLine "    <applet"
    file.WriteLine "      code=""" & appletCode & """"
    file.WriteLine "      archive=""client.jar"""
    file.WriteLine "      codebase=""" & FileUrl(appDir) & """"
    file.WriteLine "      width=""" & HtmlEscape(appletWidth) & """"
    file.WriteLine "      height=""" & HtmlEscape(appletHeight) & """>"
    file.WriteLine "      <param name=""host"" value=""" & HtmlEscape(host) & """>"
    file.WriteLine "      <param name=""port"" value=""" & HtmlEscape(port) & """>"
    file.WriteLine "      <param name=""uselogin"" value=""0"">"
    file.WriteLine "      <param name=""logsentmessages"" value=""0"">"
    file.WriteLine "      <param name=""logreceivedmessages"" value=""0"">"
    file.WriteLine "      <param name=""room"" value=""" & HtmlEscape(room) & """>"
    file.WriteLine "      <param name=""yport"" value=""" & HtmlEscape(room) & """>"
    file.WriteLine "      <param name=""account_mode"" value=""" & HtmlEscape(accountMode) & """>"
    file.WriteLine "      <param name=""launcher_version"" value=""" & HtmlEscape(launcherVersion) & """>"
    file.WriteLine "      <param name=""login_url"" value=""" & HtmlEscape(webBase) & "/applet_login.jsp"">"
    file.WriteLine "      <param name=""register_url"" value=""" & HtmlEscape(webBase) & "/applet_register.jsp"">"
    file.WriteLine "      <param name=""change_password_url"" value=""" & HtmlEscape(webBase) & "/applet_change_password.jsp"">"
    file.WriteLine "      <param name=""ldict_url"" value=""" & HtmlEscape(dictFilePath) & """>"
    If LCase(game) = "checkers" Then
        file.WriteLine "      <param name=""game"" value=""checkers"">"
    Else
        file.WriteLine "      <param name=""path"" value=""ny/servlet/YahooPoolServlet"">"
        file.WriteLine "      <param name=""update"" value=""1"">"
    End If
    file.WriteLine "    </applet>"
    file.WriteLine "  </body>"
    file.WriteLine "</html>"
    file.Close
End Sub

Function FileUrl(path)
    FileUrl = "file:///" & Replace(path, "\", "/")
End Function

Function Quote(value)
    Quote = Chr(34) & value & Chr(34)
End Function

Function HtmlEscape(value)
    value = Replace(value, "&", "&amp;")
    value = Replace(value, """", "&quot;")
    value = Replace(value, "<", "&lt;")
    value = Replace(value, ">", "&gt;")
    HtmlEscape = value
End Function

Function UrlDecode(value)
    Dim i
    Dim result
    result = ""
    i = 1
    Do While i <= Len(value)
        Dim ch
        ch = Mid(value, i, 1)
        If ch = "+" Then
            result = result & " "
        ElseIf ch = "%" And i + 2 <= Len(value) Then
            result = result & Chr(CLng("&H" & Mid(value, i + 1, 2)))
            i = i + 2
        Else
            result = result & ch
        End If
        i = i + 1
    Loop
    UrlDecode = result
End Function

Function CleanFileName(value)
    value = Replace(value, "\", "_")
    value = Replace(value, "/", "_")
    value = Replace(value, ":", "_")
    value = Replace(value, "*", "_")
    value = Replace(value, "?", "_")
    value = Replace(value, """", "_")
    value = Replace(value, "<", "_")
    value = Replace(value, ">", "_")
    value = Replace(value, "|", "_")
    value = Replace(value, " ", "_")
    CleanFileName = value
End Function

Sub EnsureFolder(path)
    If Not fso.FolderExists(path) Then
        fso.CreateFolder path
    End If
End Sub
