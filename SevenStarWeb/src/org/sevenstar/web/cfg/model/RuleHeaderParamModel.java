package org.sevenstar.web.cfg.model;

public class RuleHeaderParamModel {
	private String name;
	private String value;
	/**
	 * 类型 空,int,date 
	 * 对应三个函数setHeader  setIntHeader setDateHeader 
	 */
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
