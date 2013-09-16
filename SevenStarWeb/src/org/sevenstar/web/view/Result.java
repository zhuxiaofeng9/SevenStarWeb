package org.sevenstar.web.view;

import java.util.Map;

import org.sevenstar.web.action.Action;

/**
 * 支持 jsp(redirect/forword)/velocity/freemarker...
 *
 * @author rtm
 *
 */
public interface Result {
	public void flow(Action action, Object location);
	public void setParamMap(Map map);

	public Map getParamMap();
}
