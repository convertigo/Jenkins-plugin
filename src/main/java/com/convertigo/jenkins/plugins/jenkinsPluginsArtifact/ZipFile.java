/**
 * 
 */
/**
 * @author yinam
 *
 */
package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

//import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.JenkinsPluginException;

public class ZipFile {
	List<File> carfileList;
	List<Path> filePathList;
	Map<Path, String> projectDirMap;

	boolean bflag;

	final static String expression = "/convertigo/project/property[@name='name']/java.lang.String/@value";
	XPath xPath = XPathFactory.newInstance().newXPath();

	public ZipFile() {
		carfileList = new ArrayList<File>();
		filePathList = new ArrayList<Path>();
		projectDirMap = new HashMap<Path, String>();
	}

	public ZipFile(Path workspacePath) {
		this.bflag = true;
		carfileList = new ArrayList<File>();
		filePathList = new ArrayList<Path>();
		projectDirMap = new HashMap<Path, String>();
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		// String dir = "C:\\Users\\yinam\\test";
		// Path path = Paths.get(dir);
		// ZipFile zipFiles = new ZipFile();
		// List<File> list = null;
		// try {
		// zipFiles.doZip(path);
		// list = zipFiles.getFileList();
		// } catch (JenkinsPluginException e) {
		// e.printStackTrace();
		// }
	}

	//
	public void doZip(Path workspacePath) throws JenkinsPluginException {

		generatePrjDirList(workspacePath);

		if (this.projectDirMap != null && this.projectDirMap.size() != 0) {

			for (Map.Entry<Path, String> entry : this.projectDirMap.entrySet()) {
				cleanFilePathList();
				Path projectPath = entry.getKey();
				String projectName = entry.getValue();

				// zip one file
				generateFilePathList(projectPath.toFile());
				zipProject(this.filePathList, projectPath, projectName);

			}
		}
	}

	/**
	 * Zip zipProject
	 * 
	 * @param zipFile
	 *            output ZIP file location
	 */

	public void zipProject(List<Path> filePathList, Path projectPath,
			String zipFileName) {

		byte[] buffer = new byte[1024];

		try {
			Path dstZipFilePath = projectPath.getParent();
			Path dirName = dstZipFilePath.getFileName();
			String relativePath = zipFileName;
			if (zipFileName != null && !zipFileName.equals(dirName.toString())) {
				relativePath = zipFileName;
			}

			Path pathTmp = dstZipFilePath.resolve(zipFileName + ".car");
			File zipFile = pathTmp.toFile();
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			System.out.println("Output to : " + dstZipFilePath.toString());

			for (Path path : filePathList) {

				System.out.println("File Added : " + path.toString());
				String zipEntryName = path.toString().replace(
						projectPath.toString(), relativePath);
				System.out.println("File Added : " + zipEntryName);
				zipEntryName = zipEntryName.replaceAll("\\\\", "/");
				ZipEntry ze = new ZipEntry(zipEntryName);
				zos.putNextEntry(ze);

				File f = path.toFile();
				FileInputStream in = new FileInputStream(f);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}

			zos.closeEntry();
			// remember close it
			zos.close();

			System.out.println("Done");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Traverse a directory and get all files, and add the file into fileList
	 * 
	 * @param node
	 *            file or directory
	 */
	public void generateFilePathList(File node) {
		// add file only
		if (node.isFile()) {
			// if(!node.getName().equals(filename))
			filePathList.add(node.toPath());
		}
		if (node.isDirectory()) {
			if (!node.getName().startsWith(".")) {
				String[] subNote = node.list();

				for (String filename : subNote) {
					generateFilePathList(new File(node, filename));
				}
			}
		}
	}

	// get direcroties of builded projects
	public void generatePrjDirList(Path dirPath) throws JenkinsPluginException {

		String projectName = isSvnProjectDir(dirPath);
		if (projectName != null && (!"".equals(projectName))) {
			projectDirMap.put(dirPath, projectName.replace(".xml", ""));

			// construct carfile list
			Path carPath = dirPath.getParent().resolve(
					projectName.replace(".xml", "") + ".car");
			carfileList.add(carPath.toFile());
			return;
		}

		File[] files = dirPath.toFile().listFiles();

		for (File file : files) {
			if (file.isDirectory() && !(file.getName().startsWith("."))) {
				Path path = Paths.get(file.getAbsolutePath());
				projectName = isSvnProjectDir(path);
				if (projectName != null && (!"".equals(projectName))) {
					projectDirMap.put(path, projectName.replace(".xml", ""));

					// construct carfile list
					Path carPath = path.getParent().resolve(
							projectName.replace(".xml", "") + ".car");
					carfileList.add(carPath.toFile());
					continue;
				} else {
					generatePrjDirList(path);
				}
			}
		}
	}

	private String isSvnProjectDir(Path dirPath) throws JenkinsPluginException {
		String projectName = null;
		File dir = dirPath.toFile();
		File[] files = dir.listFiles();

		for (File file : files) {
			if (file.isFile()) {
				if (file.getName().endsWith(".xml")) {
					projectName = file.getName();
					if (parseXml(file, projectName))
						return projectName;
					else
						projectName = null;
				}
			}
		}
		return projectName;
	}

	private boolean parseXml(File file, String filename)
			throws JenkinsPluginException {
		String xmlfileName = filename.replace(".xml", "");
		String projectName = null;
		FileInputStream inf = null;
		try {
			inf = new FileInputStream(file);
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			builder = builderFactory.newDocumentBuilder();
			Document xmlDocument;
			xmlDocument = builder.parse(inf);
			projectName = (String) xPath.compile(expression).evaluate(
					xmlDocument, XPathConstants.STRING);
		} catch (FileNotFoundException e) {
			throw new JenkinsPluginException(
					"fail to find directory of project " + filename + " "
							+ e.getMessage());
		} catch (SAXException e) {
			throw new JenkinsPluginException(
					"fail to find directory of project " + filename + " "
							+ e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new JenkinsPluginException(
					"fail to find directory of project " + filename + " "
							+ e.getMessage());
		} catch (XPathExpressionException e) {
			throw new JenkinsPluginException(
					"fail to find directory of project " + filename + " "
							+ e.getMessage());
		} catch (IOException e) {
			throw new JenkinsPluginException(
					"fail to find directory of project " + filename + " "
							+ e.getMessage());
		} finally {
			if (inf != null)
				try {
					inf.close();
				} catch (IOException e) {
					throw new JenkinsPluginException(
							"fail to find directory of project " + filename
									+ " " + e.getMessage());
				}
		}

		return ((projectName != null) && (xmlfileName.equals(projectName)));
	}

	public List<File> getFileList() {
		// cherck if carfile isExist
		if (this.carfileList == null && this.carfileList.isEmpty())
			return null;
		else {
			for (File file : this.carfileList) {
				if (!file.exists())
					return null;
			}
		}
		return this.carfileList;
	}

	public void cleanFilePathList() {
		this.filePathList.clear();
	}

}