package org.sevenstar.web.interceptor;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.util.BeanHelper;
import org.sevenstar.util.OgnlHelper;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.annotation.SSList;
import org.sevenstar.web.invocation.Invocation;

import com.opensymphony.xwork.ActionContext;

public class AjaxParameterInterceptor implements Interceptor {
	private Log LOG = LogFactory.getLog(ParameterInterceptor.class);

	public Object intercept(Invocation invocation) {
		LOG.debug("interceptor[ParameterInterceptor]:before call  ");
		Action action = invocation.getAction();
		ActionContext actionContext = ActionContext.getContext();
 		Map parameterMap = actionContext.getParameters();
		Iterator iter = parameterMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();

			if (key.indexOf("[") != -1 && key.indexOf("]") != -1) {
				int seq = -1;

				/**
				 * list,取其中元素类型，初始化
				 */

				String listName = key.substring(0, key.indexOf("["));

				try {
					String seqStr = key.substring(key.indexOf("[") + 1, key
							.indexOf("]"));
					seq = Integer.parseInt(seqStr.trim());
				} catch (Exception e) {
					// pass
				}

				if (seq != -1) {
					Field field = getField(action, listName);
					if (field != null && (field.getType().equals(List.class))) {
						SSList listAnnotation = getSSListAnnotation(field);
						if (listAnnotation == null) {
							LOG.error("action[" + action.getClass().getName()
									+ "] field[" + listName + "] 没有添加SSList注释");
							continue;
						}
						List listValue = (List) BeanHelper.getPropertyValue(
								listName, action);
						if (listValue == null) {
							listValue = new ArrayList();
						}
						while (listValue.size() <= seq) {
							listValue.add(null);
						}
						if (listAnnotation != null) {
							String type = listAnnotation.type();
							Object bean = listValue.get(seq);
							if (bean == null) {
								bean = BeanHelper.newInstance(type);
							}
							listValue.set(seq, bean);
							BeanHelper.setPropertyValue(action, listName,
									listValue);
							// listValue.add(seq,bean);
						} else {
							// pass
						}
					}
				}
			}
			if (key.indexOf(".") != -1) {
				// Object[] values =
				// WebContext.getRequest().getParameterValues(key);

				Object[] values = new Object[0];
				if (parameterMap.get(key) != null) {
					if (parameterMap.get(key) instanceof Object[]) {
						values = (Object[]) parameterMap.get(key);
					} else {
						values = new Object[1];
						values[0] = parameterMap.get(key);
					}
				}
				String listName = key.substring(0, key.indexOf("."));
				Field field = getField(action, listName);
				if (field != null && (field.getType().equals(List.class))) {
					SSList listAnnotation = getSSListAnnotation(field);
					if (listAnnotation == null) {
						LOG.error("action[" + action.getClass().getName()
								+ "] field[" + listName + "] 没有添加SSList注释");
						continue;
					}
					List listValue = (List) BeanHelper.getPropertyValue(
							listName, action);
					if (listValue == null) {
						listValue = new ArrayList();
					}
					String type = listAnnotation.type();
					while (listValue.size() < values.length) {
						listValue.add(null);
					}
					BeanHelper.setPropertyValue(action, listName, listValue);
					for (int i = 0; i < values.length; i++) {
						if (listValue.get(i) == null) {
							listValue.set(i, BeanHelper.newInstance(type));
						}
						OgnlHelper.setValue(action, listName + "[" + i + "]"
								+ key.substring(key.indexOf(".")),decode( values[i]));
					}
					continue;
				}
			}
			Object value = parameterMap.get(key);
			 
			try {
				OgnlHelper.setValue(action, key, decode(value));
			} catch (Exception e) {
                /**
                 * 尝试解组
                 */
				if(value != null && value instanceof Object[]){
					Object[] values = (Object[])value;
					if(values.length == 1){
						value = values[0];
						OgnlHelper.setValue(action, key, decode(value));
					}
				}
			}

		}
		Object result = invocation.invoke();
		LOG.debug("interceptor[ParameterInterceptor]:after call  ");
		return result;
	}
	
	private Object decode(Object obj){
		if(obj == null){
			return obj;
		}
		if(obj instanceof Object[]){
			Object[] values = (Object[])obj;
			for(int i=0;i<values.length;i++){
				if(values[i] != null){
					values[i] = decode(values[i] );
				}
			}
			return values;
		}else{
 			try {
				return java.net.URLDecoder.decode(String.valueOf(obj), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
 

	private Field getField(Object object, String name) {
		if (BeanHelper.hasField(object.getClass(), name)) {
			return BeanHelper.getField(object.getClass(), name);
		}
		return null;
	}

	private SSList getSSListAnnotation(Field field) {
		if (field.isAnnotationPresent(SSList.class)) {
			return field.getAnnotation(SSList.class);
		}
		return null;
	}

	private Map paramMap;

	public Map getParamMap() {
		if (paramMap == null) {
			paramMap = new HashMap();
		}

		return paramMap;
	}

	public void setParamMap(Map map) {
		this.paramMap = map;
	}

}

