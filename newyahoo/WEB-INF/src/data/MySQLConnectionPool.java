package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

import javax.security.auth.Destroyable;

import process.ProcessPool;
import process.ProcessQueue;

public class MySQLConnectionPool implements Destroyable {

	static int		defaultInitialPoolLen	= 13;

	String			dbHost;
	int				dbPort;
	String			dbName;
	String			dbUserName;
	String			dbPassword;

	private boolean	destroyed				= false;

	Connection[]	mySqlConnections;
	boolean[]		using;
	ProcessPool		processPool;

	public MySQLConnectionPool(int initialPoolLen, ProcessPool processPool,
			String dbHost, int dbPort, String dbName, String dbUserName,
			String dbPassword) {
		this.processPool = processPool;
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbName = dbName;
		this.dbUserName = dbUserName;
		this.dbPassword = dbPassword;

		mySqlConnections = new Connection[initialPoolLen];
		using = new boolean[initialPoolLen];
	}

	public MySQLConnectionPool(ProcessPool processPool, String dbHost,
			int dbPort, String dbName, String dbUserName, String dbPassword) {
		this(defaultInitialPoolLen, processPool, dbHost, dbPort, dbName,
				dbUserName, dbPassword);
	}

	private Connection createNewConnection() {
		boolean success = false;
		for (int counter = 0; counter < 10; counter++) {
			Connection result = null;
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				result = DriverManager.getConnection("jdbc:mysql://" + dbHost
						+ ":" + dbPort + "/" + dbName
						+ "?useSSL=false&serverTimezone=America/Chicago",
						dbUserName, dbPassword);
				success = true;
				return result;
			}
			catch (SQLNonTransientConnectionException e) {
			}
			catch (ClassNotFoundException e) {
				success = true;
				e.printStackTrace();
			}
			catch (SQLException e) {
				success = true;
				e.printStackTrace();
			}
			finally {
				if (!success) {
					synchronized (this) {
						try {
							wait(1000);
						}
						catch (InterruptedException e1) {
						}
					}
				}
			}
		}
		System.err.println("Falha ao conectar!");
		return null;
	}

	private void closeConnection(Connection connection) {
		if (connection == null)
			return;
		try {
			connection.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		for (int i = 0; i < mySqlConnections.length; i++) {
			try {
				if (mySqlConnections[i] != null
						&& !mySqlConnections[i].isClosed()) {
					mySqlConnections[i].close();
					using[i] = false;
					mySqlConnections[i] = null;
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}

		mySqlConnections = null;
		using = null;

		destroyed = true;
	}

	public synchronized Connection getFreeConnection() {
		Connection connection;
		int i;
		for (i = 0; i < mySqlConnections.length; i++) {
			connection = mySqlConnections[i];
			if (connection == null)
				break;
			if (using[i])
				continue;
			try {
				if (connection.isClosed() || !connection.isValid(2)) {
					closeConnection(connection);
					connection = createNewConnection();
					mySqlConnections[i] = connection;
				}
				using[i] = true;
				return connection;
			}
			catch (SQLException e) {
				closeConnection(connection);
				mySqlConnections[i] = null;
				using[i] = false;
				e.printStackTrace();
			}
		}
		if (i == mySqlConnections.length) {
			Connection[] mySqlConnections1 = new Connection[2 * i];
			boolean[] using1 = new boolean[2 * i];
			System.arraycopy(mySqlConnections, 0, mySqlConnections1, 0, i);
			System.arraycopy(using, 0, using1, 0, i);
			mySqlConnections = mySqlConnections1;
			using = using1;
		}
		connection = createNewConnection();
		mySqlConnections[i] = connection;
		using[i] = true;
		return connection;
	}

	public ProcessQueue getProcessQueue() {
		return processPool.getProcessQueue();
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	public synchronized void releaseConnection(Connection connection) {
		for (int i = 0; i < mySqlConnections.length; i++) {
			Connection connection1 = mySqlConnections[i];
			if (connection1 != connection)
				continue;
			using[i] = false;
			return;
		}
		System.err.println("Conex o n o liberada!");
	}

	public synchronized void discardConnection(Connection connection) {
		for (int i = 0; i < mySqlConnections.length; i++) {
			Connection connection1 = mySqlConnections[i];
			if (connection1 != connection)
				continue;
			using[i] = false;
			mySqlConnections[i] = null;
			closeConnection(connection1);
			return;
		}
		closeConnection(connection);
	}

}
