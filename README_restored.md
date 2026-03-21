# ny / newyahoo Local Restore Guide

## Overview

This project is a restored Yahoo Games clone with the goal of making it possible for someone else to get it running locally without having to rediscover all of the setup details from scratch. That means documenting not just the obvious installation steps, but also the small compatibility fixes, database adjustments, deployment details, and troubleshooting discoveries that were necessary along the way.

The source in GitHub should already include the code and configuration changes that were needed during the restore process. The purpose of this README is to explain how to set up the remaining environment on a new machine, how to deploy the project, and what to watch out for if the same legacy issues come up again.

This project contains two main webapps:

* `ny` = the JSP/web frontend
* `newyahoo` = the backend/game server app, including socket listeners and applet/game code

This is a legacy project. It is not a modern turnkey build. Depending on what is already included in the repository, setup may involve a combination of:

* installing the correct Java/Tomcat/MySQL environment
* loading the database schema
* applying database compatibility fixes
* deploying the two webapps into Tomcat
* optionally recompiling Java source if prebuilt `.class` files are not already included
* using `appletviewer` for local testing

At the end of the restore process described here, the project was brought to a state where:

* Tomcat runs locally
* MySQL connects successfully
* the schema loads with a required manual fix
* both `ny` and `newyahoo` deploy successfully
* the frontend loads locally
* the backend game ports can listen locally
* Checkers works far enough to enter a room and create a table
* Pool and Pool2 can be exercised through local test files
* a local login flow exists for applet-based testing

## Quick Start

1. Install **Java 8**, **Tomcat 9**, and **MySQL 8.0**.
2. Create the `newyahoo` database and import `database\_creation.sql`.
3. Fix the `ids` table timestamp defaults if the import fails under MySQL 8.
4. Enlarge the `ids.ip` column to avoid login errors.
5. Copy `ny` and `newyahoo` into Tomcat’s `webapps` folder.
6. Replace the old MySQL JDBC driver with `mysql-connector-j-8.0.33.jar`.
7. If needed, copy the included `WEB-INF\\classes` folders into Tomcat, or recompile from source.
8. Build `client.jar` from `newyahoo`.
9. Restart Tomcat and verify ports `11998`, `11999`, and `12002` are listening.
10. Use `appletviewer` to test Checkers, Pool, or Pool2 locally.

## Current Status

This project is partially restored and locally runnable, but still legacy and rough around the edges.

What is known to work:

* local Tomcat deployment
* local MySQL 8 setup
* frontend pages under `/ny`
* backend startup under `/newyahoo`
* Checkers room connection and table creation
* Pool and Pool2 socket connection paths
* local applet testing via `appletviewer`

What is still legacy / imperfect:

* browser applet support is not realistic in modern browsers
* `appletviewer` is the main reliable local test path
* some pages and flows still assume old-era applet behavior
* some game/resource issues may still exist depending on branch state
* Pool2 required at least one type-casting code fix in `common.po2.Pool`

## Tested Environment

This setup was done on:

* Windows 11 64-bit
* Apache Tomcat 9
* Java 8 / JDK 8
* MySQL Community Server 8.0

Paths used during setup:

* source root: `<INSTALL\_PATH>\\ny-master`
* Tomcat root: `<TOMCAT\_PATH>\\apache-tomcat-9.0.116`

Replace these placeholders with the actual folders on your machine.

## Why These Versions Matter

### Tomcat 9

Use Tomcat 9, not Tomcat 10+.

This codebase uses the old `javax.\*` servlet/JSP APIs, not the newer `jakarta.\*` namespace used by Tomcat 10 and later.

### Java 8

Use Java 8 / JDK 8.

This matters because:

* the code is old and compatible with Java 8
* Java 8 includes `appletviewer`, which is essential for local testing
* modern Java versions may break old applet-era behavior

### MySQL 8.0

MySQL 8 works, but the old schema and code need a few compatibility fixes.

## Step 1: Install Tomcat 9

Install Apache Tomcat 9 for Windows 64-bit.

After extraction, the important directories are:

* `bin`
* `webapps`
* `lib`
* `work`
* `logs`

To start Tomcat:

```bat
cd /d <TOMCAT\_PATH>\\apache-tomcat-9.0.116\\bin
startup.bat
```

To verify Tomcat is running, browse to:

```text
http://localhost:8080/
```

If that page loads, Tomcat itself is working.

## Step 2: Install MySQL 8.0

