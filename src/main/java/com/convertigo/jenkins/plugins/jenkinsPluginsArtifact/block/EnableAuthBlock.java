/**
 * 
 */
/**
 * @author yinam
 *
 */
package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact.block;
import org.kohsuke.stapler.DataBoundConstructor;


public final class EnableAuthBlock {
	
	private final String userId;
	private final String password;
	private final String serverUrl;

	@DataBoundConstructor
	public EnableAuthBlock(String userId, String password, String serverUrl) {
		this.userId = userId;
		this.password = password;
		this.serverUrl = serverUrl;
	}

	public String getPassword() {
		return password;
	}
	public String getUserId() {
		return userId;
	}
	public String getServerUrl() {
		return serverUrl;
	}	
}