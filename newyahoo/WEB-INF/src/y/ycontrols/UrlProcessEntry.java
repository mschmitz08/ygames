// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst

package y.ycontrols;

import java.io.IOException;

public class UrlProcessEntry {

	public String		content;
	public int			code;
	public Object		obj;
	public String		url;
	public IOException	exception;
	public boolean		proxy_http;
	public String		method;
	public String		contentType;
	public byte[]		body;

	public UrlProcessEntry(String url, int code, Object obj, boolean proxy_http) {
		this.url = url;
		this.obj = obj;
		this.code = code;
		this.proxy_http = proxy_http;
		this.method = "GET";
	}

	public UrlProcessEntry(String url, int code, Object obj, boolean proxy_http,
			String method, String contentType, byte[] body) {
		this(url, code, obj, proxy_http);
		if (method != null && method.length() > 0)
			this.method = method;
		this.contentType = contentType;
		this.body = body;
	}
}