Use MySQL Community Server 8.0 and the Windows MSI installer.

Recommended installer choices:

* product: Server only
* config type: Development Computer
* port: 3306
* authentication method: Use Legacy Authentication Method
* install as Windows service
* start automatically

## Step 3: Open MySQL

If `mysql` is not on PATH, use the full executable path:

```bat
"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe" -u root -p
```

## Step 4: Create the Database and App User

Inside the MySQL prompt:

```sql
CREATE DATABASE newyahoo;
CREATE USER 'newyahoo'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON newyahoo.\* TO 'newyahoo'@'localhost';
FLUSH PRIVILEGES;
```

The code expects:

* database: `newyahoo`
* username: `newyahoo`
* password: `123456`

## Step 5: Import the Schema

From MySQL:

```sql
USE newyahoo;
SOURCE <INSTALL\_PATH>/ny-master/database\_creation.sql;
```

### Important: the stock SQL does not import cleanly on MySQL 8

One major table, `ids`, fails because the old SQL uses invalid zero timestamp defaults that MySQL 8 rejects.

You will need to recreate or fix the `ids` table so that timestamp fields use `NULL DEFAULT NULL` instead of old zero-date defaults.

The important timestamp fields that needed fixing were:

* `cookie\_expires`
* `last\_access`

If the import partly succeeds but the site later fails around login/account handling, check whether `ids` was created correctly.

## Step 6: Fix `ids.ip` Length

Later in testing, login/account code caused a MySQL error because the `ids.ip` column was too short.

Run:

```sql
USE newyahoo;
ALTER TABLE ids MODIFY COLUMN ip VARCHAR(64) NOT NULL DEFAULT '0.0.0.0';
```

This should be considered a required compatibility fix.

## Step 7: Seed Required Room/Game Data

At minimum, verify these tables contain usable rows:

* `games`
* `checkers\_rooms`
* `pool\_rooms`
* `pool2\_rooms`

Example check:

```sql
SELECT \* FROM checkers\_rooms;
SELECT \* FROM pool\_rooms;
SELECT \* FROM pool2\_rooms;
SELECT \* FROM games;
```

Known working checkers room example:

* `badger\_bridge`

Known working pool room example:

* `corner\_pocket`

Room names matter because the applet/server connection logic must use a room key that exactly matches what the backend registered.

## Step 8: Copy the Webapps into Tomcat

Copy these source folders into Tomcat:

* `<INSTALL\_PATH>\\ny-master\\ny`
-> `<TOMCAT\_PATH>\\apache-tomcat-9.0.116\\webapps\\ny`
* `<INSTALL\_PATH>\\ny-master\\newyahoo`
-> `<TOMCAT\_PATH>\\apache-tomcat-9.0.116\\webapps\\newyahoo`

## Step 9: Replace the Old MySQL JDBC Driver

The original repo included an old MySQL driver:

* `mysql-connector-java-5.1.39-bin.jar`

Replace it with:

* `mysql-connector-j-8.0.33.jar`

Make sure the old driver is removed so both do not coexist.

Place the new jar in at least these locations:

* `<INSTALL\_PATH>\\ny-master\\lib`
* `<TOMCAT\_PATH>\\apache-tomcat-9.0.116\\lib`

For safety during recovery, it was also copied into:

* `...\\webapps\\ny\\WEB-INF\\lib`
* `...\\webapps\\newyahoo\\WEB-INF\\lib`

## Step 10: Compatibility Code Changes

The codebase required JDBC modernization in both projects.

Files that needed updating included:

* `data\\MySQLConnectionPool.java`
* `data\\MySQLTable.java`

The important changes were:

* use the modern driver class:

```java
Class.forName("com.mysql.cj.jdbc.Driver");
```

* update the JDBC URL to include:

```text
?useSSL=false\&serverTimezone=America/Chicago
```

* remove old MySQL-specific exception imports and catches
* replace them with standard JDBC exception handling

These source changes are expected to already be present in the patched source tree.

## Step 11: Compilation May Be Optional

If the repository already includes the correct compiled `.class` files, recompiling may not be necessary.

In that case, deployment can be done by copying the included `WEB-INF\\classes` folders into the Tomcat webapps and rebuilding `client.jar` only if needed.

Recompilation is mainly needed if:

* source files were changed
* `.class` files are missing
* deployed classes do not match the patched source
* runtime behavior does not reflect recent source edits

## Step 12: Optional Compile Steps

### Compile `ny`

