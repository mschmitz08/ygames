# Runtime Folder

Place a Java 8 AppletViewer-capable runtime here if you want the launcher package to be fully self-contained.

Expected executable:

- `runtime\bin\appletviewer.exe`

If this folder is empty, the launcher falls back to checking `JAVA_HOME` and then `appletviewer` on the system `PATH`.
