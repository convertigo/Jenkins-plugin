package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kohsuke.stapler.DataBoundConstructor;

public class TestCase{
	
	private String name;
	private String type;
	private String typeName;
	private String comment;
	private String version;
	private String transaction;
	private String url;
	private String projectname;
	
	private Map<String,String> varMap;
	
	public TestCase(){

		varMap = new HashMap();
	}
	
	@DataBoundConstructor
	public TestCase(String projectname,String type, String typename,String transaction, String testcasename) {
		this.projectname = projectname;
		this.type = type;
		this.typeName = typename;		
		this.transaction = transaction;
		this.name = testcasename;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, String> getVarMap() {
		return varMap;
	}
	public void setVarMap(Map<String, String> varMap) {
		this.varMap = varMap;
	}
	
	public void addVar(String varName,String varValue) {
		if(varMap != null){
			varMap.put(varName, varValue);
		}
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTransaction() {
		return this.transaction;
	}

	public void setTransaction(String transactionName) {
		this.transaction = transactionName;
		
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}


	public void setProjectname(String projectname) {
		this.projectname = projectname;
	}

	public String getProjectname() {
		return projectname;
	}
	
	public static Set<String> findTypename(List<TestCase>testcaselist,String type) {
		Set<String> typename = new HashSet<String>();
		for(TestCase testcase:testcaselist) {			
			if(type != null && type.equals(testcase.getType())) {
				typename.add(testcase.getTypeName());
			}  			
		}				
		return typename;
	}
	
	public static Set<String> findTransaction(List<TestCase>testcaselist,String type,String typename) {
		Set<String> transaction = new HashSet<String>();
		if(type != null && type.equals("connector")) {
			for(TestCase testcase:testcaselist) {			
				if(type.equals(testcase.getType())) {
					transaction.add(testcase.getTransaction());
				}  			
			}	
		}
		return transaction;
	}
	public static List<String> findTestcase(List<TestCase>testcaselist,String type,String typename) {
		List<String> list = new ArrayList<String>();
			for(TestCase testcase:testcaselist) {			
				if(type != null && type.equals(testcase.getType()) && 
						typename != null && typename.equals(testcase.getTypeName())) {
					list.add(testcase.getName());
				}  			
			}	
		return list;
	}
}	