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

package io.openlena.ctl.util;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import io.openlena.ctl.common.vo.Server;
import io.openlena.ctl.exception.LenaException;
import io.openlena.ctl.resolver.XpathVariable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Install info utilities.
 *
 * @author Pinepond
 */
public class InstallInfoUtil {

	/**
	 * Write Server installation information in install-info.xml file.
	 *
	 * @param server server object
	 */
	public static void addInstallInfo(Server server) {
		String argoInstallFilePath = getInstallInfoFilePath();

		Document document = XmlUtil.createDocument(argoInstallFilePath);
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			if (existsServer(server.getId())) {
				throw new LenaException("Server id alreay exists. '" + server.getId() + "'");
			}

			Element serversElement = (Element) xpath.evaluate("//install/servers", document, XPathConstants.NODE);

			Element serverElement = document.createElement("server");
			serverElement.appendChild(XmlUtil.createNode(document, "id", server.getId()));
			serverElement.appendChild(XmlUtil.createNode(document, "port", server.getPort()));
			serverElement.appendChild(XmlUtil.createNode(document, "type", server.getType()));
			serverElement.appendChild(XmlUtil.createNode(document, "path", server.getPath()));

			String timestamp = SystemUtil.getTimestamp();
			serverElement.appendChild(XmlUtil.createNode(document, "cdate", timestamp));
			serverElement.appendChild(XmlUtil.createNode(document, "udate", timestamp));

			serversElement.appendChild(serverElement);

			XmlUtil.writeXmlDocument(document, argoInstallFilePath);
		}
		catch (Throwable e) {
			throw new LenaException("An error occured when saving install-info.xml file", e);
		}
	}

	/**
	 * Returns install-info file path
	 *
	 * @return install -info file path
	 */
	public static String getInstallInfoFilePath() {
		return FileUtil.getConcatPath(EnvUtil.getLenaHome(), "etc", "info", "install-info.xml");
	}

	/**
	 * The Server exist or not
	 *
	 * @param serverId the server id
	 * @return ture if the server exists , otherwise false
	 */
	public static boolean existsServer(String serverId) {
		if (!StringUtil.isBlank(getServerInstallPath(serverId))) {
			return true;
		}

		return false;
	}

	/**
	 * Search installation path of the server
	 *
	 * @param serverId the server id
	 * @return server install path
	 */
	public static String getServerInstallPath(String serverId) {
		return XmlUtil.getValueByTagName(getServerElement(serverId), "path");
	}

	/**
	 * Search element of the server in install-info.xml
	 * 
	 * @param serverId the searver Id
	 * @return element object
	 */
	private static Element getServerElement(String serverId) {
		String argoInstallFilePath = getInstallInfoFilePath();

		Document document = XmlUtil.createDocument(argoInstallFilePath);
		XPath xpath = XPathFactory.newInstance().newXPath();
		Element element = null;
		try {
			element = (Element) XmlUtil.xpathEvaluate("//install/servers/server[id=$id]", document, XPathConstants.NODE, xpath, new XpathVariable("id", serverId));
		}
		catch (XPathExpressionException e) {
			throw new LenaException("Errors in release xml file", e);
		}

		return element;
	}
	
	/**
	 * Return service port of the server id
	 * @param serverId
	 * @return port
	 */
	public static String getServicePort(String serverId) {
		return XmlUtil.getValueByTagName(getServerElement(serverId), "port");
	}
	
	/**
	 * Return Server object of server id
	 * @param serverId
	 * @return
	 */
	public static Server getServer(String serverId){
		Element serverElement = getServerElement(serverId);
		if(serverElement == null){
			throw new LenaException("There is no installed server '" + serverId + "'");
		}
		return getServerByElement(serverElement);
	}
	
	/**
	 * Return Server object of server element
	 * @param serverElement
	 * @return
	 */
	private static Server getServerByElement(Element serverElement){
		Server server = new Server();
		server.setId(XmlUtil.getValueByTagName(serverElement, "id"));
		server.setPort(XmlUtil.getValueByTagName(serverElement, "port"));
		server.setType(XmlUtil.getValueByTagName(serverElement, "type"));
		server.setPath(XmlUtil.getValueByTagName(serverElement, "path"));
        server.setRecovery(XmlUtil.getValueByTagName(serverElement, "recovery"));
		server.setVersion(XmlUtil.getValueByTagName(serverElement, "version"));
		server.setCdate(XmlUtil.getValueByTagName(serverElement, "cdate"));
		server.setUdate(XmlUtil.getValueByTagName(serverElement, "udate"));
		server.setHotfix(XmlUtil.getValueByTagName(serverElement, "hotfix"));
		
		return server;
	}
}
