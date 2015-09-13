package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.xml.transform.TransformerException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.w3c.dom.Document;

import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.RemoteService.TestCaseUrl;

/**
 * This object allows to add a ZAP command line option.
 * 
 * @see <a href="https://code.google.com/p/zaproxy/wiki/HelpCmdline">
 *      https://code.google.com/p/zaproxy/wiki/HelpCmdline</a>
 * 
 * @author ludovic.roucoux
 *
 */

public class TestCaseBlock extends AbstractDescribableImpl<TestCaseBlock>
		implements Serializable {
	private static final long serialVersionUID = -695679474175608775L;

	/** Configuration key for the command line */
	private String projectname;
	private String testcasename;
	private String xpath;

	private static List<Project> projectList;
	private static boolean flagUpdate;
	private static String url;
	private static String userId;
	private static String password;
	private static String jobName;
	private static String xmlFilePath;

	// for restoration
	private static String restProjetName;
	private static String restTestCaseName;

	@DataBoundConstructor
	public TestCaseBlock(String projectname, String testcasename, String xpath) {
		// projectList.clear();
		this.projectname = projectname;
		this.testcasename = testcasename;
		this.xpath = xpath;
		restProjetName = projectname;
		restTestCaseName = testcasename;
		// cleanAllAreas();
	}

	public static String getUserId() {
		return userId;
	}

	public static String getPassword() {
		return password;
	}

	public static String getUrl() {
		return url;
	}

	public static void setUrl(String urlTmp, Boolean clear) {
		if (clear == true) {
			cleanAllAreas();
		}
		url = urlTmp;
	}

	public static void setUserId(String userIdTmp, Boolean clear) {
		if (clear == true) {
			cleanAllAreas();
		}
		userId = userIdTmp;
	}

	public static void setPassword(String passwordTmp, Boolean clear) {
		if (clear == true) {
			cleanAllAreas();
		}
		password = passwordTmp;
	}

	public String getProjectname() {
		return this.projectname;
	}

	public String getTestcasename() {
		return this.testcasename;
	}

	public String getXpath() {
		return this.xpath;
	}

	// @JavaScriptMethod
	// public String displayResult(String projectname,String testcasename) {
	// try {
	// this.result = getTestcaseResult(projectname,testcasename);
	// } catch (JenkinsPluginException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return this.result;
	// }
	@JavaScriptMethod
	public String displayResult() {
		return "test ok";
	}

	@Extension
	public static class TestCaseBlockDescriptorImpl extends
			Descriptor<TestCaseBlock> {
		private String testcasename;
		private String projectname;
		private String xpath;
		private String result;

		public String getTestcasename() {
			return this.testcasename;
		}

		public String getProjectname() {
			return this.projectname;
		}

		public String getXpath() {
			return this.xpath;
		}

		@Override
		public String getDisplayName() {
			return "TestCase Line";
		}

		public ListBoxModel doFillProjectnameItems(
				@QueryParameter String projectname,
				@QueryParameter String testcasename) {

			ListBoxModel items = new ListBoxModel();

			if (flagUpdate == false
					&& (projectList == null || projectList.size() == 0)) {// is
																			// this
																			// a
																			// new
																			// block

				if (projectname != null && testcasename != null
						&& !"".equals(projectname) && !"".equals(testcasename)) { // is
																					// this
																					// a
					// restoration
					// block

					items.add(projectname);

				} else {
					try {
						updateProjectList();
					} catch (JenkinsPluginException e) {
						items.add("can't find any project, more details in console log");
					}

				}

			} else {

				if ((projectList != null && projectList.size() != 0)
						&& flagUpdate == false) {
					for (Project project : projectList) {
						if (project.getTestcaseList() != null
								&& project.getTestcaseList().size() != 0)
							items.add(project.getName());
					}
				}
			}

			return items;
		}

		public ListBoxModel doFillTestcasenameItems(
				@QueryParameter String projectname,
				@QueryParameter String testcasename) {

			ListBoxModel items = new ListBoxModel();

			if (flagUpdate == false
					&& (projectList == null || projectList.size() == 0)) {// is
																			// this
																			// a
																			// new
																			// block

				if (projectname != null && testcasename != null
						&& !"".equals(projectname) && !"".equals(testcasename)) { // is
																					// this
																					// a
					// restoration
					// block
					items.add(testcasename);
				}

			} else {

				if (projectname != null && !"".equals(projectname)) {
					List<TestCase> list = null;
					Project project = Project.findProjectByprojectname(
							projectList, projectname);
					list = project.getTestcaseList();
					if (list != null && list.size() != 0) {
						for (TestCase testcase : list) {
							String title = ("null".equals(testcase
									.getTransaction()) || testcase
									.getTransaction() == null) ? "(S)" : "(T)";
							String transaction = ("null".equals(testcase
									.getTransaction()) || testcase
									.getTransaction() == null) ? "" : (testcase
									.getTransaction() + ".");
							String testcasenameLoc = title + projectname + "."
									+ testcase.getTypeName() + "."
									+ transaction + testcase.getName();
							items.add(testcasenameLoc);
						}
					}
				}
			}
			return items;
		}

		public FormValidation doValidateTestCase(
				@QueryParameter("projectname") final String projectname,
				@QueryParameter("testcasename") final String testcasename)
				throws JenkinsPluginException {

			String strDisplay = null;

			try {
				this.result = getTestcaseResult(projectname, testcasename);
				strDisplay = this.result;
			} catch (JenkinsPluginException e) {
				strDisplay = e.getMessage();
			}

			return FormValidation.ok(strDisplay);
		}

		public FormValidation doEvaluateXpath(@QueryParameter String xpath)
				throws IOException, ServletException {
			if (xpath == null || xpath.isEmpty())
				return FormValidation.error("Please set expression xpath");
			else {
				TestResultAnalyzer tr = new TestResultAnalyzer();
				String display = null;
				try {
					display = tr.displayEvaluateResult(xpath, this.result);
				} catch (JenkinsPluginException e) {
					e.printStackTrace();
				}
				return FormValidation.ok(display);
			}
		}

	}

	private static String getTestcaseResult(String projectname,
			String testcasename) throws JenkinsPluginException {
		String result = "";
		if (flagUpdate == false && projectList != null
				&& projectList.size() != 0) {

			RemoteService rs = new RemoteService(url, userId, password);

			for (Project project : projectList) {
				if (projectname != null
						&& projectname.equals(project.getName())) {
					List<TestCase> list = project.getTestcaseList();
					for (TestCase testcase : list) {

						if (testcasename != null
								&& testcasename.contains("(S)")) {
							if (testcasename.contains(testcase.getName())) {
								result = rs.executeTestCase(testcase);
								break;
							}

						} else if (testcasename != null
								&& testcasename.contains("(T)")) {
							if (testcasename.contains(testcase.getName())
									&& testcasename.contains(testcase
											.getTypeName())
									&& testcasename.contains(testcase
											.getTransaction())) {
								result = rs.executeTestCase(testcase);
								break;
							}
						}

						if (!"".equals(result)) {
							break;
						}

					}
				}

			}
		}

		if ("".equals(result)) {

			if (!"".equals(testcasename)) {

				TestCase testcase = extractTestCase(testcasename);
				if (testcase != null) { // testcase found
					RemoteService rs = new RemoteService(url, userId, password);
					result = rs.executeTestCase(testcase);
				}
			}
		}

		return result;
	}

	private static void cleanAllAreas() {

		if (projectList != null) {
			projectList.clear();
			flagUpdate = false;
			projectList = null;
		}
	}

	private static synchronized final void updateProjectList()
			throws JenkinsPluginException {
		try {
			flagUpdate = true;
			RemoteService rs = new RemoteService(url, userId, password);
			projectList = rs.getListProjects();// without info
			for (Project project : projectList) {
				try {
					Project pj = rs.getProjectInfo(project.getName());
					project.addTestcaseList(pj.getTestcaseList());
				} catch (JenkinsPluginException e) {
					// TODO Auto-generated catch block
					// throw new JenkinsPluginException(
					System.out.print("Unable to get test case of Project: "
							+ project.getName() + "\n" + "Received response: "
							+ "\n" + e.getMessage() + "\n");

				}

			}
			flagUpdate = false;

		} catch (JenkinsPluginException e) {
			// TODO Auto-generated catch block
			flagUpdate = false;
			throw new JenkinsPluginException(
					"Unable to get list of projects: from convertigo server "
							+ url + "\n" + e.getMessage() + ".\n");
		}
	}

	// function after building

	public static String executTestCasesAndAnalyse(
			List<TestCaseBlock> blocklist, Document doc, String path)
			throws JenkinsPluginException {

		String logMsg = "";

		List<Project> list = new ArrayList<Project>();

		if (blocklist != null && blocklist.size() != 0) {
			for (int i = 0; i < blocklist.size(); i++) {

				TestCaseBlock tcb = blocklist.get(i);
				String testcaseS = tcb.getTestcasename();
				String xpathLoc = tcb.getXpath();

				logMsg += "\n Launching TestCase " + tcb.getTestcasename() + "\n";

				if (xpathLoc != null && !xpathLoc.isEmpty()
						&& testcaseS != null && !testcaseS.isEmpty()) {

					TestCase tc = extractTestCase(testcaseS);

					if (tc != null) {
						String result = null;
						try {
							RemoteService rs = new RemoteService(url, userId,
									password);
							result = rs.executeTestCase(tc);

							TestResultAnalyzer tra = new TestResultAnalyzer();

							List<String> asserNokList = tra
									.xmlAsserNokEvaluateResult(xpathLoc, result);// here

							if (asserNokList != null
									&& asserNokList.size() != 0) {
								try {
									logMsg += " TestCase + "
											+ tcb.getTestcasename()
											+ " FAILED ! \n";

									tra.addAssertNokIntoXmlFile(doc, path, tc,
											asserNokList);
								} catch (Exception e) {
									logMsg += " error during construction of junit xml file"
											+ e.getMessage();
								}
							} else {

								// test assert ok
								List<String> asserOkList = tra
										.xmlAsserOkEvaluateResult(xpathLoc,
												result);// here

								if (asserOkList != null
										&& asserOkList.size() != 0) { // error
																		// during
																		// assert
																		// ok
									try {
										logMsg += " TestCase + "
												+ tcb.getTestcasename()
												+ " FAILED ! \n";

										tra.addAssertNokIntoXmlFile(doc, path,
												tc, asserOkList);
									} catch (Exception e) {
										logMsg += " error during construction of junit xml file"
												+ e.getMessage();
									}
								} else {
									try {
										tra.addAssertOkIntoXmlFile(doc, path,
												tc);
									} catch (TransformerException e) {
										logMsg += " error during construction of junit xml file"
												+ e.getMessage();
									}
									logMsg += "TestCase "
											+ tcb.getTestcasename()
											+ " SUCCESS ! \n";
								}
							}

						} catch (JenkinsPluginException e) {
							logMsg += " Error during execution of TestCase "
									+ tcb.getTestcasename() + "\n"
									+ e.getMessage() + "\n";
						}

					} else {
						// error
						logMsg += "Error during extraction TestCase "
								+ tcb.getTestcasename() + "\n";
					}
				} else if ("".equals(xpathLoc)) {
					logMsg += "Xpath of TestCase " + tcb.getTestcasename()
							+ " is empty \n";
				}

			}

		}

		return logMsg;

	}

	private static TestCase extractTestCase(String testcaseS) {

		TestCase tc = new TestCase();

		String[] listItems = testcaseS.split("\\.");

		if (listItems.length == 3) { // serquence

			String projetName = listItems[0].replace("(S)", "");
			String sequenceName = listItems[1];
			String testcaseName = listItems[2];

			tc.setName(testcaseName);
			tc.setType("");
			tc.setTypeName(sequenceName);
			tc.setProjectname(projetName);
			tc.setUrl(url + "/projects/" + projetName
					+ TestCaseUrl.XML.endTail());

		} else if (listItems.length == 4) { // transaction
			String projetName = listItems[0].replace("(T)", "");
			String connectorName = listItems[1];
			String transactionName = listItems[2];
			String testcaseName = listItems[3];

			tc.setName(testcaseName);
			tc.setType("connector");
			tc.setTypeName(connectorName);
			tc.setTransaction(transactionName);
			tc.setProjectname(projetName);
			tc.setUrl(url + "/projects/" + projetName
					+ TestCaseUrl.XML.endTail());
		} else {
			// error
			tc = null;
		}

		return tc;
	}

}