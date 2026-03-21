/**
 * 
 */
package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.security.auth.Destroyable;

import java.sql.SQLNonTransientConnectionException;

import process.ProcessPool;
import process.ProcessQueue;

/**
 * @author saddam
 * 
 */
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
		//System.out.println("Criando nova conex o com o banco jdbc:mysql://"
		//		+ dbHost + ":" + dbPort + "/" + dbName + "; usu rio="
		//		+ dbUserName + "; senha=" + dbPassword + "...");
		boolean success = false;
		for(int counter = 0; counter < 10; counter++) {
			Connection result = null;
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				result = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
        + dbPort + "/" + dbName
        + "?useSSL=false&serverTimezone=America/Chicago",
        dbUserName, dbPassword);
				//System.out.println("Conex o criada com sucesso.");
				success = true;
				return result;
			}
			catch(SQLNonTransientConnectionException e) {
				//System.out.println("Erro durante a conex o (Servidor MySQL est  off-line). Tentando uma nova conex o com o servidor... (Tentativa " + (counter + 1) +")");				
			}
		
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				success = true;
				e.printStackTrace();
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				success = true;
				e.printStackTrace();
			}
			finally {
				if(!success) {
					synchronized(this) {
						try {
							wait(1000);
						}
						catch (InterruptedException e1) {}
					}					
				}
			}
		}
		System.err.println("Falha ao conectar!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.security.auth.Destroyable#destroy()
	 */
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		mySqlConnections = null;
		using = null;

		destroyed = true;
	}

	/**
	 * @return uma nova conex o com o banco
	 */
	public synchronized Connection getFreeConnection() {
		// System.out.println("Procurando por uma conex o livre...");
		Connection connection;
		int i;
		for (i = 0; i < mySqlConnections.length; i++) {
			connection = mySqlConnections[i];
			if (connection == null)
				break;
			if (using[i])
				continue;
			try {
				if (connection.isClosed()) {
					//System.out.println("Conex o ao MySQL esta fechada. Reconectando...");
					connection = createNewConnection();
					mySqlConnections[i] = connection;
				}
				using[i] = true;
				return connection;
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (i == mySqlConnections.length) {
			//System.out.println("Limite de conex  es do pool atingido!");
			//System.out.println("Duplicantdo pool...");
			Connection[] mySqlConnections1 = new Connection[2 * i];
			boolean[] using1 = new boolean[2 * i];
			System.arraycopy(mySqlConnections, 0, mySqlConnections1, 0, i);
			System.arraycopy(using, 0, using1, 0, i);
			mySqlConnections = mySqlConnections1;
			using = using1;
			//System.out.println("Pool duplicado.");
		}
		connection = createNewConnection();
		mySqlConnections[i] = connection;
		using[i] = true;
		// System.out.println("Conex o encontrada.");
		return connection;
	}

	/**
	 * @return uma fila de processos do sistema que tiver com menos elementos a
	 *         processar
	 */
	public ProcessQueue getProcessQueue() {
		return processPool.getProcessQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.security.auth.Destroyable#isDestroyed()
	 */
	public boolean isDestroyed() {
		return destroyed;
	}

	public synchronized void releaseConnection(Connection connection) {
		// System.out.println("Liberando conex o...");
		for (int i = 0; i < mySqlConnections.length; i++) {
			Connection connection1 = mySqlConnections[i];
			if (connection1 != connection)
				continue;
			using[i] = false;
			// System.out.println("Conex o liberada.");
			return;
		}
		System.err.println("Conex o n o liberada!");
	}

}
