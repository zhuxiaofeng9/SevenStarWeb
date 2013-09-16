package org.sevenstar.web.cfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author rtm 2008-5-8
 */
public class InterceptorsModel {
	private List interceptorsList;

	public void addInterceptors(InterceptorModel interceptorModel){
		getInterceptorsList().add(interceptorModel);
	}

	public List getInterceptorsList() {
		if(interceptorsList == null){
			interceptorsList = new ArrayList();
		}
		return interceptorsList;
	}

	public void setInterceptorsList(List interceptorsList) {
		this.interceptorsList = interceptorsList;
	}

}
