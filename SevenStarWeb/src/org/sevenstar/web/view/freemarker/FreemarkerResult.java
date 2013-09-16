package org.sevenstar.web.view.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.util.BeanHelper;
import org.sevenstar.util.RegexpHelper;
import org.sevenstar.web.Constants;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.action.java.DefaultAction;
import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.cfg.model.RuleHeaderParamModel;
import org.sevenstar.web.cfg.model.RuleModel;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.exception.ActionException;
import org.sevenstar.web.resource.IResource;
import org.sevenstar.web.view.Result;

import com.opensymphony.xwork.ActionContext;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerResult implements Result {
	private Log LOG = LogFactory.getLog(FreemarkerResult.class);
	private Map paramMap;

	private static boolean hasSetTemplateCacheTime = false;

	private static Object LockedObject = new Object();

	private final static String TemplateCache = "TemplateCache";

	private final static String cahceExclude = "cahceExclude";

	public Map getParamMap() {
		if (paramMap == null) {
			paramMap = new HashMap();
		}
		return paramMap;
	}

	public void setParamMap(Map map) {
		this.paramMap = map;
		if (errorLocation == null) {
			if (map.get("errorLocation") != null) {
				errorLocation = String.valueOf(map.get("errorLocation"));
			}
		}
		if (notFoundLocation == null) {
			if (map.get("404Location") != null) {
				notFoundLocation = String.valueOf(map.get("404Location"));
			} else {
				notFoundLocation = errorLocation;
			}
		}
	}

	private static boolean hasSetSharedVariable = false;

	private static String errorLocation = null;

	private static String notFoundLocation = null;

	public void flow(Action action, Object location) {
		LOG.debug("result[FreemarkerResult]:called location[" + location + "]");
		RuleModel ruleModel = SwebConfigure.getUrlModel(WebContext.getUrl());

		freemarker.template.Configuration freemarkConfig = null;
		String encode = "gbk";
		if (ruleModel.getEncode() != null && !"".equals(ruleModel.getEncode())) {
			freemarkConfig = FreemarkerManager.getConfiguration(ruleModel
					.getEncode());
			encode = ruleModel.getEncode();
		} else {
			freemarkConfig = FreemarkerManager.getConfiguration(SwebConfigure
					.getSwebModel().getEncode());
			encode = SwebConfigure.getSwebModel().getEncode();
		}
		ScopesHashModel model = (ScopesHashModel) FreemarkerManager
				.createModel(WebContext.getServletContext(),
						WebContext.getRequest(), WebContext.getResponse(),
						ActionContext.getContext());

		IResource ir = SwebConfigure.getResourceByUrl(WebContext.getUrl());
		ActionContext.getContext().put("i18n", ir);
		ActionContext.getContext().getValueStack().push(action);
		if (location instanceof String) {
			if (((String) location).indexOf("?") != -1) {
				location = ((String) location).substring(0,
						((String) location).indexOf("?"));
			}
		}
		try {
			// freemarkConfig.setSharedVariable("DateHelper", new DateHelper());
			if (!hasSetSharedVariable) {
				try {
					Iterator paramIterator = getParamMap().keySet().iterator();
					while (paramIterator.hasNext()) {
						String key = String.valueOf(paramIterator.next());
						String classPath = String.valueOf(getParamMap()
								.get(key));
						if (hasClass(classPath)) {
							freemarkConfig.setSharedVariable(key,
									BeanHelper.newInstance(classPath));
						}
					}
					 
				} finally {
					hasSetSharedVariable = true;
				}
			}

			Template template = null;
			boolean notFound = false;
			try {
				template = getTemplate(freemarkConfig, (String) location);
			} catch (IOException e) {
				notFound = true;
				System.err.println("rend error:["+(String) location+"]["+e.getMessage()+"]");
				e.printStackTrace();
				if (notFoundLocation != null) {
					Throwable trowable = e;
					while (trowable.getCause() != null) {
						if (trowable instanceof InvocationTargetException) {
							trowable = ((InvocationTargetException) trowable)
									.getTargetException();
						}
						if (trowable.getCause() != null) {
							trowable = trowable.getCause();
						}
					}
					if (action instanceof DefaultAction) {
						((DefaultAction) action).setMsg("page["+(String) location+"] not found:");
					} else {
						ActionContext.getContext().put("msg","page["+(String) location+"] not found:");
						ActionContext.getContext().put("notFountUrl", (String) location);
					}

					template = this.getTemplate(freemarkConfig, notFoundLocation);
				}else{
					throw new ActionException(
							"error render template[" + location + "]", e);
				}
			}

			if (ruleModel.getEncode() != null
					&& !"".equals(ruleModel.getEncode())) {
				template.setEncoding(ruleModel.getEncode());
				WebContext.getResponse().setCharacterEncoding(
						ruleModel.getEncode());
			} else {
				template.setEncoding(SwebConfigure.getSwebModel().getEncode());
				WebContext.getResponse().setCharacterEncoding(
						SwebConfigure.getSwebModel().getEncode());
			}
			/**
			 * 登陆页面需要缓存用户名密码
			 */
			if (!cahceExclude(String.valueOf(location))
					&& !"Y".equalsIgnoreCase(ruleModel.getHtmlcache())
					&& !"true".equalsIgnoreCase(ruleModel.getHtmlcache())) {
				WebContext.getResponse().setHeader("Pragma", "no-cache");
				WebContext.getResponse().setHeader("Cache-Control", "no-cache");
				WebContext.getResponse().setDateHeader("Expires", 0);
			}
			/**
			 * 添加header头
			 */
			if (ruleModel.getHeaderParamModelList() != null
					&& ruleModel.getHeaderParamModelList().size() > 0) {
				for (int i = 0; i < ruleModel.getHeaderParamModelList().size(); i++) {
					RuleHeaderParamModel headerParamModel = (RuleHeaderParamModel) ruleModel
							.getHeaderParamModelList().get(i);
					if (headerParamModel.getType() != null) {
						headerParamModel.setType(headerParamModel.getType()
								.trim());
					}
					if ("int".equalsIgnoreCase(headerParamModel.getType())) {
						WebContext.getResponse().setIntHeader(
								headerParamModel.getName(),
								Integer.parseInt(headerParamModel.getValue()
										.trim()));
					} else if ("date".equalsIgnoreCase(headerParamModel
							.getType())) {
						WebContext.getResponse().setDateHeader(
								headerParamModel.getName(),
								Long.parseLong(headerParamModel.getValue()
										.trim()));
					} else {
						WebContext.getResponse().setHeader(
								headerParamModel.getName(),
								headerParamModel.getValue());
					}
				}
			}
			if (WebContext.getUrl().toLowerCase().endsWith(".js")) {
				WebContext.getResponse().setContentType(
						"text/javascript;charset=" + encode);
			} else if (WebContext.getUrl().toLowerCase().endsWith(".css")) {
				WebContext.getResponse().setContentType(
						"text/css;charset=" + encode);
			} else if (WebContext.getUrl().toLowerCase().endsWith("html")
					|| WebContext.getUrl().toLowerCase().endsWith("htm")) {
				WebContext.getResponse().setContentType(
						"text/html;charset=" + encode);
			} else {
				WebContext.getResponse().setContentType(
						"text/html;charset=" + encode);
			}
			StringWriter writer = new StringWriter();
			long startTime = System.currentTimeMillis();
			try {
				template.process(model, writer);
				writer.flush();
			} catch (Throwable e) {
				e.printStackTrace();
				if (errorLocation != null) {
					Throwable trowable = e;
					while (trowable.getCause() != null) {
						if (trowable instanceof InvocationTargetException) {
							trowable = ((InvocationTargetException) trowable)
									.getTargetException();
						}
						if (trowable.getCause() != null) {
							trowable = trowable.getCause();
						}
					}
					if (action instanceof DefaultAction) {
						((DefaultAction) action).setMsg("render error:"
								+ trowable.getMessage());
					} else {
						ActionContext.getContext().put("msg",
								"render error:" + trowable.getMessage());
					}
					writer.flush();
					writer.close();
					writer = new StringWriter();
					template = this.getTemplate(freemarkConfig, errorLocation);
					template.process(model, writer);
					writer.flush();
				}
			} finally {
				LOG.debug("render template[" + location + "] couttime["
						+ (System.currentTimeMillis() - startTime) + "]");
				try {
					writer.close();
				} catch (Exception e) {
					LOG.error(e);
				}
			}
			ServletOutputStream sos = WebContext.getResponse()
					.getOutputStream();
			try {
				if (ruleModel.getEncode() != null
						&& !"".equals(ruleModel.getEncode())) {
					sos.write(writer.toString().getBytes(ruleModel.getEncode()));
				} else {
					String result = writer.toString();
					sos.write(result.getBytes(SwebConfigure.getSwebModel()
							.getEncode()));
				}

			} finally {
				if (shouldClose()) {
					sos.close();
				}
			}
		} catch (IOException e) {
			throw new ActionException(
					"error render template[" + location + "]", e);
		} catch (TemplateException e) {
			throw new ActionException(
					"error render template[" + location + "]", e);
		}
	}

	private boolean cahceExclude(String url) {
		if (getParamMap().get(cahceExclude) != null
				&& !"".equals(getParamMap().get(cahceExclude))) {
			String excludeUrls = (String) getParamMap().get(cahceExclude);
			if (excludeUrls.indexOf(Constants.separator) == -1) {
				return RegexpHelper.isGlobmatches(url, excludeUrls);
			} else {
				String[] excludeUrlss = excludeUrls.split(Constants.separator);
				boolean flag = false;
				for (int i = 0; i < excludeUrlss.length; i++) {
					if (RegexpHelper.isGlobmatches(url, excludeUrlss[i])) {
						flag = true;
					}
				}
				return flag;
			}
		}
		return false;
	}

	private boolean shouldClose() {
		if ("true".equalsIgnoreCase((String) getParamMap().get("close"))
				|| "Y".equalsIgnoreCase((String) getParamMap().get("close"))) {
			return true;
		}
		return false;
	}

	private boolean hasClass(String classPath) {
		try {
			Class.forName(classPath);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private Template getTemplate(
			freemarker.template.Configuration freemarkConfig, String location)
			throws IOException {
		if (getParamMap().get(TemplateCache) != null) {
			try {
				if (!hasSetTemplateCacheTime) {
					int cacheTimes = Integer.parseInt((String) getParamMap()
							.get(TemplateCache));
					freemarkConfig.setTemplateUpdateDelay(cacheTimes);
					synchronized (LockedObject) {
						hasSetTemplateCacheTime = true;
					}
				}
			} catch (Exception e) {
				synchronized (LockedObject) {
					hasSetTemplateCacheTime = true;
				}
				return freemarkConfig.getTemplate(location);
			}
		}
		return freemarkConfig.getTemplate(location);
	}
}
