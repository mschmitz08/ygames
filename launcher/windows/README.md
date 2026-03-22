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
3. Keep the folder in place after install, because the protocol registration points back to it.

## Runtime expectations

The launcher looks for AppletViewer in this order:

1. `runtime\bin\appletviewer.exe` inside the launcher package
2. `JAVA_HOME\bin\appletviewer.exe`
3. `appletviewer` on the system `PATH`

If none of those are available, the launcher shows a requirements message explaining that a Java 8 AppletViewer-capable runtime is still needed.

## Bundled game files

The package expects:

- `app\newyahoo\client.jar`
- `app\newyahoo\yog\...`

Those are copied into the downloadable package from the repo when the bundle is assembled.

## Notes

- This is a Windows-first launcher package.
- Modern browsers do not run the applet directly; the browser only hands off launch details to this local package.
