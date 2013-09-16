package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;

public class ResourcesModel {
	
	private List resourceList;
	
	public void addResourceModel(ResourceModel resourceModel){
		getResourceList().add(resourceModel);
	}

	public List getResourceList() {
		if(resourceList == null){
			resourceList = new ArrayList();
		}
		return resourceList;
	}

	public void setResourceList(List resourceList) {
		this.resourceList = resourceList;
	}
	
}
