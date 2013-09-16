package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author rtm 2008-5-8
 */
public class GlobalResultsModel {
	private List resultModelList;

	public void addResultModel(GlobalResultModel resultModel){
		if(resultModelList == null){
			resultModelList = new ArrayList();
		}
		getResultModelList().add(resultModel);
	}

	public List getResultModelList() {
		return resultModelList;
	}

	public void setResultModelList(List resultModelList) {
		this.resultModelList = resultModelList;
	}

}
