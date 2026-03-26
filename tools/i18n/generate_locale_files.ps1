$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$javaExe = 'C:\Program Files\Java\jdk1.8.0_202\bin\java.exe'
$toolCp = Join-Path $repoRoot 'tools\i18n'

$localeMap = @{
    'us'    = 'en'
    'es'    = 'es'
    'fr'    = 'fr'
    'de'    = 'de'
    'pt'    = 'pt'
    'it'    = 'it'
    'nl'    = 'nl'
    'sv'    = 'sv'
    'no'    = 'no'
    'da'    = 'da'
    'fi'    = 'fi'
    'pl'    = 'pl'
    'cs'    = 'cs'
    'sk'    = 'sk'
    'ro'    = 'ro'
    'hu'    = 'hu'
    'bg'    = 'bg'
    'el'    = 'el'
    'uk'    = 'uk'
    'ru'    = 'ru'
    'tr'    = 'tr'
    'ar'    = 'ar'
    'he'    = 'iw'
    'fa'    = 'fa'
    'ur'    = 'ur'
    'hi'    = 'hi'
    'bn'    = 'bn'
    'pa'    = 'pa'
    'gu'    = 'gu'
    'mr'    = 'mr'
    'ta'    = 'ta'
    'te'    = 'te'
    'kn'    = 'kn'
    'ml'    = 'ml'
    'or'    = 'or'
    'as'    = 'as'
    'ja'    = 'ja'
    'ko'    = 'ko'
    'zh_cn' = 'zh-CN'
    'zh_tw' = 'zh-TW'
    'th'    = 'th'
    'vi'    = 'vi'
    'id'    = 'id'
    'ms'    = 'ms'
    'tl'    = 'tl'
    'sw'    = 'sw'
    'am'    = 'am'
    'ha'    = 'ha'
    'yo'    = 'yo'
    'my'    = 'my'
}

function Protect-Text([string]$text) {
    if ($null -eq $text) { return '' }
    $text = $text -replace '\\n', 'ZXQNLZXQ'
    $text = $text -replace '\\t', 'ZXQTABZXQ'
    $text = $text -replace '\{0\}', 'ZXQARG0ZXQ'
    return $text
}

function Unprotect-Text([string]$text) {
    if ($null -eq $text) { return '' }
    $text = $text -replace 'ZXQARG0ZXQ', '{0}'
    $text = $text -replace 'ZXQTABZXQ', '\\t'
    $text = $text -replace 'ZXQNLZXQ', '\\n'
    return $text
}

function Invoke-TranslateBatch {
    param(
        [string]$TargetLanguage,
        [string[]]$Texts
    )

    $separator = 'ZXQSEPZXQ'
    $protected = @()
    foreach ($text in $Texts) {
        $protected += (Protect-Text $text)
    }
    $joined = [string]::Join("`n$separator`n", $protected)
    $uri = 'https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&dt=t&tl=' `
        + [uri]::EscapeDataString($TargetLanguage) + '&q=' + [uri]::EscapeDataString($joined)
    $response = Invoke-RestMethod -Uri $uri -TimeoutSec 60
    $translated = [string]$response[0][0][0]
    $parts = $translated -split $separator
    if ($parts.Count -ne $Texts.Count) {
        $parts = @()
        foreach ($text in $Texts) {
            $singleUri = 'https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&dt=t&tl=' `
                + [uri]::EscapeDataString($TargetLanguage) + '&q=' + [uri]::EscapeDataString((Protect-Text $text))
            $singleResponse = Invoke-RestMethod -Uri $singleUri -TimeoutSec 60
            $parts += [string]$singleResponse[0][0][0]
        }
    }
    $results = @()
    foreach ($part in $parts) {
        $results += (Unprotect-Text $part.Trim())
    }
    return ,$results
}

function Read-DictRows([string]$path) {
    $rows = @()
    foreach ($line in Get-Content -Path $path -Encoding UTF8) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith('#')) {
            $rows += [pscustomobject]@{ Raw = $line; Skip = $true }
            continue
        }
        $parts = $line.Split("`t", 3)
        if ($parts.Length -lt 3) {
            $rows += [pscustomobject]@{ Raw = $line; Skip = $true }
            continue
        }
        $rows += [pscustomobject]@{
            Skip = $false
            KeyHex = $parts[0]
            KeyDec = $parts[1]
            Text   = $parts[2]
        }
    }
    return ,$rows
}

