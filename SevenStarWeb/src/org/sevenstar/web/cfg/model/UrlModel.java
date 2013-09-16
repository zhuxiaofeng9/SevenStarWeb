package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author rtm 2008-5-8
 */
public class UrlModel {
	private List ruleList;

	public void addRuleModel(RuleModel ruleModel){
		this.getRuleList().add(ruleModel);
	}

	public List getRuleList() {
		if(ruleList == null){
			ruleList = new ArrayList();
		}
		return ruleList;
	}

	public void setRuleList(List ruleList) {
		this.ruleList = ruleList;
	}

}
