package org.sevenstar.web.cfg.model;
/**
 * @author rtm 2008-5-8
 */
public class ActionModel {
	private UrlModel urlModel;

	public void setUrlModel(UrlModel urlModel) {
		this.urlModel = urlModel;
	}

	public UrlModel getUrlModel() {
		if (urlModel == null) {
			urlModel = new UrlModel();
		}
		return urlModel;
	}

}
