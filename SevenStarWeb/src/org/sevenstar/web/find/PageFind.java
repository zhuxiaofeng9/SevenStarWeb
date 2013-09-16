package org.sevenstar.web.find;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sevenstar.util.BeanHelper;
import org.sevenstar.web.Constants;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.exception.ActionException;

import com.opensymphony.xwork.ActionContext;

/**
 * @author rtm 2008-5-8
 */
public class PageFind implements IActionFind {

	private static Map classMap = new HashMap();

	private static Map nonClassMap = new HashMap();

	private static Map urlMethodMap = new HashMap();

	private Map paramMap;

	private static String PARAMNAME = "package";

	private String methodName;

	/**
	 * 检查,支持两种page类 1:相应package下的同文件名类 2:相应package下的同目录名的类中的同文件名的方法
	 */
	public Action find(String actionurl) {
		String base = (String) (ActionContext.getContext())
				.get(WebContext.BASE);
	/*	if (actionurl.startsWith(base)) {
			actionurl = actionurl.substring(base.length());
		}*/
		if (actionurl.indexOf("?") != -1) {
			actionurl = actionurl.substring(0, actionurl.indexOf("?"));
		}
		Class klass = (Class) classMap.get(actionurl);
		if (klass == null && !nonClassMap.containsKey(actionurl)) {
			String url = actionurl;
			if (url.indexOf(".") != -1
					&& url.indexOf(".") > url.lastIndexOf("/")) {
				/**
				 * 去扩展名
				 */
				url = url.substring(0, url.indexOf("."));
			}
			/**
			 * 去起始 '/'
			 */
			while (url.startsWith("/")) {
				url = url.substring(1);
			}
			String[] paths = url.split("/");
			// url = url.replaceAll("/", ".");
			if (paths.length == 0) {
				paths = new String[1];
				paths[0] = url;
			}
			/**
			 * 寻找页面
			 */
			klass = loadClass(getClassPath(paths), null);
			if (klass == null) {
				/**
				 * 寻找目录
				 */
				if (url.indexOf("/") == -1) {
					/**
					 * 说明根目录下,寻找Root.java类
					 */
					paths[0] = "Root";
				} else {
					String packageUrl = url.substring(0, url.lastIndexOf("/"));
					paths = packageUrl.split("/");
					if (paths.length == 0) {
						paths = new String[1];
						paths[0] = packageUrl;
					}
				}
				/**
				 * 以文件名作为方法
				 */
				methodName = WebContext.getPageName(actionurl);
				klass = loadClass(getClassPath(paths), methodName);
				if (klass == null) {
					if (nonClassMap.size() < 10000) {
						nonClassMap.put(actionurl, null);
					}
					throw new ActionException("hasn't find Action for url["
							+ actionurl + "]");
				} else {
					classMap.put(actionurl, klass);
					urlMethodMap.put(actionurl, WebContext
							.getPageName(actionurl));

				}
			} else {
				urlMethodMap.put(actionurl, "execute");
				methodName = "execute";
			}
			if (klass == null) {
				if (nonClassMap.size() < 10000) {
					nonClassMap.put(actionurl, null);
				}
			} else {
				classMap.put(actionurl, klass);
			}
		} else {
			methodName = (String) urlMethodMap.get(actionurl);
		}
		if (klass == null) {
			throw new ActionException("hasn't find Action for url[" + actionurl
					+ "]");
		}
		Object object = BeanHelper.newInstance(klass);
		if (object instanceof Action) {
			return (Action) object;
		} else {
			throw new ActionException("hasn't find Action for url[" + actionurl
					+ "]");
		}
	}

	private static String getClassPath(String[] paths) {
		String classpath = "";
		for (int i = 0; i < paths.length; i++) {
			if (i == 0 && i == paths.length - 1) {
				classpath = paths[i].substring(0, 1).toUpperCase()
						+ paths[i].substring(1);
			} else {
				if (i == paths.length - 1) {
					classpath = classpath + "."
							+ paths[i].substring(0, 1).toUpperCase()
							+ paths[i].substring(1);
				} else {
					if (i == 0) {
						classpath = paths[i].toLowerCase();
					} else {
						classpath = classpath + "." + paths[i].toLowerCase();
					}

				}
			}
		}
		return classpath;
	}

	private Class loadClass(String classpath, String methodName) {
		if (this.paramMap == null) {
			return null;
		}
		String allPackages = (String) this.paramMap.get(PARAMNAME);
		if (allPackages == null || "".equals(allPackages.trim())) {
			return null;
		}
		if (allPackages.indexOf(Constants.separator) != -1) {
			String[] packages = allPackages.split(Constants.separator);
			/**
			 * 寻找到目录下的文件
			 */
			for (int i = 0; i < packages.length; i++) {
				try {
					Class klass = Class.forName(packages[i] + "." + classpath);
					if (methodName == null || "".equals(methodName)) {
						return klass;
					}
					if (BeanHelper.hasMethod(klass, methodName)) {
						return klass;
					}
				} catch (ClassNotFoundException e) {
					// pass
				}
			}
		} else {
			try {
				Class klass = Class.forName(allPackages + "." + classpath);
				if (methodName == null || "".equals(methodName)) {
					return klass;
				}
				if (BeanHelper.hasMethod(klass, methodName)) {
					return klass;
				}
			} catch (ClassNotFoundException e) {
				// pass
			}
		}
		return null;
	}

	public void setParamMap(Map map) {
		this.paramMap = map;
	}

	public String getMethodName() {
		if (this.methodName == null) {
			throw new ActionException(
					"need call find(url) first or hasn't find method");
		}
		return this.methodName;
	}

	public Map getParamMap() {
		if (this.paramMap == null) {
			this.paramMap = new HashMap();
		}
		return this.paramMap;
	}

}
