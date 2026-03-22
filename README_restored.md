# ny / newyahoo Restore and Setup Guide

This repository is a restored copy of an old Yahoo Games clone. It is not a modern turnkey application, but it can be run locally with the right Java, Tomcat, and MySQL setup.

This guide is meant to be practical:

- how to set up the server side
- how to deploy the two webapps
- how to build the client jar
- how to launch the applets locally for testing
- what to check first when something breaks

## What This Project Contains

- `ny`: the JSP/web frontend
- `newyahoo`: the backend startup webapp, socket listeners, and applet/client code
- `lib`: external jars used by the project
- `database_creation.sql`: main schema
- `mysql_user_creation.sql`: helper SQL for the DB user

This project is centered around Java applets. Modern browsers do not support them, so local testing should be done with Java 8 `appletviewer`, not a browser plugin.

## Known Working Environment

The restore was tested on:

- Windows 11 64-bit
- Java 8 / JDK 8
- Apache Tomcat 9
- MySQL 8.0

Use these versions if you want the least friction.

## Why These Versions

- Java 8: compatible with the source and includes `appletviewer`
- Tomcat 9: still uses `javax.*`; Tomcat 10+ moved to `jakarta.*`
- MySQL 8.0: works, but needs a couple of compatibility fixes

## Before You Start

Choose two folders and keep them handy in the commands below:

- source root: `<INSTALL_PATH>\ny-master`
- Tomcat root: `<TOMCAT_PATH>\apache-tomcat-9.0.116`

Replace those placeholders with your real paths.

## Server Setup

### 1. Install Java 8

Install JDK 8, not just a JRE.

You need it for:

- compiling if required
- running Tomcat
- using `appletviewer`

### 2. Install Tomcat 9

Extract Tomcat 9 and confirm it starts:

```bat
cd /d <TOMCAT_PATH>\apache-tomcat-9.0.116\bin
startup.bat
```

Then open:

```text
http://localhost:8080/
```

If that page loads, Tomcat is working.

### 3. Install MySQL 8.0

Recommended setup:

- product: Server only
- config type: Development Computer
- port: 3306
- authentication: legacy authentication method
- run as a Windows service

### 4. Create the Database and User

Open MySQL:

