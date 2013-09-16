package org.sevenstar.web.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.component.freemarker.FreemarkerHelper;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.action.java.DefaultAction;
import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.cfg.model.ResultTypeModel;
import org.sevenstar.web.cfg.model.RuleModel;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.invocation.Invocation;
import org.sevenstar.web.resource.IResource;
import org.sevenstar.web.view.AjaxResult;

/**
 * @author rtm 2008-5-8
 */
public class ExceptionInterceptor implements Interceptor {

	private Log LOG = LogFactory.getLog(ExceptionInterceptor.class);
	
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

	private boolean isAjaxCall() {
		String url = WebContext.getUrl();
		if (url.indexOf("?") != -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		RuleModel ruleModel = SwebConfigure.getUrlModel(url);
		if (ruleModel.getResultType().equals(getAjaxResultName())) {
			return true;
		}
		return false;
	}

	public Object intercept(Invocation invocation) {
		LOG.debug("interceptor[TestInterceptor]:before call ");
		Object result = null;
		try {
			result = invocation.invoke();
		} catch (Throwable e) {
			if (invocation.getAction() instanceof DefaultAction) {
				Throwable trowable = e;
				while (trowable.getCause() != null) {
					if("org.sevenstar.app.exception.ApplicationException".equals(trowable.getClass().getName())){
						break;
					}
					if (trowable instanceof InvocationTargetException) {
						trowable = ((InvocationTargetException) trowable)
								.getTargetException();
					}
					if (trowable.getCause() != null) {
						trowable = trowable.getCause();
					}
				}
				String msg = trowable.getMessage();
				if ("org.sevenstar.persistent.db.exception.PersistentException".equals(trowable.getClass().getName())
						|| (msg != null && msg.indexOf("PersistentException") != -1) || trowable instanceof SQLException) {
					msg = "数据库操作错误";
				}
				if (msg != null) {
					msg = msg.replaceAll("\r", "");
					msg = msg.replaceAll("\n", "");
					msg = msg.replaceAll("\t", "");
					msg = msg.replaceAll("\"", "'");
				}
				if(msg == null || "".equals(msg)){
					msg = "出错了";
				}
				e.printStackTrace();
				LOG.error(e);
				if (isAjaxCall()) {
					return "提示:" + msg;
				} else {
					IResource ir = SwebConfigure.getResourceByUrl(WebContext
							.getUrl());
					if (ir == null || trowable.getMessage() == null || (trowable.getMessage()).indexOf("$") == -1) {
						((DefaultAction) invocation.getAction()).setMsg(trowable.getMessage());
					} else {
	                    Map map = new HashMap();
	                    map.put("i18n", ir);
	                    ((DefaultAction) invocation.getAction()).setMsg(FreemarkerHelper.process(trowable.getMessage(),trowable.getMessage(),map));
					}
					return Action.ERROR;
				}
			}
 			return Action.ERROR;
		}
		LOG.debug("interceptor[TestInterceptor]:after call ");
		return result;
	}

	private Map paramMap;

	public Map getParamMap() {
		if (paramMap == null) {
			paramMap = new HashMap();
		}

		return paramMap;
	}

	public void setParamMap(Map map) {
		this.paramMap = map;
	}

}
