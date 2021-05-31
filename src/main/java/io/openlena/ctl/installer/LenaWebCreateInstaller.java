/*
* Copyright 2021 LENA Development Team.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package io.openlena.ctl.installer;

import java.util.HashMap;
import java.util.Scanner;

import io.openlena.ctl.exception.LenaException;
import io.openlena.ctl.type.InstallerCommandType;
import io.openlena.ctl.type.InstallerServerType;
import io.openlena.ctl.util.CipherUtil;
import io.openlena.ctl.util.EnvUtil;
import io.openlena.ctl.util.FileUtil;

/**
 * Installer that can create LENA Web.
 * @author Erick Yu
 *
 */
public class LenaWebCreateInstaller extends LenaInstaller {

	public LenaWebCreateInstaller(InstallerCommandType installerCommandType, InstallerServerType installerServerType) {
		super(installerCommandType, installerServerType);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Logic that actually creates the server
	 */
	public void execute() {
		HashMap<String, String> commandMap = getServerInfoFromUser();
		String serverId = commandMap.get("SERVER_ID");
		String servicePort = getParameterValue(commandMap.get("SERVICE_PORT"), getDefaultValue(getServerType() + ".service-port"));
		String runUser = getParameterValue(commandMap.get("RUN_USER"), EnvUtil.getRunuser());
		String apacheEnginePath = getParameterValue(commandMap.get("APACHE_ENGINE_PATH"), FileUtil.getConcatPath(EnvUtil.getLenaHome(), "modules", "lena-web-pe"));
		String installRootPath = getParameterValue(commandMap.get("INSTALL_ROOT_PATH"), FileUtil.getConcatPath(EnvUtil.getLenaHome(), "servers"));
		String targetPath = FileUtil.getConcatPath(installRootPath, serverId);
		String logHome = getParameterValue(commandMap.get("LOG_HOME"), FileUtil.getConcatPath(targetPath, "logs"));
		String documentRootPath = getParameterValue(commandMap.get("DOCUMENT_ROOT_PATH"), FileUtil.getConcatPath(targetPath, "htdocs"));

		try {
			FileUtil.copyDirectory(FileUtil.getConcatPath(getDepotPath(), "template", getDefaultValue(getServerType() + ".template.dirname")), targetPath);

			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "ENGN_HOME", apacheEnginePath);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "SERVER_ID", serverId);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "SERVICE_PORT", servicePort);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "RUN_USER", runUser);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "INSTALL_PATH", targetPath);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "DOC_ROOT", documentRootPath);

			if (!logHome.equals(FileUtil.getConcatPath(targetPath, "logs"))) {
				FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "LOG_HOME", logHome + "/${SERVER_ID}");
			}

			addInstallInfo(serverId, servicePort, targetPath);
		}
		catch (Throwable e) {
			throw new LenaException(e);
		}
	}

	/**
	 * @return Server information to be created
	 */
	public HashMap<String, String> getServerInfoFromUser() {
		HashMap<String, String> commandMap = new HashMap<String, String>();
		Scanner scan = new Scanner(System.in);
		System.out.println("+-------------------------------------------------------------------------------------");
		System.out.println("| 1. SERVER_ID means business code of system and its number of letter is from 3 to 5. ");
		System.out.println("|    ex : webd-lena_7180, webd-lena, lena01                                           ");
		System.out.print("|: ");
		commandMap.put("SERVER_ID", scan.nextLine());
		System.out.println("| 2. SERVICE_PORT is the port number used by HTTP Connector.                          ");
		System.out.println("|    default : 80                                                                     ");
		System.out.print("|: ");
		commandMap.put("SERVICE_PORT", scan.nextLine());
		System.out.println("| 3. RUN_USER is user running web-server                                              ");
		System.out.println("|    default : " + EnvUtil.getRunuser());
		System.out.print("|: ");
		commandMap.put("RUN_USER", scan.nextLine());
		System.out.println("| 4. APACHE_ENGINE_PATH is the path of Apache Server engine                           ");
		System.out.println("|    default : " + FileUtil.getConcatPath(EnvUtil.getLenaHome(), "modules", "lena-web-pe"));
		System.out.print("|: ");
		commandMap.put("APACHE_ENGINE_PATH", scan.nextLine());
		System.out.println("| 5. INSTALL_ROOT_PATH is Apache Server root directory in filesystem.                 ");
		System.out.println("|    default : " + FileUtil.getConcatPath(EnvUtil.getLenaHome(), "servers"));
		System.out.print("|: ");
		commandMap.put("INSTALL_ROOT_PATH", scan.nextLine());
		System.out.println("| 6. LOG_HOME is Apache Server's log directory in filesystem.                         ");
		System.out.println("|    default : " + FileUtil.getConcatPath(EnvUtil.getLenaHome(), "servers", commandMap.get("SERVER_ID"), "logs"));
		System.out.print("|: ");
		commandMap.put("LOG_HOME", scan.nextLine());
		System.out.println("| 7. DOCUMENT_ROOT_PATH is Apache Server's contents directory in filesystem.          ");
		System.out.println("|    default : " + FileUtil.getConcatPath(EnvUtil.getLenaHome(), "servers", commandMap.get("SERVER_ID"), "htdocs"));
		System.out.print("|: ");
		commandMap.put("DOCUMENT_ROOT_PATH", scan.nextLine());
		System.out.println("+-------------------------------------------------------------------------------------");
		return commandMap;
	}

}
