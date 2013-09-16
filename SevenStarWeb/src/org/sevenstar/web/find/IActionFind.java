package org.sevenstar.web.find;

import java.util.Map;

import org.sevenstar.web.action.Action;
/**
 * @author rtm 2008-5-8
 */
public interface IActionFind {
   public Action find(String url);
   public void setParamMap(Map map);
   public Map getParamMap();
   public String getMethodName();
}
