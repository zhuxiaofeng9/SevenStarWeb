package org.sevenstar.web.cfg;

import java.util.Map;

import org.sevenstar.web.cfg.model.SwebModel;
/**
 * @author rtm 2008-5-8
 */
public interface ISwebConfigure {
	public SwebModel getSwebModel();

	public void setSwebModel(SwebModel swebModel);

	public void setParamMap(Map map);

	public Map getParamMap();
}
