package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author rtm 2008-5-8
 */
public class ResultTypeModel {
	private String name;
	private String className;

	private List resultTypeParamModelList;

	public List getResultTypeParamModelList() {
		if(resultTypeParamModelList == null){
			resultTypeParamModelList = new ArrayList();
		}
		return resultTypeParamModelList;
	}

	public void setResultTypeParamModelList(List resultTypeParamModelList) {
		this.resultTypeParamModelList = resultTypeParamModelList;
	}

	public void addResultTypeParamModel(ResultTypeParamModel model){
		getResultTypeParamModelList().add(model);
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
