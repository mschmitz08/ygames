// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst


package server.net;
import core.DebugLog;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.servlet.http.HttpServletResponse;

import server.io.YahooBufferedOutputStream;
import server.io.YahooConnectionId;
import server.io.YahooInputStream;
import server.io.YahooInputStreamHandler;
import server.io.YahooOutputStream;
import server.yutils.YahooRoom;

import common.io._cls125;
import common.yutils.Translater;
import common.yutils.YahooUtils;

// Referenced classes of package y.po:
// _cls37, _cls104, _cls34, _cls157,
// _cls125, _cls176

public class YahooSocket implements _cls125, YahooInputStreamHandler {
	private static int					counter	= 1;
	public static HttpServletResponse	resp;
	public Vector<YahooConnectionId>	yportList;
	// public Socket socket;
	public YahooInputStream				input;
	public DataInputStream				dataInput;
	public YahooBufferedOutputStream	output;
	public YahooSocketHandler			handler;
	public YahooOutputStream			dataOutput;
	public boolean						closing;
	public boolean						closed;

	public YahooSocket(YahooSocketHandler handler, InputStream in,
			OutputStream out, int l) throws IOException {
		closed = false;
		DebugLog.log("YahooSocket.<init> ENTER handler=" + handler + " in=" + in + " out=" + out + " buffer=" + l);
		// System.out.println("YahooSocket.<init>(" + handler + ", " + in + ", "
		// + out + ", " + l + ")");
		yportList = new Vector<YahooConnectionId>();
		this.handler = handler;
		// this.socket = socket;
		closing = false;
		try {
			input = new YahooInputStream(this, in, l);
			dataInput = new DataInputStream(input);
			output = new YahooBufferedOutputStream(
					new BufferedOutputStream(out), l);
			// output.address = handler.getRemoteHost(this) + "("
			// + counter + ")";
			counter++;
			dataOutput = new YahooOutputStream(output);
			output.yahoo();
			// resp.flushBuffer();
			input.checkProxyHeader();
			int encodeKey = YahooUtils.randomRange(0, 255);
			int decodeKey = YahooUtils.randomRange(0, 255);
			Translater encoder = new Translater(encodeKey);
			Translater decoder = new Translater(decodeKey);
			output.sendAlgorithm(decodeKey, encodeKey);
			DebugLog.log("YahooSocket.<init> sent algorithm decodeKey=" + decodeKey + " encodeKey=" + encodeKey);
			input.setDecoder(decoder);
			output.setEncoder(encoder);
			DebugLog.log("YahooSocket.<init> EXIT OK");
		}
		catch (IOException ioexception) {
			DebugLog.log("YahooSocket.<init> IOException", ioexception);
			handler.close(this);
			throw ioexception;
		}
	}

	public void close() {
		DebugLog.log("YahooSocket.close ENTER closing=" + closing + " closed=" + closed + " yportListSize=" + (yportList == null ? -1 : yportList.size()));
		if (closing || closed)
			return;
		closing = true;
		try {
			closeAllIds();
			if (output != null) {
				output.close();
				output = null;
			}
			if (input != null) {
				input.close();
				input = null;
			}
			if (handler != null) {
				handler.close(this);
				handler = null;
			}
		}
		finally {
			handler = null;
			closing = false;
			closed = true;
			DebugLog.log("YahooSocket.close EXIT");
		}
	}

	public void close(YahooConnectionId id) {
		if (yportList.remove(id))
			handler.handleClose(this, id);
	}

	@SuppressWarnings("unchecked")
	public void closeAllIds() {
		Vector<YahooConnectionId> vector = (Vector<YahooConnectionId>) yportList
				.clone();
		for (int i = 0; i < vector.size(); i++)
			close(vector.elementAt(i));
	}

	YahooConnectionId getYPort(int index) {
		for (int l = 0; l < yportList.size(); l++) {
			YahooConnectionId id = yportList.elementAt(l);
			if (id.getRoom().getIndex() == index)
				return id;
		}

		return null;
	}

	public boolean isClosed() {
		return closed || handler != null && handler.isClosed(this);
	}

	public boolean isValidCommand(YahooConnectionId id, boolean flag)
			throws IOException {
		YahooRoom room = id.getRoom();
		if (room == null)
			return false;
		if (flag)
			return input.isValidIntCommand(room.getIndex());
		return input.isValidIntCommand(room.getIndex());
	}

	public synchronized void processMessages() throws IOException {
		// System.out.println("YahooSocket.processMessages()");
		int k = input.readByte();
		DebugLog.log("YahooSocket.processMessages command=" + k + " char=" + (char)k + " yportListSize=" + yportList.size());
		if (k == 88) // 'X'
		{
			DebugLog.log("YahooSocket command X close");
			close();
		}
		else if (k == 111) // 'o'
		{
			String yport = input.readUTF();
			DebugLog.log("YahooSocket open yport=" + yport);
			YahooConnectionId id = new YahooConnectionId(this, dataOutput,
					output);
			id.setIp(handler.getRemoteHost(this));
			DebugLog.log("YahooSocket new id ip=" + id.getIp());
			yportList.add(id);
			DebugLog.log("YahooSocket yportListSize after add=" + yportList.size());
			handler.handleOpen(this, yport, id);
			DebugLog.log("YahooSocket handleOpen returned room=" + (id.getRoom() == null ? "null" : id.getRoom().getYport()));
		}
		else if (k == 100) // 'd'
		{
			int index = input.readInt();
			YahooConnectionId id = getYPort(index);
			DebugLog.log("YahooSocket command d index=" + index + " id=" + id);
			input.readShortLength();
			handler.handleProcess(this, id, dataInput, false);
			DebugLog.log("YahooSocket command d processed index=" + index);
			if (!input.isExpectedPacketLength())
				throw new IOException("Unexpected packet length");
		}
		else if (k == 101) // 'e'
		{
			int index = input.readInt();
			YahooConnectionId id = getYPort(index);
			DebugLog.log("YahooSocket command e index=" + index + " id=" + id);
			input.readIntLength();
			handler.handleProcess(this, id, dataInput, true);
			DebugLog.log("YahooSocket command e processed index=" + index);
			if (!input.isExpectedPacketLength())
				throw new IOException("Unexpected packet length");
		}
		else {
			DebugLog.log("YahooSocket illegal command=" + k + " char=" + (char)k);
			throw new IOException("Illegal connection proxy command: " + k);
		}
	}

	/**
	 * @param id
	 * @param name
	 */
	public void releaseProfileId(YahooConnectionId id, String name) {
		handler.releaseProfileId(id, name);
	}
}
