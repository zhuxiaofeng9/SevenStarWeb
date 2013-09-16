package org.sevenstar.web.view;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.exception.ActionException;

public class InputStreamResult implements Result {
	private Log LOG = LogFactory.getLog(InputStreamResult.class);
	private Map paramMap;

	public void flow(Action action, Object location) {
		HttpServletResponse resp = WebContext.getResponse();
		InputStream is = (InputStream) location;
		OutputStream os = null;
		try {
			os = resp.getOutputStream();
			byte[] bs = new byte[1024];
			int length = -1;
			while ((length = is.read(bs)) != -1) {
				if (length == 1024) {
					os.write(bs);
				} else {
					byte[] tmp = new byte[length];
					System.arraycopy(bs, 0, tmp, 0, length);
					os.write(tmp);
				}
				bs = new byte[1024];
			}

		} catch (FileNotFoundException e) {
			throw new ActionException(e);
		} catch (IOException e) {
			throw new ActionException(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				throw new ActionException(e);
			}
		}

	}

	public Map getParamMap() {
		if (paramMap == null) {
			paramMap = new HashMap();
		}

		return paramMap;
	}

	public void setParamMap(Map map) {
		this.paramMap = map;
	}

}
