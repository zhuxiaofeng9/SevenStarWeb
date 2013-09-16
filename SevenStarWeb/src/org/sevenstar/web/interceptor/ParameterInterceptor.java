package org.sevenstar.web.interceptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.util.BeanHelper;
import org.sevenstar.util.OgnlHelper;
import org.sevenstar.util.RegexpHelper;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.annotation.SSList;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.exception.ActionException;
import org.sevenstar.web.invocation.Invocation;
import com.opensymphony.xwork.ActionContext;

/**
 * @author rtm 2008-5-8
 */
public class ParameterInterceptor implements Interceptor {
	private Log LOG = LogFactory.getLog(ParameterInterceptor.class);

	private static String SQLKEY = "(')|(;)|(--)|(\\*)|(%)|(and\\s+)|(or\\s+)|(select\\s+)|(update\\s+)|(insert\\s+)|(delete\\s+)|(drop\\s+)|(mid\\s+)|(char\\s+)|(truncate\\s+)|(script\\s+)";
	private static String SCRIPTKEY = "";
	private static String CSSKEY = "";

	public boolean doScriptCheck(String str) {
		if (str == null || "".equals(str.trim())) {
			return false;
		}

		String newStr = str.toLowerCase();
 
		if (newStr.indexOf("<") != -1 || newStr.indexOf("(") != -1
				|| newStr.indexOf("expression") != -1
				|| newStr.indexOf("onerror") != -1
				|| newStr.indexOf("xss:") != -1
				|| newStr.indexOf("onload") != -1 || newStr.indexOf("\"") != -1
				|| newStr.indexOf("&") != -1 || newStr.indexOf("&lt;") != -1
				|| newStr.indexOf("&#60;") != -1
				|| newStr.indexOf("script") != -1
				|| newStr.indexOf("frame") != -1
				|| newStr.indexOf("document") != -1) {
			return true;
		}

		Pattern p = Pattern
				.compile("on(mouseover|mouseon|mouseout|click|db lclick|blur|focus|change)");
		Matcher m = p.matcher(str);
		return m.find();
	}

