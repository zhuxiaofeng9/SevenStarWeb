package org.sevenstar.web.action.java;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.util.BeanHelper;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.interceptor.Interceptor;
import org.sevenstar.web.invocation.Invocation;
import org.sevenstar.web.exception.ActionException;

/**
 * @author rtm 2008-5-8
 */
public class DefaultJavaInvocation implements Invocation {
	private static Log LOG = LogFactory.getLog(DefaultJavaInvocation.class);
	/**
	 * 默认从page文件夹开始
	 */
	private String rootPackage = "page";

	private Action action;

	private String methodName;

	private boolean executed = false;

	private Object result = Action.SUCCESS;

	private List interceptorList;

	private List hasInterceptorList;

	public DefaultJavaInvocation() {
		LOG.debug("invocation[DefaultJavaInvocation]:instance");
		/**
		 * 初始化interceptor
		 */
		interceptorList = SwebConfigure.getInterceptorList(WebContext.getUrl());
		hasInterceptorList = new ArrayList();
	}

	private Interceptor getNextInterceptor() {
		if (hasInterceptorList.size() < interceptorList.size()) {
			Interceptor interceptor = (Interceptor) interceptorList
					.get(hasInterceptorList.size());
			hasInterceptorList.add(interceptor);
			return interceptor;
		} else {
			return null;
		}
	}

	public Object invoke() {
		Interceptor interceptor = getNextInterceptor();
		if (interceptor != null) {
			result = interceptor.intercept(this);
			if(!Action.SUCCESS.equals(result)){
				return result;
			}
		}
		if (!hasExecuted()) {
			LOG.debug("invocation[DefaultJavaInvocation]:invoke");
			if (BeanHelper.hasMethodIgnoreCase(getAction().getClass(), this
					.getMethodName())) {
				Method method = BeanHelper.getMethodIgnoreCase(getAction()
						.getClass(), this.getMethodName());
				if (method.getParameterTypes().length == 0) {
					executed = true;
					return BeanHelper
							.invoke(method, getAction(), null);
				} else {
					/**
					 * 尝试按照类型组织参数
					 */
					throw new ActionException("need implements");
				}
			} else {
				LOG.warn("cann't find method["+ this.getMethodName() + "] in action["+ this.getClass().getName() + "]");
				return Action.SUCCESS;
				//throw new ActionException("cann't find method["+ this.getMethodName() + "] in action["+ this.getClass().getName() + "]");
			}
		}
		return result;
	}

	public boolean hasExecuted() {
		return executed;
	}

	public Action getAction() {
		return this.action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getMethodName() {
		if (this.methodName == null) {
			this.methodName = "execute";
		}
		return this.methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setHasExecuted() {
		this.executed = true;
	}

}
