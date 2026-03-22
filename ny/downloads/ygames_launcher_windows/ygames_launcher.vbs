Option Explicit

Dim shell
Dim fso
Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

Dim baseDir
baseDir = fso.GetParentFolderName(WScript.ScriptFullName)

Dim appDir
appDir = fso.BuildPath(baseDir, "app\newyahoo")

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

game = "pool"
room = "corner_pocket"
host = "127.0.0.1"
port = "11998"
accountMode = ""
launcherVersion = ""
webBase = "http://127.0.0.1:8080/ny"

ParseArguments

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
command = Quote(appletViewerPath) & " -J-Djava.security.policy=" & Quote(policyPath) & " " & Quote(htmlPath)
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
            End If
        End If
    Next
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

Sub ShowRequirementError()
    Dim message
    message = "This launcher needs Java 8 AppletViewer support before it can start the game." & vbCrLf & vbCrLf & _
        "What the launcher checked:" & vbCrLf & _
        "1. Bundled runtime in the launcher package" & vbCrLf & _
        "2. JAVA_HOME\bin\appletviewer.exe" & vbCrLf & _
        "3. appletviewer on your PATH" & vbCrLf & vbCrLf & _
        "What to do next:" & vbCrLf & _
        "- Install a Java 8 JDK that includes appletviewer, or" & vbCrLf & _
        "- Place an approved bundled Java 8 runtime under the launcher's runtime folder." & vbCrLf & vbCrLf & _
        "Launcher folder:" & vbCrLf & baseDir
    MsgBox message, vbExclamation, "Y! Games Launcher Requirements"
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
    file.WriteLine "      width=""1024"""
    file.WriteLine "      height=""768"">"
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
