package org.sevenstar.web.interceptor;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sevenstar.web.Constants;
import org.sevenstar.web.cfg.SwebConfigure;
import org.sevenstar.web.context.WebContext;
import org.sevenstar.web.exception.ActionException;
import org.sevenstar.web.invocation.Invocation;

import com.opensymphony.xwork.ActionContext;

/**
 * @author rtm 2008-5-8
 */
public class UploadInterceptor implements Interceptor {
	private Map paramMap;
	private static String MAXSIZE = "maxsize";
	private static String FOLDER = "folder";
	private static String ALLOW = "allow";
	private static String DENY = "deny";
	private static String PATH_LOGIC = "path_logic";
	private static String PATH_ABSOLUTE = "path_absolute";
	private Log LOG = LogFactory.getLog(UploadInterceptor.class);

	private static String currentDirPath = null;

	private static String absoluteRootPath = null;

	private static String logicRootPath = null;

	private static Object LockedObject = new Object();

	public static String getRootPath() {
		return currentDirPath;
	}

	public static String getLogicPath(File file) {
		if (file == null) {
			return null;
		}
		String path = file.getPath();
		if (logicRootPath != null && !"".endsWith(logicRootPath)) {
            return path.substring(currentDirPath.length()-logicRootPath.length());
		} else {
			return path.substring(currentDirPath.length());
		}
	}

