package org.sevenstar.web;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.action.java.DefaultAction;
import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.cfg.model.RuleModel;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.exception.ActionException;
import org.sevenstar.web.find.IActionFind;
import org.sevenstar.web.find.PageFind;
import org.sevenstar.web.invocation.Invocation;
import org.sevenstar.web.location.IResultLocation;
import org.sevenstar.web.location.ResultModel;
import org.sevenstar.web.url.IParse;
import org.sevenstar.web.view.Result;

import com.opensymphony.xwork.ActionContext;

/**
 * @author rtm 2008-5-8
 */
public class ApplicationDispatcher {
	public final static String INVOCATION = "sevenstarweb_invocation";
	private static Log LOG = LogFactory.getLog(ApplicationDispatcher.class);

	public static void execute(String url) {
		RuleModel ruleModel = SwebConfigure.getUrlModel(url);
		if(ruleModel.getEncode() != null && !"".equals(ruleModel.getEncode())){
			try {
				WebContext.getRequest().setCharacterEncoding(ruleModel.getEncode());
			} catch (UnsupportedEncodingException e) {
  				throw new RuntimeException(e);
			}
		}
		/**
		 * 调用类型
		 */
		Invocation invocation = SwebConfigure
				.getInvocation(ruleModel.getType());
		ActionContext.getContext().put(INVOCATION, invocation);
		/**
		 * url解析 假URL->真URL
		 */
		IParse parse = SwebConfigure.getParse(url);
		String realUrl = url;
		if (parse != null) {
			realUrl = parse.parse(url);
			ActionContext.getContext().put(WebContext.REALURL, realUrl);
		} else {
			ActionContext.getContext().put(WebContext.REALURL, url);
		}
		/**
		 * 寻找action实例
		 */
		IActionFind find = SwebConfigure.getActionFind(url);
		if (find == null) {
			throw new ActionException("hasn't configure find for url[" + url
					+ "]");
		}
		Action action = new DefaultAction();
		String methodName = "execute";
		try {
			action = find.find(realUrl);
			methodName = find.getMethodName();
		} catch (ActionException e) {
			if (find instanceof PageFind) {
				LOG.info(e);
			} else {
				throw e;
			}
		} finally {
			if (action == null) {
				action = new DefaultAction();
			}
		}
		/**
		 * 设置调用
		 */
		invocation.setAction(action);
		invocation.setMethodName(methodName);
		Object actionResult = null;
		try {
			actionResult = invocation.invoke();
		} catch (ActionException e) {
			if (find instanceof PageFind) {
				LOG.info(e);
			} else {
				throw e;
			}
		}
		if (actionResult == null) {
			// pass
			return;
		}
		/**
		 * 取返回路径以及返回方式
		 */
		if (actionResult != null) {
			IResultLocation resultLocation = SwebConfigure
					.getResultLocation(ruleModel.getResultLocation());
			ResultModel model = resultLocation.getResultModel(ActionContext
					.getContext(), invocation.getAction(), invocation
					.getMethodName(), actionResult);
			Result result = SwebConfigure.getResult(model.getType());
			result.flow(invocation.getAction(), model.getLocation());
		}
	}
}
