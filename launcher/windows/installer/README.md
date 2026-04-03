# RetroPlayHub Launcher MSI

This folder contains the first WiX-based installer scaffold for the native RetroPlayHub launcher.

Current scope:

- publishes `RetroPlayHubLauncher.exe`
- publishes `RetroPlayHubUpdater.exe`
- stages the current launcher bundle from `ny/downloads/ygames_launcher_windows`
- generates a WiX file manifest for the staged files
- builds a per-user MSI that installs to `%LocalAppData%\RetroPlayHubLauncher`
- registers the existing `nygames://` protocol for compatibility

Build command:

```powershell
powershell -ExecutionPolicy Bypass -File .\launcher\windows\installer\Build-RetroPlayHubLauncherMsi.ps1
```

To also publish the built MSI into `ny/downloads` for the updater to fetch:

```powershell
powershell -ExecutionPolicy Bypass -File .\launcher\windows\installer\Build-RetroPlayHubLauncherMsi.ps1 -PublishToDownloads
```

Notes:

- This is an installer scaffold, not the final signed release flow yet.
- The updater can already check and repair `client.jar`.
- The updater now looks for `RetroPlayHubLauncher_<version>.msi` and `RetroPlayHubLauncher.msi` under `ny/downloads`.
