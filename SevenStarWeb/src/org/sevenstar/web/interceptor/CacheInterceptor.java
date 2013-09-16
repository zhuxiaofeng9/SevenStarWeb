package org.sevenstar.web.interceptor;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.component.cache.ehcache.EHCacheHelper;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.invocation.Invocation;
import org.sevenstar.web.action.Action;

import org.sevenstar.util.RegexpHelper;

import java.util.Iterator;

/**
 * @author rtm 2008-5-16
 */
public class CacheInterceptor implements Interceptor {
	private static Log LOG = LogFactory.getLog(CacheInterceptor.class);
	private Map paramMap;

	public Map getParamMap() {
		if (this.paramMap == null) {
			this.paramMap = new HashMap();
		}
		return this.paramMap;
	}

	public Object intercept(Invocation invocation) {
		if (EHCacheHelper.get(getCache(WebContext.getUrl()).getName(), WebContext.getUrl()) != null) {
			invocation.setAction((Action) EHCacheHelper.get(getCache(WebContext.getUrl()).getName(),
					WebContext.getUrl()));
			invocation.setHasExecuted();
			return Action.SUCCESS;
		} else {
			Object result = invocation.invoke();
			EHCacheHelper.put(getCache(WebContext.getUrl()).getName(), WebContext.getUrl(), invocation
					.getAction());
			return result;
		}
	}

	private Cache getCache(String url) {
		if (this.getParamMap().size() == 0) {
			return EHCacheHelper.getCache("webcache");
		} else {
			Iterator iter = this.getParamMap().keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				String value = (String) this.getParamMap().get(key);
				if (RegexpHelper.isGlobmatches(WebContext.getUrl(), key)) {
					try {
						long seconds = Long.parseLong(value);
						Cache cache = EHCacheHelper.getCache(key);
						cache.getCacheConfiguration().setTimeToIdleSeconds(seconds);
						cache.getCacheConfiguration().setTimeToLiveSeconds(seconds);
						LOG.debug("url["+WebContext.getUrl()+"] return cache["+key+"] live["+value+"] idle["+value+"]");
						return cache;
					} catch (Exception e) {
						LOG.debug("url["+WebContext.getUrl()+"] return cache[webcache] live[600] idle[600]");
						return EHCacheHelper.getCache("webcache");
					}
				}
			}
			LOG.debug("url["+WebContext.getUrl()+"] return cache[webcache] live[600] idle[600]");
			return EHCacheHelper.getCache("webcache");
		}
	}

	public void setParamMap(Map map) {
		this.paramMap = map;
		if (this.paramMap == null) {
			this.paramMap = new HashMap();
		}
	}

	private String[] getAllPattern() {
		String[] patterns = new String[getParamMap().size()];
		return null;
	}

}
