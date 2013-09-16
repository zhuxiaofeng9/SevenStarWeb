package org.sevenstar.web.invocation;


import org.sevenstar.web.action.Action;

/**
 * @author rtm 2008-5-8
 */
public interface Invocation {
   public boolean hasExecuted();
   public void setHasExecuted();
   public Object invoke();
   public Action getAction();
   public void setAction(Action action);
   public String getMethodName();
   public void setMethodName(String methodName);
 }
