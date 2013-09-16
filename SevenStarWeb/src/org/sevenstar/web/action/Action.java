package org.sevenstar.web.action;
/**
 * @author rtm 2008-2-7
 */
public interface Action {

	public static final String SUCCESS = "success";

	public static final String ERROR = "error";

	public static final String REDIRECT = "redirect";

	public Object execute();
}
