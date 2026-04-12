package core;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServlet;

import process.ProcessPool;
import server.k.YahooCheckersServer;
import server.po.YahooPoolServer;
import server.yutils.YahooRoomHandler;
import common.utils.ClientJarHash;
import data.MySQLConnectionPool;
import data.MySQLTable;

public class Initializer extends HttpServlet implements YahooRoomHandler {

    private static final long serialVersionUID = -4558999460178582604L;
    public static Initializer selfInstance = null;

    public static java.sql.Connection mySqlConnection;
    private static String dbHost = "127.0.0.1";
    private static int dbPort = 3306;
    private static String dbName = "newyahoo";
    private static String dbUserName = "newyahoo";
    private static String dbPassword = "123456";
    private int poolPort = 11998;
    private int checkersPort = 11999;

    public MySQLTable ids;
    public MySQLTable games;

    public MySQLTable pool_rooms;
    public MySQLTable pool_ignoreds;
    public MySQLTable pool_profiles;
    public MySQLTable pool_games;

    public MySQLTable checkers_rooms;
    public MySQLTable checkers_ignoreds;
    public MySQLTable checkers_profiles;
    public MySQLTable checkers_games;

    private Hashtable<String, MySQLTable> tables;
    private ProcessPool processPool;
    private MySQLConnectionPool connectionPool;
    private YahooPoolServer poolServer;
    private YahooCheckersServer checkersServer;
    private String publishedClientJarPath;
    private String publishedClientHash = "";
    private long publishedClientJarLastModified = -1L;
    private long publishedClientJarLength = -1L;

    public int getPoolPort() {
        return poolPort;
    }

    public int getCheckersPort() {
        return checkersPort;
    }

    public YahooPoolServer getPoolServer() {
        return poolServer;
    }

    public YahooCheckersServer getCheckersServer() {
        return checkersServer;
    }

    public synchronized String getPublishedClientHash() {
        File jarFile = resolvePublishedClientJarFile();
        if (jarFile == null || !jarFile.exists()) {
            publishedClientHash = "";
            publishedClientJarLastModified = -1L;
            publishedClientJarLength = -1L;
            return "";
        }

        long lastModified = jarFile.lastModified();
        long length = jarFile.length();
        if (publishedClientHash.length() == 0
                || lastModified != publishedClientJarLastModified
                || length != publishedClientJarLength) {
            publishedClientHash = ClientJarHash.computeFileSha256(jarFile);
            publishedClientJarLastModified = lastModified;
            publishedClientJarLength = length;
            System.out.println("Published client hash "
                    + (publishedClientHash.length() == 0 ? "unavailable"
                            : publishedClientHash)
                    + " from " + jarFile.getPath());
        }

        return publishedClientHash;
    }

    private String readConfig(String name, String defaultValue) {
        String value = getInitParameter(name);
        if ((value == null || value.length() == 0) && getServletContext() != null)
            value = getServletContext().getInitParameter(name);
        if (value == null || value.length() == 0)
            value = System.getProperty(name);
        if (value == null || value.length() == 0)
            return defaultValue;
        return value;
    }

    private int readConfigInt(String name, int defaultValue) {
        String value = readConfig(name, null);
        if (value == null || value.length() == 0)
            return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private File resolvePublishedClientJarFile() {
        if (publishedClientJarPath != null && publishedClientJarPath.length() > 0)
            return new File(publishedClientJarPath);

        File fromWebapps = resolvePublishedClientJarFromWebapps();
        if (fromWebapps != null)
            return fromWebapps;

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && catalinaBase.length() > 0)
            return new File(catalinaBase,
                    "webapps/ny/downloads/ygames_launcher_windows/app/newyahoo/client.jar");

        return null;
    }

    private File resolvePublishedClientJarFromWebapps() {
        if (getServletContext() == null)
            return null;
        String rootPath = getServletContext().getRealPath("/");
        if (rootPath == null || rootPath.length() == 0)
            return null;
        File webappRoot = new File(rootPath);
        File webappsRoot = webappRoot.getParentFile();
        if (webappsRoot == null)
            return null;
        return new File(webappsRoot,
                "ny/downloads/ygames_launcher_windows/app/newyahoo/client.jar");
    }

