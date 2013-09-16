package org.sevenstar.web.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.util.BeanHelper;
import org.sevenstar.util.RegexpHelper;
import org.sevenstar.web.Constants;
import org.sevenstar.web.action.java.DefaultJavaInvocation;
import org.sevenstar.web.cfg.model.FindModel;
import org.sevenstar.web.cfg.model.FindParamModel;
import org.sevenstar.web.cfg.model.GlobalResultModel;
import org.sevenstar.web.cfg.model.InterceptorModel;
import org.sevenstar.web.cfg.model.InterceptorParamModel;
import org.sevenstar.web.cfg.model.InterceptorsModel;
import org.sevenstar.web.cfg.model.ActionModel;
import org.sevenstar.web.cfg.model.InvocationModel;
import org.sevenstar.web.cfg.model.ParseModel;
import org.sevenstar.web.cfg.model.ResourceModel;
import org.sevenstar.web.cfg.model.ResourceParamModel;
import org.sevenstar.web.cfg.model.ResultLocationModel;
import org.sevenstar.web.cfg.model.ResultTypeModel;
import org.sevenstar.web.cfg.model.ResultTypeParamModel;
import org.sevenstar.web.cfg.model.RuleExcludeRuleModel;
import org.sevenstar.web.cfg.model.RuleModel;
import org.sevenstar.web.cfg.model.SwebModel;
import org.sevenstar.web.exception.ActionException;
import org.sevenstar.web.find.IActionFind;
import org.sevenstar.web.interceptor.Interceptor;
import org.sevenstar.web.invocation.Invocation;
import org.sevenstar.web.location.DefaultResultLocation;
import org.sevenstar.web.location.IResultLocation;
import org.sevenstar.web.resource.IResource;
import org.sevenstar.web.url.IParse;
import org.sevenstar.web.view.Result;

/**
 * @author rtm 2008-5-8
 */
public class SwebConfigure {

	private static Log LOG = LogFactory.getLog(SwebConfigure.class);

	public static String getWelcomeFile() {
		return SwebConfigureFactory.getSwebModel().getWelcomeFile();
	}

	public static ActionModel getActionModel() {
		return SwebConfigureFactory.getSwebModel().getActionModel();
	}

	public static SwebModel getSwebModel() {
		return SwebConfigureFactory.getSwebModel();
	}

	public static List getActionUrlRuleList() {
		return getSwebModel().getActionModel().getUrlModel().getRuleList();
	}

	public static Result getResult(String resultType) {
		List resultModelList = getSwebModel().getResultTypesModel()
				.getResultTypesList();
		for (int i = 0; i < resultModelList.size(); i++) {
			ResultTypeModel model = (ResultTypeModel) resultModelList.get(i);
			if (resultType.equals(model.getName())) {
				Result result = (Result) BeanHelper.newInstance(model
						.getClassName());
				if (model.getResultTypeParamModelList().size() > 0) {
					Map paramMap = new HashMap();
					for (int j = 0; j < model.getResultTypeParamModelList()
							.size(); j++) {
						ResultTypeParamModel resultTypeParamModel = (ResultTypeParamModel) model
								.getResultTypeParamModelList().get(j);
						paramMap.put(resultTypeParamModel.getName(),
								resultTypeParamModel.getValue());
					}
					result.setParamMap(paramMap);
				}
				return result;
			}
		}
		return null;
	}

	public static IResultLocation getResultLocation(String location) {
		List modelList = getSwebModel().getResultLocationsModel()
				.getLocationsList();
		for (int i = 0; i < modelList.size(); i++) {
			ResultLocationModel model = (ResultLocationModel) modelList.get(i);
			if (location.equals(model.getName())) {
				return (IResultLocation) BeanHelper.newInstance(model
						.getClassName());
			}
		}
		return new DefaultResultLocation();
	}