```bat
cd /d <INSTALL\_PATH>\\ny-master\\ny\\WEB-INF\\src
dir /s /b \*.java > sources.txt
javac -cp "<INSTALL\_PATH>\\ny-master\\lib\\\*;<TOMCAT\_PATH>\\apache-tomcat-9.0.116\\lib\\\*" -d <INSTALL\_PATH>\\ny-master\\ny\\WEB-INF\\classes @sources.txt
```

### Compile `newyahoo`

```bat
cd /d <INSTALL\_PATH>\\ny-master\\newyahoo\\WEB-INF\\src
dir /s /b \*.java > sources.txt
javac -cp "<INSTALL\_PATH>\\ny-master\\lib\\\*;<TOMCAT\_PATH>\\apache-tomcat-9.0.116\\lib\\\*" -d <INSTALL\_PATH>\\ny-master\\newyahoo\\WEB-INF\\classes @sources.txt
```

## Step 13: Copy Compiled or Included Classes into Tomcat

For `ny`:

```bat
xcopy /E /I /Y <INSTALL\_PATH>\\ny-master\\ny\\WEB-INF\\classes <TOMCAT\_PATH>\\apache-tomcat-9.0.116\\webapps\\ny\\WEB-INF\\classes
```

For `newyahoo`:

```bat
xcopy /E /I /Y <INSTALL\_PATH>\\ny-master\\newyahoo\\WEB-INF\\classes <TOMCAT\_PATH>\\apache-tomcat-9.0.116\\webapps\\newyahoo\\WEB-INF\\classes
```

If JSPs were changed, copy those too.

## Step 14: Restart Tomcat

Normal restart:

```bat
cd /d <TOMCAT\_PATH>\\apache-tomcat-9.0.116\\bin
shutdown.bat
startup.bat
```

Cleaner restart with cache clearing:

```bat
cd /d <TOMCAT\_PATH>\\apache-tomcat-9.0.116\\bin
shutdown.bat
rmdir /S /Q <TOMCAT\_PATH>\\apache-tomcat-9.0.116\\work\\Catalina\\localhost\\ny
rmdir /S /Q <TOMCAT\_PATH>\\apache-tomcat-9.0.116\\work\\Catalina\\localhost\\newyahoo
startup.bat
```

## Step 15: Understand the Two App Contexts

### `/ny`

This is the main frontend you browse to:

```text
http://localhost:8080/ny/
```

It contains pages such as:

* `index.jsp`
* `rooms.jsp`
* `checkers.jsp`
* `pool.jsp`
* `login.jsp`
* `register.jsp`

### `/newyahoo`

This is mainly a backend startup webapp. It is not expected to behave like a normal browseable site.

Visiting:

```text
http://localhost:8080/newyahoo/
```

may return not found, and that does not necessarily mean deployment failed.

Its purpose is largely to start backend services.

## Step 16: Backend Game Ports

The `newyahoo` backend is responsible for opening these game ports:

* `11998` = pool
* `11999` = checkers
* `12002` = pool2

Check them with:

```bat
netstat -ano | findstr :11998
netstat -ano | findstr :11999
netstat -ano | findstr :12002
```

If those ports are not listening, the applets will fail to connect.

If ports are already bound by an old Java/Tomcat process, kill the old PID and restart Tomcat cleanly.

## Step 17: Build `client.jar`

The repo did not include a ready-made `client.jar`, but the compiled client classes did exist.

Build it manually from `newyahoo`:

```bat
cd /d <INSTALL\_PATH>\\ny-master\\newyahoo
del client.jar
jar cf client.jar -C WEB-INF\\classes . -C . yog
```

The `yog` folder must be included because it contains required resources such as:

* `.ldict` files
* sound files
* some image resources

Verify contents:

```bat
jar tf client.jar | findstr /I "yog/"
```

## Step 18: Use `appletviewer` for Local Testing

Modern browsers are not a realistic way to run these applets.

Use Java 8’s `appletviewer`.

Example:

```bat
cd /d <INSTALL\_PATH>\\ny-master\\newyahoo
appletviewer -J-Djava.security.policy=<INSTALL\_PATH>\\ny-master\\ny\\pool.policy checkers\_test.html
```

or:

```bat
cd /d <INSTALL\_PATH>\\ny-master\\newyahoo
appletviewer -J-Djava.security.policy=<INSTALL\_PATH>\\ny-master\\ny\\pool.policy pool\_test.html
```

Applet test HTML files must use numeric width/height values, not percentages.

