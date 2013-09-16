package org.sevenstar.web.location;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sevenstar.util.BeanHelper;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.annotation.SSResult;
import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.cfg.model.GlobalResultModel;
import org.sevenstar.web.cfg.model.ResultTypeModel;
import org.sevenstar.web.cfg.model.RuleModel;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.view.AjaxResult;
import org.sevenstar.web.view.FileResult;
import org.sevenstar.web.view.InputStreamResult;

import com.opensymphony.xwork.ActionContext;

/**
 * @author rtm 2008-5-8
 */
public class DefaultResultLocation implements IResultLocation {

	private String getFileResultName() {
		List resultList = SwebConfigure.getSwebModel().getResultTypesModel()
				.getResultTypesList();
		for (int i = 0; i < resultList.size(); i++) {
			ResultTypeModel rtm = (ResultTypeModel) resultList.get(i);
			if (FileResult.class.getName().equals(rtm.getClassName())) {
				return rtm.getName();
			}
		}
		return null;
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

	private String getInputStreamResultName() {
		List resultList = SwebConfigure.getSwebModel().getResultTypesModel()
				.getResultTypesList();
		for (int i = 0; i < resultList.size(); i++) {
			ResultTypeModel rtm = (ResultTypeModel) resultList.get(i);
			if (InputStreamResult.class.getName().equals(rtm.getClassName())) {
				return rtm.getName();
			}
		}
		return null;
	}

	public ResultModel getResultModel(ActionContext actionContext,
			Action action, String methodName, Object actionResult) {
		String url = (String) ActionContext.getContext().get(WebContext.URL);
		if (url.indexOf("?") != -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		RuleModel ruleModel = SwebConfigure.getUrlModel(url);
		ResultModel rm = new ResultModel();
		if (actionResult instanceof File) {
			rm.setLocation(actionResult);
			rm.setType(getFileResultName());
			return rm;
		}
		if (actionResult instanceof InputStream) {
			rm.setLocation(actionResult);
			rm.setType(getInputStreamResultName());
			return rm;
		}
		if (!(actionResult instanceof String)) {
			rm.setLocation(actionResult);
			if (!hasMoreThan2AjaxResultName()) {
				rm.setType(getAjaxResultName());
			} else {
				rm.setType(ruleModel.getResultType());
			}
			return rm;
		}
		if (getAllAjaxResultNameMap().containsKey( ruleModel.getResultType())) {
			rm.setLocation(actionResult);
			if (!hasMoreThan2AjaxResultName()) {
				rm.setType(getAjaxResultName());
			} else {
				rm.setType(ruleModel.getResultType());
			}
			return rm;
		}
		if (!Action.SUCCESS.equals(actionResult)) {
			/**
			 * 取返回url
			 */
			List globalResultList = SwebConfigure.getGlobalResult(String
					.valueOf(actionResult));
			boolean hasFind = false;
			if (globalResultList != null) {
				for (int i = 0; i < globalResultList.size(); i++) {
					GlobalResultModel globalResultModel = (GlobalResultModel) globalResultList
							.get(i);
					if (globalResultModel.getType().equals(ruleModel.getResultType())) {
						rm.setLocation(globalResultModel.getLocation());
 						// ActionContext.getContext().put(WebContext.REALURL,globalResultModel.getLocation());
						hasFind = true;
						break;
					}
				}
				if(!hasFind && globalResultList.size() > 0){
					GlobalResultModel globalResultModel = (GlobalResultModel) globalResultList.get(0);
					rm.setLocation(globalResultModel.getLocation());
					rm.setType(globalResultModel.getType());
					return rm;
				}
			}
			if (!hasFind) {
				/**
				 * action返回的直接url
				 */
				// ActionContext.getContext().put(WebContext.REALURL,result);
				rm.setLocation(actionResult);
			}
		} else {
			rm.setLocation((String) ActionContext.getContext().get(
					WebContext.REALURL));
		}
		rm.setType(ruleModel.getResultType());
		/**
		 * 注释指明特例的返回方式
		 */
		Method method = BeanHelper.getMethod(action.getClass(), methodName);
		if (method != null && method.isAnnotationPresent(SSResult.class)) {
			SSResult ssResult = (SSResult) method.getAnnotation(SSResult.class);
			if (!"".equals(ssResult.type())) {
				rm.setType(ssResult.type());
			}
			if (ssResult.location() != null && !"".equals(ssResult.location())) {
				rm.setLocation(ssResult.location());
			}
		}
		return rm;
	}

}