	public static boolean check(String url) {
		if (url != null && url.indexOf("?") != -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		if (getActionUrlRuleList().size() > 0) {
			List ruleList = getActionUrlRuleList();
			for (int i = 0; i < ruleList.size(); i++) {
				RuleModel ruleModel = (RuleModel) ruleList.get(i);
				/**
				 * 检查是否符合perl5表达式
				 */
				boolean isPerl5Match = false;
				try {
					isPerl5Match = RegexpHelper.isPerl5matches(url,
							ruleModel.getPattern());
				} catch (Throwable e) {
					// pass
				}
				if (RegexpHelper.isGlobmatches(url, ruleModel.getPattern())
						|| isPerl5Match) {
					LOG.debug("find url(" + url + ") RuleModel("
							+ ruleModel.toString() + ")");
					boolean isExclude = false;
					if (ruleModel.getRuleExcludeRuleModelList().size() > 0) {
						for (int j = 0; j < ruleModel
								.getRuleExcludeRuleModelList().size(); j++) {
							RuleExcludeRuleModel excludeRuleModel = (RuleExcludeRuleModel) ruleModel
									.getRuleExcludeRuleModelList().get(j);
							if (RegexpHelper.isGlobmatches(url,
									excludeRuleModel.getPattern())) {
								isExclude = true;
								LOG.debug("  find url(" + url + ") RuleModel["
										+ ruleModel.toString()
										+ "],but exclude pattern["
										+ excludeRuleModel.getPattern() + "]");
								continue;
							}
						}
					}
					if (!isExclude) {
						return true;
					}
				}
			}
		}
		LOG.debug("not find url(" + url + ") RuleModel");
		return false;
	}

	public static RuleModel getUrlModel(String url) {
		if (url != null && url.indexOf("?") != -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		if (getActionUrlRuleList().size() > 0) {
			/**
			 * url替换
			 */
			List ruleList = getActionUrlRuleList();
			for (int i = 0; i < ruleList.size(); i++) {
				RuleModel ruleModel = (RuleModel) ruleList.get(i);
				boolean isPerl5Match = false;
				try {
					isPerl5Match = RegexpHelper.isPerl5matches(url,
							ruleModel.getPattern());
				} catch (Throwable e) {
					// pass
				}
				if (RegexpHelper.isGlobmatches(url, ruleModel.getPattern())
						|| isPerl5Match) {
					/**
					 * 判断是否排除掉
					 */
					List excludeList = ruleModel.getRuleExcludeRuleModelList();
					for (int j = 0; j < excludeList.size(); j++) {
						RuleExcludeRuleModel erm = (RuleExcludeRuleModel) excludeList
								.get(j);
						try {
							if (RegexpHelper.isGlobmatches(url,
									erm.getPattern())
									|| RegexpHelper.isPerl5matches(url,
											erm.getPattern())) {
								continue;
							}
						} catch (Throwable e) {
							// pass
						}

					}
					return ruleModel;
				}
			}
		}
		return null;
	}

	public static Invocation getInvocation(String invocationName) {
		List invocationModelList = getSwebModel().getInvocationsModel()
				.getInvocationModelList();
		for (int i = 0; i < invocationModelList.size(); i++) {
			InvocationModel model = (InvocationModel) invocationModelList
					.get(i);
			if (invocationName.equals(model.getName())) {
				return (Invocation) BeanHelper
						.newInstance(model.getClassName());
			}
		}
		return new DefaultJavaInvocation();
	}

	public static IActionFind getActionFind(String url) {
		if (url != null && url.indexOf("?") != -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		RuleModel ruleModel = getUrlModel(url);
		if (ruleModel == null) {
			return null;
		}
		String find = ruleModel.getFind();
		if (find == null || "".equals(find.trim())) {
			return null;
		}
		List findModelList = getSwebModel().getFindsModel().getFindList();
		for (int i = 0; i < findModelList.size(); i++) {
			FindModel model = (FindModel) findModelList.get(i);
			if (find.equals(model.getName())) {
				Object object = BeanHelper.newInstance(model.getClassName());
				if (object instanceof IActionFind) {
					IActionFind findObject = (IActionFind) object;
					if (model.getParamModelList().size() > 0) {
						Map map = new HashMap();
						for (int j = 0; j < model.getParamModelList().size(); j++) {
							FindParamModel findParamModel = (FindParamModel) model
									.getParamModelList().get(j);
							map.put(findParamModel.getName(),
									findParamModel.getValue());
						}
						findObject.setParamMap(map);
					}
					return findObject;
				}
			}
		}
		return null;
	}

	public static IResource getResourceByUrl(String url) {
		if (url != null && url.indexOf("?") != -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		RuleModel rm = getUrlModel(url);
		if (rm.getResource() != null && !"".equals(rm.getResource())) {
			return getResource(rm.getResource());
		}
		return getResource(getSwebModel().getResource());
	}

	public static IResource getResource(String resource) {
		if (resource == null || "".endsWith(resource)) {
			return null;
		}
		List resourceList = getSwebModel().getResourcesModel()
				.getResourceList();
		for (int i = 0; i < resourceList.size(); i++) {
			ResourceModel rm = (ResourceModel) resourceList.get(i);
			if (resource.equals(rm.getName())) {
				if (rm.getClassName() != null && !"".equals(rm.getClassName())) {
					IResource ir = (IResource) BeanHelper.newInstance(rm
							.getClassName());
					if (rm.getParamModelList().size() > 0) {
						Map map = new HashMap();
						for (int j = 0; j < rm.getParamModelList().size(); j++) {
							ResourceParamModel rpm = (ResourceParamModel) rm
									.getParamModelList().get(j);
							map.put(rpm.getName(), rpm.getValue());
						}
						ir.setParamMap(map);
					}
					return ir;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	public static List getAllResourceList() {
		List list = new ArrayList();
		List resourceList = getSwebModel().getResourcesModel()
				.getResourceList();
		for (int i = 0; i < resourceList.size(); i++) {
			ResourceModel rm = (ResourceModel) resourceList.get(i);
			if (rm.getClassName() != null && !"".equals(rm.getClassName())) {
				IResource ir = (IResource) BeanHelper.newInstance(rm
						.getClassName());
				if (rm.getParamModelList().size() > 0) {
					Map map = new HashMap();
					for (int j = 0; j < rm.getParamModelList().size(); j++) {
						ResourceParamModel rpm = (ResourceParamModel) rm
								.getParamModelList().get(j);
						map.put(rpm.getName(), rpm.getValue());
					}
					ir.setParamMap(map);
				}
				list.add(ir);
			}
		}
		return list;
	}

	public static IParse getParse(String url) {
		if (url != null && url.indexOf("?") != -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		RuleModel ruleModel = getUrlModel(url);
		String parse = ruleModel.getParse();
		if (parse == null || "".equals(parse)) {
			return null;
		}
		List parseModelList = getSwebModel().getParsesModel().getParseList();
		for (int i = 0; i < parseModelList.size(); i++) {
			ParseModel model = (ParseModel) parseModelList.get(i);
			if (parse.equals(model.getName())) {
				Object object = BeanHelper.newInstance(model.getClassName());
				if (object instanceof IParse) {
					return (IParse) object;
				}
			}
		}
		return null;
	}

	public static List getGlobalResult(String name) {
		if (name == null || "".equals(name)) {
			return null;
		}
		List allList = getSwebModel().getGlobalResultsModel()
				.getResultModelList();
		List list = new ArrayList();
		for (int i = 0; i < allList.size(); i++) {
			GlobalResultModel resultModel = (GlobalResultModel) allList.get(i);
			if (name.trim().equals(resultModel.getName())) {
				list.add(resultModel);
			}
		}
		return list;
	}

	public static List getInterceptorList(String url) {
		if (url != null && url.indexOf("?") != -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		List list = new ArrayList();
		RuleModel ruleModel = getUrlModel(url);
		if (ruleModel == null) {
			return list;
		}
		String interceptors = ruleModel.getInteceptors();
		if (interceptors != null) {
			interceptors = interceptors.trim();
		}
		if ("".equals(interceptors)) {
			return list;
		}
		if (interceptors.indexOf(Constants.separator) != -1) {
			String inters[] = interceptors.split(Constants.separator);
			for (int i = 0; i < inters.length; i++) {
				InterceptorModel model = getInterceptorModel(inters[i]);
				Interceptor interceptor = (Interceptor) BeanHelper
						.newInstance(model.getClassName());
				if (model.getParamModelList().size() > 0) {
					Map paramMap = new HashMap();
					for (int j = 0; j < model.getParamModelList().size(); j++) {
						InterceptorParamModel interceptorParamModel = (InterceptorParamModel) model
								.getParamModelList().get(j);
						paramMap.put(interceptorParamModel.getName(),
								interceptorParamModel.getValue());
					}
					interceptor.setParamMap(paramMap);
				}
				list.add(interceptor);
			}
		} else {
			InterceptorModel model = getInterceptorModel(interceptors);
			Interceptor interceptor = (Interceptor) BeanHelper
					.newInstance(model.getClassName());
			if (model.getParamModelList().size() > 0) {
				Map paramMap = new HashMap();
				for (int j = 0; j < model.getParamModelList().size(); j++) {
					InterceptorParamModel interceptorParamModel = (InterceptorParamModel) model
							.getParamModelList().get(j);
					paramMap.put(interceptorParamModel.getName(),
							interceptorParamModel.getValue());
				}
				interceptor.setParamMap(paramMap);
			}
			list.add(interceptor);
		}
		return list;
	}

	private static InterceptorModel getInterceptorModel(String name) {
		InterceptorsModel interceptorsModel = getSwebModel()
				.getInterceptorsModel();
		for (int i = 0; i < interceptorsModel.getInterceptorsList().size(); i++) {
			InterceptorModel interceptorModel = (InterceptorModel) interceptorsModel
					.getInterceptorsList().get(i);
			if (name.equals(interceptorModel.getName())) {
				return interceptorModel;
			}
		}
		throw new ActionException("hasn't find interceptor for [" + name
				+ "],check your config");
	}
}