	public Object intercept(Invocation invocation) {
		LOG.debug("interceptor[UploadInterceptor]:before call  ");
		long maxSize = Long.MAX_VALUE;
		if (getParamMap().get(MAXSIZE) != null
				&& !"".equals(getParamMap().get(MAXSIZE))) {
			try {
				maxSize = Long.parseLong((String) getParamMap().get(MAXSIZE));
			} catch (Exception e) {
				LOG.error(e);
			}
		}
		if (currentDirPath == null) {
			synchronized (LockedObject) {
				String path_logic = (String) getParamMap().get(PATH_LOGIC);
				if (path_logic != null && !"".equals(path_logic)
						&& !path_logic.startsWith("/")) {
					path_logic = "/" + path_logic;
				}
				logicRootPath = path_logic;
				String path_absolute = (String) getParamMap()
						.get(PATH_ABSOLUTE);

				if (path_logic != null && !"".equals(path_logic)) {
					currentDirPath = WebContext.getRequest().getRealPath(
							path_logic);
				}
				if (path_absolute != null && !"".equals(path_absolute)) {
					currentDirPath = path_absolute;
				}
				absoluteRootPath = path_absolute;
				if (currentDirPath == null || "".equals(currentDirPath)) {
					throw new ActionException("没有设置上传路径");
				}
			}
		}
		// 检查输入请求是否为multipart表单数据。
		boolean isMultipart = ServletFileUpload.isMultipartContent(WebContext
				.getRequest());
		if (!isMultipart) {
			LOG
					.debug("interceptor[UploadInterceptor]:no encType='multipart/form-data' defined ");
			LOG.debug("interceptor[UploadInterceptor]:after call  ");
			return invocation.invoke();
		}
		// 为该请求创建一个DiskFileItemFactory对象，通过它来解析请求。执行解析后，所有的表单项目都保存在一个List中。
		DiskFileItemFactory factory = new DiskFileItemFactory();
  		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setHeaderEncoding(SwebConfigure.getSwebModel().getEncode());
		upload.setSizeMax(maxSize);
		try {
			List items = upload.parseRequest(WebContext.getRequest());
			Iterator itr = items.iterator();
			Map parameterMap = new HashMap();
			parameterMap.putAll(ActionContext.getContext().getParameters());
			while (itr.hasNext()) {
				FileItem item = (FileItem) itr.next();
				// 检查当前项目是普通表单项目还是上传文件。
				if (item.isFormField()) {// 如果是普通表单项目，显示表单内容。
					String fieldName = item.getFieldName();
					String value = item.getString();
					if (value != null && fieldName != null
							&& !"".equals(fieldName)) {
						try {
							value = new String(value.getBytes("iso8859-1"),
									SwebConfigure.getSwebModel().getEncode());
						} catch (UnsupportedEncodingException e) {
							LOG.error(e);
						}
						if(parameterMap.get(fieldName) == null){
							parameterMap.put(fieldName, value);
						}else{
							if(parameterMap.get(fieldName) instanceof Object[]){
								Object[] values = (Object[])parameterMap.get(fieldName); 
								Object[] newValues = new Object[values.length+1];
								System.arraycopy(values, 0, newValues, 0, values.length);
								newValues[newValues.length-1] = value;
								parameterMap.put(fieldName, newValues);
							}else{
								Object[] newValues = new Object[2];
								newValues[0]  = parameterMap.get(fieldName);
								newValues[1] = value;
								parameterMap.put(fieldName, newValues);
							}
						}
						
					}
				} else {
					// 如果是上传文件，则设置新的文件名并且保存
					String fileNameLong = item.getName();
					// 如果上传文件输入框为空，则将上传文件输入字段设置为null并返回
					if (fileNameLong == null
							|| fileNameLong.trim().length() == 0) {
					//	parameterMap.put(item.getFieldName(), null);
						continue;
					}
					fileNameLong = fileNameLong.replace('\\', '/');
					String[] pathParts = fileNameLong.split("/");
					String fileName = pathParts[pathParts.length - 1];
					if (fileName.indexOf(" ") != -1) {
						fileName = fileName.replaceAll(" ", "_");
					}
					String nameWithoutExt = getNameWithoutExtension(fileName);
					if (nameWithoutExt.indexOf(" ") != -1) {
						nameWithoutExt = nameWithoutExt.replaceAll(" ", "_");
					}
					String ext = getExtension(fileName);
					// nameWithoutExt = nameWithoutExt.getBytes().toString() +
					// "-"+ System.currentTimeMillis();

					StringBuffer errorMsg = new StringBuffer();
					if (allow(ext)) {
						try {
							Calendar cal = Calendar.getInstance();
							cal.setTime(new java.util.Date());
							String year = String
									.valueOf(cal.get(Calendar.YEAR));
							String month = String.valueOf(cal
									.get(Calendar.MONTH) + 1);
							String day = String.valueOf(cal
									.get(Calendar.DAY_OF_MONTH));
							if (month.length() == 1) {
								month = "0" + month;
							}
							if (day.length() == 1) {
								day = "0" + day;
							}
							long currentTime = System.currentTimeMillis();
							File pathToSave = new File(getRealPath(
									currentDirPath, year, month, day),
									nameWithoutExt + "_" + currentTime + "."
											+ ext);
							String fileUrl = getLogicPath(currentDirPath, year,
									month, day)
									+ "/" + nameWithoutExt + "." + ext;
							item.write(pathToSave);
							/**
							 * 清理临时文件
							 */
							File tmpFile = ((DiskFileItem)item).getStoreLocation();
							if(tmpFile !=null && tmpFile.isFile() && !tmpFile.getPath().equals(pathToSave.getPath())){
								((DiskFileItem)item).delete();
							}
							if(parameterMap.get(item.getFieldName()) == null){
								parameterMap.put(item.getFieldName(), pathToSave);
							}else{
								if(parameterMap.get(item.getFieldName()) instanceof Object[]){
									Object[] values = (Object[])parameterMap.get(item.getFieldName()); 
									Object[] newValues = new Object[values.length+1];
									System.arraycopy(values, 0, newValues, 0, values.length);
									newValues[newValues.length-1] = pathToSave;
									parameterMap.put(item.getFieldName(), newValues);
								}else{
									Object[] newValues = new Object[2];
									newValues[0]  = parameterMap.get(item.getFieldName());
									newValues[1] = pathToSave;
									parameterMap.put(item.getFieldName(), newValues);
								}
							}
							//parameterMap.put(item.getFieldName(), pathToSave);
						} catch (Exception e) {
							throw new ActionException(e);
						}
					} else {
						throw new ActionException(errorMsg.toString());
					}
				}
			}
			ActionContext.getContext().setParameters(parameterMap);
		} catch (FileUploadException e) {
			LOG.warn(e);
		}
		LOG.debug("interceptor[UploadInterceptor]:after call  ");
		return invocation.invoke();
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

	private boolean allow(String ext) {
		if (ext == null || "".equals(ext)) {
			throw new ActionException("扩展名为空禁止上传");
		}
		String allowedExtensionsFile = "";
		String deniedExtensionsFile = "";
		if (this.getParamMap().get(ALLOW) != null) {
			allowedExtensionsFile = (String) this.getParamMap().get(ALLOW);
		}
		if (this.getParamMap().get(DENY) != null) {
			deniedExtensionsFile = (String) this.getParamMap().get(DENY);
		}
		List allowedExtensionsFileList = stringToList(allowedExtensionsFile);
		List deniedExtensionsFileList = stringToList(deniedExtensionsFile);
		if (deniedExtensionsFileList.contains(ext.toLowerCase())) {
			throw new ActionException("扩展名为[" + ext + "]禁止上传");
		}
		if (allowedExtensionsFileList.size() == 0
				|| allowedExtensionsFileList.contains(ext.toLowerCase())) {
			return true;
		}
		throw new ActionException("扩展名为[" + ext + "]的文件不在服务器的可上传文件范围内");
	}

	private List stringToList(String str) {
		if (str == null || "".equals(str)) {
			return new ArrayList();
		}
		String[] strArr = str.split(Constants.separator);

		ArrayList tmp = new ArrayList();
		if (str.length() > 0) {
			for (int i = 0; i < strArr.length; ++i) {
				tmp.add(strArr[i].toLowerCase());
			}
		}
		return tmp;
	}

	private String getNameWithoutExtension(String fileName) {
		if (fileName.indexOf(".") != -1) {
			return fileName.substring(0, fileName.lastIndexOf("."));
		} else {
			return fileName;
		}
	}

	private String getExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}

	private String getLogicPath(String rootpath, String year, String month,
			String day) {
		if (rootpath.endsWith("/")) {
			return rootpath + year + "/" + month + "/" + day;
		}
		return rootpath + "/" + year + "/" + month + "/" + day;
	}

	private String getRealPath(String rootpath, String year, String month,
			String day) {
		File yearFile = new File(rootpath + "/" + year);
		if (!yearFile.exists()) {
			yearFile.mkdir();
		}

		File monthFile = new File(rootpath + "/" + year + "/" + month);
		if (!monthFile.exists()) {
			monthFile.mkdir();
		}

		File dayFile = new File(rootpath + "/" + year + "/" + month + "/" + day);
		if (!dayFile.exists()) {
			dayFile.mkdir();
		}
		return rootpath + "/" + year + "/" + month + "/" + day;
	}

}
