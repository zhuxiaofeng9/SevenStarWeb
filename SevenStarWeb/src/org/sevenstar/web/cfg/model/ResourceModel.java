package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;

public class ResourceModel {
	private String name;
	private String className;
	
	private List paramModelList;
	
	public void addParamModel(ResourceParamModel paramModel){
		if(paramModelList == null){
			paramModelList = new ArrayList();
		}
		paramModelList.add(paramModel);
	}

	public List getParamModelList() {
		if(paramModelList == null){
			paramModelList = new ArrayList();
		}
		return paramModelList;
	}

	public void setParamModelList(List paramModelList) {
		this.paramModelList = paramModelList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
