package core;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;

import process.ProcessPool;
import data.MySQLConnectionPool;
import data.MySQLTable;

public class Initializer extends HttpServlet {

    public static Initializer selfInstance = null;

    private static final long serialVersionUID = -1573278850881282759L;

    private static String dbHost = "localhost";
    private static int dbPort = 3306;
    private static String dbName = "newyahoo";
    private static String dbUsername = "newyahoo";
    private static String dbPassword = "123456";
    private String gameHost = "127.0.0.1";
    private int checkersPort = 11999;
    private int poolPort = 11998;

    public MySQLTable ids;
    public MySQLTable games;
    public MySQLTable checkers_rooms;
    public MySQLTable pool_rooms;

    public ProcessPool processPool;
    public MySQLConnectionPool connectionPool;

    public static int initialPoolLen = 13;

    public Connection[] mySqlConnections;
    public boolean[] using;

    public String getGameHost() {
        return gameHost;
    }

    public int getCheckersPort() {
        return checkersPort;
    }

    public int getPoolPort() {
        return poolPort;
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
        selfInstance = null;

        for (int i = 0; i < mySqlConnections.length; i++) {
            try {
                if (mySqlConnections[i] != null && !mySqlConnections[i].isClosed()) {
                    mySqlConnections[i].close();
                    using[i] = false;
                    mySqlConnections[i] = null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        mySqlConnections = null;
        using = null;

        ids = null;
        games = null;
        checkers_rooms = null;
        pool_rooms = null;

        connectionPool.destroy();
        connectionPool = null;

        processPool.destroy();
        processPool = null;
    }

    @Override
    public void init() {
        selfInstance = this;

        dbHost = readConfig("newyahoo.db.host", dbHost);
        dbPort = readConfigInt("newyahoo.db.port", dbPort);
        dbName = readConfig("newyahoo.db.name", dbName);
        dbUsername = readConfig("newyahoo.db.username", dbUsername);
        dbPassword = readConfig("newyahoo.db.password", dbPassword);
        gameHost = readConfig("newyahoo.host", gameHost);
        checkersPort = readConfigInt("newyahoo.port.checkers", checkersPort);
        poolPort = readConfigInt("newyahoo.port.pool", poolPort);

        processPool = new ProcessPool();
        connectionPool = new MySQLConnectionPool(processPool, dbHost, dbPort, dbName, dbUsername, dbPassword);

        mySqlConnections = new Connection[initialPoolLen];
        using = new boolean[initialPoolLen];

        try {
            ids = new MySQLTable(connectionPool, "ids");
            games = new MySQLTable(connectionPool, "games");
            checkers_rooms = new MySQLTable(connectionPool, "checkers_rooms");
            pool_rooms = new MySQLTable(connectionPool, "pool_rooms");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
