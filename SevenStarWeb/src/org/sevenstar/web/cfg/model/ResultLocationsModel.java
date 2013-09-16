package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author rtm 2008-5-8
 */
public class ResultLocationsModel {
	private List locationsList;

	public void addLocationModel(ResultLocationModel locationModel){
		if(locationsList == null){
			locationsList = new ArrayList();
		}
		getLocationsList().add(locationModel);
	}

	public List getLocationsList() {
		if(locationsList == null){
			locationsList = new ArrayList();
		}
		return locationsList;
	}

	public void setLocationsList(List locationsList) {
		this.locationsList = locationsList;
	}



}
