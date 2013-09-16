package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author rtm 2008-5-8
 */
public class InterceptorModel {
	private String name;
	private String className;

	private List paramModelList;

	public void addParamModel(InterceptorParamModel interceptorParamModel) {
		getParamModelList().add(interceptorParamModel);
	}

	public List getParamModelList() {
		if (paramModelList == null) {
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
