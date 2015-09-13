/*
 * Copyright (c) 2001-2011 Convertigo SA.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 * $URL: svn://devus.twinsoft.fr/convertigo/CEMS_opensource/trunk/Studio/src/com/twinsoft/convertigo/engine/util/RemoteAdminException.java $
 * $Author: jibrilk $
 * $Revision: 28547 $
 * $Date: 2011-10-12 17:56:49 +0200 (mer., 12 oct. 2011) $
 */
package com.convertigo.jenkins.plugins.jenkinsPluginsArtifact;

public class JenkinsPluginException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String stackTrace = null;
	public String exceptionName = null;

	public JenkinsPluginException(String errorMessage) {
		super(errorMessage);
	}

	public JenkinsPluginException(String errorMessage, Throwable exception) {
		super(errorMessage, exception);
	}

	public JenkinsPluginException(String errorMessage, String exceptionName, String stackTrace) {
		super(errorMessage);
		this.stackTrace  = stackTrace;
		this.exceptionName = exceptionName;
	}

}
