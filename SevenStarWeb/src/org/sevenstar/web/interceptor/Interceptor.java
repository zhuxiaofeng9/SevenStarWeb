package org.sevenstar.web.interceptor;

import java.util.Map;

import org.sevenstar.web.invocation.Invocation;
/**
 * @author rtm 2008-5-8
 */
public interface Interceptor {
	public static String INTERCEPTOR = "SEVENSTAR_INTERCEPTOR";
	public static String HAS_INTERCEPTOR = "HAS_SEVENSTAR_INTERCEPTOR";

	public Object intercept(Invocation invocation);

	public void setParamMap(Map map);

	public Map getParamMap();
}
