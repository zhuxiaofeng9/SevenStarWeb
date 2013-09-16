package org.sevenstar.web.action.java;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.cfg.model.RuleModel;
import org.sevenstar.web.context.WebContext;

/**
 * @author rtm 2008-5-8
 */
public class DefaultAction implements Action {
	private Log LOG = LogFactory.getLog(DefaultAction.class);
	/**
	 * 跳转机制
	 */
	private String nextUrl;

	private String msg;

	/**
	 * 出错信息，比照webwork2
	 * 
	 * @TODO 未实现
	 */
	private Map errorMap;
	
	
	private String token;

	public void addError(String key, String message) {
		getErrorMap().put(key, message);
	}

	public Object execute() {
		return SUCCESS;
	}

	/**
	 * 直接输出
	 * @param result
	 */
	public void write(String result) {
		if (result == null || "".equals(result)) {
			return;
		}
		RuleModel ruleModel = SwebConfigure.getUrlModel(WebContext.getUrl());
		ServletOutputStream sos = null;
		try {
			sos = WebContext.getResponse().getOutputStream();
			if (ruleModel.getEncode() != null
					&& !"".equals(ruleModel.getEncode())) {
				sos.write(result.getBytes(ruleModel.getEncode()));
			} else {
				sos.write(result.getBytes(SwebConfigure.getSwebModel()
						.getEncode()));
			}
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			if (sos != null) {
				try {
					sos.close();
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
	}

	public String getNextUrl() {
		return nextUrl;
	}

	public void setNextUrl(String nextUrl) {
		this.nextUrl = nextUrl;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Map getErrorMap() {
		if (errorMap == null) {
			errorMap = new HashMap();
		}
		return errorMap;
	}

	public void setErrorMap(Map errorMap) {
		this.errorMap = errorMap;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
