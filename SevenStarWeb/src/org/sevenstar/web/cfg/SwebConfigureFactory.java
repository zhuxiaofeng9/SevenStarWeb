package org.sevenstar.web.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.sevenstar.component.xml.Nodelet;
import org.sevenstar.component.xml.NodeletException;
import org.sevenstar.component.xml.NodeletParser;
import org.sevenstar.component.xml.NodeletUtils;
import org.sevenstar.util.BeanHelper;
import org.sevenstar.web.cfg.model.SwebModel;
import org.sevenstar.web.exception.ActionException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * @author rtm 2008-5-8
 */
public class SwebConfigureFactory {
	private static ISwebConfigure configureImpl;

	private static final String CONFIGPATH = "sweb-config.xml";

	private static SwebConfigModel configModel;
	
	private static  SwebModel swebModel;
	
	
	static{
		NodeletParser parser = new NodeletParser();
		configModel = new SwebConfigModel();
		addConfig(parser);
		try {
			parser.parse(NodeletParser.class.getClassLoader().getResourceAsStream(CONFIGPATH));
		} catch (NodeletException e) {
 			throw new ActionException(e);
		}
		configureImpl = (ISwebConfigure) BeanHelper.newInstance(configModel
				.getClassName());
		if (configModel.getParamList().size() > 0) {
			List paramModelList = configModel.getParamList();
			Map paramMap = new HashMap();
			for (int i = 0; i < paramModelList.size(); i++) {
				SwebConfigureParamModel paramModel = (SwebConfigureParamModel) paramModelList
						.get(i);
				paramMap.put(paramModel.getName(), paramModel
						.getValue());
			}
			configureImpl.setParamMap(paramMap);
		}
		swebModel = configureImpl.getSwebModel();
		
	}
	
	private static void addConfig(NodeletParser parser){
		parser.addNodelet("/sweb/config", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				configModel.setName(NodeletUtils.getNodeValue(map,"name"));
				configModel.setClassName(NodeletUtils.getNodeValue(map,"class"));
			}
		});
		parser.addNodelet("/sweb/config/param", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				SwebConfigureParamModel paramModel = new SwebConfigureParamModel();
				paramModel.setName(NodeletUtils.getNodeValue(map,"name"));
				paramModel.setValue(NodeletUtils.getNodeValue(map,"value"));
				configModel.addParamModel(paramModel);
			}
		});
		parser.addNodelet("/sweb/config/end()", new Nodelet() {
			public void process(Node node) throws Exception {
				//pass
  			}
		});
	}
	/*
	private static void parseByDom4j(){
		Document doc = XmlHelper.readByClassPath(CONFIGPATH);
		Element configEle = (Element) doc.selectObject("/sweb/config");
		configModel = new SwebConfigModel();
		configModel.setName(XmlHelper.getAttributeValue(configEle, "name"));
		configModel.setClassName(XmlHelper.getAttributeValue(configEle, "class"));
		List paramEleList = configEle.elements();
		if (paramEleList != null && paramEleList.size() > 0) {
			for (int i = 0; i < paramEleList.size(); i++) {
				Element paramEle = (Element) paramEleList.get(i);
				SwebConfigureParamModel paramModel =  new SwebConfigureParamModel();
				paramModel.setName(XmlHelper.getAttributeValue(paramEle, "name"));
				paramModel.setValue(XmlHelper.getAttributeValue(paramEle, "value"));
				configModel.addParamModel(paramModel);
			}
		}
		configureImpl = (ISwebConfigure) BeanHelper.newInstance(configModel
				.getClassName());
		if (configModel.getParamList().size() > 0) {
			List paramModelList = configModel.getParamList();
			Map paramMap = new HashMap();
			for (int i = 0; i < paramModelList.size(); i++) {
				SwebConfigureParamModel paramModel = (SwebConfigureParamModel) paramModelList
						.get(i);
				paramMap.put(paramModel.getName(), paramModel
						.getValue());
			}
			configureImpl.setParamMap(paramMap);
		}
		swebModel = configureImpl.getSwebModel();
	}
*/
	public static SwebModel getSwebModel() {
		return swebModel;
	}
 

	private static ISwebConfigure getConfigure() {
		return configureImpl;
	}
	public static void main(String[] args){
		SwebModel model = SwebConfigureFactory.getSwebModel();
		System.out.println("finish ..");
	}
 
}



class SwebConfigModel {
	private String name;
	private String className;
	private List paramList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List getParamList() {
		if (this.paramList == null) {
			this.paramList = new ArrayList();
		}
		return paramList;
	}

	public void setParamList(List paramList) {
		this.paramList = paramList;
	}

	public void addParamModel(SwebConfigureParamModel model) {
		getParamList().add(model);
	}
}

class SwebConfigureParamModel {
	private String name;
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
