/**
 * 
 */
/**
 * @author yinam
 *
 */
package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.block;
import hudson.util.ListBoxModel;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.JenkinsPluginException;
import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.Project;
import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.RemoteService;
import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.TestCase;
import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.DeploymentBuilder;
import com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.TestCaseBlock;


public final class EnableTestCaseBlock {
	
	private final List<TestCaseBlock> testcaseblock;
	
	@DataBoundConstructor
	public EnableTestCaseBlock(List<TestCaseBlock> testcaseblock) {
		this.testcaseblock = (testcaseblock != null)?testcaseblock:(new ArrayList<TestCaseBlock>());
	}

	public List<TestCaseBlock> getTestcaseblock() {
		return testcaseblock;
	}

}