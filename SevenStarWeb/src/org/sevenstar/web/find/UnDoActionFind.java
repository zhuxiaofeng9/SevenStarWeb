package org.sevenstar.web.find;

import java.util.HashMap;
import java.util.Map;

import org.sevenstar.web.action.Action;
import org.sevenstar.web.action.java.DefaultAction;
import org.sevenstar.util.BeanHelper;

/**
 * 什么也不做，仅仅是为了URL的映射，比如 css，模版等的版本
 * @author rtm
 *
 */
public class UnDoActionFind implements IActionFind{

	private Map paramMap;


	public Action find(String url) {
 		return (DefaultAction)BeanHelper.newInstance(DefaultAction.class);
	}

	public String getMethodName() {
 		return "execute";
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

}
