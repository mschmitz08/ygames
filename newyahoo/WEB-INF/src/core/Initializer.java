package core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServlet;

import process.ProcessPool;
import server.k.YahooCheckersServer;
import server.po.YahooPoolServer;
import server.po2.YahooPoolServer2;
import server.yutils.YahooRoomHandler;
import data.MySQLConnectionPool;
import data.MySQLTable;

public class Initializer extends HttpServlet implements YahooRoomHandler {

    private static final long serialVersionUID = -4558999460178582604L;

    public static java.sql.Connection mySqlConnection;
    private static String dbHost = "127.0.0.1";
    private static int dbPort = 3306;
    private static String dbName = "newyahoo";
    private static String dbUserName = "newyahoo";
    private static String dbPassword = "123456";
    private int poolPort = 11998;
    private int checkersPort = 11999;
    private int pool2Port = 12002;

    public MySQLTable ids;
    public MySQLTable games;

    public MySQLTable pool_rooms;
    public MySQLTable pool_ignoreds;
    public MySQLTable pool_profiles;
    public MySQLTable pool_games;

    public MySQLTable pool2_rooms;
    public MySQLTable pool2_ignoreds;
    public MySQLTable pool2_profiles;
    public MySQLTable pool2_games;

    public MySQLTable checkers_rooms;
    public MySQLTable checkers_ignoreds;
    public MySQLTable checkers_profiles;
    public MySQLTable checkers_games;

    private Hashtable<String, MySQLTable> tables;
    private ProcessPool processPool;
    private MySQLConnectionPool connectionPool;
    private YahooPoolServer poolServer;
    private YahooPoolServer2 poolServer2;
    private YahooCheckersServer checkersServer;

    public int getPoolPort() {
        return poolPort;
    }

    public int getCheckersPort() {
        return checkersPort;
    }

    public int getPool2Port() {
        return pool2Port;
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

    @Override
    public void destroy() {
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

        pool2_rooms = null;
        pool2_ignoreds = null;
        pool2_profiles = null;
        pool2_games = null;

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
            if (poolServer2 != null) {
                poolServer2.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        poolServer2 = null;

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

        try {
            dbHost = readConfig("newyahoo.db.host", dbHost);
            dbPort = readConfigInt("newyahoo.db.port", dbPort);
            dbName = readConfig("newyahoo.db.name", dbName);
            dbUserName = readConfig("newyahoo.db.username", dbUserName);
            dbPassword = readConfig("newyahoo.db.password", dbPassword);
            poolPort = readConfigInt("newyahoo.port.pool", poolPort);
            checkersPort = readConfigInt("newyahoo.port.checkers", checkersPort);
            pool2Port = readConfigInt("newyahoo.port.pool2", pool2Port);

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

            System.out.println("Creating pool2 tables");
            pool2_rooms = new MySQLTable(connectionPool, "pool2_rooms");
            tables.put("pool2_rooms", pool2_rooms);
            pool2_ignoreds = new MySQLTable(connectionPool, "pool2_ignoreds");
            tables.put("pool2_ignoreds", pool2_ignoreds);
            pool2_profiles = new MySQLTable(connectionPool, "pool2_profiles");
            tables.put("pool2_profiles", pool2_profiles);
            pool2_games = new MySQLTable(connectionPool, "pool2_games");
            tables.put("pool2_games", pool2_games);

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

            yports.clear();
            try {
                System.out.println("Loading pool2 room names");
                rs = pool2_rooms.getAllValues();
                while (rs.next()) {
                    yports.add(rs.getString("name"));
                }
                System.out.println("Pool2 room count: " + yports.size());
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    pool2_rooms.closeResultSet(rs);
                    rs = null;
                }
            }

            System.out.println("Starting YahooPoolServer2 on " + pool2Port);
            poolServer2 = new YahooPoolServer2(this, pool2Port, yports);
            System.out.println("YahooPoolServer2 created");

            System.out.println("NEWYAHOO INIT END");
        } catch (Throwable t) {
            System.out.println("NEWYAHOO INIT FAILED");
            t.printStackTrace();
        }
    }
}
