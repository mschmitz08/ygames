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

    public MySQLTable ids;
    public MySQLTable games;
    public MySQLTable checkers_rooms;
    public MySQLTable pool_rooms;
    public MySQLTable pool2_rooms;

    public ProcessPool processPool;
    public MySQLConnectionPool connectionPool;

    public static int initialPoolLen = 13;

    public Connection[] mySqlConnections;
    public boolean[] using;

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
        pool2_rooms = null;

        connectionPool.destroy();
        connectionPool = null;

        processPool.destroy();
        processPool = null;
    }

    @Override
    public void init() {
        selfInstance = this;

        processPool = new ProcessPool();
        connectionPool = new MySQLConnectionPool(processPool, dbHost, dbPort, dbName, dbUsername, dbPassword);

        mySqlConnections = new Connection[initialPoolLen];
        using = new boolean[initialPoolLen];

        try {
            ids = new MySQLTable(connectionPool, "ids");
            games = new MySQLTable(connectionPool, "games");
            checkers_rooms = new MySQLTable(connectionPool, "checkers_rooms");
            pool_rooms = new MySQLTable(connectionPool, "pool_rooms");
            pool2_rooms = new MySQLTable(connectionPool, "pool2_rooms");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}