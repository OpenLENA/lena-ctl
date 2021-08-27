/*
 * Copyright 2021 LENA Development Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.openlena.ctl.common.vo;

/**
 * Object that is having information of server.
 * @author Erick Yu
 * 
 */
public class Server {
	private String id;
	private String port;
	private String type;
	private String path;
	private String recovery;
	private String version;
	private String cdate;
	private String udate;
	private String hotfix;

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * @return recovery
	 */
	public String getRecovery() {
		return recovery;
	}

	/**
	 * @param recovery
	 */
	public void setRecovery(String recovery) {
		this.recovery = recovery;
	}

	/**
	 * @return version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return cdate
	 */
	public String getCdate() {
		return cdate;
	}

	/**
	 * @param cdate
	 */
	public void setCdate(String cdate) {
		this.cdate = cdate;
	}

	/**
	 * @return udate
	 */
	public String getUdate() {
		return udate;
	}

	/**
	 * @param udate
	 */
	public void setUdate(String udate) {
		this.udate = udate;
	}
	
	/**
	 * @return hotfix
	 */
	public String getHotfix() {
		return hotfix;
	}

	/**
	 * @param hotfix
	 */
	public void setHotfix(String hotfix) {
		this.hotfix = hotfix;
	}

}