## Step 19: Security Policy for Local Socket Access

The applets need permission to open sockets to the local game ports.

A permissive policy file used during debugging was:

```java
grant {
    permission java.security.AllPermission;
};
```

If the applet throws `AccessControlException`, the policy file is the first thing to check.

## Step 20: Login / Account Notes

A lot of the old registration flow is legacy and not very usable as-is.

During recovery:

* a manual test account was inserted
* `status = 1` was required for the account to be treated as active
* later, code was adjusted so new accounts could skip “email confirmation pending” by defaulting to active status

If a newly created account says `email confirmation pending`, check whether its `status` is still `0`.

For local dev, active users should typically have:

* `status = 1`

## Step 21: Local Applet Login Behavior

A later improvement added an applet login flow so that `appletviewer` does not require manually editing `cookie` / `ycookie` every time.

The intended flow is:

1. launch the applet through `appletviewer`
2. the applet prompts for credentials
3. the applet hits a lightweight login endpoint
4. the server returns the generated auth values
5. the applet connects to the room

If the applet still sends `undefined` as username or `Invalid cookie`, check:

* whether the modified applet classes were rebuilt into `client.jar`
* whether the test HTML has correct `<param name="...">` syntax
* whether `name`, `cookie`, `ycookie`, and `login\_url` are set the way the patched flow expects

## Step 22: Game-Specific Notes

### Checkers

Checkers is the most restored path and was brought far enough to:

* connect to a room
* create a table

The room key that worked was:

* `badger\_bridge`

### Pool

Pool uses port:

* `11998`

A working room key example was:

* `corner\_pocket`

### Pool2

Pool2 uses port:

* `12002`

Pool2 appears to be a separate second pool implementation rather than just an alias.

It has its own:

* DB tables
* server package
* client package
* listener port

At least one error that came up repeatedly in Pool2 was:

```text
java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.Float
```

This pointed to `common.po2.Pool.getFloatProperty(...)` being too strict. The fix was to make it tolerate numeric values like `Integer` as well as `Float`.

## Troubleshooting

### Stale Deployment

A common issue was that source files were fixed, but Tomcat was still using stale deployed classes or cached JSP compilations.

If runtime behavior does not match the source, do all of the following:

1. recompile if needed
2. recopy files into Tomcat `webapps`
3. clear `work\\Catalina\\localhost\\...`
4. restart Tomcat

### Missing `client.jar`

The repo did not include the jar needed by applet pages. It had to be built manually.

### MySQL 8 schema incompatibilities

The old schema file was not MySQL-8-clean.

### Empty room tables

If room pages look blank, verify the tables actually contain rooms.

### `newyahoo` not browseable

That is expected. It is mainly a backend startup app.

### Browser support

Modern browsers do not natively run this old applet. Use `appletviewer`.

## Suggested Troubleshooting Checklist

If something breaks, check these in order:

### Tomcat

* Does `http://localhost:8080/` load?
* Does `http://localhost:8080/ny/` load?

### MySQL

* Does the `newyahoo` database exist?
* Did `ids` import correctly?
* Was `ids.ip` enlarged?

### Ports

* Are `11998`, `11999`, and `12002` listening?
* Are they listening under the current Tomcat/Java PID?

### Deployment

* Were updated classes copied into the correct Tomcat app?
* Were both `ny` and `newyahoo` deployed?
* Was Tomcat work cache cleared?

### Client

* Was `client.jar` rebuilt after applet class changes?
* Does it include the `yog` folder?
* Is the test HTML in the same folder as `client.jar` when using `appletviewer`?

### Auth

* Is the user active (`status = 1`)?
* Is the applet still sending blank or undefined auth values?
* Is the login endpoint reachable?

## Security / Legacy Warnings

This project is old and was never designed for modern security expectations.

Be aware of all of the following:

* applets are obsolete
* browser plugin support is effectively dead
* local dev may require broad Java security permissions
* passwords and cookie flows are legacy
* the project should be treated as a historical/experimental restoration, not a production-ready service

## Summary

This repo was not a clean turnkey restore. It required:

* modernizing DB connectivity
* fixing old schema assumptions
* seeding missing data
* patching JSPs
* rebuilding client artifacts
* optionally compiling from source
* using `appletviewer` for local testing
* carefully redeploying both webapps
* debugging stale class and socket issues

That said, the codebase is real enough to revive. With the patched source and the setup process described above, it is possible to get surprisingly far locally.

