package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author rtm 2008-5-8
 */
public class ParsesModel {
	private List parseList;

	public List getParseList() {
		if(parseList == null){
			parseList = new ArrayList();
		}
		return parseList;
	}

	public void setParseList(List parseList) {
		this.parseList = parseList;
	}

	public void addParseModel(ParseModel parseModel){
		getParseList().add(parseModel);
	}

}
