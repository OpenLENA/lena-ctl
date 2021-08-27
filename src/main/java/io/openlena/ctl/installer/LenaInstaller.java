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

package io.openlena.ctl.installer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import io.openlena.ctl.common.vo.Server;
import io.openlena.ctl.type.InstallerCommandType;
import io.openlena.ctl.type.InstallerServerType;
import io.openlena.ctl.util.EnvUtil;
import io.openlena.ctl.util.FileUtil;
import io.openlena.ctl.util.InstallConfigUtil;
import io.openlena.ctl.util.InstallInfoUtil;
import io.openlena.ctl.util.ReleaseInfoUtil;
import io.openlena.ctl.util.StringUtil;

/**
 * Abstract class for server create installer
 * @author Erick Yu
 *
 */
public abstract class LenaInstaller implements Installer {
	private InstallerCommandType installerCommandType;
	private InstallerServerType installerServerType;

	private String depotPath;

	private Map<String, String> resultMap;
	private Map<String, String> defaultValueMap;

	/**
	 * @param installerCommandType command
	 * @param installerServerType serverType
	 */
	public LenaInstaller(InstallerCommandType installerCommandType, InstallerServerType installerServerType) {
		this.installerCommandType = installerCommandType;
		this.installerServerType = installerServerType;
	}

	/**
	 * @param serverId
	 * @param servicePort
	 * @param path install path
	 */
	protected void addInstallInfo(String serverId, String servicePort, String path) {
		addInstallInfo(serverId, servicePort, path, "");
	}

	/**
	 * @param serverId
	 * @param servicePort
	 * @param path install path
	 * @param hotfix version
	 */
	protected void addInstallInfo(String serverId, String servicePort, String path, String hotfix) {
		Server server = new Server();
		server.setId(serverId);
		server.setPort(servicePort);
		server.setPath(path);
		server.setType(getServerType());
		InstallInfoUtil.addInstallInfo(server);
	}
	
	/**
	 * @param serverId
	 * @param servicePort
	 * @param path install path
	 * @param version
	 * @param hotfix version
	 */
	protected void addInstallInfo(String serverId, String servicePort, String path, String version, String hotfix){
		Server server = new Server();
		server.setId(serverId);
		server.setPort(servicePort);
		server.setPath(path);
		server.setType(getServerType());
		server.setVersion(version);
		server.setHotfix(hotfix);
		InstallInfoUtil.addInstallInfo(server);
	}

	/**
	 * @return serverType
	 */
	protected String getServerType() {
		return installerServerType.getServerType();
	}
	
	/**
	 * Return install path.
	 * @param serverId Server ID
	 * @param servicePort Service Port
	 * @return Install Directory Name
	 */
	public String getTargetDirName(String serverId, String servicePort){
		//return serverId + "_" + servicePort;
		return serverId;
	}

	/**
	 * @return depot path
	 */
	public String getDepotPath() {
		return this.depotPath;
	}

	/**
	 * execute server creation logic
	 */
	public void execute(String[] args) {
		// TODO Auto-generated method stub
		load(args);
		execute();
	}

	/**
	 * @param args arguments
	 */
	private void load(String args[]) {
		this.depotPath = ReleaseInfoUtil.getDepotPath(getServerType());

		resultMap = new LinkedHashMap<String, String>();
		defaultValueMap = getDefaultValueMap();
		resultMap.put("LENA_HOME", EnvUtil.getLenaHome());
		resultMap.put("JAVA_HOME", EnvUtil.getUserJavahome());
	}

	/**
	 * @param key
	 * @return default value already defined
	 */
	protected String getDefaultValue(String key) {
		return defaultValueMap.get(key);
	}

	/**
	 * @return map that is having default value
	 */
	private Map<String, String> getDefaultValueMap() {
		Map<String, String> map = new HashMap<String, String>();

		// lena-was default value
		map.put("lena-was.service-port", InstallConfigUtil.getProperty("lena-was.service-port.default", "8080"));
		map.put("lena-was.run-user", InstallConfigUtil.getProperty("lena-was.run-user.default", "lena"));
		map.put("lena-was.ajp-address", InstallConfigUtil.getProperty("lena-was.ajp-address.default", "127.0.0.1"));
		map.put("lena-was.jvm-route", InstallConfigUtil.getProperty("lena-was.jvm-route.default", "host1_8180"));
		map.put("lena-was.template.dirname", InstallConfigUtil.getProperty("lena-was.template.dirname", "base"));

		// lena-web default value
		map.put("lena-web.service-port", InstallConfigUtil.getProperty("lena-web.service-port.default", "80"));
		map.put("lena-web.run-user", InstallConfigUtil.getProperty("lena-web.run-user.default", "lenaw"));
		map.put("lena-web.template.dirname", InstallConfigUtil.getProperty("lena-web.template.dirname", "base"));

		return map;
	}

	/**
	 * @param value
	 * @param defaultValue
	 * @return default value if value is null
	 */
	protected String getParameterValue(String value, String defaultValue) {
		return StringUtil.isBlank(value) ? defaultValue : value;
	}

	protected abstract void execute();

}
