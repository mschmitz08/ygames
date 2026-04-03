param(
    [Parameter(Mandatory = $true)]
    [string]$SourceRoot,

    [Parameter(Mandatory = $true)]
    [string]$OutputPath
)

$ErrorActionPreference = "Stop"

function New-SafeId {
    param(
        [string]$Prefix,
        [string]$RelativePath
    )

    $clean = $RelativePath -replace '[^A-Za-z0-9_]', '_'
    if ($clean.Length -gt 50) {
        $clean = $clean.Substring(0, 50)
    }

    $sha1 = [System.Security.Cryptography.SHA1]::Create()
    try {
        $hashBytes = $sha1.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($RelativePath))
    }
    finally {
        $sha1.Dispose()
    }

    $hash = [System.BitConverter]::ToString($hashBytes).Replace("-", "").Substring(0, 10)

    return "${Prefix}_${clean}_${hash}"
}

function Write-DirectoryTree {
    param(
        [System.Text.StringBuilder]$Builder,
        [string]$CurrentPath,
        [string]$RelativePath,
        [int]$IndentLevel
    )

    $directories = Get-ChildItem -Path $CurrentPath -Directory | Sort-Object Name
    foreach ($directory in $directories) {
        $childRelativePath = if ([string]::IsNullOrWhiteSpace($RelativePath)) {
            $directory.Name
        } else {
            Join-Path $RelativePath $directory.Name
        }

        $directoryId = New-SafeId -Prefix "DIR" -RelativePath $childRelativePath
        $indent = " " * $IndentLevel
        [void]$Builder.AppendLine("$indent<Directory Id=`"$directoryId`" Name=`"$($directory.Name)`">")
        Write-DirectoryTree -Builder $Builder -CurrentPath $directory.FullName -RelativePath $childRelativePath -IndentLevel ($IndentLevel + 2)
        [void]$Builder.AppendLine("$indent</Directory>")
    }
}

function Write-FileComponents {
    param(
        [System.Text.StringBuilder]$Builder,
        [string]$SourceRoot,
        [string]$RelativePath,
        [string]$DirectoryId
    )

    $currentPath = if ([string]::IsNullOrWhiteSpace($RelativePath)) { $SourceRoot } else { Join-Path $SourceRoot $RelativePath }
    $files = Get-ChildItem -Path $currentPath -File | Sort-Object Name
    foreach ($file in $files) {
        $fileRelativePath = if ([string]::IsNullOrWhiteSpace($RelativePath)) {
            $file.Name
        } else {
            Join-Path $RelativePath $file.Name
        }

        $normalizedRelativePath = $fileRelativePath -replace '\\', '/'
        $componentId = New-SafeId -Prefix "CMP" -RelativePath $normalizedRelativePath
        $fileId = New-SafeId -Prefix "FIL" -RelativePath $normalizedRelativePath

        [void]$Builder.AppendLine("    <Component Id=`"$componentId`" Directory=`"$DirectoryId`" Guid=`"*`">")
        [void]$Builder.AppendLine("      <File Id=`"$fileId`" Source=`"`$(var.LauncherSourceDir)\$($fileRelativePath -replace '/', '\')`" KeyPath=`"yes`" />")
        [void]$Builder.AppendLine("    </Component>")
    }

    $directories = Get-ChildItem -Path $currentPath -Directory | Sort-Object Name
    foreach ($directory in $directories) {
        $childRelativePath = if ([string]::IsNullOrWhiteSpace($RelativePath)) {
            $directory.Name
        } else {
            Join-Path $RelativePath $directory.Name
        }

        $directoryId = New-SafeId -Prefix "DIR" -RelativePath $childRelativePath
        Write-FileComponents -Builder $Builder -SourceRoot $SourceRoot -RelativePath $childRelativePath -DirectoryId $directoryId
    }
}

$sourceRoot = (Resolve-Path $SourceRoot).Path
$builder = [System.Text.StringBuilder]::new()

[void]$builder.AppendLine('<Wix xmlns="http://wixtoolset.org/schemas/v4/wxs">')
[void]$builder.AppendLine('  <Fragment>')
[void]$builder.AppendLine('    <DirectoryRef Id="INSTALLFOLDER">')
Write-DirectoryTree -Builder $builder -CurrentPath $sourceRoot -RelativePath "" -IndentLevel 6
[void]$builder.AppendLine('    </DirectoryRef>')
[void]$builder.AppendLine('  </Fragment>')
[void]$builder.AppendLine('  <Fragment>')
[void]$builder.AppendLine('    <ComponentGroup Id="LauncherFiles">')
Write-FileComponents -Builder $builder -SourceRoot $sourceRoot -RelativePath "" -DirectoryId "INSTALLFOLDER"
[void]$builder.AppendLine('    </ComponentGroup>')
[void]$builder.AppendLine('  </Fragment>')
[void]$builder.AppendLine('</Wix>')

[System.IO.File]::WriteAllText($OutputPath, $builder.ToString(), [System.Text.UTF8Encoding]::new($false))
