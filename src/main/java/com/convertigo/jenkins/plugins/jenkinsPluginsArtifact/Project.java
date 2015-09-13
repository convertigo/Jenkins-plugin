package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;


public class Project {
	
	private String name;
	private String comment;
	private String deployDate;
	private String exported;
	private String version;
	private List<TestCase> testcaseList;
	
	public Project(){
		this.name = "";
		this.comment = "";
		this.deployDate = "";
		this.exported = "";
		this.version = "";
		this.testcaseList = new ArrayList<TestCase>();
	}
	
	@DataBoundConstructor
	public Project(String name, String comment, String deployDate, String exported, String version) {
		this.name = name;
		this.comment = comment;
		this.deployDate = deployDate;
		this.exported = exported;
		this.version = version;
		this.testcaseList = new ArrayList<TestCase>();
	}

	public Project(String name) {
		this.name = name;
		this.comment = "";
		this.deployDate = "";
		this.exported = "";
		this.version = "";
		this.testcaseList = new ArrayList<TestCase>();
	}
	
	public String getName() {
		return name;
	}
	public String getComment() {
		return comment;
	}
	public String getDeployDate() {
		return deployDate;
	}	
	public String getExported() {
		return exported;
	}	
	public String getVersion() {
		return version;
	}	
	public void addTestCase(TestCase testcase) {
		if(testcaseList != null)
			testcaseList.add(testcase);
	}
	
	public TestCase createTestCase() {
		return new TestCase();
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setDeployDate(String deployDate) {
		this.deployDate = deployDate;
	}

	public void setExported(String exported) {
		this.exported = exported;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<TestCase> getTestcaseList() {
		return testcaseList;
	}

	public void setTestcaseList(List<TestCase> testcaseList) {
		this.testcaseList = testcaseList;
	}
	public void addTestcaseList(List<TestCase> testcaseList) {
		if(testcaseList != null && testcaseList.size() != 0) {
			this.testcaseList.addAll(testcaseList);
		}
	}
	public static Project findProjectByprojectname(List<Project>projectlist,String projectname) {
		Project project = null;

		for(Project item:projectlist) {
			if(projectname != null && projectname.equals(item.getName())) {
				project = item; 
			}
		}		
		return project;
	}	
	

	
}
