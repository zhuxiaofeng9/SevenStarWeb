package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author rtm 2008-5-8
 */
public class ResultTypesModel {
	private List resultTypesList;

	public List getResultTypesList() {
		if(resultTypesList == null){
			resultTypesList = new ArrayList();
		}
		return resultTypesList;
	}

	public void setResultTypesList(List resultTypesList) {
		this.resultTypesList = resultTypesList;
	}

	public void addResultTypeModel(ResultTypeModel resultTypeModel){
		getResultTypesList().add(resultTypeModel);
	}

}
