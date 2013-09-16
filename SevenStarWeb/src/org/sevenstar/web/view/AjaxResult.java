package org.sevenstar.web.view;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.util.BeanHelper;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.exception.ActionException;
import org.stringtree.json.JSONWriter;

import com.thoughtworks.xstream.XStream;

public class AjaxResult implements Result {

	private static Log LOG = LogFactory.getLog(AjaxResult.class);

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

	private String getContentType() {
		if (this.getParamMap().get("ContentType") != null
				&& !"".equals(this.getParamMap().get("ContentType"))) {
			return String.valueOf(this.getParamMap().get("ContentType"));
		}
		return "text/json";
	}

	private String getResponseEncode() {
		if (this.getParamMap().get("response_encode") != null
				&& !"".equals(this.getParamMap().get("response_encode"))) {
			return String.valueOf(this.getParamMap().get("response_encode"));
		}
		return SwebConfigure.getSwebModel().getEncode();
	}

	private String getRequestEncode() {
		if (this.getParamMap().get("request_encode") != null
				&& !"".equals(this.getParamMap().get("request_encode"))) {
			return String.valueOf(this.getParamMap().get("request_encode"));
		}
		if (this.getParamMap().get("json_encode") != null
				&& !"".equals(this.getParamMap().get("json_encode"))) {
			return String.valueOf(this.getParamMap().get("json_encode"));
		}
		if (this.getParamMap().get("xml_encode") != null
				&& !"".equals(this.getParamMap().get("xml_encode"))) {
			return String.valueOf(this.getParamMap().get("xml_encode"));
		}
		return SwebConfigure.getSwebModel().getEncode();
	}

	private boolean shouldClose() {
		if ("true".equalsIgnoreCase((String) getParamMap().get("close"))
				|| "Y".equalsIgnoreCase((String) getParamMap().get("close"))) {
			return true;
		}
		return false;
	}

	public void doJsonFlow(Action action, Object location) {
		/**
		 * json格式
		 */
		JSONWriter jsonWriter = new JSONWriter();
  		OutputStream os = null;
		try {
			WebContext.getResponse().setCharacterEncoding(getResponseEncode());
			WebContext.getResponse().setHeader("Charset",getResponseEncode());  
			WebContext.getResponse().setContentType(
					getContentType() + "; charset=" + getResponseEncode());
			os = WebContext.getResponse().getOutputStream();
			String result = jsonWriter.write(location);
 			// os.write(result.getBytes("utf-8"));//
			os.write(result.getBytes(getRequestEncode()));
		} catch (Exception e) {
			throw new ActionException(e);
		} finally {

			if (shouldClose()) {
				try {
					os.close();
				} catch (IOException e) {
					LOG.error(e);
				}
			}

		}
	}

	public void flow(Action action, Object location) {
		if ("text/json".equalsIgnoreCase(getContentType())) {
			doJsonFlow(action, location);
			return;
		} else if ("text/xml".equalsIgnoreCase(getContentType())) {
			doXmlFlow(action, location);
			return;
		} else {
			throw new ActionException(
					"error content type,should be text/json or text/xml");
		}
	}

	private static Map aliasMap = null;

	private static Object LockedObject = new Object();

	public void doXmlFlow(Action action, Object location) {
		OutputStream os = null;
		try {
			String xml = "";
			WebContext.getResponse().setCharacterEncoding(getResponseEncode());
			WebContext.getResponse().setContentType(
					"text/xml; charset=" + getResponseEncode());
			os = WebContext.getResponse().getOutputStream();
			if (location instanceof String) {
				xml = (String) location;
			} else {
				XStream xstream = new XStream();
				if (aliasMap == null) {
					synchronized (LockedObject) {
						aliasMap = new HashMap();
						if (paramMap != null && paramMap.entrySet() != null) {
							Set entrySet = paramMap.entrySet();
							Iterator entryItr = entrySet.iterator();
							
							while (entryItr.hasNext()) {
								Entry entry = (Entry) entryItr.next();
								String key = (String) entry.getKey();
								try {
									Class clazz = BeanHelper
											.loadClass((String) entry
													.getValue());
									aliasMap
											.put(key, clazz);
								} catch (Throwable e) {
                                    //pass
								}
							}
						}
					}
				}
				if (aliasMap != null && aliasMap.entrySet() != null) {
					Set entrySet = aliasMap.entrySet();
					Iterator entryItr = entrySet.iterator();
					while (entryItr.hasNext()) {
						Entry entry = (Entry) entryItr.next();
						xstream.alias((String) entry.getKey(), (Class) entry
								.getValue());
					}
				}
				xml = xstream.toXML(location);
			}
			os.write(xml.getBytes(getRequestEncode()));
		} catch (Exception e) {
			throw new ActionException(e);
		} finally {
			if (shouldClose() && os != null) {
				try {
					os.close();
				} catch (Exception e) {
					LOG.error(e);
				}
			}
		}
	}

}
