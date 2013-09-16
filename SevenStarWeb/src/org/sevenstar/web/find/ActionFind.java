package org.sevenstar.web.find;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.sevenstar.util.BeanHelper;
import org.sevenstar.web.Constants;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.exception.ActionException;

/**
 * 在指定目录下以 Class路径形式查找Action eg: url：ManagerAction!execute.do 或者
 * ManagerAction.do 指定package： test.manager classpath：test.manager.ManagerAction
 * 
 * @author rtm
 * 
 */
public class ActionFind implements IActionFind {

	private static Map classMap = new HashMap();

	private static Map nonClassMap = new HashMap();

	private static Map urlMethodMap = new HashMap();

	private Map paramMap;

	private String methodName;

	public Action find(String url) {

		String actionName = "";
		String method = "";
		if (url.indexOf("?") != -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		/**
		 * 取最后一段 ManagerAction!execute.do
		 */
		if (url.indexOf("/") != -1) {
			url = url.substring(url.lastIndexOf("/") + 1);
		}
		Class actionClass = (Class) classMap.get(url);
		if (actionClass == null && !nonClassMap.containsKey(url)) {
			if (url.indexOf("!") != -1) {
				actionName = url.substring(0, url.indexOf("!"));
				method = url.substring(url.indexOf("!") + 1, url
						.lastIndexOf("."));
			} else {
				actionName = url.substring(0, url.lastIndexOf("."));
				method = "execute";
			}
			actionClass = loadClass(actionName);
 			urlMethodMap.put(url, method);
 		} else {
			method = (String) urlMethodMap.get(url);
		}
		if (actionClass == null) {
			if (nonClassMap.size() < 10000) {
 				nonClassMap.put(url, null);
 			}
			throw new ActionException("cann't find action for url[" + url + "]");
		} else {
			Object object = BeanHelper.newInstance(actionClass);
			if (object instanceof Action) {
 				classMap.put(url, actionClass);
 				methodName = method;
				return (Action) object;
			} else {
				if (nonClassMap.size() < 10000) {
 					nonClassMap.put(url, null);
 				}
				throw new ActionException("type error,find ["
						+ actionClass.getName()
						+ "],cann't find action for url[" + url + "]");
			}
		}
	}

	private Class loadClass(String actionName) {
		if (paramMap != null && paramMap.get("package") != null
				&& !"".equals(paramMap.get("package"))) {
			String packages = (String) paramMap.get("package");
			if (packages.indexOf(Constants.separator) != -1) {
				String[] packs = packages.split(Constants.separator);
				for (int i = 0; i < packs.length; i++) {
					Class klass = loadClassByActionName(actionName, packs[i]);
					if (klass != null) {
						return klass;
					}
				}
			} else {
				return loadClassByActionName(actionName, packages);
			}
		}
		return null;
	}

	public static void main(String[] args) {
		ActionFind find = new ActionFind();
		Class klass = find.loadClassByActionName("S_userAction",
				"org/sevenstar");
		System.out.println(klass.getName());
	}

	private Class loadClassByActionName(String actionName, String rootPackage) {
		try {
			if (rootPackage.indexOf(".") != -1) {
				rootPackage = rootPackage.replaceAll("\\.", "/");
			}
			Enumeration pathEnumeration = ActionFind.class.getClassLoader()
					.getResources(rootPackage);
			if (pathEnumeration != null) {
				while (pathEnumeration.hasMoreElements()) {
					URL url = (URL) pathEnumeration.nextElement();
					String path = url.getPath();
					String protocol = url.getProtocol();
					if ("file".equalsIgnoreCase(protocol)) {
						File file = new File(url.getPath());
						Class klass = loadClassByActionNameInFile(actionName,
								file);
						if (klass != null) {
							return klass;
						}
					}
					if ("zip".equalsIgnoreCase(protocol)
							|| "vfszip".equals(protocol)) {
						String warPath = null;
						if (path.lastIndexOf("!/") != -1) {
							warPath = path.substring(0, path.lastIndexOf("!/"));
						} else if (path.lastIndexOf("/WEB-INF") != -1) {
							/**
							 * for jboss5 vfszip
							 */
							warPath = path.substring(0, path
									.lastIndexOf("/WEB-INF"));
						} else if (path.indexOf(".jar") != -1) {
							/**
							 * for jboss5 vfszip
							 */
							warPath = path.substring(0,
									path.indexOf(".jar") + 4);
						} else {
							continue;
						}
						JarFile warFile = new JarFile(new File(warPath));
						Enumeration enumeration = warFile.entries();
						while (enumeration.hasMoreElements()) {
							JarEntry jarEntry = (JarEntry) enumeration
									.nextElement();
							if (jarEntry.getName().startsWith(
									"WEB-INF/classes/")
									&& jarEntry.getName().endsWith(".class")) {
								String classPath = jarEntry.getName();
								classPath = classPath
										.substring("WEB-INF/classes/".length());
								classPath = classPath.replaceAll("\\\\", "/");
								classPath = classPath.replaceAll("/", ".");
								if (classPath.endsWith("." + actionName
										+ ".class")) {
									classPath = classPath.substring(0,
											classPath.lastIndexOf("."));
									Class klass = BeanHelper
											.loadClass(classPath);
									return klass;
								}
							} else {
								if (jarEntry.getName().endsWith(".class")) {
									String classPath = jarEntry.getName();
									classPath = classPath.substring(0,
											classPath.length()
													- ".class".length());
									classPath = classPath.replaceAll("\\\\",
											"/");
									classPath = classPath.replaceAll("/", ".");
									if (classPath.endsWith("." + actionName
											+ ".class")) {
										classPath = classPath.substring(0,
												classPath.lastIndexOf("."));
										Class klass = BeanHelper
												.loadClass(classPath);
										return klass;
									}
								}
							}
						}

					}
					if ("jar".equalsIgnoreCase(protocol)) {
						URL jarUrl = new URL(path);
						path = jarUrl.getPath();
						if (path.endsWith("!/" + rootPackage)) {
							path = path.substring(0, path.lastIndexOf("!/"
									+ rootPackage));
							JarFile jarFile = new JarFile(new File(path));
							Enumeration enumeration = jarFile.entries();
							while (enumeration.hasMoreElements()) {
								JarEntry jarEntry = (JarEntry) enumeration
										.nextElement();
								if (jarEntry.getName().startsWith(
										rootPackage + "/")
										&& jarEntry.getName()
												.endsWith(".class")) {
									String classPath = jarEntry.getName();
									if (classPath.endsWith(".class")) {
										classPath = classPath.substring(0,
												classPath.length()
														- ".class".length());
										classPath = classPath.replaceAll(
												"\\\\", "/");
										classPath = classPath.replaceAll("/",
												".");
										if (classPath
												.endsWith("." + actionName)) {
											Class klass = BeanHelper
													.loadClass(classPath);
											return klass;
										}
									}
								}
							}
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Class loadClassByActionNameInFile(String actionName, File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				Class klass = loadClassByActionNameInFile(actionName, files[i]);
				if (klass != null) {
					return klass;
				}
			}
		} else {
			if (file.getPath().endsWith(".class")) {
				Class klass = loadClassByLocalURL(file.getPath());
				if (klass.getName().endsWith("." + actionName)) {
					return klass;
				}
			}
		}
		return null;
	}

	private Class loadClassByLocalURL(String path) {
		String prefix = File.separator + "WEB-INF" + File.separator + "classes"
				+ File.separator;
		String classPath = path.substring(path.indexOf(prefix)
				+ prefix.length());
		if (File.separator.equals("\\")) {
			classPath = classPath.replaceAll("\\\\", ".");
		} else {
			classPath = classPath.replaceAll(File.separator, ".");
		}
		if (classPath.endsWith(".class")) {
			classPath = classPath.substring(0, classPath.length()
					- ".class".length());
		}
		return BeanHelper.loadClass(classPath);
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