    @Override
    public void destroy() {
        selfInstance = null;
        try {
            if (connectionPool != null) {
                connectionPool.destroy();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        connectionPool = null;

        try {
            if (processPool != null) {
                processPool.destroy();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        processPool = null;

        ids = null;
        games = null;

        pool_rooms = null;
        pool_ignoreds = null;
        pool_profiles = null;
        pool_games = null;

        checkers_rooms = null;
        checkers_ignoreds = null;
        checkers_profiles = null;
        checkers_games = null;

        if (tables != null) {
            tables.clear();
            tables = null;
        }

        try {
            if (poolServer != null) {
                poolServer.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        poolServer = null;

        try {
            if (checkersServer != null) {
                checkersServer.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        checkersServer = null;

        System.gc();
    }

    @Override
    public MySQLTable getTable(String name) {
        return tables.get(name);
    }

    @Override
    public void init() {
        System.out.println("NEWYAHOO INIT START");
        selfInstance = this;

        try {
            dbHost = readConfig("newyahoo.db.host", dbHost);
            dbPort = readConfigInt("newyahoo.db.port", dbPort);
            dbName = readConfig("newyahoo.db.name", dbName);
            dbUserName = readConfig("newyahoo.db.username", dbUserName);
            dbPassword = readConfig("newyahoo.db.password", dbPassword);
            publishedClientJarPath = readConfig("newyahoo.client.jar.path", "");
            poolPort = readConfigInt("newyahoo.port.pool", poolPort);
            checkersPort = readConfigInt("newyahoo.port.checkers", checkersPort);
            System.out.println("Creating ProcessPool");
            processPool = new ProcessPool();

            System.out.println("Creating MySQLConnectionPool to " + dbHost + ":" + dbPort + "/" + dbName);
            connectionPool = new MySQLConnectionPool(processPool, dbHost, dbPort, dbName, dbUserName, dbPassword);

            System.out.println("Creating table map");
            tables = new Hashtable<String, MySQLTable>();

            System.out.println("Creating ids");
            ids = new MySQLTable(connectionPool, "ids");
            tables.put("ids", ids);

            System.out.println("Creating games");
            games = new MySQLTable(connectionPool, "games");
            tables.put("games", games);

            System.out.println("Creating pool tables");
            pool_rooms = new MySQLTable(connectionPool, "pool_rooms");
            tables.put("pool_rooms", pool_rooms);
            pool_ignoreds = new MySQLTable(connectionPool, "pool_ignoreds");
            tables.put("pool_ignoreds", pool_ignoreds);
            pool_profiles = new MySQLTable(connectionPool, "pool_profiles");
            tables.put("pool_profiles", pool_profiles);
            pool_games = new MySQLTable(connectionPool, "pool_games");
            tables.put("pool_games", pool_games);

            System.out.println("Creating checkers tables");
            checkers_rooms = new MySQLTable(connectionPool, "checkers_rooms");
            tables.put("checkers_rooms", checkers_rooms);
            checkers_ignoreds = new MySQLTable(connectionPool, "checkers_ignoreds");
            tables.put("checkers_ignoreds", checkers_ignoreds);
            checkers_profiles = new MySQLTable(connectionPool, "checkers_profiles");
            tables.put("checkers_profiles", checkers_profiles);
            checkers_games = new MySQLTable(connectionPool, "checkers_games");
            tables.put("checkers_games", checkers_games);

            Vector<String> yports = new Vector<String>();
            ResultSet rs = null;

            try {
                System.out.println("Loading pool room names");
                rs = pool_rooms.getAllValues();
                while (rs.next()) {
                    yports.add(rs.getString("name"));
                }
                System.out.println("Pool room count: " + yports.size());
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    pool_rooms.closeResultSet(rs);
                    rs = null;
                }
            }

            System.out.println("Starting YahooPoolServer on " + poolPort);
            poolServer = new YahooPoolServer(this, poolPort, yports);
            System.out.println("YahooPoolServer created");

            yports.clear();
            try {
                System.out.println("Loading checkers room names");
                rs = checkers_rooms.getAllValues();
                while (rs.next()) {
                    yports.add(rs.getString("name"));
                }
                System.out.println("Checkers room count: " + yports.size());
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    checkers_rooms.closeResultSet(rs);
                    rs = null;
                }
            }

            System.out.println("Starting YahooCheckersServer on " + checkersPort);
            checkersServer = new YahooCheckersServer(this, checkersPort, yports);
            System.out.println("YahooCheckersServer created");

            System.out.println("Pool 2 runtime is retired; skipping Pool 2 server startup");
            getPublishedClientHash();

            System.out.println("NEWYAHOO INIT END");
        } catch (Throwable t) {
            System.out.println("NEWYAHOO INIT FAILED");
            t.printStackTrace();
        }
    }
}
