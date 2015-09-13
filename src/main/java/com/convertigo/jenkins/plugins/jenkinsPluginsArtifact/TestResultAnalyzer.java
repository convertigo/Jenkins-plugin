package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class TestResultAnalyzer {

	private static final String syntaxAsOk = "assert ok";
	private static final String syntaxAsNok = "assert nok";

	// xml junit
	private final static String messsageErr = "error";
	private final static String messageErrType = "error";

	List<String> listAssertOk;
	List<String> listAssertNok;
	Map<String, ReturnType> mapAssertOk;
	Map<String, ReturnType> mapAssertNok;

	public enum ReturnType {
		SYNTAX_OK("Xpath syntax is correct", 1), SYNTAX_NOK(
				"Xpath syntax is not correct", 2), FOUND(
				"Xpath syntax is correct and found", 0), NOT_FOUND(
				"Xpath syntax is correct but not found ", 3);

		String message;
		int codeError;

		ReturnType(String msg, int code) {
			this.message = msg;
			this.codeError = code;
		}

		String getMessage() {
			return message;
		}
	}

	public enum AssertType {
		OK, NOK
	}

	public TestResultAnalyzer() {
		listAssertNok = new ArrayList<String>();
		listAssertOk = new ArrayList<String>();
		mapAssertOk = new HashMap<String, ReturnType>();
		mapAssertNok = new HashMap<String, ReturnType>();
	}

	public void addToListAssertOkh(String xpathline) {
		if (listAssertOk != null) {
			listAssertOk.add(xpathline);

		} else {
			listAssertOk = new ArrayList<String>();
		}
	}

	public void addToListAssertNok(String xpathline) {
		if (listAssertNok != null) {
			listAssertNok.add(xpathline);

		} else {
			listAssertNok = new ArrayList<String>();
		}
	}

	public ReturnType evaluateXpath(String xpathline, Document doc) {
		ReturnType returnType = ReturnType.SYNTAX_OK;

		String exprXpath = xpathline;

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(exprXpath);
		} catch (XPathExpressionException e) {
			returnType = ReturnType.SYNTAX_NOK; // syntax error
			return returnType;
		}
		if (doc != null) {
			try {
				NodeList result = (NodeList) expr.evaluate(doc,
						XPathConstants.NODESET);
				if (result.getLength() > 0) {
					returnType = ReturnType.FOUND;
				} else {
					returnType = ReturnType.NOT_FOUND;
				}
			} catch (XPathExpressionException e) {
				returnType = ReturnType.NOT_FOUND; // syntax ok but not found
				e.printStackTrace();
			}
		}
		return returnType;
	}

	public void evaluateXpaths(AssertType type, List<String> xpathlines,
			Document doc) {

		if (type == AssertType.OK) {

			for (int i = 0; i < xpathlines.size(); i++) {

				String exprXpath = xpathlines.get(i);
				ReturnType returnType = evaluateXpath(exprXpath, doc);
				if (mapAssertOk != null)
					mapAssertOk.put(exprXpath, returnType);
				else {
					mapAssertOk = new HashMap<String, ReturnType>();
					mapAssertOk.put(exprXpath, returnType);
				}
			}

		} else if (type == AssertType.NOK) {
			for (int i = 0; i < xpathlines.size(); i++) {

				String exprXpath = xpathlines.get(i);
				ReturnType returnType = evaluateXpath(exprXpath, doc);
				if (mapAssertNok != null)
					mapAssertNok.put(exprXpath, returnType);
				else {
					mapAssertNok = new HashMap<String, ReturnType>();
					mapAssertNok.put(exprXpath, returnType);
				}
			}
		}

	}

	public String displayEvaluateResult(String xpaths, String text)
			throws JenkinsPluginException {

		StringBuilder evaluateResultB = new StringBuilder();
		Document doc = null;
		if (text != null && !text.isEmpty()) {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				doc = builder.parse(new InputSource(new StringReader(text)));
			} catch (Exception e) {
				throw new JenkinsPluginException(
						"Unable to parse this TestCase response \n"
								+ e.getMessage() + "\n");
			}
		}

		listAssertOk = getListAssertOk(xpaths);

		if (!listAssertOk.isEmpty()) {
			evaluateXpaths(AssertType.OK, listAssertOk, doc);

			for (Map.Entry<String, TestResultAnalyzer.ReturnType> entry : mapAssertOk
					.entrySet()) {
				evaluateResultB.append(entry.getKey()).append("  ")
						.append(entry.getValue()).append("\n");
			}
		}

		listAssertNok = getListAssertNok(xpaths);

		if (!listAssertNok.isEmpty()) {
			evaluateXpaths(AssertType.NOK, listAssertNok, doc);

			for (Map.Entry<String, TestResultAnalyzer.ReturnType> entry : mapAssertNok
					.entrySet()) {
				TestResultAnalyzer.ReturnType rt = (TestResultAnalyzer.ReturnType) entry
						.getValue();
				evaluateResultB.append(entry.getKey()).append("  ")
						.append(rt.getMessage()).append("\n");
			}
		}
		return evaluateResultB.toString();
	}

	public List<String> xmlAsserNokEvaluateResult(String xpaths, String text)
			throws JenkinsPluginException {

		List<String> responseList = new ArrayList<String>();

		Document doc = null;
		if (text != null && !text.isEmpty()) {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				doc = builder.parse(new InputSource(new StringReader(text)));
			} catch (Exception e) {
				throw new JenkinsPluginException(
						"Unable to parse this TestCase response \n"
								+ e.getMessage() + "\n");
			}
		}

		listAssertNok = getListAssertNok(xpaths);

		if (!listAssertNok.isEmpty()) {
			evaluateXpaths(AssertType.NOK, listAssertNok, doc);

			for (Map.Entry<String, TestResultAnalyzer.ReturnType> entry : mapAssertNok
					.entrySet()) {
				TestResultAnalyzer.ReturnType rt = (TestResultAnalyzer.ReturnType) entry
						.getValue();
				if (rt != TestResultAnalyzer.ReturnType.NOT_FOUND) // filter
																	// items
																	// whose
																	// syntax ok
																	// but not
																	// found in
																	// xml
																	// result
					responseList.add(entry.getKey() + " " + rt.getMessage()
							+ "\n");
			}
		}
		return responseList;
	}

	public List<String> xmlAsserOkEvaluateResult(String xpaths, String text)
			throws JenkinsPluginException {

		List<String> responseList = new ArrayList<String>();// return error list

		Document doc = null;
		if (text != null && !text.isEmpty()) {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				doc = builder.parse(new InputSource(new StringReader(text)));
			} catch (Exception e) {
				throw new JenkinsPluginException(
						"Unable to parse this TestCase response \n"
								+ e.getMessage() + "\n");
			}
		}

		listAssertOk = getListAssertOk(xpaths);

		if (!listAssertOk.isEmpty()) {
			evaluateXpaths(AssertType.OK, listAssertOk, doc);

			for (Map.Entry<String, TestResultAnalyzer.ReturnType> entry : mapAssertOk
					.entrySet()) {
				TestResultAnalyzer.ReturnType rt = (TestResultAnalyzer.ReturnType) entry
						.getValue();
				if (rt != TestResultAnalyzer.ReturnType.FOUND) // filter items
																// whose syntax
																// ok but not
																// found in xml
																// result
					responseList.add(entry.getKey() + " " + rt.getMessage()
							+ "\n");
			}
		}

		return responseList;
	}

	public List<String> displayAsserOkEvaluateResult(String xpaths, String text)
			throws JenkinsPluginException {

		StringBuilder evaluateResultB = new StringBuilder();
		Document doc = null;
		if (text != null && !text.isEmpty()) {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				doc = builder.parse(new InputSource(new StringReader(text)));
			} catch (Exception e) {
				throw new JenkinsPluginException(
						"Unable to parse this TestCase response \n"
								+ e.getMessage() + "\n");
			}
		}

		listAssertOk = getListAssertOk(xpaths);

		if (!listAssertOk.isEmpty()) {
			evaluateXpaths(AssertType.OK, listAssertOk, doc);

			for (Map.Entry<String, TestResultAnalyzer.ReturnType> entry : mapAssertOk
					.entrySet()) {
				evaluateResultB.append(entry.getKey()).append("  ")
						.append(entry.getValue()).append("\n");
			}
		}

		// listAssertNok = getListAssertNok(xpaths);
		//
		// if(!listAssertNok.isEmpty()) {
		// evaluateXpaths(AssertType.NOK, listAssertNok,doc);
		//
		// for (Map.Entry<String, TestResultAnalyzer.ReturnType> entry :
		// mapAssertNok.entrySet()) {
		// TestResultAnalyzer.ReturnType rt =
		// (TestResultAnalyzer.ReturnType)entry.getValue();
		// evaluateResultB.append(entry.getKey()).append("  ").append(rt.getMessage()).append("\n");
		// }
		// }
		return listAssertOk;
	}

	public ReturnType evaluateXpath(String xpathline, String text) {

		ReturnType returnType = ReturnType.SYNTAX_OK;
		;
		String exprXpath = xpathline;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(text)));
		} catch (Exception e) {
			returnType = ReturnType.SYNTAX_NOK;
			return returnType;
		}

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(exprXpath);
		} catch (XPathExpressionException e) {
			returnType = ReturnType.SYNTAX_NOK;
			return returnType;
		}
		try {
			NodeList result = (NodeList) expr.evaluate(doc,
					XPathConstants.NODESET);
			if (result.getLength() > 0) {
				returnType = ReturnType.FOUND;
			} else {
				returnType = ReturnType.NOT_FOUND;
			}
		} catch (XPathExpressionException e) {
			returnType = ReturnType.NOT_FOUND;
			e.printStackTrace();
		}

		return returnType;

	}

	public List<String> getListAssertOk(String str) {
		List<String> list = new ArrayList<String>();
		boolean addNextLine = false;

		if (str != null) {
			String[] strList = str.split("(\\n|;)");

			for (String line : strList) {
				if (line != null && line.length() > 0) {
					if (line.contains(syntaxAsOk)) {
						addNextLine = true;
					} else if (line.contains(syntaxAsNok)) {
						addNextLine = false;
					} else if (addNextLine == true) {
						list.add(line);
					}
				}
			}

		}
		return list;
	}

	public List<String> getListAssertNok(String str) {
		List<String> list = new ArrayList<String>();
		boolean addNextLine = false;

		if (str != null) {
			String[] strList = str.split("(\\n|;)");

			for (String line : strList) {
				if (line != null && line.length() > 0) {
					if (line.contains(syntaxAsNok)) {
					//if (syntaxAsNok.equals(line)) {	
						addNextLine = true;
					} else if (line.contains(syntaxAsOk)) {
						addNextLine = false;
					} else if (addNextLine == true) {
						list.add(line);
					}
				}
			}
		}
		return list;
	}

	public Document generateXmlFile(String jobName, String path)
			throws JenkinsPluginException {
		Document doc = null;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("testsuites");

			// set attribute to testsuites
			Attr nameAttr = doc.createAttribute("name");
			nameAttr.setValue(jobName);
			rootElement.setAttributeNode(nameAttr);

			Attr idAttr = doc.createAttribute("id");
			idAttr.setValue("");
			rootElement.setAttributeNode(idAttr);

			doc.appendChild(rootElement);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			String filename = jobName + "_TestCaseReport.xml";

			StreamResult result = new StreamResult(new File(path + filename));
			transformer.transform(source, result);

		} catch (ParserConfigurationException e) {

			throw new JenkinsPluginException(
					"error during creation of xml file for TestCase report \n"
							+ e.getMessage() + "\n");

		} catch (TransformerException tfe) {
			throw new JenkinsPluginException(
					"error during creation of xml file for TestCase report \n"
							+ tfe.getMessage() + "\n");
		}
		return doc;
	}

	public void addAssertNokIntoXmlFile(Document doc, String path,
			TestCase testcase, List<String> displaylist)
			throws JenkinsPluginException, TransformerException {

		if (doc != null) {

			String projectname = testcase.getProjectname();
			Boolean projetFound = false;

			// find weather project existe

			NodeList nodelists = doc.getElementsByTagName("testsuite");

			for (int i = 0; i < nodelists.getLength(); i++) {

				Element testsuite = (Element) nodelists.item(i);
				String testcaseName = testsuite.getAttribute("name");

				if (testcaseName != null
						&& testcaseName.equals(testcase.getProjectname())) {

					// reset flag
					projetFound = true;

					// add testcase result
					Element newTestcase = doc.createElement("testcase");

					if (testcase.getType() != null
							&& testcase.getType().equals("connector")) {
						newTestcase.setAttribute("name",
								"T:" + testcase.getTransaction() + ":"
										+ testcase.getTypeName() + ":"
										+ testcase.getName());
					} else {
						newTestcase.setAttribute(
								"name",
								"S:" + testcase.getTypeName() + ":"
										+ testcase.getName());
					}

					// add failure to testcase
					Element newFailure = doc.createElement("failure");
					newFailure.setAttribute("message", messsageErr);
					newFailure.setAttribute("message", messageErrType);
					newFailure.setTextContent(displaylist.toString());
					newTestcase.appendChild(newFailure);

					testsuite.appendChild(newTestcase);
				}
			}

			if ((projetFound == false)) {

				// add new testsuite
				Element newTestsuite = doc.createElement("testsuite");
				newTestsuite.setAttribute("id", "");
				newTestsuite.setAttribute("name", testcase.getProjectname());

				// add testcase result
				Element newTestcase = doc.createElement("testcase");

				if (testcase.getType() != null
						&& testcase.getType().equals("connector")) {
					newTestcase.setAttribute(
							"name",
							"T:" + testcase.getTransaction() + ":"
									+ testcase.getTypeName() + ":"
									+ testcase.getName());
				} else {
					newTestcase.setAttribute(
							"name",
							"S:" + testcase.getTypeName() + ":"
									+ testcase.getName());
				}

				// add failure to testcase
				Element newFailure = doc.createElement("failure");
				newFailure.setAttribute("message", messsageErr);
				newFailure.setAttribute("message", messageErrType);
				newFailure.setTextContent(displaylist.toString());
				newTestcase.appendChild(newFailure);

				newTestsuite.appendChild(newTestcase);

				Element testsuites = doc.getDocumentElement();

				if (testsuites == null) {
					throw new JenkinsPluginException(
							"error of junit xml document ");
				}
				testsuites.appendChild(newTestsuite);

			}

			// serializing
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			StreamResult result = new StreamResult(path);
			transformer.transform(source, result);
		}

	}

	public void addAssertOkIntoXmlFile(Document doc, String path,
			TestCase testcase) throws JenkinsPluginException,
			TransformerException {

		if (doc != null) {

			String projectname = testcase.getProjectname();
			Boolean projetFound = false;

			// find weather project existe

			NodeList nodelists = doc.getElementsByTagName("testsuite");

			for (int i = 0; i < nodelists.getLength(); i++) {

				Element testsuite = (Element) nodelists.item(i);
				String testcaseName = testsuite.getAttribute("name");

				if (testcaseName != null
						&& testcaseName.equals(testcase.getProjectname())) {

					// reset flag
					projetFound = true;

					// add testcase result
					Element newTestcase = doc.createElement("testcase");

					if (testcase.getType() != null
							&& testcase.getType().equals("connector")) {
						newTestcase.setAttribute("name",
								"T:" + testcase.getTransaction() + ":"
										+ testcase.getTypeName() + ":"
										+ testcase.getName());
					} else {
						newTestcase.setAttribute(
								"name",
								"S:" + testcase.getTypeName() + ":"
										+ testcase.getName());
					}
					testsuite.appendChild(newTestcase);
				}
			}

			if ((projetFound == false)) {

				// add new testsuite
				Element newTestsuite = doc.createElement("testsuite");
				newTestsuite.setAttribute("id", "");
				newTestsuite.setAttribute("name", testcase.getProjectname());

				// add testcase result
				Element newTestcase = doc.createElement("testcase");

				if (testcase.getType() != null
						&& testcase.getType().equals("connector")) {
					newTestcase.setAttribute(
							"name",
							"T:" + testcase.getTransaction() + ":"
									+ testcase.getTypeName() + ":"
									+ testcase.getName());
				} else {
					newTestcase.setAttribute(
							"name",
							"S:" + testcase.getTypeName() + ":"
									+ testcase.getName());
				}
				newTestsuite.appendChild(newTestcase);

				Element testsuites = doc.getDocumentElement();

				if (testsuites == null) {
					throw new JenkinsPluginException(
							"error of junit xml document ");
				}
				testsuites.appendChild(newTestsuite);

			}

			// serializing
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			StreamResult result = new StreamResult(path);
			transformer.transform(source, result);
		}

	}

	// public static void main(String arg[]) {
	// TestResultAnalyzer test = new TestResultAnalyzer();
	//
	// String
	// xmlDocument="C:\\Users\\yinam\\JenkinsPluginsWorkspace\\jenkinsPluginsArtifact\\work\\jobs\\deployement2\\config.xml";
	// String xpathline = "locationss=";
	// try {
	// DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
	// DocumentBuilder builder=factory.newDocumentBuilder();
	// Document doc=builder.parse(xmlDocument);
	// ReturnType rt = test.evaluateXpath(xpathline, doc);
	// System.out.print(rt.message);
	// }
	// catch ( ParserConfigurationException|SAXException|IOException e) {
	// e.printStackTrace();
	// }
	//
	//
	// }

}
