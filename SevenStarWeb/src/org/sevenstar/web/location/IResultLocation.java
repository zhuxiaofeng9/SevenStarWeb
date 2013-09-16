package org.sevenstar.web.location;

import org.sevenstar.web.action.Action;

import com.opensymphony.xwork.ActionContext;
/**
 * @author rtm 2008-5-8
 */
public interface IResultLocation {
   public ResultModel getResultModel(ActionContext actionContext,Action action,String methodName,Object actionResult);
}
