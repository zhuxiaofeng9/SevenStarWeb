package org.sevenstar.web.view.jsp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.util.BeanHelper;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.exception.ActionException;
import org.sevenstar.web.view.Result;

import com.opensymphony.xwork.ActionContext;
/**
 * @author rtm 2008-5-8
 */
public class JspForwardResult implements Result {
	private Log LOG = LogFactory.getLog(JspForwardResult.class);
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

	public void flow(Action action,Object location) {
		LOG.debug("result[JspForwardResult]:called location["+location+"]");
		if(!((String)location).startsWith("/")){
			location = "/"+location;
		}
		RequestDispatcher rd = WebContext.getRequest().getRequestDispatcher(((String)location));
		WebContext.getRequest().setAttribute("ActionContext", ActionContext.getContext());
		WebContext.getRequest().setAttribute("base", WebContext.getRequest().getContextPath());
		WebContext.getRequest().setAttribute("i18n", SwebConfigure.getResourceByUrl(WebContext.getUrl()));
		/**
		 * 设置参数，支持JSTL
		 */
		Field[] fields = BeanHelper.getFields(action.getClass());
		for(int i=0;i<fields.length;i++){
			try {
				fields[i].setAccessible(true);
				WebContext.getRequest().setAttribute(fields[i].getName(), fields[i].get(action));
			} catch (IllegalArgumentException e) {
				throw new ActionException(e);
			} catch (IllegalAccessException e) {
				throw new ActionException(e);
			}
		}
		try {
			rd.forward(WebContext.getRequest(), WebContext.getResponse());
		} catch (ServletException e) {
			throw new ActionException(e);
		} catch (IOException e) {
			throw new ActionException(e);
		}
	}

}
