// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst


package server.net;

import core.DebugLog;
import core.Initializer;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

import login.Login;
import server.io.YahooConnectionId;
import server.yutils.IgnoredEntry;
import server.yutils.YahooRoom;
import common.utils.ClientJarHash;
import data.MySQLTable;

// Referenced classes of package y.po:
// _cls65, _cls157, _cls162, _cls176,
// _cls56, _cls168, _cls173, _cls37

public abstract class YahooServerConnection extends Thread implements
		YahooSocketHandler {

	public long				a;
	public YahooSocket		yahooSocket;
	public boolean			m;
	Socket					socket;
	protected String		roomVersion;
	protected String		tableVersion;
	protected YahooServer	server;
	boolean					closing;

	public YahooServerConnection(YahooServer server, Socket socket) {
		this(server, socket, YahooServerConnection.class.getName());
	}

	public YahooServerConnection(YahooServer server, Socket socket, String name) {
		super(name);
		a = System.currentTimeMillis();
		m = false;
		this.server = server;
		this.socket = socket;
		closing = false;
		DebugLog.log("YahooServerConnection ctor class=" + getClass().getName() + " socket=" + socket + " remote=" + (socket == null ? "null" : socket.getRemoteSocketAddress()));
		start();
	}

	/**
	 * 
	 */
	public void close() {
		DebugLog.log("YahooServerConnection.close ENTER class=" + getClass().getName() + " closing=" + closing + " socket=" + socket);
		if (closing)
			return;

		closing = true;
		try {
			interrupt();
			if (socket != null) {
				try {
					socket.close();
				}
				catch (IOException e) {
				}
				socket = null;
			}
		}
		finally {
			closing = false;
			DebugLog.log("YahooServerConnection.close EXIT class=" + getClass().getName());
		}
	}

	public void close(YahooSocket sender) {
		DebugLog.log("YahooServerConnection.close(sender) class=" + getClass().getName() + " sender=" + sender + " yahooSocket=" + yahooSocket);
		if (yahooSocket != null) {
			yahooSocket.close();
			yahooSocket = null;
		}
		server.getConnections().remove(this);
		DebugLog.log("YahooServerConnection.close(sender) removed connection activeCount=" + server.getConnections().size());
	}

	private boolean doProcess(YahooConnectionId id,
			DataInputStream datainputstream) {
		try {
			DebugLog.log("YahooServerConnection.doProcess ENTER id=" + id + " state=" + (id == null ? "null" : Integer.toString(id.getState())));
			YahooRoom room = id.getRoom();
			if (id.getState() != 0) {
				byte cmd = datainputstream.readByte();
				DebugLog.log("YahooServerConnection.doProcess existing-state cmd=" + cmd + " char=" + (char)cmd + " room=" + (room == null ? "null" : room.getYport()));
				room.parseData(id, cmd, datainputstream);
			}
			else {
				int firstByte = datainputstream.read();
				DebugLog.log("YahooServerConnection.doProcess login firstByte=" + firstByte);
				id.setCookie(datainputstream.readUTF());
				String ycookie = datainputstream.readUTF();
				id.setYcookie(ycookie);
				DebugLog.log("YahooServerConnection.doProcess login cookie=" + id.getCookie() + " ycookie=" + ycookie);
				id.setAgent(datainputstream.readUTF());
				id.setIntl_code(datainputstream.readUTF());
				DebugLog.log("YahooServerConnection.doProcess login agent=" + id.getAgent() + " intl_code=" + id.getIntl_code());
				if (!id.getCookie().startsWith("id=")) {
					room.alert(id, "Invalid cookie");
					id.close();
					return false;
				}

				MySQLTable ids = server.getTable("ids");

				String idname = id.getCookie().substring(3);
				if (!(Login.isValidName(idname) && Login
						.loginExist(ids, idname))) {
					room.alert(id, "Login invalid or not exist");
					id.close();
					return false;
				}
				if (!Login.isValidCookie(ids, idname, ycookie)) {
					room.alert(id, "Invalid login");
					id.close();
					return false;
				}

				String clientHash = ClientJarHash.extractClientHashFromAgent(id
						.getAgent());
				String cleanAgent = ClientJarHash.stripClientHashFromAgent(id
						.getAgent());
				id.setAgent(cleanAgent);
				String publishedClientHash = Initializer.selfInstance == null ? ""
						: Initializer.selfInstance.getPublishedClientHash();
				if (publishedClientHash != null && publishedClientHash.length() > 0) {
					if (clientHash == null || clientHash.length() == 0) {
						DebugLog.log("YahooServerConnection.doProcess client hash missing id="
								+ idname + " expected=" + publishedClientHash
								+ " agent=" + cleanAgent);
						room.alert(id,
								"Client version does not match the current version. Please refresh the page or reinstall the launcher.");
						id.close();
						return false;
					}
					if (!publishedClientHash.equalsIgnoreCase(clientHash)) {
						DebugLog.log("YahooServerConnection.doProcess client hash mismatch id="
								+ idname + " expected=" + publishedClientHash
								+ " actual=" + clientHash + " agent="
								+ cleanAgent);
						room.alert(id,
								"Client version does not match the current version. Please refresh the page or reinstall the launcher.");
						id.close();
						return false;
					}
				}
				else
					DebugLog.log("YahooServerConnection.doProcess published client hash unavailable; allowing login id="
							+ idname);

				DebugLog.log("YahooServerConnection.doProcess login validated idname=" + idname);
				server.aquireProfileId(id, idname);
				if (id.getProfileId() == null) {
					DebugLog.log("YahooServerConnection.doProcess profile acquisition failed for "
							+ idname + " room=" + (room == null ? "null" : room.getYport()));
					room.alert(id,
							"Unable to load your room profile right now. Please try joining again.");
					id.close();
					return false;
				}

				IgnoredEntry entry = id.getIgnoredEntry();
				if (entry != null && entry.type == IgnoredEntry.BAN) {
					room.alert(id, "Your are banned from the game "
							+ (entry.time == -1 ? "forever" : "until "
									+ new Date(entry.date.getTime()
											+ entry.time * 60 * 1000))
							+ ".\r\nReason: " + entry.reason);
					id.close();
					return false;
				}

				synchronized (id) {
					id.write(0);
					id.writeUTF(idname);
					id.flush();
				}

				DebugLog.log("YahooServerConnection.doProcess calling room.addId for " + idname + " room=" + room.getYport());
				room.addId(id);
				id.setState(1);
				DebugLog.log("YahooServerConnection.doProcess login complete state=1 name=" + idname);
			}
			DebugLog.log("YahooServerConnection.doProcess EXIT true");
			return true;
		}
		catch (SocketException e) {
			DebugLog.log("YahooServerConnection.doProcess SocketException", e);
		}
		catch (EOFException e) {
			DebugLog.log("YahooServerConnection.doProcess EOFException", e);
		}
		catch (Throwable throwable) {
			DebugLog.log("YahooServerConnection.doProcess Throwable", throwable);
			throwable.printStackTrace();
		}
		if (id != null) {
			id.close();
			id = null;
		}
		return false;
	}

	protected abstract MySQLTable getIgnoredsTable();

	public String getRemoteHost(YahooSocket sender) {
		return socket.getInetAddress().getHostAddress();
	}

	public void handleClose(YahooSocket sender, YahooConnectionId id) {
		DebugLog.log("YahooServerConnection.handleClose sender=" + sender + " id=" + id + " room=" + (id == null || id.getRoom() == null ? "null" : id.getRoom().getYport()));
		YahooRoom room = id.getRoom();
		if (room != null)
			room.removeId(id);
	}

	public void handleOpen(YahooSocket sender, String yport,
			YahooConnectionId id) throws IOException {

		DebugLog.log("YahooServerConnection.handleOpen yport=" + yport + " id=" + id);
		YahooRoom room = server.getRoom(yport);
		if (room == null) {
			DebugLog.log("YahooServerConnection.handleOpen room not found yport=" + yport);
			return;
		}

		id.setRoom(room);
		DebugLog.log("YahooServerConnection.handleOpen assigned room index=" + room.getIndex() + " yport=" + room.getYport() + " roomVersion=" + roomVersion + " tableVersion=" + tableVersion);
		synchronized (id) {
			id.writeUTF("GAMES");
			id.writeUTF(roomVersion);
			id.writeUTF(tableVersion);
			id.flush();
		}
		DebugLog.log("YahooServerConnection.handleOpen sent GAMES handshake");
	}

	public void handleProcess(YahooSocket sender, YahooConnectionId id,
			DataInputStream datainputstream, boolean flag) throws IOException {
		DebugLog.log("YahooServerConnection.handleProcess ENTER id=" + id + " flag=" + flag);
		boolean flag1;
		do {
			flag1 = doProcess(id, datainputstream);
			DebugLog.log("YahooServerConnection.handleProcess loop flag1=" + flag1 + " idState=" + (id == null ? "null" : Integer.toString(id.getState())));
		}
		while (flag1 && yahooSocket.isValidCommand(id, flag));
		DebugLog.log("YahooServerConnection.handleProcess EXIT");
	}

	public boolean isClosed(YahooSocket sender) {
		return socket.isClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * server.net.YahooSocketHandler#releaseProfileId(server.io.YahooConnectionId
	 * , java.lang.String)
	 */
	@Override
	public void releaseProfileId(YahooConnectionId id, String name) {
		server.releaseProfileId(id, name);
	}

	@Override
	public void run() {
		DebugLog.log("YahooServerConnection.run ENTER class=" + getClass().getName() + " socket=" + socket);
		try {
			yahooSocket = new YahooSocket(this, socket.getInputStream(), socket
					.getOutputStream(), 65536);
			DebugLog.log("YahooServerConnection.run yahooSocket created class=" + getClass().getName());
			do {
				DebugLog.log("YahooServerConnection.run processMessages loop class=" + getClass().getName() + " closing=" + closing);
				yahooSocket.processMessages();
			}
			while (!closing && yahooSocket != null && !yahooSocket.isClosed());
			DebugLog.log("YahooServerConnection.run loop exited class=" + getClass().getName() + " closing=" + closing + " yahooSocket=" + yahooSocket);
		}
		catch (EOFException e) {
			DebugLog.log("YahooServerConnection.run EOFException", e);
		}
		catch (SocketException e) {
			DebugLog.log("YahooServerConnection.run SocketException", e);
		}
		catch (Throwable throwable) {
			DebugLog.log("YahooServerConnection.run Throwable", throwable);
			throwable.printStackTrace();
		}
		finally {
			DebugLog.log("YahooServerConnection.run FINALLY close()");
			close();
		}
	}

}
