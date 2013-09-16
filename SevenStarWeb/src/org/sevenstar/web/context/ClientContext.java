package org.sevenstar.web.context;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class ClientContext {

	private String protocol;

	private String length;

	private String method;

	private String clientName;

	private String clientIp;

	private String clientSystemInfo;

	private String clientLanguage;

	private String clientEncoding;

	private String clientCharset;

	private String clientAccept;

	private String requestURI;

	private String requestURL;

	private String serverName;

	private int serverPort;

	private String headInfo;

	private Map cookieMap;

	private List headInfoExceptName;

	private ClientContext() {
		// pass
	}

	public ClientContext(HttpServletRequest request) {
		initialize(request);
	}

	public String toString() {
		return "clientName:[" + clientName + "],clientIp:[" + clientIp
				+ "],clientSystemInfo:[" + clientSystemInfo
				+ "],clientLanguage:[" + clientLanguage + "],protocol:["
				+ protocol + "],length:[" + length + "],method:[" + method
				+ "],clientEncoding:[" + clientEncoding + "],"
				+ "clientCharset[" + clientCharset + "]," + "clientAccept["
				+ clientAccept + "]," + "requestURI:[" + requestURI + "],"
				+ "requestURL:[" + requestURL + "]," + "serverName:["
				+ serverName + "]," + "serverPort:[" + serverPort + "],"
				+ "headInfo:[" + headInfo + "]";
	}

	private void initialize(HttpServletRequest request) {
		initHeadinfoExceptName();
		this.clientIp = getIpAddr(request);
		this.protocol = request.getProtocol();
		this.method = request.getMethod();
		this.clientName = request.getRemoteHost();
		this.clientSystemInfo = request.getHeader("User-Agent");
		this.clientLanguage = request.getHeader("accept-language");
		this.clientEncoding = request.getHeader("accept-encoding");
		this.clientCharset = request.getHeader("accept-charset");
		this.clientAccept = request.getHeader("accept");
		this.length = request.getHeader("Content-Length");
		this.requestURI = request.getRequestURI();
		if (request.getRequestURL() != null
				|| "".equals(request.getRequestURL())) {
			this.requestURL = String.valueOf(request.getRequestURL());
		}
		this.serverName = request.getServerName();
		this.serverPort = request.getServerPort();
		getHeadInfoStr(request);
		getAllCookieInfo(request);
	}

	private void initHeadinfoExceptName() {
		headInfoExceptName = new ArrayList();
		headInfoExceptName.add("user-agent");
		headInfoExceptName.add("accept-language");
		headInfoExceptName.add("accept-encoding");
		headInfoExceptName.add("accept-charset");
		headInfoExceptName.add("accept");
		headInfoExceptName.add("content-length");
	}

	private String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip != null && ip.length() > 15) {
			if (ip.indexOf(",") > 0) {
				ip = ip.substring(0, ip.indexOf(","));
				String ipLeval = ip.substring(ip.indexOf(",") + 1, ip.length());
				while ("unknown".equals(ip)) {
					if (ip.indexOf(",") > 0) {
						ip = ipLeval.substring(0, ipLeval.indexOf(","));
					} else {
						ip = ipLeval;
						break;
					}
				}
			}
		}
		return ip;
	}

	private void getAllCookieInfo(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		this.cookieMap = new HashMap();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				this.cookieMap.put(cookie.getName(), cookie.getValue());
			}
		}
	}

	private void getHeadInfoStr(HttpServletRequest request) {
		Enumeration em = request.getHeaderNames();
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		while (em.hasMoreElements()) {
			String name = (String) em.nextElement();
			if (headInfoExceptName.contains(name.toLowerCase()))
				continue;
			sb.append("[");
			sb.append(name + "=" + request.getHeader(name));
			sb.append("]");
		}
		sb.append("}");
		this.headInfo = sb.toString();
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getClientSystemInfo() {
		return clientSystemInfo;
	}

	public void setClientSystemInfo(String clientSystemInfo) {
		this.clientSystemInfo = clientSystemInfo;
	}

	public String getClientLanguage() {
		return clientLanguage;
	}

	public void setClientLanguage(String clientLanguage) {
		this.clientLanguage = clientLanguage;
	}

	public String getClientEncoding() {
		return clientEncoding;
	}

	public void setClientEncoding(String clientEncoding) {
		this.clientEncoding = clientEncoding;
	}

	public String getClientCharset() {
		return clientCharset;
	}

	public void setClientCharset(String clientCharset) {
		this.clientCharset = clientCharset;
	}

	public String getClientAccept() {
		return clientAccept;
	}

	public void setClientAccept(String clientAccept) {
		this.clientAccept = clientAccept;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getHeadInfo() {
		return headInfo;
	}

	public void setHeadInfo(String headInfo) {
		this.headInfo = headInfo;
	}

	public Map getCookieMap() {
		return cookieMap;
	}

	public void setCookieMap(Map cookieMap) {
		this.cookieMap = cookieMap;
	}

	public java.util.List getHeadNotLogName() {
		return headInfoExceptName;
	}

	public void setHeadNotLogName(java.util.List headInfoExceptName) {
		this.headInfoExceptName = headInfoExceptName;
	}

}
