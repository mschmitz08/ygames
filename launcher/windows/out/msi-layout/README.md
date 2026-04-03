# Y! Games Windows Launcher

This package gives the website a local handoff target instead of pretending the browser can still run Java applets in-page.

## What it does

- registers the `nygames://` protocol for the current Windows user
- checks for AppletViewer support before launch
- starts Pool or Checkers with the selected room, host, and port
- opens the applet window without asking the player to type commands

## Install

1. Extract the launcher folder somewhere permanent.
2. Run `install_launcher.bat`.
3. The installer copies the launcher into `%LocalAppData%\YGamesLauncher` and registers the protocol there.
4. Re-running the installer refreshes that same stable location.

## Runtime expectations

The launcher looks for AppletViewer in this order:

1. `runtime\bin\appletviewer.exe` inside the launcher package
2. `JAVA_HOME\bin\appletviewer.exe`
3. Standard Java install folders under `Program Files`
4. `appletviewer` on the system `PATH`

If none of those are available, the launcher shows a requirements message explaining that a Java 8 AppletViewer-capable runtime is still needed.

## Window size

The launcher reads `launcher_settings.ini` from the install folder.

You can also open the local settings dialog from the website's `Launcher Settings` button, or pass width/height straight from the website launcher form.

Example:

```ini
width=1400
height=900
```

If you want a larger or smaller game window, edit those values and launch again.

## Bundled game files

The package expects:

- `app\newyahoo\client.jar`
- `app\newyahoo\yog\...`

Those are copied into the downloadable package from the repo when the bundle is assembled.

## Notes

- This is a Windows-first launcher package.
- Modern browsers do not run the applet directly; the browser only hands off launch details to this local package.
