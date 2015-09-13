package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.protocol.Protocol;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class RemoteService {
		
	//http
	private String serverUrl;
	private HttpClient httpClient;
	private HttpState httpState;
	private boolean bHttps;
	private int port;
	private String host;
	private String protocol;
	private String authority;
	private String path;
	private boolean bAssembleXsl;
	private boolean bAuthenticated;
	private String userId;
	private String password;
	List<Project> projectList;
	
	//xPath
	private static final String projectListExpression = "/admin/projects/project";
	private static final String projectInforExpression = "/admin/project";
	private static final String projectInforConnectorExp = "/admin/project/connector";	
	private static final String projectInforSequenceExp = "/admin/project/sequence";	
	private static final String testcaseFromCurrentNodeExp = ".//testcase";
	private static final String transFromCurrentNodeExp = ".//transaction";
	private static final String varFromCurrentNodeExp = ".//variable";
	
	public RemoteService(){
		this.serverUrl = "www.convertigo.com";		
		this.protocol = "http";
		this.port = 80;
	}
	
	public RemoteService(String serverUrl, String userId, String password) throws JenkinsPluginException{
		
		this.serverUrl = serverUrl;
		this.httpClient = new HttpClient();
		//parse Url
		try {	
				URL url = new URL(serverUrl);	
				
				this.protocol = url.getProtocol();
				this.authority = url.getAuthority(); 
				this.host = url.getHost();
				this.path = url.getPath();
				this.bHttps = "https".equals(url.getProtocol())?true:false;
				this.bAssembleXsl = false;
				this.bAuthenticated = false;
				
				//port 
				if (url.getPort()==-1) {
					if (bHttps) this.port = 443;
					else this.port = 80;
				}	
				else
					this.port = url.getPort();
				this.userId = userId;
				this.password = password;
				
			} catch (MalformedURLException e) {
				throw new JenkinsPluginException("The deployment server is not valid: " + serverUrl + "\n"+ e.getMessage());
			}		
	
	}	
	
	public void login() throws JenkinsPluginException {
		
		String loginServiceURL = protocol+"://"+authority+path+"/admin/services/engine.Authenticate";
		PostMethod loginMethod = null;

		try {			
				if (this.userId.isEmpty()) {
					throw new JenkinsPluginException("Unable to connect to the deployment server: \"Server administrator\" field is empty.");
				}
				if (this.password.isEmpty()) {
					throw new JenkinsPluginException("Unable to connect to the deployment server: \"Password\" field is empty.");
				}
			
				this.httpState = (this.httpState==null)?new HttpState():this.httpState;
				
				this.httpClient.setState(this.httpState);
				
				loginMethod = new PostMethod(loginServiceURL);
				loginMethod.addParameter("authType", "login");
				loginMethod.addParameter("authUserName", this.userId);
				loginMethod.addParameter("authPassword", this.password);
				
				int returnCode = httpClient.executeMethod(loginMethod);
				String httpResponse = loginMethod.getResponseBodyAsString();
				
				if (returnCode == HttpStatus.SC_OK) {
					Document domResponse;
					try {
							DocumentBuilder parser = DocumentBuilderFactory
									.newInstance().newDocumentBuilder();
							domResponse = parser.parse(new InputSource(
									new StringReader(httpResponse)));
							domResponse.normalize();
		
							NodeList nodeList = domResponse
									.getElementsByTagName("error");
	
						if (nodeList.getLength() != 0) {
							throw new JenkinsPluginException(
									"Unable to connect to the deployment server: wrong username or password.");
						}
						this.bAuthenticated = true;
						
					} catch (ParserConfigurationException e) {
						throw new JenkinsPluginException(
								"Unable to parse the deployment server response: \n"
										+ e.getMessage() + ".\n"
										+ "Received response: " + httpResponse);
					} catch (IOException e) {
						throw new JenkinsPluginException(
								"An unexpected error has occured during the deployment server login.\n"
										+ "(IOException) " + e.getMessage() + "\n"
										+ "Received response: " + httpResponse, e);
					} catch (SAXException e) {
						throw new JenkinsPluginException(
								"Unable to parse the Convertigo server response: "
										+ e.getMessage() + ".\n"
										+ "Received response: " + httpResponse);
					}
				} else {
					decodeResponseError(httpResponse);
				}
			}catch (HttpException e) {
				throw new JenkinsPluginException(
						"An unexpected error has occured during the Convertigo server login.\n"
								+ "Cause: " + e.getMessage(), e);
			} catch (UnknownHostException e) {
				throw new JenkinsPluginException(
						"Unable to find the Convertigo server (unknown host): "
								+ e.getMessage());
			} catch (IOException e) {
				String message = e.getMessage();
				
				if (message.indexOf("unable to find valid certification path") != -1) {
					throw new JenkinsPluginException(
							"The SSL certificate of the Convertigo server is not trusted.\nPlease check the 'Trust all certificates' checkbox.");
				}
				else throw new JenkinsPluginException(
						"Unable to reach the Convertigo server: \n"
								+ "(IOException) " + e.getMessage(), e);
			} finally {
				Protocol.unregisterProtocol("https");
				if (loginMethod != null)
					loginMethod.releaseConnection();
			}			
	}

	
	
	public Project getProjectInfo(String projectName) throws JenkinsPluginException{
		
		login();
		
		Project project = new Project(projectName);
		String projectInfoUrl = protocol+"://"+authority+path+"/admin/services/projects.GetTestPlatform";
		//String projectInfoUrl = protocol+"://"+authority+path+"/project.html#"+projectName;
		PostMethod getProjectInfoMethod = null;
		//GetMethod getProjectInfoMethod = null;
		try {
			getProjectInfoMethod = new PostMethod(projectInfoUrl);
			getProjectInfoMethod.setParameter("projectName", projectName);
			
			//getProjectInfoMethod = new GetMethod(projectInfoUrl);
			
			
			int returnCode = httpClient.executeMethod(getProjectInfoMethod);
			String httpResponse = getProjectInfoMethod.getResponseBodyAsString();
			
			if (returnCode == HttpStatus.SC_OK) {
				Document domResponse;
				try {
						DocumentBuilder parser = DocumentBuilderFactory
								.newInstance().newDocumentBuilder();
						domResponse = parser.parse(new InputSource(
								new StringReader(httpResponse)));
						domResponse.normalize();
	
						NodeList nodeList = domResponse
								.getElementsByTagName("error");

					if (nodeList.getLength() != 0) {
						throw new JenkinsPluginException(
								"Unable to parse response of GetTestPlatform of project " + projectName);
					}
					this.bAuthenticated = true;
					parseGetTestPlaformXml(domResponse,project);
					
				} catch (ParserConfigurationException e) {
					throw new JenkinsPluginException(
							"Unable to parse the deployment server response: \n"
									+ e.getMessage() + ".\n"
									+ "Received response: " + httpResponse);
				} catch (IOException e) {
					throw new JenkinsPluginException(
							"An unexpected error has occured during the deployment server login.\n"
									+ "(IOException) " + e.getMessage() + "\n"
									+ "Received response: " + httpResponse, e);
				} catch (SAXException e) {
					throw new JenkinsPluginException(
							"Unable to parse the Convertigo server response: "
									+ e.getMessage() + ".\n"
									+ "Received response: " + httpResponse);
				}catch (XPathExpressionException e) {
					throw new JenkinsPluginException("fail to find list of Projects " +e.getMessage());
				}
			} else {
				decodeResponseError(httpResponse);
			}
		}catch (HttpException e) {
			throw new JenkinsPluginException(
					"An unexpected error has occured during the Convertigo server login.\n"
							+ "Cause: " + e.getMessage(), e);
		} catch (UnknownHostException e) {
			throw new JenkinsPluginException(
					"Unable to find the Convertigo server (unknown host): "
							+ e.getMessage());
		} catch (IOException e) {
			String message = e.getMessage();
			
			if (message.indexOf("unable to find valid certification path") != -1) {
				throw new JenkinsPluginException(
						"The SSL certificate of the Convertigo server is not trusted.\nPlease check the 'Trust all certificates' checkbox.");
			}
			else throw new JenkinsPluginException(
					"Unable to reach the Convertigo server: \n"
							+ "(IOException) " + e.getMessage(), e);
		} finally {
			Protocol.unregisterProtocol("https");
			if (getProjectInfoMethod != null)
				getProjectInfoMethod.releaseConnection();
		}		
		return project;
	}	
	

	public List<Project> getListProjects() throws JenkinsPluginException{
		
		login();
		
		List<Project> projectlist = null;		
		String projectListUrl = protocol+"://"+authority+path+"/admin/services/projects.List";
		GetMethod getListMethod = null;
		
		try {
			
			getListMethod = new GetMethod(projectListUrl);
			int returnCode = httpClient.executeMethod(getListMethod);
			String httpResponse = getListMethod.getResponseBodyAsString();
			
			if (returnCode == HttpStatus.SC_OK) {
				Document domResponse;
				
				try {
						DocumentBuilder parser = DocumentBuilderFactory
								.newInstance().newDocumentBuilder();
						domResponse = parser.parse(new InputSource(
								new StringReader(httpResponse)));
						domResponse.normalize();
						NodeList nodeList = domResponse
								.getElementsByTagName("error");

					if (nodeList.getLength() != 0) {
						throw new JenkinsPluginException(
								"Unable to connect to the deployment server: wrong username or password.");
					}
					this.bAuthenticated = true;
					
					projectlist = fillProjectList(domResponse);
					
				} catch (ParserConfigurationException e) {
					throw new JenkinsPluginException(
							"Unable to parse the deployment server response: \n"
									+ e.getMessage() + ".\n"
									+ "Received response: " + httpResponse);
				} catch (IOException e) {
					throw new JenkinsPluginException(
							"An unexpected error has occured during the deployment server login.\n"
									+ "(IOException) " + e.getMessage() + "\n"
									+ "Received response: " + httpResponse, e);
				} catch (SAXException e) {
					throw new JenkinsPluginException(
							"Unable to parse the Convertigo server response: "
									+ e.getMessage() + ".\n"
									+ "Received response: " + httpResponse);
				}catch (XPathExpressionException e) {
					throw new JenkinsPluginException("fail to find list of Projects " +e.getMessage());
				}
			} else {
				decodeResponseError(httpResponse);
			}
		}catch (HttpException e) {
			throw new JenkinsPluginException(
					"An unexpected error has occured during the Convertigo server login.\n"
							+ "Cause: " + e.getMessage(), e);
		} catch (UnknownHostException e) {
			throw new JenkinsPluginException(
					"Unable to find the Convertigo server (unknown host): "
							+ e.getMessage());
		} catch (IOException e) {
			String message = e.getMessage();
			
			if (message.indexOf("unable to find valid certification path") != -1) {
				throw new JenkinsPluginException(
						"The SSL certificate of the Convertigo server is not trusted.\nPlease check the 'Trust all certificates' checkbox.");
			}
			else throw new JenkinsPluginException(
					"Unable to reach the Convertigo server: \n"
							+ "(IOException) " + e.getMessage(), e);
		} finally {
			Protocol.unregisterProtocol("https");
			if (getListMethod != null)
				getListMethod.releaseConnection();
		}	
		
		return projectlist;
	}	
		
	
	String executeTestCase(TestCase testcase) throws JenkinsPluginException{		

		String strResponse = null; 
		
		login();
		
		//String checkAuthServiceURL = protocol+"://"+authority+path+"/admin/services/engine.CheckAuthentication";
		String exeTestCaseUrl = null;
		if(!"".equalsIgnoreCase(testcase.getUrl()) )
			exeTestCaseUrl = testcase.getUrl();
		else {
			exeTestCaseUrl = serverUrl+ "/projects/"+testcase.getProjectname()+TestCaseUrl.XML.endTail();;
		}
		
		GetMethod exeTestCaseMethod = null;

		try {
			exeTestCaseMethod = new GetMethod(exeTestCaseUrl);		
			
			//
			//List<NameValuePair> listQueryString = new ArrayList<NameValuePair>();
			if(testcase.getType()!=null && testcase.getType().equals("connector")) {
				NameValuePair[]  QueryStringArray = { 	new NameValuePair("__connector",testcase.getTypeName()),
										new NameValuePair("__transaction",testcase.getTransaction()), 
										new NameValuePair("__testcase",testcase.getName())};
				exeTestCaseMethod.setQueryString(QueryStringArray);
			}
			else {
				NameValuePair[]  QueryStringArray = { 	new NameValuePair("__sequence",testcase.getTypeName()),
														new NameValuePair("__testcase",testcase.getName())};
				exeTestCaseMethod.setQueryString(QueryStringArray);
			}

			
			int returnCode = httpClient.executeMethod(exeTestCaseMethod);
			String httpResponse = exeTestCaseMethod.getResponseBodyAsString();
			Document domResponse = null;
			if (returnCode == HttpStatus.SC_OK) {
				
				try {
						DocumentBuilder parser = DocumentBuilderFactory
								.newInstance().newDocumentBuilder();
						domResponse = parser.parse(new InputSource(
								new StringReader(httpResponse)));
						domResponse.normalize();
	
						NodeList nodeList = domResponse.getElementsByTagName("error");
//						if (nodeList.getLength() != 0) {
//							throw new JenkinsPluginException(
//									"Unable to parse response of testcase "+ testcase.getName() + "\n" + httpResponse);
//						}

						strResponse = httpResponse;

				} catch (ParserConfigurationException e) {
					throw new JenkinsPluginException(
							"Unable to parse server response: \n"
									+ e.getMessage() + ".\n"
									+ "Received response: " + httpResponse);
				} catch (IOException e) {
					throw new JenkinsPluginException(
							"An unexpected error has occured during the deployment server login.\n"
									+ "(IOException) " + e.getMessage() + "\n"
									+ "Received response: " + httpResponse, e);
				} catch (SAXException e) {
					throw new JenkinsPluginException(
							"Unable to parse the Convertigo server response: "
									+ e.getMessage() + ".\n"
									+ "Received response: " + httpResponse);
				}
			} else {
				decodeResponseError(httpResponse);
			}
		}catch (HttpException e) {
			throw new JenkinsPluginException(
					"An unexpected error has occured during the Convertigo server login.\n"
							+ "Cause: " + e.getMessage(), e);
		} catch (UnknownHostException e) {
			throw new JenkinsPluginException(
					"Unable to find the Convertigo server (unknown host): "
							+ e.getMessage());
		} catch (IOException e) {
			String message = e.getMessage();
			
			if (message.indexOf("unable to find valid certification path") != -1) {
				throw new JenkinsPluginException(
						"The SSL certificate of the Convertigo server is not trusted.\nPlease check the 'Trust all certificates' checkbox.");
			}
			else throw new JenkinsPluginException(
					"Unable to reach the Convertigo server: \n"
							+ "(IOException) " + e.getMessage(), e);
		} finally {
			Protocol.unregisterProtocol("https");
			if (exeTestCaseMethod != null)
				exeTestCaseMethod.releaseConnection();
		}			
		return strResponse;
	}
	
	
	
	private void decodeResponseError(String httpResponse) throws JenkinsPluginException {
		Document domResponse;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			domResponse = parser.parse(new InputSource(new StringReader(httpResponse)));
			domResponse.normalize();

			NodeList nodeList = domResponse.getElementsByTagName("error");

			if (nodeList.getLength() != 0) {
				Element errorNode = (Element) nodeList.item(0);

				Element errorMessage = (Element) errorNode.getElementsByTagName("message").item(0);

				Element exceptionName = (Element) errorNode.getElementsByTagName("exception").item(0);

				Element stackTrace = (Element) errorNode.getElementsByTagName("stacktrace").item(0);

				throw new JenkinsPluginException(
						errorMessage.getTextContent(),
						exceptionName == null ? "" : exceptionName.getTextContent(),
						stackTrace == null ? "" : stackTrace.getTextContent());
			}		
		} catch (ParserConfigurationException e) {
			throw new JenkinsPluginException(
					"Unable to parse the deployment server response: \n"+ e.getMessage() + ".\n" + "Received response: " + httpResponse);
		} catch (IOException e) {
			throw new JenkinsPluginException(
					"An unexpected error has occured during the Convertigo Project deployment.\n"
							+ "(IOException) " + e.getMessage() + "\n"
							+ "Received response: " + httpResponse, e);
		} catch (SAXException e) {
			throw new JenkinsPluginException(
					"Unable to parse the Deployment server response: "
							+ e.getMessage() + ".\n"
							+ "Received response: " + httpResponse);
		}
	}

	
