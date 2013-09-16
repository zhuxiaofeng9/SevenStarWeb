package org.sevenstar.web.view.freemarker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.component.freemarker.SevenStarBeanWrapper;
import org.sevenstar.component.freemarker.SevenStarClassTemplateLoader;
import org.sevenstar.web.context.WebContext;

import com.opensymphony.xwork.ActionContext;

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;

public class FreemarkerManager {
	private static final Log LOG = LogFactory.getLog(FreemarkerManager.class);
	private static final String ATTR_REQUEST_MODEL = ".freemarker.Request";
	private static final String ATTR_REQUEST_PARAMETERS_MODEL = ".freemarker.RequestParameters";
	public static final String CONFIG_SERVLET_CONTEXT_KEY = ".freemarker.ServletContext";
	public static final String KEY_APPLICATION = "Application";
	public static final String KEY_REQUEST_MODEL = "Request";
	public static final String KEY_SESSION_MODEL = "Session";
	public static final String KEY_JSP_TAGLIBS = "JspTaglibs";
	public static final String KEY_REQUEST_PARAMETER_MODEL = "Parameters";
	public static final String KEY_EXCEPTION = "exception";

	private static freemarker.template.Configuration CFG = null;
	
	
    private static Map cfgMap = new HashMap();
 
	private static Object LockedObject = new Object();

	static {
		init();
		// SpringFreemarkerIntergrade.inject(WebContext.getServletContext(),CFG);
	}

	private static void init() {
		synchronized (LockedObject) {
			cfgMap.put("gbk", getConfig("gbk"));
			cfgMap.put("utf-8", getConfig("utf-8"));
			/*
			CFG = null;
			CFG = new freemarker.template.Configuration();
			CFG.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[] {
					new WebappTemplateLoader(WebContext.getServletContext()),
					new SevenStarClassTemplateLoader() }));
			CFG
					.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
			CFG.setObjectWrapper(new SevenStarBeanWrapper());
			CFG.setWhitespaceStripping(true);
			CFG.setDefaultEncoding(SwebConfigure.getSwebModel().getEncode());
			CFG.setOutputEncoding(SwebConfigure.getSwebModel().getEncode());
			CFG.setNumberFormat("0.####");
			loadSettings(WebContext.getServletContext(), CFG);
			*/
 		}
	}
	
	private static freemarker.template.Configuration getConfig(String encode){
		freemarker.template.Configuration config =   new freemarker.template.Configuration();
		config.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[] {
				new WebappTemplateLoader(WebContext.getServletContext()),
				new SevenStarClassTemplateLoader() }));
		config
				.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		config.setObjectWrapper(new SevenStarBeanWrapper());
		config.setWhitespaceStripping(true);
		config.setDefaultEncoding(encode);
		config.setOutputEncoding(encode);
		config.setNumberFormat("0.####");
		loadSettings(WebContext.getServletContext(), config);
		return config;
	}

	public static freemarker.template.Configuration getConfiguration(String encode) {
		if(cfgMap.containsKey(encode.toLowerCase())){
			return ( freemarker.template.Configuration)cfgMap.get(encode.toLowerCase());
		}else{
			cfgMap.put(encode.toLowerCase(), getConfig(encode.toLowerCase()));
			return ( freemarker.template.Configuration)cfgMap.get(encode.toLowerCase());
		}
		//return CFG;
	}

	private static void loadSettings(ServletContext servletContext,
			freemarker.template.Configuration configuration) {
		InputStream in = null;
		try {
			in = loadFile("freemarker.properties", FreemarkerManager.class);

			if (in != null) {
				Properties p = new Properties();
				p.load(in);
				configuration.setSettings(p);
			}
		} catch (IOException e) {
			LOG
					.error(
							"Error while loading freemarker settings from /freemarker.properties",
							e);
		} catch (TemplateException e) {
			LOG
					.error(
							"Error while loading freemarker settings from /freemarker.properties",
							e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
	}

	public static TemplateModel createModel(ServletContext servletContext,
			HttpServletRequest request, HttpServletResponse response,
			ActionContext actionContext) {
		ScopesHashModel model = buildScopesHashModel(servletContext, request,
				response, new SevenStarBeanWrapper(), actionContext);
		populateContext(model, request, response, actionContext);
		return model;
	}

	public static ScopesHashModel buildScopesHashModel(
			ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response, ObjectWrapper wrapper,
			ActionContext actionContext) {
		ScopesHashModel model = new ScopesHashModel(wrapper, servletContext,
				request, actionContext.getValueStack());

		HttpSession session = request.getSession(false);
		if (session != null) {
			model.put(KEY_SESSION_MODEL, new HttpSessionHashModel(session,
					wrapper));
		} else {
			// no session means no attributes ???
			// model.put(KEY_SESSION_MODEL, new SimpleHash());
		}
		// Create hash model wrapper for the request
		HttpRequestHashModel requestModel = (HttpRequestHashModel) request
				.getAttribute(ATTR_REQUEST_MODEL);

		if ((requestModel == null) || (requestModel.getRequest() != request)) {
			requestModel = new HttpRequestHashModel(request, response, wrapper);
			request.setAttribute(ATTR_REQUEST_MODEL, requestModel);
		}

		model.put(KEY_REQUEST_MODEL, requestModel);

		// Create hash model wrapper for request parameters
		HttpRequestParametersHashModel reqParametersModel = (HttpRequestParametersHashModel) request
				.getAttribute(ATTR_REQUEST_PARAMETERS_MODEL);
		if (reqParametersModel == null || requestModel.getRequest() != request) {
			reqParametersModel = new HttpRequestParametersHashModel(request);
			request.setAttribute(ATTR_REQUEST_PARAMETERS_MODEL,
					reqParametersModel);
		}
		model.put(KEY_REQUEST_PARAMETER_MODEL, reqParametersModel);
		return model;
	}

	public static void populateContext(ScopesHashModel model,
			HttpServletRequest req, HttpServletResponse resp,
			ActionContext actionContext) {
		Map standard = new HashMap();
		standard.put("request", req);
		standard.put("req", req);
		standard.put("response", resp);
		standard.put("res", resp);
		standard.put("session", req.getSession(false));
		standard.put("contextPath", req.getContextPath());
		standard.put("stack", actionContext.getValueStack());
		standard.put("contextUrl", req.getContextPath());
		String base = req.getContextPath();
		
		if(base != null && base.indexOf(".") != -1){
			base = base.substring(0,base.indexOf("."));
		}
		standard.put("base", base);
		model.putAll(standard);
		Throwable exception = (Throwable) req
				.getAttribute("javax.servlet.error.exception");
		if (exception == null) {
			exception = (Throwable) req
					.getAttribute("javax.servlet.error.JspException");
		}
		if (exception != null) {
			model.put(KEY_EXCEPTION, exception);
		}
	}

	private static URL getResource(String resourceName, Class callingClass) {
		URL url = null;

		url = Thread.currentThread().getContextClassLoader().getResource(
				resourceName);

		if (url == null) {
			url = SevenStarClassTemplateLoader.class.getClassLoader()
					.getResource(resourceName);
		}

		if (url == null) {
			url = callingClass.getClassLoader().getResource(resourceName);
		}

		return url;
	}

	private static InputStream loadFile(String fileName, Class clazz) {
		URL fileUrl = getResource(fileName, clazz);

		if (fileUrl == null) {
			return null;
		}

		InputStream is;

		try {
			is = fileUrl.openStream();

			if (is == null) {
				throw new IllegalArgumentException("No file '" + fileName
						+ "' found as a resource");
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("No file '" + fileName
					+ "' found as a resource");
		}

		return is;
	}

}
