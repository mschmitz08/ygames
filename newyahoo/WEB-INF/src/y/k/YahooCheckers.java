// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) deadcode fieldsfirst

package y.k;

import core.DebugLog;
import y.yutils.CustomYahooGamesApplet;

// Referenced classes of package y.k:
// _cls59, _cls48, _cls109

public class YahooCheckers extends CustomYahooGamesApplet {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7425616548007021454L;

	public YahooCheckers() {
		// YahooButton yahooButton = new YahooButton("Botão de teste");
	}

	@Override
	public void initProperties() {
		DebugLog.log("YahooCheckers.initProperties ENTER");
		try {
			DebugLog.log("YahooCheckers codeBase=" + getCodeBase());
			DebugLog.log("YahooCheckers documentBase=" + getDocumentBase());
			DebugLog.log("YahooCheckers param host=" + getParameter("host"));
			DebugLog.log("YahooCheckers param port=" + getParameter("port"));
			DebugLog.log("YahooCheckers param room=" + getParameter("room"));
			DebugLog.log("YahooCheckers param yport=" + getParameter("yport"));
			DebugLog.log("YahooCheckers param cookie=" + getParameter("cookie"));
			DebugLog.log("YahooCheckers param ldict_url=" + getParameter("ldict_url"));
			super.tableSettings = new CheckersTableOptions();
			super.initProperties();
			DebugLog.log("YahooCheckers.initProperties EXIT OK");
		} catch (Throwable t) {
			DebugLog.log("YahooCheckers.initProperties FAILED", t);
			throw t;
		}
	}
}