public void checkAuthentication() throws JenkinsPluginException {
		
		String checkAuthServiceURL = protocol+"://"+authority+path+"/admin/services/engine.CheckAuthentication";
		PostMethod checkAuthMethod = null;

		try {
			checkAuthMethod = new PostMethod(checkAuthServiceURL);

			int returnCode = httpClient.executeMethod(checkAuthMethod);
			String httpResponse = checkAuthMethod.getResponseBodyAsString();
			
			if (returnCode == HttpStatus.SC_OK) {
				Document domResponse; 
				try {
						DocumentBuilder parser = DocumentBuilderFactory
								.newInstance().newDocumentBuilder();
						domResponse = parser.parse(new InputSource(
								new StringReader(httpResponse)));
						domResponse.normalize();
	
						NodeList nodeList = domResponse.getElementsByTagName("error");
						if (nodeList.getLength() != 0) {
							throw new JenkinsPluginException(
									"Unable to check Authentication with user id " + this.userId);
						}
						
						NodeList nodelist = domResponse.getElementsByTagName("authenticated");
						if (nodelist.getLength() != 1) {
							throw new JenkinsPluginException(
									"Unable to check Authentication with user id " + this.userId);
						} else {							
							 Node node = nodeList.item(0);
						}

				} catch (ParserConfigurationException e) {
					throw new JenkinsPluginException(
							"Unable to parse the deployment server response: \n"
									+ e.getMessage() + ".\n"
									+ "Received response: " + httpResponse);
				} catch (IOException e) {
					throw new JenkinsPluginException(
							"An unexpected error has occured during the deployment server login.\n"
									+ "(IOException) " + e.getMessage() + "\n"
									+ "Received response: " + httpResponse, e);
				} catch (SAXException e) {
					throw new JenkinsPluginException(
							"Unable to parse the Convertigo server response: "
									+ e.getMessage() + ".\n"
									+ "Received response: " + httpResponse);
				}
			} else {
				decodeResponseError(httpResponse);
			}
		}catch (HttpException e) {
			throw new JenkinsPluginException(
					"An unexpected error has occured during the Convertigo server login.\n"
							+ "Cause: " + e.getMessage(), e);
		} catch (UnknownHostException e) {
			throw new JenkinsPluginException(
					"Unable to find the Convertigo server (unknown host): "
							+ e.getMessage());
		} catch (IOException e) {
			String message = e.getMessage();
			
			if (message.indexOf("unable to find valid certification path") != -1) {
				throw new JenkinsPluginException(
						"The SSL certificate of the Convertigo server is not trusted.\nPlease check the 'Trust all certificates' checkbox.");
			}
			else throw new JenkinsPluginException(
					"Unable to reach the Convertigo server: \n"
							+ "(IOException) " + e.getMessage(), e);
		} finally {
			Protocol.unregisterProtocol("https");
			if (checkAuthMethod != null)
				checkAuthMethod.releaseConnection();
		}			
	}
		
	
	List<Project> fillProjectList(Document xmlDocument) throws XPathExpressionException{
		
		List<Project> list = new ArrayList<Project>();
		
		XPath xPath =  XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile(projectListExpression).evaluate(xmlDocument, XPathConstants.NODESET);
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			
			String name =   nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
			String comment =   nodeList.item(i).getAttributes().getNamedItem("comment").getNodeValue();
			String deployDate =   nodeList.item(i).getAttributes().getNamedItem("deployDate").getNodeValue();
			String exported =   nodeList.item(i).getAttributes().getNamedItem("exported").getNodeValue();
			String version =   nodeList.item(i).getAttributes().getNamedItem("version").getNodeValue();
			Project Project = new Project(name,comment,deployDate,exported,version);
			list.add(Project);
		}
		
		return list;
		
	}

	public List<TestCase>  getTestCaseListByProject(Project project) throws JenkinsPluginException{

		return project.getTestcaseList();

	}
	
	
	void parseGetTestPlaformXml(Document xmlDocument,Project project) throws XPathExpressionException{
		
		XPath xPath =  XPathFactory.newInstance().newXPath();
		
		//project
		Node projectInfo = (Node) xPath.compile(projectInforExpression).evaluate(xmlDocument, XPathConstants.NODE);
		String projectName = projectInfo.getAttributes().getNamedItem("name").getNodeValue();
		String projectVersion = projectInfo.getAttributes().getNamedItem("version").getNodeValue();
		String projectComment = projectInfo.getAttributes().getNamedItem("comment").getNodeValue();
		
		project.setComment(projectComment);
		project.setName(projectName);
		project.setVersion(projectVersion);
		
		String url = protocol+"://"+authority+path+"/projects/"+projectName+TestCaseUrl.XML.endTail();
		
		//connector
		NodeList connectorList = (NodeList) xPath.compile(projectInforConnectorExp).evaluate(xmlDocument, XPathConstants.NODESET);
		
		for (int i = 0; i < connectorList.getLength(); i++) {
	
			Node connector = connectorList.item(i);
			String connectorName = connector.getAttributes().getNamedItem("name").getNodeValue();
						
			//transaction
			NodeList transactionList = (NodeList) xPath.compile(transFromCurrentNodeExp).evaluate(connector, XPathConstants.NODESET);			
			for(int j=0;j<transactionList.getLength();j++) {
				
				Node transaction = transactionList.item(j);
				String transactionName = transaction.getAttributes().getNamedItem("name").getNodeValue();
				
				
				//testcase
				NodeList testcaseList = (NodeList) xPath.compile(testcaseFromCurrentNodeExp).evaluate(transaction, XPathConstants.NODESET);					
				for(int z = 0; z<testcaseList.getLength();z++) {
					
					Node testcaseNd = testcaseList.item(z);
					String testcaseName = testcaseNd.getAttributes().getNamedItem("name").getNodeValue();
					TestCase testcase = new TestCase();
					testcase.setName(testcaseName);
					testcase.setTransaction(transactionName);
					testcase.setType("connector");
					testcase.setTypeName(connectorName);
					testcase.setUrl(url);
					
					project.addTestCase(testcase);
				}
			}
		}
		
		//connector
		NodeList sequenceList = (NodeList) xPath.compile(projectInforSequenceExp).evaluate(xmlDocument, XPathConstants.NODESET);
		
		for (int i = 0; i < sequenceList.getLength(); i++) {
	
			Node sequence = sequenceList.item(i);
			String sequenceName = sequence.getAttributes().getNamedItem("name").getNodeValue();
						
			//testcase
			NodeList testcaseList = (NodeList) xPath.compile(testcaseFromCurrentNodeExp).evaluate(sequence, XPathConstants.NODESET);					
			for(int z = 0; z<testcaseList.getLength();z++) {
				
				Node testcaseNd = testcaseList.item(z);
				String testcaseName = testcaseNd.getAttributes().getNamedItem("name").getNodeValue();
				TestCase testcase = new TestCase();
				testcase.setName(testcaseName);
				testcase.setType("sequence");
				testcase.setTypeName(sequenceName);
				testcase.setUrl(url);
				project.addTestCase(testcase);
			}
		}
	}
	public enum TestCaseUrl{
		C80("/index.html"),
		XML("/.pxml?"),
		JSON("/.json");		
		private final String tailend;		
		TestCaseUrl(String tail) {
			this.tailend = tail;
		}
		
		public String endTail(){
			return tailend;
		}

	}
	
	
	
	public void deployArchive(File fileUpload) throws JenkinsPluginException {

		String deployServiceURL = protocol + "://" + authority + path
				+ "/admin/services/projects.Deploy?bAssembleXsl="
				+ this.bAssembleXsl;

		PostMethod deployMethod = null;
		// Protocol myhttps = null;

		try {
			deployMethod = new PostMethod(deployServiceURL);

			Part[] parts = { new FilePart(fileUpload.getName(), fileUpload) };
			deployMethod.setRequestEntity(new MultipartRequestEntity(parts,
					deployMethod.getParams()));

			int returnCode = httpClient.executeMethod(deployMethod);
			String httpResponse = deployMethod.getResponseBodyAsString();

			if (returnCode == HttpStatus.SC_OK) {
				Document domResponse;
				try {
					DocumentBuilder parser = DocumentBuilderFactory
							.newInstance().newDocumentBuilder();
					domResponse = parser.parse(new InputSource(
							new StringReader(httpResponse)));
					domResponse.normalize();

					NodeList nodeList = domResponse
							.getElementsByTagName("error");

					if (nodeList.getLength() != 0) {
						Element errorNode = (Element) nodeList.item(0);

						Element errorMessage = (Element) errorNode
								.getElementsByTagName("message").item(0);

						Element exceptionName = (Element) errorNode
								.getElementsByTagName("exception").item(0);

						Element stackTrace = (Element) errorNode
								.getElementsByTagName("stacktrace").item(0);

						if (errorMessage != null) {
							throw new JenkinsPluginException(
									errorMessage.getTextContent(),
									exceptionName.getTextContent(),
									stackTrace.getTextContent());
						} else {
							throw new JenkinsPluginException(
									"An unexpected error has occured during the Convertigo project deployment: \n"
											+ "Body content: \n\n");
							// +
							// XMLUtils.prettyPrintDOMWithEncoding(domResponse,"UTF-8"));
						}
					}
				} catch (ParserConfigurationException e) {
					throw new JenkinsPluginException(
							"Unable to parse the Convertigo server response: \n"
									+ e.getMessage() + ".\n"
									+ "Received response: " + httpResponse);
				} catch (IOException e) {
					throw new JenkinsPluginException(
							"An unexpected error has occured during the Convertigo project deployment.\n"
									+ "(IOException) " + e.getMessage() + "\n"
									+ "Received response: " + httpResponse, e);
				} catch (SAXException e) {
					throw new JenkinsPluginException(
							"Unable to parse the Convertigo server response: "
									+ e.getMessage() + ".\n"
									+ "Received response: " + httpResponse);
				}
			} else {
				decodeResponseError(httpResponse);
			}
		} catch (HttpException e) {
			throw new JenkinsPluginException(
					"An unexpected error has occured during the Convertigo project deployment.\n"
							+ "Cause: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new JenkinsPluginException(
					"Unable to reach the Convertigo server: \n"
							+ "(IOException) " + e.getMessage(), e);
		} catch (Exception e) {
			throw new JenkinsPluginException(
					"Unable to reach the Convertigo server: \n"
							+ "(Exception) " + e.getMessage(), e);
		} finally {
			Protocol.unregisterProtocol("https");
			if (deployMethod != null)
				deployMethod.releaseConnection();
		}
	}
	
	
	public static void main( String[] args ) {
		
		try {
			
//	    	String dir = "C:\\Users\\yinam\\JenkinsPluginsWorkspace\\jenkinsPluginsArtifact\\work\\jobs\\deployement2\\workspace\\Nouveau dossier"; 
//	    	Path path = Paths.get(dir);
//	    	ZipFile zipFiles =  new ZipFile();
//	    	List<File> list = null;
//	    	try {
//				zipFiles.doZip(path);			
//				list = zipFiles.getFileList();
//			} catch (JenkinsPluginException e) {
//				e.printStackTrace();
//			}			
			
			String url = "http://localhost:18080/convertigo";
			RemoteService rs = new RemoteService(url,"admin","admin");
			//dp.deployMultiProjects(list);
			Project project = rs.getProjectInfo("sampleMobileMobTV");
			List<TestCase> testcase = rs.getTestCaseListByProject(project);
			//rs.executeTestCase(testcase.get(7));
			System.out.printf(rs.executeTestCase(testcase.get(6)));
			int a = 0;
			int b = 0;
		} catch (JenkinsPluginException e) {
			e.printStackTrace();
		}
		
	}

}
