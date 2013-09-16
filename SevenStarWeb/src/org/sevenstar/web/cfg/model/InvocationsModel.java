package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author rtm 2008-5-8
 */
public class InvocationsModel {
	private List invocationModelList;

	public void addInvocationModel(InvocationModel invocationModel){
		getInvocationModelList().add(invocationModel);
	}

	public List getInvocationModelList() {
		if(invocationModelList == null){
			invocationModelList = new ArrayList();
		}
		return invocationModelList;
	}

	public void setInvocationModelList(List invocationModelList) {
		this.invocationModelList = invocationModelList;
	}
}
