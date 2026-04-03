# RetroPlayHub Windows Launcher Migration

This folder holds the native Windows replacement for the current script-based launcher.

Projects:

- `RetroPlayHubLauncher`
  - launch-time responsibilities only
  - parse protocol arguments
  - load settings
  - prepare launch HTML/policy
  - start `appletviewer.exe`

- `RetroPlayHubUpdater`
  - update-time responsibilities only
  - download manifest
  - compare installed hashes
  - download changed files or a new MSI
  - relaunch the launcher after update

The long-term goal is:

- `RetroPlayHubLauncher.msi` installs both executables and the bundled app files
- the installed updater handles future updates
- the script launcher can be retired once feature parity is reached

Current status:

- `RetroPlayHubLauncher` can resolve the bundle, write launch artifacts, discover `appletviewer`, and launch Pool/Checkers.
- `RetroPlayHubUpdater` can poll `launcher_state.jsp`, compare local launcher/client versions, and refresh `client.jar` when that is the only stale piece.
- `launcher/windows/installer` now contains the first WiX MSI scaffold for packaging the native launcher.
