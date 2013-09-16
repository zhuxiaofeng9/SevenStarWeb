package org.sevenstar.web.resource;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.context.WebContext;

public class PropertyResource implements IResource {

	private Map paramMap;

	private static Map resourceMap = new HashMap();

	private static Object LockedObject = new Object();
	
	public static void main(String[] args) throws UnsupportedEncodingException{
		System.out.println(Locale.US);
		ResourceBundle rb = ResourceBundle.getBundle("ApplicationResources_zh_CN");
		String value = rb.getString("name");
		System.out.println(value);
		System.out.println(new String(value.getBytes("utf-8"),"gbk"));
	}

	public String get(String key) {
		String locale = SwebConfigure.getSwebModel().getLocale();
		if (locale == null || "".equals(locale)) {
			locale = WebContext.getRequest().getLocale().toString();
		}
        Map localeMap = (Map)resourceMap.get(locale);
        if(localeMap == null || localeMap.size() == 0){
        	return null;
        }
        Iterator iter = localeMap.keySet().iterator();
        while(iter.hasNext()){
        	 String localeKey = String.valueOf(iter.next());
        	 ResourceBundle rb  = (ResourceBundle)localeMap.get(localeKey);
        	 if(rb.getString(key) != null){
        		 String value = rb.getString(key);
        		 return value;
        	 }
        }
        return null;
	}

	public Map getParamMap() {
		if (paramMap == null) {
			paramMap = new HashMap();
		}
		return paramMap;
	}

	public void setParamMap(Map map) {
		paramMap = map;
		Iterator iter = map.keySet().iterator();
		while (iter.hasNext()) {
			String key = String.valueOf(iter.next());
			String locale = SwebConfigure.getSwebModel().getLocale();
			if (locale == null || "".equals(locale)) {
				locale = WebContext.getRequest().getLocale().toString();
			}
			if (!resourceMap.containsKey(locale)) {
				resourceMap.put(locale, new HashMap());
			}
			Map localMap = (Map) resourceMap.get(locale);
			if (!localMap.containsKey(key)) {
				String value = (String) map.get(key);
				if (locale != null && !"".equals(locale)) {
					if (hasResource(value + "_" + locale)) {
 						ResourceBundle rb = ResourceBundle.getBundle(value
								+ "_" + locale,Locale.US);
						localMap.put(key, rb);
					} else {
						if (hasResource(value)) {
							ResourceBundle rb = ResourceBundle.getBundle(value,Locale.US);
							localMap.put(key, rb);
						}
					}
				}
			}
		}
	}

	private boolean hasResource(String path) {
		try {
			ResourceBundle.getBundle(path);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
