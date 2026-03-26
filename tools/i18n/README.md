# Localization Pipeline

This folder is the working area for large-scale language support.

`LdictTool.java` converts the legacy Yahoo `.ldict` files into editable UTF-8 TSV files and back again.

Build the tool:

```bat
cd /d C:\Users\mschm\Desktop\ygames2\tools\i18n
javac LdictTool.java
```

Export the shipped English dictionaries:

```bat
java LdictTool export C:\Users\mschm\Desktop\ygames2\newyahoo\yog\y\k\us-t4.ldict C:\Users\mschm\Desktop\ygames2\tools\i18n\checkers-us.tsv
java LdictTool export C:\Users\mschm\Desktop\ygames2\newyahoo\yog\y\po\us-ti.ldict C:\Users\mschm\Desktop\ygames2\tools\i18n\pool-us.tsv
```

Build a translated dictionary after editing the TSV:

```bat
java LdictTool build C:\Users\mschm\Desktop\ygames2\tools\i18n\checkers-ja.tsv C:\Users\mschm\Desktop\ygames2\newyahoo\yog\y\k\ja-t4.ldict
java LdictTool build C:\Users\mschm\Desktop\ygames2\tools\i18n\pool-ja.tsv C:\Users\mschm\Desktop\ygames2\newyahoo\yog\y\po\ja-ti.ldict
```

Notes:

- The launcher now exposes a top-50 language list from `ny/WEB-INF/i18n/locales.txt`.
- Any locale without a matching dictionary still falls back to `us`.
- The same generated `.ldict` files should also be copied into the launcher staging tree before packaging the launcher ZIP.
