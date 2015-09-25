package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


//import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.block.EnableAuthBlock;
import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.block.EnableTestCaseBlock;
import com.google.common.collect.ImmutableList;

//import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.ZipFile;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * HelloWorldBuilder is created. The created instance is persisted to
 * the project configuration XML by using XStream, so this allows you to use
 * instance fields (like @link #name) to remember the configuration.
 *
 * <p>
 * When a build is performed, the
 * @link #perform(AbstractBuild, Launcher, BuildListener) method will be
 * invoked.
 *
 * @author convertigo
 */
public class DeploymentBuilder extends Builder {

	// plugin configurationg
	// private final String carFolder;
	// private final String fileName;
	private final String url;
	private final String userId;
	private final String password;
	private final boolean enableAutoDeploy;
	private final boolean enableZipFile;
	private final List<TestCaseBlock> testcaseblock;
	private final boolean enableTestCase;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"

	// public DeploymentBuilder(EnableAuthBlock enableAutoDeployment) {
	// //this.enableDeployment = enableDeployment;
	// this.serverUrl = (enableAutoDeployment ==
	// null)?"":enableAutoDeployment.getServerUrl();
	// this.userId = (enableAutoDeployment ==
	// null)?"":enableAutoDeployment.getUserId();
	// this.password = (enableAutoDeployment ==
	// null)?"":enableAutoDeployment.getPassword();
	// this.enableAutoDeployment = (enableAutoDeployment == null) ? false :
	// true;
	// }
	// @DataBoundConstructor
	// public DeploymentBuilder(String url,String userId,String
	// password,TestCase enableTestcase) {
	// //this.enableDeployment = enableDeployment;
	// this.url = url;
	// this.userId = userId;
	// this.password = password;
	// this.projectname = enableTestcase.getProjectname();
	// this.testcasename = enableTestcase.getName();
	// this.type = enableTestcase.getType();
	// this.typename = enableTestcase.getTypeName();
	// this.transaction = enableTestcase.getTransaction();
	// this.enableTestCase = (enableTestcase == null)? false : true;
	// }
	// @DataBoundConstructor
	// public DeploymentBuilder(String url,String userId,String
	// password,TestcaseListForm testcaselistForm) {
	// //this.enableDeployment = enableDeployment;
	// this.url = url;
	// this.userId = userId;
	// this.password = password;
	// this.testcaselistForm = testcaselistForm;
	// }
	@DataBoundConstructor
	public DeploymentBuilder(String url, String userId, String password,
			boolean enableZipFile, boolean enableAutoDeploy,
			EnableTestCaseBlock enableTestCase) {
		// this.enableDeployment = enableDeployment;
		this.url = url;
		this.userId = userId;
		this.password = password;
		this.enableZipFile = enableZipFile;
		this.enableAutoDeploy = enableAutoDeploy;
		this.testcaseblock = (enableTestCase != null) ? enableTestCase
				.getTestcaseblock() : null;

		if (enableTestCase != null) {
			this.enableTestCase = true;
			TestCaseBlock.setUserId(userId, false);
			TestCaseBlock.setUrl(url, false);
			TestCaseBlock.setPassword(password, false);
		} else {
			this.enableTestCase = false;
			TestCaseBlock.setUserId("", false);
			TestCaseBlock.setUrl("", false);
			TestCaseBlock.setPassword("", false);
		}

	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return url;
	}

	public List<TestCaseBlock> getTestcaseblock() {
		return this.testcaseblock;
	}

	public boolean isEnableZipFile() {
		return enableZipFile;
	}

	public boolean isEnableAutoDeploy() {
		return enableAutoDeploy;
	}

	public boolean isEnableTestCase() {
		return this.enableTestCase;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) {
		// This is where you 'build' the project.
		// Since this is a dummy, we just say 'hello world' and call that a
		// build.

		// This also shows how you can consult the global configuration of the
		// builder
		/*
		 * if (getDescriptor().getUseFrench())
		 * listener.getLogger().println("Bonjour, "+name+"!"); else
		 * listener.getLogger().println("Hello, "+name+"!");
		 */

		// listener.getLogger().println("serverUrl "+serverUrl+"\n");
		// listener.getLogger().println("userId "+userId+"\n");
		// listener.getLogger().println("password "+password+"\n");

		//
		// //ZipFile carFile = new
		// ZipFile(moduleRoot.toString(),workspace.toString(),fileName,carFolder);
		// //carFile.doZip();
		// //carFile.generateFileList(new File(workSpacePathS));
		// //carFile.zipIt(workSpacePathS,"test");
		// System.out.printf("moduleRoot, "+ moduleRoot +"\n");
		// System.out.printf("workspace, "+ workspace+ "\n");
		// System.out.printf("serverUrl, "+ serverUrl+ "\n");
		// System.out.printf("userId, "+ userId+ "\n");
		// System.out.printf("password, "+ password+ "\n");
		// //CarFiles carf= new CarFiles(filepath,fileName);

		FilePath workspace = build.getWorkspace();
		StringBuilder logMsgSb = new StringBuilder("");
		
		if ((enableAutoDeploy) || (enableZipFile)) {
			try {
				Path path = null;
				URI uri = workspace.toURI();
				path = Paths.get(uri);
				ZipFile zipFiles = new ZipFile();
				List<File> list = null;
				zipFiles.doZip(path);
				list = zipFiles.getFileList();

				// print zip list
				for (File file : list) {
					listener.getLogger()
							.println(file.toURI() + " to deploy \n");
				}

				if (enableAutoDeploy) {
					Deployment dp = new Deployment(url, userId, password);
					dp.deployMultiProjects(list);
				}
			} catch (IOException e) {
				//e.printStackTrace();
				//logMsg += e.getMessage();
				logMsgSb.append(e.getMessage() + "\n");
			} catch (InterruptedException e) {
				//e.printStackTrace();
				//logMsg += e.getMessage();
				logMsgSb.append(e.getMessage() + "\n");
			} catch (JenkinsPluginException e) {
				//logMsg += e.getMessage();
				logMsgSb.append(e.getMessage() + "\n");
			}
		}

		if (enableTestCase) {

			if (testcaseblock != null && testcaseblock.size() != 0) {
				EnvVars envVars = new EnvVars();
				try {

					envVars = build.getEnvironment(listener);
					String jobName = envVars.get("JOB_NAME");
					
					//create new folder TestReports
					boolean success = (new File(workspace+ File.separator + "TestReports")).mkdir();
					String path = workspace.toString() + File.separator
							+ "TestReports" + File.separator;

					TestCaseBlock.setUrl(url, false);
					TestCaseBlock.setUserId(userId, false);
					TestCaseBlock.setPassword(password, false);

					TestResultAnalyzer tra = new TestResultAnalyzer();
					Document doc = tra.generateXmlFile(jobName, path);
					String xmlPath = path + jobName + "_TestCaseReport.xml";
					String logMsg = TestCaseBlock.executTestCasesAndAnalyse(testcaseblock, doc,
							xmlPath);
					logMsgSb.append("\n\n" + logMsg + "\n");

				} catch (IOException e) {
					// TODO Auto-generated catch block
					logMsgSb.append(e.getMessage() + "\n");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e2.printStackTrace();
					logMsgSb.append(e.getMessage() + "\n");
				} catch (JenkinsPluginException e) {
					// TODO Auto-generated catch block
					logMsgSb.append(e.getMessage() + "\n");
				}
			}
		}
		
		listener.getLogger().println(logMsgSb.toString());
		return true;
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Descriptor for @link HelloWorldBuilder. Used as a singleton. The class
	 * is marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See
	 * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	// This indicates to Jenkins that this is an implementation of an extension
	// point.
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Builder> {
		/**
		 * To persist global configuration information, simply store it in a
		 * field and call save().
		 *
		 * <p>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */
		private boolean useFrench;
		private String url;
		private String password;
		private String userId;
		private List<TestCase> testcaseList;
		private static List<Project> projectList;
		private String projectname;
		private String testcasename;
		private String typename;
		private String type;
		private String transaction;
		private TestCase testcase;
		private List<TestCaseBlock> testcaseblock;
		private boolean enableTestCase;

		/**
		 * In order to load the persisted global configuration, you have to call
		 * load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}

		/**
		 * Performs on-the-fly validation of the form field 'name'.
		 *
		 * @param url value
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 *         <p>
		 *         Note that returning {@link FormValidation#error(String)} does
		 *         not prevent the form from being saved. It just means that a
		 *         message will be displayed to the user.
		 *         
		 * @throws IOException, ServletException
		 */
		public FormValidation doCheckUrl(@QueryParameter String url)
				throws IOException, ServletException {

			if (url == null || url.isEmpty()) {
				return FormValidation
						.error("Deployment Server Url is requied ");
			} else {
				try {
					URL urlreal = new URL(url);
					if (Deployment.pingUrl(url)) {
						this.url = url;
						TestCaseBlock.setUrl(url, false);
						return FormValidation.ok();
					} else {
						TestCaseBlock.setUrl(url, false);
						return FormValidation.error("Deployment Server Url "
								+ url + " is not reachable ");
					}

				} catch (MalformedURLException e) {
//					throw new JenkinsPluginException(
//							"The deployment server is not valid: " + url + "\n"
//									+ e.getMessage());
					return FormValidation
							.error("Deployment Server Url is not valid ");
				}
			}

		}

		public FormValidation doCheckUserId(@QueryParameter String userId)
				throws IOException, ServletException {
			if (userId.isEmpty() || userId == null) 
				return FormValidation.error("Please set a userId");
			else {
				this.userId = userId;
				TestCaseBlock.setUserId(userId, false);
				return FormValidation.ok();
			}
		}

		public FormValidation doCheckPassword(@QueryParameter String password)
				throws IOException, ServletException {
			if (password.isEmpty() || password == null)
				return FormValidation.error("Please set a password");
			else {
				this.password = password;
				TestCaseBlock.setPassword(password, false);
				return FormValidation.ok();
			}

		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project
			// types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Convertigo Jenkins Plugin";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			// useFrench = formData.getBoolean("useFrench");
			// ^Can also use req.bindJSON(this, formData);
			// (easier when there are many fields; need set* methods for this,
			// like setUseFrench)

			JSONArray list = formData.getJSONArray("Testcaseblock");

			// List<TestCaseBlock>
			save();
			return super.configure(req, formData);
		}

		/**
		 * This method returns true if the global configuration says we should
		 * speak French.
		 *
		 * The method name is bit awkward because global.jelly calls this method
		 * to determine the initial state of the checkbox by the naming
		 * convention.
		 */
		public boolean getUseFrench() {
			return useFrench;
		}

		public String getProjectname() {
			return projectname;
		}

		public String getTestcasename() {
			return testcasename;
		}

		public String getTypename() {
			return typename;
		}

		public String getType() {
			return type;
		}

		public String getTransaction() {
			return transaction;
		}

		public boolean isEnableTestCase() {
			return enableTestCase;
		}

		public List<TestCaseBlock> getTestcaseblock() {
			return this.testcaseblock;
		}

		public void setTestcaseblock(List<TestCaseBlock> testcaseblock) {
			this.testcaseblock = testcaseblock;
		}

		public List<Descriptor> getTestcaseblockDescriptors() {
			Jenkins jenkins = Jenkins.getInstance();
			return ImmutableList.of(jenkins.getDescriptor(TestCaseBlock.class));
		}

		// public List<Project> doGetProjectList(){
		// try {
		// TestCase testcase = new
		// TestCase("http://localhost:18080/convertigo","admin","admin");
		// projectList = testcase.getProjectList();
		// } catch (JenkinsPluginException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return projectList;
		// }
		//

		// public FormValidation doGetProjectList(
		// @QueryParameter("url") final String url,
		// @QueryParameter("userId") final String userId,
		// @QueryParameter("password") final String password
		// ){
		//
		// try {
		// TestCase testcase = new
		// TestCase("http://localhost:18080/convertigo","admin","admin");
		// projectList = testcase.getProjectList();
		// } catch (JenkinsPluginException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return FormValidation.ok();
		// }

		// public ListBoxModel doFillProjectListItems() {
		// ListBoxModel items = new ListBoxModel();
		// List<Project> projectList = null;
		// try {
		// TestCase testcase = new
		// TestCase("http://localhost:18080/convertigo","admin","admin");
		// projectList = testcase.getProjectList();
		// } catch (JenkinsPluginException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// for (Project project : projectList) {
		//
		// Option op = new Option(project.getName());
		// items.add(op);
		// }
		// return items;
		// }

		// public ListBoxModel doFillProjectnameItems() {
		// ListBoxModel items = new ListBoxModel();
		// try {
		// RemoteService rs = new
		// RemoteService("http://localhost:18080/convertigo","admin","admin");
		// projectList = rs.getListProjects();//without info
		// for (Project project : projectList) {
		// try {
		// Project pj = rs.getProjectInfo(project.getName());
		// project.addTestcaseList(pj.getTestcaseList());
		// } catch (JenkinsPluginException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// }
		//
		// } catch (JenkinsPluginException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// for (Project project : projectList) {
		// if(project.getTestcaseList() != null &&
		// project.getTestcaseList().size() != 0)
		// items.add(project.getName());
		// }
		//
		// return items;
		// }
		//
		// public ListBoxModel doFillTestcaseItems(@QueryParameter String
		// projectname) {
		//
		// ListBoxModel items = new ListBoxModel();
		// List<TestCase> list = null;
		// if(projectname != null && !projectname.isEmpty()) {
		// Project project = Project.findProjectByprojectname(projectList,
		// projectname);
		// list = project.getTestcaseList();
		// if(list!=null&&list.size() != 0) {
		// for (TestCase testcase : list) {
		// String transaction = ("null".equals(testcase.getTransaction()) ||
		// testcase.getTransaction() == null)?"":testcase.getTransaction();
		// String testcasename = projectname +"." + testcase.getTypeName()+ "."
		// + transaction + "." + testcase.getName();
		// items.add(testcasename);
		// }
		// }
		// }
		//
		// return items;
		// }

	}

}
