package org.sevenstar.web.find;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.util.BeanHelper;
import org.sevenstar.util.ResourceHelper;
import org.sevenstar.web.Constants;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.annotation.SSAction;
import org.sevenstar.web.exception.ActionException;

/**
 * @author rtm 2008-5-8
 */
public class AnnotationFind implements IActionFind {

	private static Log LOG = LogFactory.getLog(AnnotationFind.class);

	private static Map classMap = new HashMap();

	private static Map nonClassMap = new HashMap();

	private static Map urlMethodMap = new HashMap();

	private static String PARAMNAME = "package";

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
			String rootPackage = (String) this.getParamMap().get(PARAMNAME);
			if (rootPackage.indexOf(".") != -1) {
				rootPackage = rootPackage.replaceAll("\\.", "/");
			}
			if (rootPackage.indexOf(Constants.separator) != -1) {
				String[] rootPackages = rootPackage.split(Constants.separator);
				for (int i = 0; i < rootPackages.length; i++) {
					actionClass = loadClassByAnnotionName(actionName,
							rootPackages[i]);
					if (actionClass != null) {
						break;
					}
				}
			} else {
				actionClass = loadClassByAnnotionName(actionName, rootPackage);
			}
		//	synchronized (urlMethodMap) {
				urlMethodMap.put(url, method);
		//	}
		} else {
			method = (String) urlMethodMap.get(url);
		}
		if (actionClass == null) {
			if (nonClassMap.size() < 10000) {
			//	synchronized (nonClassMap) {
					nonClassMap.put(url, null);
			//	}
			}
			throw new ActionException("cann't find action for url[" + url + "]");
		} else {
			Object object = BeanHelper.newInstance(actionClass);
			if (object instanceof Action) {
			//	synchronized (classMap) {
					classMap.put(url, actionClass);
			//	}
				methodName = method;
				return (Action) object;
			} else {
				if (nonClassMap.size() < 10000) {
				//	synchronized (nonClassMap) {
						nonClassMap.put(url, null);
				//	}
				}
				throw new ActionException("type error,find ["
						+ actionClass.getName()
						+ "],cann't find action for url[" + url + "]");
			}
		}
	}

	public String getMethodName() {
		if (methodName == null) {
			methodName = "execute";
		}
		return methodName;
	}

	public void setParamMap(Map map) {
		paramMap = map;
	}

	public Map getParamMap() {
		if (this.paramMap == null) {
			this.paramMap = new HashMap();
		}
		return this.paramMap;
	}
	
	private Class loadClassByAnnotionName(String actionName, String rootPackage) {
		List classList = ResourceHelper.findClassList(rootPackage, "*");
		for(int i=0;i<classList.size();i++){
			Class klass = (Class)classList.get(i);
			if (klass
					.isAnnotationPresent(SSAction.class)) {
				SSAction ssaction = (SSAction) klass
						.getAnnotation(SSAction.class);
				if (actionName.equals(ssaction
						.name())) {
					return klass;
				}
			}
		}
		return null;
	}

	

	public static void main(String[] args) throws IOException {
		String name = "test.Name";
		name = name.replaceAll("\\.", "/");
		System.out.println(name);
	}

}
