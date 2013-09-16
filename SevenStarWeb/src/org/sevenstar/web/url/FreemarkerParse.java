package org.sevenstar.web.url;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * @author rtm 2008-5-8
 */
public class FreemarkerParse implements IParse {
	private Log LOG = LogFactory.getLog(FreemarkerParse.class);
	public String parse(String url) {
		String showUrl = url;
		String preUrl = url;
		String lastUrl = "";
		if (preUrl.indexOf("?") != -1) {
			preUrl = url.substring(0, preUrl.indexOf("?"));
			lastUrl = url.substring(url.indexOf("?") + 1);
		}
		String preUrlNoExt = preUrl;
		String ext = "";
		if (preUrlNoExt.indexOf(".") != -1) {
			preUrlNoExt = preUrlNoExt.substring(0, preUrlNoExt.indexOf("."));
			ext = preUrl.substring(preUrl.indexOf(".") + 1);
		}
		String newUrl = preUrlNoExt + "." + "ftl";
		if (lastUrl != null && !"".equals(lastUrl)) {
			newUrl = newUrl + "?" + lastUrl;
		}
		LOG.debug("parse[FreemarkerParse]:["+url+"]->["+newUrl+"]");
		return newUrl;
	}

}
