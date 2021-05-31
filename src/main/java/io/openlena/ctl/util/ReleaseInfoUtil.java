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
import io.openlena.ctl.exception.LenaException;
import io.openlena.ctl.resolver.XpathVariable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Release info utilities.
 *
 * @author Pinepond
 */
public class ReleaseInfoUtil {

	/**
	 * Search the depot path corresponding to the server type
	 *
	 * @param serverType the server type
	 * @return depot path
	 */
	public static String getDepotPath(String serverType) {
		return FileUtil.getConcatPath(EnvUtil.getLenaHome(), "depot", serverType, getModuleVersion(serverType));
	}

	/**
	 * Search Module version of the server type
	 *
	 * @param serverType the server type
	 * @return module version
	 */
	public static String getModuleVersion(String serverType) {
		String version = XmlUtil.getValueByTagName(getModuleElement(serverType), "version");

		if (version == null || "".equals(version.trim())) {
			throw new LenaException(serverType + " is incorrect");
		}

		return version;
	}

	/**
	 * Returns the module element in the release-info.xml file.
	 *
	 * @param serverType Server Type
	 * @return Element Object
	 */
	private static Element getModuleElement(String serverType) {
		Document document = XmlUtil.createDocument(getReleaseInfoFilePath());
		XPath xpath = XPathFactory.newInstance().newXPath();
		Element element = null;
		try {
			element = (Element) XmlUtil.xpathEvaluate("//release/depot/modules/module[id=$id]", document, XPathConstants.NODE, xpath, new XpathVariable("id", serverType));
		}
		catch (XPathExpressionException e) {
			throw new LenaException("An error occured when reading release-info.xml file", e);
		}

		return element;
	}

	/**
	 * Search the path of the release-info.xml file.
	 *
	 * @param lenaHome the lena home
	 * @return release info file path
	 */
	public static String getReleaseInfoFilePath(String lenaHome) {
		String argoReleaseFilePath = FileUtil.getConcatPath(lenaHome, "etc", "info", "release-info.xml");
		if (!FileUtil.exists(argoReleaseFilePath)) {
			throw new LenaException("There is no release-info.xml file");
		}
		return argoReleaseFilePath;
	}

	/**
	 * Search the path of the release-info.xml file.
	 *
	 * @return release info file path
	 */
	public static String getReleaseInfoFilePath() {
		return getReleaseInfoFilePath(EnvUtil.getLenaHome());
	}
}
