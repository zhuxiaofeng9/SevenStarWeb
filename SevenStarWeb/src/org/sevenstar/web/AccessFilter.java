package org.sevenstar.web;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.sevenstar.util.RegexpHelper;

public class AccessFilter implements Filter {

	private static Log LOG = LogFactory.getLog(AccessFilter.class);

	private FilterConfig config;

	private String[] excludeUrls;

	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String method = httpRequest.getMethod();
		if(  !("GET".equalsIgnoreCase(method)  || "POST".equals(method))){
			goErrorPage((HttpServletRequest) request,
					(HttpServletResponse) response, "Illegal method");
		}
		 
		String url = ((HttpServletRequest) request).getRequestURI();
		for (int i = 0; i < excludeUrls.length; i++) {
			if (!excludeUrls[i].startsWith("/")) {
				excludeUrls[i] = "/" + excludeUrls[i];
			}
			if (!excludeUrls[i].startsWith(((HttpServletRequest) request)
					.getContextPath())) {
				excludeUrls[i] = ((HttpServletRequest) request)
						.getContextPath()
						+ excludeUrls[i];
			}
			if (RegexpHelper.isGlobmatches(url, excludeUrls[i])) {
				chain.doFilter(request, response);
				return;
			}
		}
		String queryString = httpRequest.getQueryString();
		if (queryString != null) {
			queryString = queryString.toLowerCase();
			if (queryString.indexOf("nexturl=") != -1) {
				String newString = queryString.substring(queryString
						.indexOf("nexturl=")
						+ "nexturl=".length() );
				if (newString != null) {
					if(newString.startsWith("http://") || newString.startsWith("www")){
						goErrorPage((HttpServletRequest) request,
								(HttpServletResponse) response, "Illegal value");
						return;
					}
					if (newString.indexOf("/") != -1) {
						newString = newString.substring(0, newString
								.indexOf("/"));
					}
					if (newString.indexOf("\\") != -1) {
						newString = newString.substring(0, newString
								.indexOf("\\"));
					}
					if(newString != null && newString.indexOf(".") != -1){
						goErrorPage((HttpServletRequest) request,
								(HttpServletResponse) response, "Illegal value");
						return;
					}
					 
				}
			}
		}
		if (doSQLCheck(queryString) || doScriptCheck(queryString)
				|| doCssCheck(queryString)) {
			goErrorPage((HttpServletRequest) request,
					(HttpServletResponse) response, "Illegal value");
			return;
		}
		Iterator iter = ((HttpServletRequest) request).getParameterMap()
				.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			Object value = ((HttpServletRequest) request).getParameterMap()
					.get(key);
			if (value != null && value instanceof Object[]) {
				if (((Object[]) value).length == 1) {
					String newValue = String.valueOf(((Object[]) value)[0]);
					if(newValue == null || "".equals(newValue)){
						continue;
					}
					if (doSQLCheck(newValue) || doScriptCheck(newValue)
							|| doCssCheck(newValue)) {
						goErrorPage((HttpServletRequest) request,
								(HttpServletResponse) response, "Illegal value");
						return;
					}
					if(key != null && String.valueOf(key).toLowerCase().trim().equals("nexturl")){
 						if(newValue.startsWith("http://") || newValue.startsWith("www")){
							goErrorPage((HttpServletRequest) request,
									(HttpServletResponse) response, "Illegal value");
							return;
						}
						if (newValue.indexOf("/") != -1) {
							newValue = newValue.substring(0, newValue
									.indexOf("/"));
						}
						if (newValue.indexOf("\\") != -1) {
							newValue = newValue.substring(0, newValue
									.indexOf("\\"));
						}
						if(newValue != null && newValue.indexOf(".") != -1){
							goErrorPage((HttpServletRequest) request,
									(HttpServletResponse) response, "Illegal value");
							return;
						}
					}
				}
			}
		}
		chain.doFilter(request, response);
	}

	public void goErrorPage(HttpServletRequest request,
			HttpServletResponse response, String errorMsg) throws IOException {
		if (errorMsg == null || "".equals(errorMsg)) {
			errorMsg = "error";
		}
		StringBuffer sb = new StringBuffer();
		sb
				.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=gbk\">");
		sb
				.append("<script language='javascript'>setTimeout(function goIndex(){ window.location.target='_top'; window.document.location.href='http://zj.ct10000.com' }, 5000); </script>");
		sb
				.append("<table align=center><tr><td><a href='http://zj.ct10000.com' target='_top'>"
						+ errorMsg + "</a></td></tr></table><br>");
		sb
				.append("<table align=center><tr><td><a href='http://zj.ct10000.com' target='_top'>"
						+ request.getRequestURL() + "</a></td></tr></table>");
		for (int i = 0; i < 30; i++) {
			sb
					.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		ServletOutputStream sos = response.getOutputStream();
		sos.write(sb.toString().getBytes("utf-8"));
		try {
			sos.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public boolean doSQLCheck(String str) {
		if (str == null || "".equals(str.trim())) {
			return false;
		}
		return isGlobmatches(
				str,
				"(')|(;)|(--)|(\\*)|(%)|(and\\s+)|(or\\s+)|(select\\s+)|(update\\s+)|(insert\\s+)|(delete\\s+)|(drop\\s+)|(mid\\s+)|(char\\s+)|(truncate\\s+)|(script\\s+)");
	}

	public boolean doScriptCheck(String str) {
		if (str == null || "".equals(str.trim())) {
			return false;
		}
		if (str.indexOf("<") != -1 || str.indexOf("(") != -1
				|| str.indexOf(":") != -1) {
			return true;
		}
		return false;
	}

	public boolean doCssCheck(String str) {
		if (str == null || "".equals(str.trim())) {
			return false;
		}
		return false;
	}

	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		if (config.getInitParameter("excludeUrls") != null
				&& !"".equals(config.getInitParameter("excludeUrls"))) {
			String excludeUrlss = config.getInitParameter("excludeUrls");
			if (excludeUrlss.indexOf(Constants.separator) != -1) {
				excludeUrls = excludeUrlss.split(Constants.separator);
			} else {
				excludeUrls = new String[1];
				excludeUrls[0] = excludeUrlss;
			}
		} else {
			excludeUrls = new String[0];
		}
	}

	private final static boolean isGlobmatches(String str, String patternstr) {
		if (patternstr == null || "".equals(patternstr)) {
			return false;
		}
		GlobCompiler compiler = new GlobCompiler();
		PatternMatcher matcher = new Perl5Matcher();

		Pattern pattern;
		try {
			pattern = compiler.compile(patternstr);
		} catch (MalformedPatternException e) {
			e.printStackTrace();
			return false;
		}
		return matcher.matches(str, pattern);

	}

}
