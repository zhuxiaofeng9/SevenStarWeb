package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author rtm 2008-5-8
 */
public class RuleModel {

	private String pattern;

	private String resultType;

	private String type;

	private String parse;

	private String inteceptors;

	private String find;

	private String resultLocation;
	
	private String resource;
	
	private String encode;
	
	private String htmlcache;
	

	private List ruleExcludeRuleModelList;
	
	private List headerParamModelList;

	public String toString() {
		return "pattern[" + pattern + "];resultType[" + resultType + "];type["
				+ type + "];parse[" + parse + "];inteceptors[" + inteceptors
				+ "];resultLocation[" + resultLocation + "]";
	}
	
	
	
	

	public List getHeaderParamModelList() {
		if(headerParamModelList == null){
			headerParamModelList = new ArrayList();
		}
		return headerParamModelList;
	}





	public void setHeaderParamModelList(List headerParamModelList) {
		this.headerParamModelList = headerParamModelList;
	}





	public String getHtmlcache() {
		return htmlcache;
	}





	public void setHtmlcache(String htmlcache) {
		this.htmlcache = htmlcache;
	}





	public String getResource() {
		return resource;
	}



	public void setResource(String resource) {
		this.resource = resource;
	}



	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

	public String getParse() {
		return parse;
	}

	public void setParse(String parse) {
		this.parse = parse;
	}

	public String getInteceptors() {
		return inteceptors;
	}

	public void setInteceptors(String inteceptors) {
		this.inteceptors = inteceptors;
	}

	public String getFind() {
		return find;
	}

	public void setFind(String find) {
		this.find = find;
	}

	public String getResultLocation() {
		return resultLocation;
	}

	public void setResultLocation(String resultLocation) {
		this.resultLocation = resultLocation;
	}

	public void addRuleExcludeRuleModel(RuleExcludeRuleModel ruleExcludeRuleModel){
		getRuleExcludeRuleModelList().add(ruleExcludeRuleModel);
	}

	public List getRuleExcludeRuleModelList() {
		if(ruleExcludeRuleModelList == null){
			ruleExcludeRuleModelList = new ArrayList();
		}
		return ruleExcludeRuleModelList;
	}

	public void setRuleExcludeRuleModelList(List ruleExcludeRuleModelList) {
		this.ruleExcludeRuleModelList = ruleExcludeRuleModelList;
	}



	public String getEncode() {
		return encode;
	}



	public void setEncode(String encode) {
		this.encode = encode;
	}
	
	

}