function Write-DictRows([string]$path, $rows, [hashtable]$translations) {
    $output = New-Object System.Collections.Generic.List[string]
    foreach ($row in $rows) {
        if ($row.Skip) {
            $output.Add($row.Raw)
            continue
        }
        $text = $translations[$row.Text]
        if ([string]::IsNullOrEmpty($text)) {
            $text = $row.Text
        }
        $output.Add(($row.KeyHex + "`t" + $row.KeyDec + "`t" + $text))
    }
    [System.IO.File]::WriteAllLines($path, $output, [System.Text.Encoding]::UTF8)
}

function Read-UiRows([string]$path) {
    $rows = @()
    foreach ($line in Get-Content -Path $path -Encoding UTF8) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith('#')) {
            continue
        }
        $parts = $line.Split("`t", 3)
        if ($parts.Length -lt 3) {
            continue
        }
        $rows += [pscustomobject]@{
            Locale = $parts[0]
            Key    = $parts[1]
            Text   = $parts[2]
        }
    }
    return ,$rows
}

function Write-UiRows([string]$path, $baseRows, [hashtable]$localeTranslations) {
    $output = New-Object System.Collections.Generic.List[string]
    $output.Add('# locale<TAB>key<TAB>value')
    foreach ($locale in $localeMap.Keys) {
        foreach ($row in $baseRows) {
            $text = $row.Text
            if ($locale -ne 'us') {
                $translatedSet = $localeTranslations[$locale]
                if ($translatedSet -and $translatedSet.ContainsKey($row.Text)) {
                    $text = $translatedSet[$row.Text]
                }
            }
            $output.Add($locale + "`t" + $row.Key + "`t" + $text)
        }
    }
    [System.IO.File]::WriteAllLines($path, $output, [System.Text.Encoding]::UTF8)
}

$checkersUsPath = Join-Path $repoRoot 'tools\i18n\checkers-us.tsv'
$poolUsPath = Join-Path $repoRoot 'tools\i18n\pool-us.tsv'
$uiMessagesPath = Join-Path $repoRoot 'newyahoo\yog\i18n\ui_messages.tsv'
$localesPath = Join-Path $repoRoot 'ny\WEB-INF\i18n\locales.txt'

$checkersRows = Read-DictRows $checkersUsPath
$poolRows = Read-DictRows $poolUsPath
$uiBaseRows = (Read-UiRows $uiMessagesPath | Where-Object { $_.Locale -eq 'us' })

$uniqueTexts = New-Object System.Collections.Generic.List[string]
$seenTexts = @{}
foreach ($row in @($checkersRows + $poolRows)) {
    if ($row.Skip) { continue }
    if (-not $seenTexts.ContainsKey($row.Text)) {
        $seenTexts[$row.Text] = $true
        $uniqueTexts.Add($row.Text)
    }
}
foreach ($row in $uiBaseRows) {
    if (-not $seenTexts.ContainsKey($row.Text)) {
        $seenTexts[$row.Text] = $true
        $uniqueTexts.Add($row.Text)
    }
}

$allLocaleTranslations = @{}
foreach ($locale in $localeMap.Keys) {
    if ($locale -eq 'us') { continue }
    $target = $localeMap[$locale]
    Write-Host "Translating locale $locale ($target)..."
    $translations = @{}
    for ($index = 0; $index -lt $uniqueTexts.Count; $index += 20) {
        $chunk = $uniqueTexts[$index..([Math]::Min($index + 19, $uniqueTexts.Count - 1))]
        $translatedChunk = Invoke-TranslateBatch -TargetLanguage $target -Texts $chunk
        for ($i = 0; $i -lt $chunk.Count; $i++) {
            $translations[$chunk[$i]] = $translatedChunk[$i]
        }
    }
    $allLocaleTranslations[$locale] = $translations

    $checkersOut = Join-Path $repoRoot ("tools\i18n\checkers-" + $locale + ".tsv")
    $poolOut = Join-Path $repoRoot ("tools\i18n\pool-" + $locale + ".tsv")
    Write-DictRows $checkersOut $checkersRows $translations
    Write-DictRows $poolOut $poolRows $translations

    & $javaExe -cp $toolCp LdictTool build $checkersOut (Join-Path $repoRoot ("newyahoo\yog\y\k\" + $locale + "-t4.ldict"))
    & $javaExe -cp $toolCp LdictTool build $poolOut (Join-Path $repoRoot ("newyahoo\yog\y\po\" + $locale + "-ti.ldict"))
}

Write-UiRows $uiMessagesPath $uiBaseRows $allLocaleTranslations

Write-Host "Completed locale generation for" ($localeMap.Keys.Count) "languages."
