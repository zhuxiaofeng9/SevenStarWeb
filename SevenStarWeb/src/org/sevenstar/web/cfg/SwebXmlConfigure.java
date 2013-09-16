package org.sevenstar.web.cfg;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sevenstar.component.xml.Nodelet;
import org.sevenstar.component.xml.NodeletException;
import org.sevenstar.component.xml.NodeletParser;
import org.sevenstar.component.xml.NodeletUtils;
import org.sevenstar.web.cfg.model.FindModel;
import org.sevenstar.web.cfg.model.FindParamModel;
import org.sevenstar.web.cfg.model.GlobalResultModel;
import org.sevenstar.web.cfg.model.InterceptorModel;
import org.sevenstar.web.cfg.model.InterceptorParamModel;
import org.sevenstar.web.cfg.model.InvocationModel;
import org.sevenstar.web.cfg.model.ParseModel;
import org.sevenstar.web.cfg.model.ParsesModel;
import org.sevenstar.web.cfg.model.ResourceModel;
import org.sevenstar.web.cfg.model.ResourceParamModel;
import org.sevenstar.web.cfg.model.ResultLocationModel;
import org.sevenstar.web.cfg.model.ResultTypeModel;
import org.sevenstar.web.cfg.model.ResultTypeParamModel;
import org.sevenstar.web.cfg.model.RuleExcludeRuleModel;
import org.sevenstar.web.cfg.model.RuleHeaderParamModel;
import org.sevenstar.web.cfg.model.RuleModel;
import org.sevenstar.web.cfg.model.SwebModel;
import org.sevenstar.web.exception.ActionException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author rtm 2008-5-8
 */
public class SwebXmlConfigure implements ISwebConfigure {

	private String XMLFILE = "file";

	private Map paramMap;

	private static Log LOG = LogFactory.getLog(SwebXmlConfigure.class);

	private SwebModel swebModel = null;

	private static ThreadLocal swebModelThreadLocal = new ThreadLocal();
	private static ThreadLocal ThreadLocalState = new ThreadLocal();

	public void setSwebModel(SwebModel swebModel) {
		this.swebModel = swebModel;
	}

	public SwebModel getSwebModel() {
		if (swebModel != null) {
			return swebModel;
		}
		if (getParamMap().get(XMLFILE) == null) {
			throw new ActionException("没有配置xml配置文件名");
		}
		String filePath = (String) getParamMap().get(XMLFILE);
		swebModel = parse(filePath);
		return swebModel;
	}

	public SwebModel parse(String filepath) {
		SwebModel swebModel = new SwebModel();
		swebModelThreadLocal.set(swebModel);
		NodeletParser parser = new NodeletParser();
		addSweb(parser);
		addSwebWelcomeFile(parser);
		addParses(parser);
		addResources(parser);
		addFinds(parser);
		addInvocations(parser);
		addInterceptors(parser);
		addResultLocations(parser);
		addResultTypes(parser);
		addGlobalResults(parser);
		addRules(parser);
		try {
			parser.parse(NodeletParser.class.getClassLoader()
					.getResourceAsStream("sweb-config-detail.xml"));
		} catch (NodeletException e) {
			throw new ActionException(e);
		}
		return swebModel;
	}

