package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact;

import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
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

public class Deployment {

	// http
	private String serverUrl;
	private HttpClient httpClient;
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
	private RemoteService rs;
	
	
	public Deployment() {
		this.serverUrl = "www.convertigo.com";
		this.protocol = "http";
		this.port = 80;
	}

	public Deployment(String serverUrl, String userId, String password)
			throws JenkinsPluginException {

		this.serverUrl = serverUrl;
		this.httpClient = new HttpClient();

		// parse Url
		try {
			URL url = new URL(serverUrl);

			this.protocol = url.getProtocol();
			this.authority = url.getAuthority();
			this.host = url.getHost();
			this.path = url.getPath();
			this.bHttps = "https".equals(url.getProtocol()) ? true : false;
			this.bAssembleXsl = false;
			this.bAuthenticated = false;

			// port
			if (url.getPort() == -1) {
				if (bHttps)
					this.port = 443;
				else
					this.port = 80;
			} else
				this.port = url.getPort();
			this.userId = userId;
			this.password = password;
			
			rs = new RemoteService(serverUrl,userId,password);
			

		} catch (MalformedURLException e) {
			throw new JenkinsPluginException(
					"The deployment server is not valid: " + serverUrl + "\n"
							+ e.getMessage());
		}

	}


	public void deployMultiProjects(List<File> projectList)
			throws JenkinsPluginException {

		int nbrError = 0;
		StringBuffer fullMsgError = new StringBuffer("");

		for (File file : projectList) {
			try {
				rs.checkAuthentication();
				if (this.bAuthenticated == false) {
						rs.login();
				}
				rs.deployArchive(file);
			} catch (JenkinsPluginException e) {
				nbrError++;
				String msgError = "Unable to deploy project "
						+ file.toURI().toString() + "\n";
				fullMsgError.append(msgError);
				fullMsgError.append(e.getMessage() + "\n");
			}
		}
		if (nbrError != 0)
			throw new JenkinsPluginException(fullMsgError.append(
					"there are " + nbrError
							+ " errors during convertigo deployment process. ")
					.toString());
	}



	public static boolean pingUrl(final String address) {
		try {
			final URL url = new URL(address);
			final HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setConnectTimeout(1000 * 5); // mTimeout is in seconds
			final long startTime = System.currentTimeMillis();
			urlConn.connect();
			final long endTime = System.currentTimeMillis();
			if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				System.out.println("Time (ms) : " + (endTime - startTime));
				System.out.println("Ping to " + address + " was success");
				return true;
			}
		} catch (final MalformedURLException e1) {
			e1.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return false;
	}


	public static boolean pingUrl(final URL url) {
		try {
			// final URL url = new URL("http://" + address);
			final HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setConnectTimeout(1000 * 2); // mTimeout is in seconds
			final long startTime = System.currentTimeMillis();
			urlConn.connect();
			final long endTime = System.currentTimeMillis();
			if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				System.out.println("Time (ms) : " + (endTime - startTime));
				System.out
						.println("Ping to " + url.toString() + " was success");
				return true;
			}
		} catch (final MalformedURLException e1) {
			e1.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	
	public static void main(String[] args) {

		// try {
		//
		// String dir =
		// "C:\\Users\\yinam\\JenkinsPluginsWorkspace\\jenkinsPluginsArtifact\\work\\jobs\\deployement2\\workspace\\Nouveau dossier";
		// Path path = Paths.get(dir);
		// ZipFile zipFiles = new ZipFile();
		// List<File> list = null;
		// try {
		// zipFiles.doZip(path);
		// list = zipFiles.getFileList();
		// } catch (JenkinsPluginException e) {
		// e.printStackTrace();
		// }
		//
		// String url = "http://localhost:18080/convertigo";
		// Deployment dp = new Deployment(url, "admin", "admin");
		// dp.deployMultiProjects(list);
		//
		// } catch (JenkinsPluginException e) {
		// e.printStackTrace();
		// }
		//
	}

}