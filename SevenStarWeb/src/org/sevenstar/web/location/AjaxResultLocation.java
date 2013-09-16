package org.sevenstar.web.location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sevenstar.web.action.Action;
import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.cfg.model.ResultTypeModel;
import org.sevenstar.web.cfg.model.RuleModel;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.view.AjaxResult;

import com.opensymphony.xwork.ActionContext;

public class AjaxResultLocation implements IResultLocation{

	public ResultModel getResultModel(ActionContext actionContext,
			Action action, String methodName, Object actionResult) {
		String url = (String) ActionContext.getContext().get(WebContext.URL);
		if(url.indexOf("?") != -1){
			url = url.substring(0,url.indexOf("?"));
		}
		RuleModel ruleModel = SwebConfigure.getUrlModel(url);
		ResultModel rm = new ResultModel();
		if (!hasMoreThan2AjaxResultName()) {
			rm.setType(getAjaxResultName());
		} else {
			rm.setType(ruleModel.getResultType());
		}
		if(rm.getType() == null || "".equals(rm.getType())){
			rm.setType(ruleModel.getType());
		}
		rm.setLocation(actionResult);
		return rm;
	}
	

	private String getAjaxResultName() {
		List resultList = SwebConfigure.getSwebModel().getResultTypesModel()
				.getResultTypesList();
		for (int i = 0; i < resultList.size(); i++) {
			ResultTypeModel rtm = (ResultTypeModel) resultList.get(i);
			if (AjaxResult.class.getName().equals(rtm.getClassName())) {
				return rtm.getName();
			}
		}
		return null;
	}
	
	private static Map ajaxResultNameMap = new HashMap();

	private Map getAllAjaxResultNameMap() {
		if(ajaxResultNameMap.size() != 0){
			return ajaxResultNameMap;
		}
 		List resultList = SwebConfigure.getSwebModel().getResultTypesModel()
				.getResultTypesList();
		int num = 0;
		for (int i = 0; i < resultList.size(); i++) {
			ResultTypeModel rtm = (ResultTypeModel) resultList.get(i);
			if (AjaxResult.class.getName().equals(rtm.getClassName())) {
				ajaxResultNameMap.put(rtm.getName(), null);
			}
		}
		return ajaxResultNameMap;
	}

	private boolean hasMoreThan2AjaxResultName() {
		 return getAllAjaxResultNameMap().size() > 1;
	}

}
