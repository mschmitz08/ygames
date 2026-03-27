// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst

package y.ycontrols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import y.controls.ProcessHandler;
import y.yutils.AbstractYahooApplet;

// Referenced classes of package y.po:
// _cls65, _cls72, _cls93, _cls168,
// _cls87, _cls148, _cls173

public class UrlProcessQueue implements Runnable, ProcessHandler {

	private Vector<UrlProcessEntry>	items;
	private boolean					stoped;
	private AbstractYahooApplet		applet;
	private Thread					thisThread;

	public UrlProcessQueue(AbstractYahooApplet _pcls168) {
		items = new Vector<UrlProcessEntry>();
		applet = _pcls168;
		thisThread = new Thread(this, UrlProcessQueue.class.getName());
	}

	public synchronized void add(UrlProcessEntry process) {
		items.add(process);
		notify();
	}

	public void process(int code, Object obj) {
		applet.doError((Throwable) obj);
	}

	@Override
	public void run() {
		while (!stoped) {
			UrlProcessEntry item;
			synchronized (this) {
				while (items.size() == 0 && !stoped)
					try {
						wait();
					}
					catch (InterruptedException _ex) {
					}
				if (stoped)
					break;
				item = items.elementAt(0);
				items.remove(0);
			}
			try {
				URL url;
				if (item.proxy_http) {
					URL url1 = applet.getCodeBase();
					url = new URL(url1.getProtocol(), url1.getHost(), url1
							.getPort(), item.url);
				}
				else {
					url = new URL(item.url);
				}
				URLConnection urlconnection = openConnection(url, item);
				StringBuffer stringbuffer = new StringBuffer();
				InputStream input = null;
				try {
					if (item.url.endsWith(".gz"))
						input = new GZIPInputStream(urlconnection
								.getInputStream());
					else
						input = urlconnection.getInputStream();
					int i;
					while ((i = input.read()) != -1)
						stringbuffer.append((char) i);
				}
				finally {
					if (input != null)
						input.close();
				}
				item.content = new String(stringbuffer);
			}
			catch (IOException ioexception) {
				item.exception = ioexception;
			}
			catch (SecurityException securityexception) {
				if (applet.getParameter("signedcab") != null) {
					String host = "unknown";
					try {
						host = InetAddress.getByName(
								applet.getParameter("host")).toString();
					}
					catch (UnknownHostException _ex) {
					}
					try {
						applet.getAppletContext().showDocument(
								new URL(applet.getDocumentBase()
										.toExternalForm()
										+ "&nosignedcab=yes&exception=yes&ip="
										+ host), "_self");
					}
					catch (MalformedURLException _ex) {
						applet.lblStatus
								.setCaption("Exception 201 loading text: "
										+ securityexception.toString());
					}
				}
				else {
					applet.lblStatus.setCaption("Exception 202 loading text: "
							+ securityexception.toString());
				}
				return;
			}
			catch (Throwable throwable) {
				applet.lblStatus.setCaption("Exception 203 loading text: "
						+ throwable.toString());
				return;
			}
			applet.processor.addProcess(applet, 5, item);
		}
	}

	/**
	 * 
	 */
	public void start() {
		thisThread.start();
	}

	public synchronized void stop() {
		stoped = true;
		notify();
	}

	private URLConnection openConnection(URL url, UrlProcessEntry item)
			throws IOException {
		URL currentUrl = url;
		String method = item.method != null ? item.method : "GET";
		byte[] body = item.body;
		for (int redirects = 0; redirects < 5; redirects++) {
			URLConnection urlconnection = currentUrl.openConnection();
			if (!(urlconnection instanceof HttpURLConnection)) {
				configureConnection(urlconnection, item.contentType, body, method);
				return urlconnection;
			}
			HttpURLConnection http = (HttpURLConnection) urlconnection;
			http.setInstanceFollowRedirects(false);
			configureConnection(http, item.contentType, body, method);
			int responseCode = http.getResponseCode();
			if (!isRedirect(responseCode))
				return http;
			String location = http.getHeaderField("Location");
			http.disconnect();
			if (location == null || location.length() == 0)
				throw new IOException("HTTP redirect without Location");
			currentUrl = new URL(currentUrl, location);
			if (responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
				method = "GET";
				body = null;
			}
		}
		throw new IOException("Too many HTTP redirects");
	}

	private void configureConnection(URLConnection urlconnection,
			String contentType, byte[] body, String method) throws IOException {
		if (urlconnection instanceof HttpURLConnection) {
			HttpURLConnection http = (HttpURLConnection) urlconnection;
			http.setRequestMethod(method);
			http.setUseCaches(false);
		}
		if (body != null && body.length > 0) {
			urlconnection.setDoOutput(true);
			if (contentType != null)
				urlconnection.setRequestProperty("Content-Type", contentType);
			OutputStream output = null;
			try {
				output = urlconnection.getOutputStream();
				output.write(body);
				output.flush();
			}
			finally {
				if (output != null)
					output.close();
			}
		}
	}

	private boolean isRedirect(int responseCode) {
		return responseCode == HttpURLConnection.HTTP_MOVED_PERM
				|| responseCode == HttpURLConnection.HTTP_MOVED_TEMP
				|| responseCode == HttpURLConnection.HTTP_SEE_OTHER
				|| responseCode == 307 || responseCode == 308;
	}
}
