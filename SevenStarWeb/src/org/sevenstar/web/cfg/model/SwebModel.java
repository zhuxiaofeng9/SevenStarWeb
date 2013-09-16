package org.sevenstar.web.cfg.model;
/**
 * @author rtm 2008-5-8
 */
public class SwebModel {

	private ActionModel actionModel;

	private String encode;
	
	/**
	 * 国际化
	 */
	private String locale;
	
	private String resource;
	
	private ResourcesModel resourcesModel;

	private String welcomeFile;

	private InterceptorsModel interceptorsModel;

	private ParsesModel parsesModel;

	private ResultTypesModel resultTypesModel;

	private GlobalResultsModel globalResultsModel;

	private FindsModel findsModel;

	private ResultLocationsModel resultLocationsModel;

	private InvocationsModel invocationsModel;
	
	

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public InvocationsModel getInvocationsModel() {
		if(invocationsModel == null){
			invocationsModel = new InvocationsModel();
		}
		return invocationsModel;
	}

	public void setInvocationsModel(InvocationsModel invocationsModel) {
		this.invocationsModel = invocationsModel;
	}

	public ResultLocationsModel getResultLocationsModel() {
		if(resultLocationsModel == null){
			resultLocationsModel = new ResultLocationsModel();
		}
		return resultLocationsModel;
	}

	public void setResultLocationsModel(
			ResultLocationsModel resultLocationsModel) {
		this.resultLocationsModel = resultLocationsModel;
	}

	public FindsModel getFindsModel() {
		if (findsModel == null) {
			findsModel = new FindsModel();
		}
		return findsModel;
	}

	public void setFindsModel(FindsModel findsModel) {
		this.findsModel = findsModel;
	}

	public InterceptorsModel getInterceptorsModel() {
		if (interceptorsModel == null) {
			interceptorsModel = new InterceptorsModel();
		}
		return interceptorsModel;
	}

	public void setInterceptorsModel(InterceptorsModel interceptorsModel) {
		this.interceptorsModel = interceptorsModel;
	}

	public ParsesModel getParsesModel() {
		if (parsesModel == null) {
			parsesModel = new ParsesModel();
		}
		return parsesModel;
	}

	public void setParsesModel(ParsesModel parseModel) {
		this.parsesModel = parseModel;
	}

	public ResultTypesModel getResultTypesModel() {
		if (resultTypesModel == null) {
			resultTypesModel = new ResultTypesModel();
		}
		return resultTypesModel;
	}

	public void setResultTypesModel(ResultTypesModel resultTypesModel) {
		this.resultTypesModel = resultTypesModel;
	}

	public GlobalResultsModel getGlobalResultsModel() {
		if (globalResultsModel == null) {
			globalResultsModel = new GlobalResultsModel();
		}
		return globalResultsModel;
	}

	public void setGlobalResultsModel(GlobalResultsModel globalResultsModel) {
		this.globalResultsModel = globalResultsModel;
	}

	public ActionModel getActionModel() {
		if (actionModel == null) {
			actionModel = new ActionModel();
		}
		return actionModel;
	}

	public void setActionModel(ActionModel actionModel) {
		this.actionModel = actionModel;
	}

	public String getEncode() {
		if (encode == null) {
			encode = "GBK";
		}
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public String getWelcomeFile() {
		return welcomeFile;
	}

	public void setWelcomeFile(String welcomeFile) {
		this.welcomeFile = welcomeFile;
	}

	public ResourcesModel getResourcesModel() {
		if(resourcesModel == null){
			resourcesModel = new ResourcesModel();
		}
		return resourcesModel;
	}

	public void setResourcesModel(ResourcesModel resourcesModel) {
		this.resourcesModel = resourcesModel;
	}
	
	

}