	private void addSweb(NodeletParser parser) {
		parser.addNodelet("/sweb", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				SwebModel swebModel = ((SwebModel) swebModelThreadLocal.get());
				swebModel.setEncode(NodeletUtils.getNodeValue(map, "encode"));
				if (swebModel.getEncode() == null
						|| "".equals(swebModel.getEncode())) {
					/**
					 * 默认GBK
					 */
					swebModel.setEncode("GBK");
				}
				swebModel.setLocale(NodeletUtils.getNodeValue(map, "locale"));
				swebModel.setResource(NodeletUtils
						.getNodeValue(map, "resource"));
			}
		});
	}

	private void addSwebWelcomeFile(NodeletParser parser) {
		parser.addNodelet("/sweb/welcome-file", new Nodelet() {
			public void process(Node node) throws Exception {
				SwebModel swebModel = ((SwebModel) swebModelThreadLocal.get());
				swebModel.setWelcomeFile(node.getTextContent().trim());
			}
		});
	}

	private void addParses(NodeletParser parser) {
		parser.addNodelet("/sweb/parses/parse", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				SwebModel swebModel = ((SwebModel) swebModelThreadLocal.get());
				ParsesModel parsesModel = new ParsesModel();
				ParseModel parseModel = new ParseModel();
				swebModel.getParsesModel().addParseModel(parseModel);
				parseModel.setName(NodeletUtils.getNodeValue(map, "name"));
				parseModel
						.setClassName(NodeletUtils.getNodeValue(map, "class"));
			}
		});
	}

	private void addResources(NodeletParser parser) {
		parser.addNodelet("/sweb/resources/resource", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				ResourceModel resourceModel = new ResourceModel();
				resourceModel.setName(NodeletUtils.getNodeValue(map, "name"));
				resourceModel.setClassName(NodeletUtils.getNodeValue(map,
						"class"));
				ThreadLocalState.set(resourceModel);
			}
		});
		parser.addNodelet("/sweb/resources/resource/param", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				ResourceModel resourceModel = (ResourceModel) ThreadLocalState
						.get();
				ResourceParamModel resourceParamModel = new ResourceParamModel();
				resourceParamModel.setName(NodeletUtils.getNodeValue(map,
						"name"));
				resourceParamModel.setValue(NodeletUtils.getNodeValue(map,
						"value"));
				resourceModel.addParamModel(resourceParamModel);
			}
		});
		parser.addNodelet("/sweb/resources/resource/end()", new Nodelet() {
			public void process(Node node) throws Exception {
				SwebModel swebModel = ((SwebModel) swebModelThreadLocal.get());
				swebModel.getResourcesModel().addResourceModel(
						(ResourceModel) ThreadLocalState.get());
			}
		});
	}

	private void addFinds(NodeletParser parser) {
		parser.addNodelet("/sweb/finds/find", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				FindModel findModel = new FindModel();
				findModel.setName(NodeletUtils.getNodeValue(map, "name"));
				findModel.setClassName(NodeletUtils.getNodeValue(map, "class"));
				ThreadLocalState.set(findModel);
			}
		});
		parser.addNodelet("/sweb/finds/find/param", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				FindModel findModel = (FindModel) ThreadLocalState.get();
				FindParamModel findParamModel = new FindParamModel();
				findParamModel.setName(NodeletUtils.getNodeValue(map, "name"));
				findParamModel
						.setValue(NodeletUtils.getNodeValue(map, "value"));
				findModel.addParamModel(findParamModel);
			}
		});
		parser.addNodelet("/sweb/finds/find/end()", new Nodelet() {
			public void process(Node node) throws Exception {
				SwebModel swebModel = ((SwebModel) swebModelThreadLocal.get());
				swebModel.getFindsModel().addFindModel(
						(FindModel) ThreadLocalState.get());
			}
		});
	}

	private void addInvocations(NodeletParser parser) {
		parser.addNodelet("/sweb/invocations/invocation", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				InvocationModel invocationModel = new InvocationModel();
				invocationModel.setName(NodeletUtils.getNodeValue(map, "name"));
				invocationModel.setClassName(NodeletUtils.getNodeValue(map,
						"class"));
				ThreadLocalState.set(invocationModel);
			}
		});
		parser.addNodelet("/sweb/invocations/invocation/param", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				InvocationModel invocationModel = (InvocationModel) ThreadLocalState
						.get();
				// pass
			}
		});
		parser.addNodelet("/sweb/invocations/invocation/end()", new Nodelet() {
			public void process(Node node) throws Exception {
				SwebModel swebModel = ((SwebModel) swebModelThreadLocal.get());
				swebModel.getInvocationsModel().addInvocationModel(
						(InvocationModel) ThreadLocalState.get());
			}
		});
	}

	private void addInterceptors(NodeletParser parser) {
		parser.addNodelet("/sweb/interceptors/interceptor", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				InterceptorModel interceptorModel = new InterceptorModel();
				interceptorModel
						.setName(NodeletUtils.getNodeValue(map, "name"));
				interceptorModel.setClassName(NodeletUtils.getNodeValue(map,
						"class"));
				ThreadLocalState.set(interceptorModel);
			}
		});
		parser.addNodelet("/sweb/interceptors/interceptor/param",
				new Nodelet() {
					public void process(Node node) throws Exception {
						NamedNodeMap map = node.getAttributes();
						InterceptorModel interceptorModel = (InterceptorModel) ThreadLocalState
								.get();
						InterceptorParamModel interceptorParamModel = new InterceptorParamModel();
						interceptorParamModel.setName(NodeletUtils
								.getNodeValue(map, "name"));
						interceptorParamModel.setValue(NodeletUtils
								.getNodeValue(map, "value"));
						interceptorModel.addParamModel(interceptorParamModel);
					}
				});
		parser.addNodelet("/sweb/interceptors/interceptor/end()",
				new Nodelet() {
					public void process(Node node) throws Exception {
						SwebModel swebModel = ((SwebModel) swebModelThreadLocal
								.get());
						swebModel.getInterceptorsModel().addInterceptors(
								(InterceptorModel) ThreadLocalState.get());
					}
				});
	}

	private void addResultLocations(NodeletParser parser) {
		parser.addNodelet("/sweb/result-locations/location", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				ResultLocationModel resultLocationModel = new ResultLocationModel();
				resultLocationModel.setName(NodeletUtils.getNodeValue(map,
						"name"));
				resultLocationModel.setClassName(NodeletUtils.getNodeValue(map,
						"class"));
				ThreadLocalState.set(resultLocationModel);
			}
		});
		parser.addNodelet("/sweb/result-locations/location/param",
				new Nodelet() {
					public void process(Node node) throws Exception {
						NamedNodeMap map = node.getAttributes();

					}
				});
		parser.addNodelet("/sweb/result-locations/location/end()",
				new Nodelet() {
					public void process(Node node) throws Exception {
						SwebModel swebModel = ((SwebModel) swebModelThreadLocal
								.get());
						swebModel.getResultLocationsModel().addLocationModel(
								(ResultLocationModel) ThreadLocalState.get());
					}
				});
	}

	private void addResultTypes(NodeletParser parser) {
		parser.addNodelet("/sweb/result-types/result-type", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				ResultTypeModel resultTypeModel = new ResultTypeModel();
				resultTypeModel.setName(NodeletUtils.getNodeValue(map, "name"));
				resultTypeModel.setClassName(NodeletUtils.getNodeValue(map,
						"class"));
				ThreadLocalState.set(resultTypeModel);
			}
		});
		parser.addNodelet("/sweb/result-types/result-type/param",
				new Nodelet() {
					public void process(Node node) throws Exception {
						NamedNodeMap map = node.getAttributes();
						ResultTypeModel resultTypeModel = (ResultTypeModel) ThreadLocalState
								.get();
						ResultTypeParamModel resultTypeParamModel = new ResultTypeParamModel();
						resultTypeParamModel.setName(NodeletUtils.getNodeValue(
								map, "name"));
						resultTypeParamModel.setValue(NodeletUtils
								.getNodeValue(map, "value"));
						resultTypeModel
								.addResultTypeParamModel(resultTypeParamModel);
					}
				});
		parser.addNodelet("/sweb/result-types/result-type/end()",
				new Nodelet() {
					public void process(Node node) throws Exception {
						SwebModel swebModel = ((SwebModel) swebModelThreadLocal
								.get());
						swebModel.getResultTypesModel().addResultTypeModel(
								(ResultTypeModel) ThreadLocalState.get());
					}
				});
	}

	private void addGlobalResults(NodeletParser parser) {
		parser.addNodelet("/sweb/global-results/result", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				GlobalResultModel globalResultModel = new GlobalResultModel();
				globalResultModel.setName(NodeletUtils
						.getNodeValue(map, "name"));
				globalResultModel.setType(NodeletUtils
						.getNodeValue(map, "type"));
				globalResultModel.setLocation(NodeletUtils.getNodeValue(map,
						"location"));
				ThreadLocalState.set(globalResultModel);
			}
		});
		parser.addNodelet("/sweb/global-results/result/param", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();

			}
		});
		parser.addNodelet("/sweb/global-results/result/end()", new Nodelet() {
			public void process(Node node) throws Exception {
				SwebModel swebModel = ((SwebModel) swebModelThreadLocal.get());
				swebModel.getGlobalResultsModel().addResultModel(
						(GlobalResultModel) ThreadLocalState.get());
			}
		});
	}

	private void addRules(NodeletParser parser) {
		parser.addNodelet("/sweb/action/url/rule", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				RuleModel ruleModel = new RuleModel();
				ruleModel.setEncode(NodeletUtils.getNodeValue(map, "encode"));
				ruleModel.setFind(NodeletUtils.getNodeValue(map, "find"));
				ruleModel.setInteceptors(NodeletUtils.getNodeValue(map,
						"inteceptors"));
				ruleModel.setParse(NodeletUtils.getNodeValue(map, "parse"));
				ruleModel.setPattern(NodeletUtils.getNodeValue(map, "pattern"));
				ruleModel.setResource(NodeletUtils
						.getNodeValue(map, "resource"));
				ruleModel.setResultLocation(NodeletUtils.getNodeValue(map,
						"resultLocation"));
				ruleModel.setResultType(NodeletUtils.getNodeValue(map,
						"resultType"));
				ruleModel.setType(NodeletUtils.getNodeValue(map, "type"));
				ruleModel.setHtmlcache(NodeletUtils.getNodeValue(map,
						"htmlcache"));
				ThreadLocalState.set(ruleModel);
			}
		});
		parser.addNodelet("/sweb/action/url/rule/exclude", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				RuleModel ruleModel = (RuleModel) ThreadLocalState.get();
				RuleExcludeRuleModel ruleExcludeRuleModel = new RuleExcludeRuleModel();
				ruleExcludeRuleModel.setPattern(NodeletUtils.getNodeValue(map,
						"pattern"));
				ruleModel.addRuleExcludeRuleModel(ruleExcludeRuleModel);
			}
		});
		parser.addNodelet("/sweb/action/url/rule/header/param", new Nodelet() {
			public void process(Node node) throws Exception {
				NamedNodeMap map = node.getAttributes();
				RuleModel ruleModel = (RuleModel) ThreadLocalState.get();
				RuleHeaderParamModel ruleHeaderParamModel = new RuleHeaderParamModel();
				ruleHeaderParamModel.setName(NodeletUtils.getNodeValue(map,
						"name"));
				ruleHeaderParamModel.setValue(NodeletUtils.getNodeValue(map,
						"name"));
				if (ruleHeaderParamModel.getName() != null
						&& !"".equalsIgnoreCase(ruleHeaderParamModel.getName())) {
					ruleModel.getHeaderParamModelList().add(
							ruleHeaderParamModel);
				}
			}
		});
		parser.addNodelet("/sweb/action/url/rule/end()", new Nodelet() {
			public void process(Node node) throws Exception {
				SwebModel swebModel = ((SwebModel) swebModelThreadLocal.get());
				swebModel.getActionModel().getUrlModel().addRuleModel(
						(RuleModel) ThreadLocalState.get());
			}
		});
	}

	/*
	 * private SwebModel parseByDom4j(String filePath) { Document doc =
	 * XmlHelper.readByClassPath(filePath); Element root = (Element)
	 * doc.selectObject("/sweb"); SwebModel swebModel = (SwebModel)
	 * XmlHelper.initialize(SwebModel.class, root); if (swebModel.getEncode() ==
	 * null || "".equals(swebModel.getEncode())) {
	 *  // 默认GBK
	 * 
	 * swebModel.setEncode("GBK"); }
	 * 
	 * //root welcome file
	 * 
	 * Element welcomeFileEle = (Element) doc
	 * .selectObject("/sweb/welcome-file"); swebModel.setWelcomeFile((String)
	 * welcomeFileEle.getData());
	 *  // interceptors配置
	 * 
	 * Element interceptorsEle = (Element) doc
	 * .selectObject("/sweb/interceptors"); InterceptorsModel interceptorsModel =
	 * new InterceptorsModel(); if (interceptorsEle != null) { List
	 * interceptorEleList = interceptorsEle.elements(); for (int i = 0; i <
	 * interceptorEleList.size(); i++) { Element interceptorEle = (Element)
	 * interceptorEleList.get(i); InterceptorModel interceptorModel = new
	 * InterceptorModel(); interceptorModel.setName(XmlHelper.getAttributeValue(
	 * interceptorEle, "name"));
	 * interceptorModel.setClassName(XmlHelper.getAttributeValue(
	 * interceptorEle, "class")); List paramEleList = interceptorEle.elements();
	 * if (paramEleList != null && paramEleList.size() > 0) { for (int j = 0; j <
	 * paramEleList.size(); j++) { Element paramEle = (Element)
	 * paramEleList.get(j); InterceptorParamModel interceptorParamModel =
	 * (InterceptorParamModel) XmlHelper
	 * .initialize(InterceptorParamModel.class, paramEle);
	 * interceptorModel.addParamModel(interceptorParamModel); } }
	 * interceptorsModel.addInterceptors(interceptorModel); } }
	 * swebModel.setInterceptorsModel(interceptorsModel);
	 * 
	 * //Resources配置
	 * 
	 * Element resourcesEle = (Element) doc.selectObject("/sweb/resources");
	 * ResourcesModel resourcesModel = new ResourcesModel(); if (resourcesEle !=
	 * null) { List resourceEleList = resourcesEle.elements(); for (int i = 0; i <
	 * resourceEleList.size(); i++) { Element resourceEle = (Element)
	 * resourceEleList.get(i); ResourceModel resourceModel = new
	 * ResourceModel();
	 * resourceModel.setName(XmlHelper.getAttributeValue(resourceEle, "name"));
	 * resourceModel.setClassName(XmlHelper.getAttributeValue( resourceEle,
	 * "class")); List paramEleList = resourceEle.elements(); if (paramEleList !=
	 * null && paramEleList.size() > 0) { for (int j = 0; j <
	 * paramEleList.size(); j++) { Element paramEle = (Element)
	 * paramEleList.get(j); ResourceParamModel resourceParamModel =
	 * (ResourceParamModel) XmlHelper .initialize(ResourceParamModel.class,
	 * paramEle); resourceModel.addParamModel(resourceParamModel); } }
	 * resourcesModel.addResourceModel(resourceModel); } }
	 * swebModel.setResourcesModel(resourcesModel);
	 * 
	 *  // parses配置
	 * 
	 * Element parsesEle = (Element) doc.selectObject("/sweb/parses");
	 * ParsesModel parsesModel = new ParsesModel(); if (parsesEle != null) {
	 * List parseEleList = parsesEle.elements(); for (int i = 0; i <
	 * parseEleList.size(); i++) { Element parseEle = (Element)
	 * parseEleList.get(i); ParseModel parseModel = new ParseModel();
	 * parseModel.setName(XmlHelper .getAttributeValue(parseEle, "name"));
	 * parseModel.setClassName(XmlHelper.getAttributeValue(parseEle, "class"));
	 * parsesModel.addParseModel(parseModel); } }
	 * swebModel.setParsesModel(parsesModel);
	 * 
	 *  // invocations配置
	 * 
	 * Element invocationsEle = (Element) doc
	 * .selectObject("/sweb/invocations"); InvocationsModel invocationsModel =
	 * new InvocationsModel(); if (invocationsEle != null) { List
	 * invocationsEleList = invocationsEle.elements(); for (int i = 0; i <
	 * invocationsEleList.size(); i++) { Element invocationEle = (Element)
	 * invocationsEleList.get(i); InvocationModel invocationModel = new
	 * InvocationModel(); invocationModel.setName(XmlHelper.getAttributeValue(
	 * invocationEle, "name"));
	 * invocationModel.setClassName(XmlHelper.getAttributeValue( invocationEle,
	 * "class")); invocationsModel.addInvocationModel(invocationModel); } }
	 * swebModel.setInvocationsModel(invocationsModel);
	 * 
	 * //result-types配置
	 * 
	 * Element resultTypesEle = (Element) doc
	 * .selectObject("/sweb/result-types"); ResultTypesModel resultTypesModel =
	 * new ResultTypesModel(); if (resultTypesEle != null) { List
	 * resultTypeEleList = resultTypesEle.elements(); for (int i = 0; i <
	 * resultTypeEleList.size(); i++) { Element resultTypeEle = (Element)
	 * resultTypeEleList.get(i); ResultTypeModel resultTypeModel = new
	 * ResultTypeModel(); resultTypeModel.setName(XmlHelper.getAttributeValue(
	 * resultTypeEle, "name"));
	 * resultTypeModel.setClassName(XmlHelper.getAttributeValue( resultTypeEle,
	 * "class")); List resultTypeParamEleList = resultTypeEle.elements(); if
	 * (resultTypeParamEleList != null && resultTypeParamEleList.size() > 0) {
	 * for (int j = 0; j < resultTypeParamEleList.size(); j++) { Element
	 * resultTypeParamEle = (Element) resultTypeParamEleList .get(j);
	 * ResultTypeParamModel resultTypeParamModel = (ResultTypeParamModel)
	 * XmlHelper .initialize(ResultTypeParamModel.class, resultTypeParamEle);
	 * resultTypeModel .addResultTypeParamModel(resultTypeParamModel); } }
	 * resultTypesModel.addResultTypeModel(resultTypeModel); } }
	 * swebModel.setResultTypesModel(resultTypesModel);
	 * 
	 * //global-results配置
	 * 
	 * Element globalResultsEle = (Element) doc
	 * .selectObject("/sweb/global-results"); GlobalResultsModel
	 * globalResultsModel = new GlobalResultsModel(); if (globalResultsEle !=
	 * null) { List globalResultEleList = globalResultsEle.elements(); for (int
	 * i = 0; i < globalResultEleList.size(); i++) { Element globalResultEle =
	 * (Element) globalResultEleList.get(i); GlobalResultModel globalResultModel =
	 * (GlobalResultModel) XmlHelper .initialize(GlobalResultModel.class,
	 * globalResultEle); globalResultsModel.addResultModel(globalResultModel); } }
	 * swebModel.setGlobalResultsModel(globalResultsModel);
	 *  // result-location配置
	 * 
	 * Element resultLocationsEle = (Element) doc
	 * .selectObject("/sweb/result-locations"); ResultLocationsModel
	 * resultLocationsModel = new ResultLocationsModel(); if (resultLocationsEle !=
	 * null) { List resultLocationsList = resultLocationsEle.elements(); for
	 * (int i = 0; i < resultLocationsList.size(); i++) { Element
	 * resultLocationEle = (Element) resultLocationsList .get(i);
	 * ResultLocationModel resultLocationModel = new ResultLocationModel();
	 * resultLocationModel.setName(XmlHelper.getAttributeValue(
	 * resultLocationEle, "name"));
	 * resultLocationModel.setClassName(XmlHelper.getAttributeValue(
	 * resultLocationEle, "class"));
	 * resultLocationsModel.addLocationModel(resultLocationModel); } }
	 * swebModel.setResultLocationsModel(resultLocationsModel);
	 *  // find配置
	 * 
	 * Element findsEle = (Element) doc.selectObject("/sweb/finds"); FindsModel
	 * findsModel = new FindsModel(); if (findsEle != null) { List findEleList =
	 * findsEle.elements(); for (int i = 0; i < findEleList.size(); i++) {
	 * Element findEle = (Element) findEleList.get(i); FindModel findModel = new
	 * FindModel(); findModel.setName(XmlHelper.getAttributeValue(findEle,
	 * "name")); findModel.setClassName(XmlHelper.getAttributeValue(findEle,
	 * "class")); List paramEleList = findEle.elements(); if (paramEleList !=
	 * null && paramEleList.size() > 0) { for (int j = 0; j <
	 * paramEleList.size(); j++) { Element paramEle = (Element)
	 * paramEleList.get(j); FindParamModel findParamModel = (FindParamModel)
	 * XmlHelper .initialize(FindParamModel.class, paramEle);
	 * findModel.addParamModel(findParamModel); } }
	 * findsModel.addFindModel(findModel); } }
	 * swebModel.setFindsModel(findsModel);
	 *  // Action配置
	 * 
	 * Element actionEle = (Element) doc.selectObject("/sweb/action"); if
	 * (actionEle != null) { ActionModel actionModel = (ActionModel)
	 * XmlHelper.initialize( ActionModel.class, actionEle);
	 * swebModel.setActionModel(actionModel); } Element urlEle = (Element)
	 * doc.selectObject("/sweb/action/url"); if (urlEle != null) { List ruleList =
	 * urlEle.elements(); for (int i = 0; i < ruleList.size(); i++) { Element
	 * ruleEle = (Element) ruleList.get(i); RuleModel ruleModel = (RuleModel)
	 * XmlHelper.initialize( RuleModel.class, ruleEle); List excludeUrls =
	 * ruleEle.elements(); if (excludeUrls != null && excludeUrls.size() > 0) {
	 * for (int j = 0; j < excludeUrls.size(); j++) { Element excludeUrlEle =
	 * (Element) excludeUrls.get(j); RuleExcludeRuleModel ruleExcludeRuleModel =
	 * (RuleExcludeRuleModel) XmlHelper .initialize(RuleExcludeRuleModel.class,
	 * excludeUrlEle); ruleModel.addRuleExcludeRuleModel(ruleExcludeRuleModel); } }
	 * swebModel.getActionModel().getUrlModel() .addRuleModel(ruleModel); } }
	 * LOG.debug("Configure:init"); return swebModel; }
	 */

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
