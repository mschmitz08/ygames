# Deploy Helper

This folder packages the compile, launcher-refresh, and Tomcat redeploy flow into one repeatable script.

## Files

- `redeploy.cmd`
  - Rebuilds `newyahoo`
  - Rebuilds `newyahoo/client.jar`
  - Copies that jar into the launcher staging folder
  - Reads the launcher version from `ny/index.jsp`
  - Rebuilds the versioned launcher ZIP and the generic ZIP
  - Rebuilds `ny`
  - Clears Tomcat `webapps` and `work`
  - Mirrors `ny` and `newyahoo` into Tomcat
  - Starts Tomcat again

- `deploy.local.example.cmd`
  - Template for machine-specific paths

- `deploy.local.cmd`
  - Your local copy of the template
  - This file should contain your actual Tomcat install path

## First-Time Setup

1. Copy `deploy.local.example.cmd` to `deploy.local.cmd`.
2. Edit `deploy.local.cmd`.
3. Set `TOMCAT_ROOT` to your Tomcat folder.

Example:

```cmd
set "TOMCAT_ROOT=C:\Users\mschm\Desktop\apache-tomcat-9.0.116"
```

## Running It

From Command Prompt:

```cmd
cd /d C:\path\to\ygames2\tools\deploy
redeploy.cmd
```

## Notes

- The launcher ZIP filename is version-aware. The script reads the version directly from `ny/index.jsp`.
- If you bump the launcher version in code, the deploy script will automatically produce the matching versioned ZIP.
- `deploy.local.cmd` is intended to stay machine-specific.
