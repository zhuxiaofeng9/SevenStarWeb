package org.sevenstar.web.resource;

import java.util.Map;

public interface IResource {
	public void setParamMap(Map map);
	public String get(String key);
	public Map getParamMap();
}
