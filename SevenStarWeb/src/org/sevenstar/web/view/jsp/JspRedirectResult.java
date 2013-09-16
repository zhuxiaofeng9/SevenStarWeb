package org.sevenstar.web.view.jsp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.exception.ActionException;
import org.sevenstar.web.view.Result;
/**
 * @author rtm 2008-5-8
 */
public class JspRedirectResult implements Result {
	private Log LOG = LogFactory.getLog(JspRedirectResult.class);
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

	public void flow(Action action, Object location) {
		LOG.debug("result[JspRedirectResult]:called location[" + location+ "]");
		HttpServletResponse resp = WebContext.getResponse();
		/**
		 * TODO 将action的值作为URL的一部分传递，是否有必要？？
		 */
		try {
			resp.sendRedirect(((String)location));
		} catch (IOException e) {
			throw new ActionException(e);
		}
	}

}