	/**
	 * 检查跨站脚本以及SQL注入
	 */
	public Object intercept(Invocation invocation) {
		LOG.debug("interceptor[ParameterInterceptor]:before call  ");
		if (needCheckUrl()) {
			String url = WebContext.getUrl();
			String lowerUrl = url.toLowerCase();
			if (lowerUrl.indexOf("<") != -1 || lowerUrl.indexOf("(") != -1) {
				lowerUrl = lowerUrl.replaceAll("<", ",");
				lowerUrl = lowerUrl.replaceAll("=", ",");
				lowerUrl = lowerUrl.replaceAll("\\(", ",");
				throw new ActionException("Illegal url[" + lowerUrl + "]");
			}
			if (lowerUrl.indexOf("<script") != -1) {
				lowerUrl = lowerUrl.replaceAll("script", "scrigt");
				throw new ActionException("Illegal url[" + lowerUrl + "]");
			}
			if (lowerUrl.indexOf("<iframe") != -1) {
				lowerUrl = lowerUrl.replaceAll("iframe", "ifrane");
				throw new ActionException("Illegal url[" + lowerUrl + "]");
			}
		}
		Action action = invocation.getAction();
		ActionContext actionContext = ActionContext.getContext();
		Map parameterMap = actionContext.getParameters();
		Iterator iter = parameterMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (key != null
					&& (key.indexOf("#") != -1 || key.indexOf("u0023") != -1)) {
				throw new ActionException("Illegal parameter name[" + key + "]");
			}
			if (key.indexOf("[") != -1 && key.indexOf("]") != -1) {
				int seq = -1;

				/**
				 * list,取其中元素类型，初始化
				 */

				String listName = key.substring(0, key.indexOf("["));

				try {
					String seqStr = key.substring(key.indexOf("[") + 1,
							key.indexOf("]"));
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
				checkValue(key, values);
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
								+ key.substring(key.indexOf(".")), values[i]);
					}
					continue;
				}
			}
			Object value = parameterMap.get(key);
			checkValue(key, value);

			try {
				OgnlHelper.setValue(action, key, value);
			} catch (Exception e) {
				e.printStackTrace();
				/**
				 * 尝试解组
				 */
				if (value != null && value instanceof Object[]) {
					Object[] values = (Object[]) value;
					if (values.length == 1) {
						value = values[0];
						OgnlHelper.setValue(action, key, value);
					}
				}
			}

		}
		Object result = invocation.invoke();
		LOG.debug("interceptor[ParameterInterceptor]:after call  ");
		return result;
	}

	private void checkValue(String key, Object value) {
		if (value != null) {
			String url = WebContext.getUrl();
			Iterator iter = this.getParamMap().keySet().iterator();
			while (iter.hasNext()) {
				String pattern = String.valueOf(iter.next());
				if (RegexpHelper.isGlobmatches(url, pattern)) {
					if ("false".equalsIgnoreCase(String.valueOf(this
							.getParamMap().get(pattern)))
							|| "N".equalsIgnoreCase(String.valueOf(this
									.getParamMap().get(pattern)))) {
						return;
					}
				}
			}
			if (value instanceof Object[]) {
				for (int i = 0; i < ((Object[]) value).length; i++) {
					if (((Object[]) value)[i] != null) {
						String newValue = String.valueOf(
								((((Object[]) value))[i])).toLowerCase();

						if (newValue.indexOf("<") != -1
								|| newValue.indexOf("(") != -1) {
							newValue = newValue.replaceAll("<", ",");
							newValue = newValue.replaceAll("=", ",");
							newValue = newValue.replaceAll("\\(", ",");
							throw new ActionException("Illegal key[" + key
									+ "] value[" + newValue + "]");
						}
						if (newValue.indexOf("script") != -1) {
							newValue = newValue.replaceAll("script", "scrigt");
							throw new ActionException("Illegal key[" + key
									+ "] value[" + newValue + "]");
						}
						if (newValue.indexOf("<script") != -1
								&& newValue.indexOf("</script") != -1) {
							newValue = newValue.replaceAll("script", "scrigt");
							throw new ActionException("Illegal key[" + key
									+ "] value[" + newValue + "]");
						}
						if (newValue.indexOf("iframe") != -1) {
							newValue = newValue.replaceAll("iframe", "ifrane");
							throw new ActionException("Illegal key[" + key
									+ "] value[" + newValue + "]");
						}
						if (RegexpHelper.isGlobmatches(newValue, SQLKEY)) {
							throw new ActionException("Illegal key[" + key
									+ "] value[" + newValue + "]");
						}
						if (doScriptCheck(newValue)) {
							throw new ActionException("Illegal key[" + key
									+ "] value[" + newValue + "]");
						}
					}
				}
			} else {
				String newValue = String.valueOf(value).toLowerCase();
				if (newValue.indexOf("<") != -1 && newValue.indexOf("(") != -1) {
					newValue = newValue.replaceAll("<", ",");
					newValue = newValue.replaceAll("=", ",");
					newValue = newValue.replaceAll("\\(", ",");
					throw new ActionException("Illegal key[" + key + "] value["
							+ newValue + "]");
				}
				if (newValue.indexOf("script") != -1) {
					newValue = newValue.replaceAll("script", "scrigt");
					throw new ActionException("Illegal key[" + key + "] value["
							+ newValue + "]");
				}
				if (newValue.indexOf("<script") != -1
						&& newValue.indexOf("</script") != -1) {
					newValue = newValue.replaceAll("script", "scrigt");
					throw new ActionException("Illegal key[" + key + "] value["
							+ newValue + "]");
				}
				if (newValue.indexOf("iframe") != -1) {
					newValue = newValue.replaceAll("iframe", "ifrane");
					throw new ActionException("Illegal key[" + key + "] value["
							+ newValue + "]");
				}
				if (RegexpHelper.isGlobmatches(newValue, SQLKEY)) {
					throw new ActionException("Illegal key[" + key + "] value["
							+ newValue + "]");
				}
				if (doScriptCheck(newValue)) {
					throw new ActionException("Illegal key[" + key + "] value["
							+ newValue + "]");
				}
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

	private boolean needCheckUrl() {
		if ("false".equalsIgnoreCase("" + this.getParamMap().get("checkUrl"))
				|| "N".equalsIgnoreCase("" + this.getParamMap().get("checkUrl"))) {
			return false;
		}
		return true;
	}

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