```bat
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

Then run:

```sql
CREATE DATABASE newyahoo;
CREATE USER 'newyahoo'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON newyahoo.* TO 'newyahoo'@'localhost';
FLUSH PRIVILEGES;
```

The current code expects:

- database: `newyahoo`
- username: `newyahoo`
- password: `123456`

### 5. Import the Schema

From inside MySQL:

```sql
USE newyahoo;
SOURCE <INSTALL_PATH>/ny-master/database_creation.sql;
```

### 6. Fix the `ids` Table for MySQL 8

The stock SQL uses old zero-date timestamp defaults that MySQL 8 rejects.

If `database_creation.sql` fails on the `ids` table, recreate or alter it so these columns use `NULL DEFAULT NULL` instead of `0000-00-00 00:00:00`:

- `cookie_expires`
- `last_access`

This is a required compatibility fix.

### 7. Fix the `ids.ip` Column Length

Run:

```sql
USE newyahoo;
ALTER TABLE ids MODIFY COLUMN ip VARCHAR(64) NOT NULL DEFAULT '0.0.0.0';
```

Without this change, login/account flows can fail.

### 7.1 Fix the `ids.password` Column Length

Modern password hashes are much longer than the original plaintext field.

Run:

```sql
USE newyahoo;
ALTER TABLE ids MODIFY COLUMN password VARCHAR(255) NOT NULL;
```

Without this change, new registrations or password upgrades can be truncated.

### 8. Verify Required Seed Data

Make sure these tables contain usable rows:

- `games`
- `checkers_rooms`
- `pool_rooms`
- `pool2_rooms`

Example:

```sql
SELECT * FROM games;
SELECT * FROM checkers_rooms;
SELECT * FROM pool_rooms;
SELECT * FROM pool2_rooms;
```

To replace an existing Pool room list with the larger historical Yahoo Pool set, run:

```sql
SOURCE <INSTALL_PATH>/ygames2/sql/replace_pool_rooms_historical.sql;
```

Known working room names:

- Checkers: `badger_bridge`
- Pool: `corner_pocket`

The room keys must match what the backend registers.

## Deploy the Webapps

### 9. Copy the Webapps into Tomcat

Copy:

- `<INSTALL_PATH>\ny-master\ny` to `<TOMCAT_PATH>\apache-tomcat-9.0.116\webapps\ny`
- `<INSTALL_PATH>\ny-master\newyahoo` to `<TOMCAT_PATH>\apache-tomcat-9.0.116\webapps\newyahoo`

### 10. Replace the MySQL JDBC Driver

Use:

- `mysql-connector-j-8.0.33.jar`

Do not leave the old MySQL jar in place if both exist.

At minimum, put the new jar in:

- `<INSTALL_PATH>\ny-master\lib`
- `<TOMCAT_PATH>\apache-tomcat-9.0.116\lib`

If needed during troubleshooting, also copy it into:

- `...\webapps\ny\WEB-INF\lib`
- `...\webapps\newyahoo\WEB-INF\lib`

### 11. Compile Only If Needed

If the repository already contains matching `.class` files, recompiling may not be necessary.

Recompile if:

- you changed source files
- `WEB-INF\classes` is missing
- deployed behavior does not match the current source

Compile `ny`:

```bat
cd /d <INSTALL_PATH>\ny-master\ny\WEB-INF\src
dir /s /b *.java > sources.txt
javac -cp "<INSTALL_PATH>\ny-master\lib\*;<TOMCAT_PATH>\apache-tomcat-9.0.116\lib\*" -d <INSTALL_PATH>\ny-master\ny\WEB-INF\classes @sources.txt
```

Compile `newyahoo`:

```bat
cd /d <INSTALL_PATH>\ny-master\newyahoo\WEB-INF\src
dir /s /b *.java > sources.txt
javac -cp "<INSTALL_PATH>\ny-master\lib\*;<TOMCAT_PATH>\apache-tomcat-9.0.116\lib\*" -d <INSTALL_PATH>\ny-master\newyahoo\WEB-INF\classes @sources.txt
```

### 12. Copy Compiled Classes into Tomcat

If you recompiled locally, copy the classes into the deployed webapps.

For `ny`:

```bat
xcopy /E /I /Y <INSTALL_PATH>\ny-master\ny\WEB-INF\classes <TOMCAT_PATH>\apache-tomcat-9.0.116\webapps\ny\WEB-INF\classes
```

For `newyahoo`:

```bat
xcopy /E /I /Y <INSTALL_PATH>\ny-master\newyahoo\WEB-INF\classes <TOMCAT_PATH>\apache-tomcat-9.0.116\webapps\newyahoo\WEB-INF\classes
```

If you changed JSPs, copy those too.

### 13. Build `client.jar`

The applet pages need `client.jar`.

Build it from the `newyahoo` folder:

```bat
cd /d <INSTALL_PATH>\ny-master\newyahoo
del client.jar
jar cf client.jar -C WEB-INF\classes . -C . yog
```

The `yog` folder must be included because it contains required resources such as:

- `.ldict` files
- sound files
- image assets

Quick check:

```bat
jar tf client.jar | findstr /I "yog/"
```

### 14. Restart Tomcat

Basic restart:

```bat
cd /d <TOMCAT_PATH>\apache-tomcat-9.0.116\bin
shutdown.bat
startup.bat
```

If you suspect stale JSP/class cache, do a cleaner restart:

```bat
cd /d <TOMCAT_PATH>\apache-tomcat-9.0.116\bin
shutdown.bat
rmdir /S /Q <TOMCAT_PATH>\apache-tomcat-9.0.116\work\Catalina\localhost\ny
rmdir /S /Q <TOMCAT_PATH>\apache-tomcat-9.0.116\work\Catalina\localhost\newyahoo
startup.bat
```

## Verify the Server

### 15. Check the Web Frontend

Open:

```text
http://localhost:8080/ny/
```

This is the main frontend.

Typical pages:

- `/ny/index.jsp`
- `/ny/login.jsp`
- `/ny/register.jsp`
- `/ny/checkers.jsp`
- `/ny/pool.jsp`
- `/ny/rooms.jsp`

### 16. Understand `/newyahoo`

`/newyahoo` is mostly a backend startup webapp. It is not expected to behave like a normal website.

If:

```text
http://localhost:8080/newyahoo/
```

does not show a normal page, that does not automatically mean deployment failed.

### 17. Verify the Game Ports

The backend should open:

- `11998` for Pool
- `11999` for Checkers
- `12002` for Pool2

Check them:

```bat
netstat -ano | findstr :11998
netstat -ano | findstr :11999
netstat -ano | findstr :12002
```

If these ports are not listening, the applets will not connect.

## Client Setup and Testing

### 18. Use `appletviewer`, Not a Browser

Modern browsers do not run these applets.

Use Java 8 `appletviewer`.

### 19. Security Policy for Local Socket Access

The applets need permission to connect to the local socket servers.

A permissive policy file used during debugging was:

```java
grant {
    permission java.security.AllPermission;
};
```

If you get `AccessControlException`, check the policy file first.

### 20. Launch the Applets Locally

Example for Checkers:

```bat
cd /d <INSTALL_PATH>\ny-master\newyahoo
appletviewer -J-Djava.security.policy=<INSTALL_PATH>\ny-master\ny\pool.policy checkers_test.html
```

Example for Pool:

```bat
cd /d <INSTALL_PATH>\ny-master\newyahoo
appletviewer -J-Djava.security.policy=<INSTALL_PATH>\ny-master\ny\pool.policy pool_test.html
```

Notes:

- test HTML files should use numeric `width` and `height`, not percentages
- `client.jar` and the test HTML should be in the expected local locations

### 20.1 Logging Options

There are two separate kinds of logging in this project:

- `debug` controls the newer file-based debug logging used by `core.DebugLog`
- `logsentmessages` and `logreceivedmessages` control the `>>:` / `<<:` packet trace printed to the command window

Example:

```html
<param name="debug" value="1">
<param name="logsentmessages" value="0">
<param name="logreceivedmessages" value="0">
```

Notes:

- if `debug` is omitted or set to `0`, file logging is off by default
- if `logsentmessages` / `logreceivedmessages` are omitted or set to `0`, network packet tracing is off by default
- the packet trace is independent from `debug`, so you can enable one without enabling the other
- the noisy `>>:` and `<<:` output means packet tracing is on, not necessarily that file debug logging is on

### 21. Local Login Behavior

The restored code supports a local applet login flow so you do not have to hand-edit cookies every time.

Expected flow:

1. launch the applet with `appletviewer`
2. enter credentials
3. applet calls the lightweight login endpoint
4. server returns auth values
5. applet connects to the selected room

If you still see blank usernames, `undefined`, or `Invalid cookie`, check:

- `client.jar` was rebuilt after client-side changes
- the applet HTML has the right `<param>` values
- the account is active
- the login endpoint is reachable

### 22. Account Notes

Legacy account behavior is still rough.

Important detail:

- active accounts should usually have `status = 1`
- newer builds store passwords as salted PBKDF2 hashes, not plaintext
- existing plaintext passwords are upgraded to hashed storage on the next successful login

If a user sees `email confirmation pending`, check whether `status` is still `0`.

## What Is Known to Work

The restore reached a point where all of the following worked locally:

- Tomcat startup
- MySQL 8 connectivity
- deployment of both `ny` and `newyahoo`
- frontend pages under `/ny`
- backend socket listeners
- Checkers room connection and table creation
- Pool and Pool2 connection paths
- local applet testing with `appletviewer`

## What Is Still Legacy

- browser applet support is effectively dead
- `appletviewer` is the realistic local test path
- auth and registration are legacy
- some source appears restored/decompiled
- this should be treated as a historical or experimental project, not production-ready software

## Troubleshooting

### If the Site Loads but Games Do Not Connect

Check, in order:

1. Tomcat is running
2. MySQL is reachable
3. the room tables contain data
4. `newyahoo` started the socket listeners
5. ports `11998`, `11999`, and `12002` are listening
6. `client.jar` exists and includes `yog`

### If Runtime Behavior Does Not Match the Source

This project is very sensitive to stale deployments.

Do all of the following:

1. recompile if needed
2. recopy updated classes and JSPs into Tomcat
3. clear Tomcat `work\Catalina\localhost\...`
4. restart Tomcat

### If MySQL Import Fails

Most likely cause:

- the `ids` table timestamp defaults are not MySQL-8-compatible

### If Login Fails

Check:

- `ids` imported correctly
- `ids.ip` was enlarged to `VARCHAR(64)`
- `ids.password` was enlarged to `VARCHAR(255)`
- the account exists
- the account is active with `status = 1`
- cookies/ycookies are being generated and returned

### If Room Lists Are Empty

Check:

- `games`
- `checkers_rooms`
- `pool_rooms`
- `pool2_rooms`

### If `/newyahoo` Looks Broken in a Browser

That can be normal. It mainly exists to initialize backend services.

## Quick Checklist

If you just want the short version:

1. Install Java 8, Tomcat 9, and MySQL 8.0.
2. Create the `newyahoo` database and user.
3. Import `database_creation.sql`.
4. Fix the `ids` timestamp defaults if MySQL 8 rejects them.
5. Alter `ids.ip` to `VARCHAR(64)`.
6. Alter `ids.password` to `VARCHAR(255)`.
7. Verify room tables contain data.
8. Copy `ny` and `newyahoo` into Tomcat `webapps`.
9. Replace the old MySQL JDBC jar with `mysql-connector-j-8.0.33.jar`.
10. Recompile only if needed.
11. Build `client.jar`.
12. Restart Tomcat.
13. Confirm `/ny/` loads and the backend ports are listening.
14. Use `appletviewer` to test the client locally.

## Final Notes

This repo was not a clean turnkey project. It had to be brought back piece by piece. The patched source in this repository is enough to get surprisingly far, but setup still requires some manual compatibility work and old-school deployment steps.

If you keep the exact environment described above, the restore process is much easier to reproduce.
