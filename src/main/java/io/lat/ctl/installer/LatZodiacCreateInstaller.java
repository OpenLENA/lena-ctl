/*
 * Copyright 2022 LA:T Development Team.
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

package io.lat.ctl.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.lat.ctl.exception.LatException;
import io.lat.ctl.type.InstallerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EnvUtil;
import io.lat.ctl.util.FileUtil;
import io.lat.ctl.util.PropertyUtil;
import io.lat.ctl.util.StringUtil;
import io.lat.ctl.util.XmlUtil;

/**
 * Installer that can create LA:T Zodiac
 * 
 * @author ksseo
 *
 */
public class LatZodiacCreateInstaller extends LatInstaller {

	private static final Logger LOGGER = LoggerFactory.getLogger(LatZodiacCreateInstaller.class);

	public LatZodiacCreateInstaller(InstallerCommandType installerCommandType,
			InstallerServerType installerServerType) {
		super(installerCommandType, installerServerType);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Logic that actually creates the server
	 */
	public void execute() throws IOException {
		HashMap<String, String> commandMap = getServerInfoFromUser();

		String serverId = commandMap.get("SERVER_ID");
		String servicePort = getParameterValue(commandMap.get("SERVICE_PORT"),
				getDefaultValue(getServerType() + ".service-port"));
		String secondaryServerIp = commandMap.get("SECONDARY_SERVER_IP");
		String secondaryServicePort = getParameterValue(commandMap.get("SECONDARY_SERVICE_PORT"),
				getDefaultValue(getServerType() + ".secondary-service-port"));
		String runUser = getParameterValue(commandMap.get("RUN_USER"), EnvUtil.getRunuser());
		String installRootPath = FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", getServerType());
		String targetPath = FileUtil.getConcatPath(installRootPath, getTargetDirName(serverId, servicePort));
		String logHome = getParameterValue(commandMap.get("LOG_HOME"), FileUtil.getConcatPath(targetPath, "logs"));

		// validate options
		if (!StringUtil.isNumeric(servicePort)) {
			throw new LatException("Service Port should be numeric.");
		}
		if (!StringUtil.isNumeric(secondaryServicePort)) {
			throw new LatException("Service Port should be numeric.");
		}

		// installPath check
		if (FileUtil.exists(targetPath)) {
			throw new LatException(targetPath + " already exists.");
		}

		// run user check
		if ("root".equals(runUser) && !EnvUtil.isRootUserAllowed()) {
			throw new LatException(getServerType() + " can't run as root user.");
		}

		FileUtil.copyDirectory(getDepotPath(), targetPath);
		
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "JAVA_HOME", EnvUtil.getUserJavahome());
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "LAT_HOME", EnvUtil.getLatHome());
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "SERVER_ID", serverId);
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "ENGN_VERSION", getEngineVersion());
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "ZODIAC_HOME", targetPath);
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "RUN_USER", runUser);
		if (!logHome.equals(FileUtil.getConcatPath(targetPath, "logs"))) {
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "LOG_HOME", logHome + "/${SERVER_ID}");
		}
		
		PropertyUtil.setProperty(FileUtil.getConcatPath(targetPath, "session.conf"), "server.name", serverId);
		PropertyUtil.setProperty(FileUtil.getConcatPath(targetPath, "session.conf"), "primary.port", servicePort);
		PropertyUtil.setProperty(FileUtil.getConcatPath(targetPath, "session.conf"), "secondary.host",
				secondaryServerIp);
		PropertyUtil.setProperty(FileUtil.getConcatPath(targetPath, "session.conf"), "secondary.port",
				secondaryServicePort);

		// update install-info.xml
		addInstallInfo(serverId, servicePort, targetPath);

	}

	/**
	 * @param targetPath
	 */
	public void setSampleApplicationDocBase(String targetPath) {
		String rootXmlPath = FileUtil.getConcatPath(targetPath, "conf", "Catalina", "localhost", "ROOT.xml");

		if (FileUtil.exists(rootXmlPath)) {
			Document document = XmlUtil.createDocument(rootXmlPath);
			XPath xpath = XPathFactory.newInstance().newXPath();
			try {
				Element element = (Element) XmlUtil.xpathEvaluate("//Context", document, XPathConstants.NODE, xpath);

				String docBase = element.getAttribute("docBase");

				String defaultDocBase = FileUtil.getConcatPath(EnvUtil.getLatHome(), "lat", "depot", "lat-application",
						"ROOT");
				if (!defaultDocBase.equals(docBase)) {
					element.setAttribute("docBase", defaultDocBase);
					XmlUtil.writeXmlDocument(document, rootXmlPath);
				}
			} catch (XPathExpressionException e) {
				LOGGER.debug("fail in setting sample application docbase");
			}
		}
	}

	/**
	 * @return Server information to be created
	 */
	public HashMap<String, String> getServerInfoFromUser() {
		HashMap<String, String> commandMap = new HashMap<String, String>();
		Scanner scan = new Scanner(System.in);

		System.out.println("+-------------------------------------------------------------------------------------");
		System.out.println("| 1. SERVER_ID means business code of system and its maximum number of letters is 20. ");
		System.out.println("|    ex :  session-5105                                                               ");
		System.out.print("|: ");
		commandMap.put("SERVER_ID", scan.nextLine());
		System.out.println("| 2. SERVICE_PORT is the port number used by Session Server.                          ");
		System.out.println("|    ex : 5105                                                                        ");
		System.out.print("|: ");
		commandMap.put("SERVICE_PORT", scan.nextLine());
		System.out.println("| 3. SECONDARY_SERVER_IP is the ip number communicate with Secondary Session Server   ");
		System.out.println("|    ex : 127.0.0.1                                                                   ");
		System.out.print("|: ");
		commandMap.put("SECONDARY_SERVER_IP", scan.nextLine());
		System.out.println("| 4. SECONDARY_SERVICE_PORT is the port number used by Secondary Session Server.      ");
		System.out.println("|    ex : 5106                                                                        ");
		System.out.print("|: ");
		commandMap.put("SECONDARY_SERVICE_PORT", scan.nextLine());
		System.out.println("| 5. RUN_USER is user running Session Server                                          ");
		System.out.println("|    ex : lena, wasadm                                                                ");
		System.out.print("|: ");
		commandMap.put("RUN_USERT", scan.nextLine());
		System.out.println("| 6. INSTALL_ROOT_PATH is is server root directory in filesystem.                     ");
		System.out.println(
				"|    default : " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", getServerType()));
		System.out.print("|: ");
		commandMap.put("INSTALL_ROOT_PATH", scan.nextLine());
		System.out.println("| 7. LOG_HOME is LENA Session Server's log directory in filesystem.                   ");
		System.out.println("|    If you don't want to use default log directory input your custom log home prefix.");
		System.out.println("|    default : " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances",
				getServerType(), commandMap.get("SERVER_ID"), "logs"));
		System.out.print("|: ");
		commandMap.put("LOG_HOME", scan.nextLine());
		System.out.println("+-------------------------------------------------------------------------------------");

		return commandMap;
	}

	public static String getEngineVersion() throws IOException {
		String[] cmd;
		if (System.getProperty("os.name").indexOf("Windows") > -1) {
			cmd = new String[] { "cmd", "/c",
					"ls -1r --sort=version " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "engines", "zodiac") };
		} else {
			cmd = new String[] { "/bin/sh", "-c",
					"ls -1r --sort=version " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "engines", "zodiac") };
		}

		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String s = br.readLine();

		if (s == null) {
			throw new LatException("Tomcat engine is not installed");
		} else {
			return s;
		}
	}
}
