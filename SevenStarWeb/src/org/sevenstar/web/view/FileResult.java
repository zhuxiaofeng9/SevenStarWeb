package org.sevenstar.web.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.web.action.Action;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.exception.ActionException;

public class FileResult implements Result {
	private Log LOG = LogFactory.getLog(FileResult.class);
	private Map paramMap;

	private String encode = "gbk";

	private String mode = "attachment";

	public void flow(Action action, Object location) {
		File file = (File) location;
		HttpServletResponse resp = WebContext.getResponse();
		String fileName = null;
		try {
			fileName = new String((file.getName()).getBytes(encode),
					"iso8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new ActionException(e);
		}
		resp.setContentType("application/x-msdownload:charset=" + encode);
		resp.setHeader("Content-Disposition", mode + ";filename=" + fileName);
		FileInputStream fis = null;
		OutputStream os = null;
		try {
			fis = new FileInputStream(file);
			os = resp.getOutputStream();
			byte[] bs = new byte[1024];
			int length = -1;
			while ((length = fis.read(bs)) != -1) {
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
				if (fis != null) {
					fis.close();
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
		if (this.paramMap != null && this.paramMap.get("encode") != null) {
			encode = String.valueOf(this.paramMap.get("encode"));
		}
		if (this.paramMap != null && this.paramMap.get("mode") != null) {
			mode = String.valueOf(this.paramMap.get("mode"));
			if (!"attachment".equalsIgnoreCase(mode)
					&& !"inline".equalsIgnoreCase(mode)) {
				mode = "attachment";
			}
		}
	}

}
