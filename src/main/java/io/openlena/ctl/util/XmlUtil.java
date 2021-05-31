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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import io.openlena.ctl.exception.LenaException;
import io.openlena.ctl.resolver.LenaXPathVariableResolver;
import io.openlena.ctl.resolver.XpathVariable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Xml utililies.
 *
 * @author Pinepond
 */
public class XmlUtil {
	/**
	 * Returns the Document object corresponding to xmlPath.
	 *
	 * @param xmlPath XML Path
	 * @return Document Object
	 */
	public static Document createDocument(String xmlPath) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		DocumentBuilder builder = null;
		Document document = null;
		try {
			builder = factory.newDocumentBuilder();
			document = builder.parse(xmlPath);
		}
		catch (Throwable e) {
			throw new LenaException("An error occured when creating xml document.", e);
		}
		return document;
	}

	/**
	 * Calls xpath's evaluate and returns the result.
	 *
	 * @param expression xpath expression
	 * @param item document object
	 * @param returnType return type (xpath)
	 * @param xpath XPath object
	 * @return Object returned by xpath call
	 * @throws XPathExpressionException the x path expression exception
	 */
	public static Object xpathEvaluate(String expression, Object item, QName returnType, XPath xpath) throws XPathExpressionException {
		return xpathEvaluate(expression, item, returnType, xpath, (XpathVariable) null);
	}

	/**
	 * Calls xpath's evaluate and returns the result.
	 *
	 * @param expression xpath expression
	 * @param item document object
	 * @param returnType return type (xpath)
	 * @param xpath XPath object
	 * @param variable Xpath Variable
	 * @return Object returned by xpath call
	 * @throws XPathExpressionException the x path expression exception
	 */
	public static Object xpathEvaluate(String expression, Object item, QName returnType, XPath xpath, XpathVariable variable) throws XPathExpressionException {
		return xpathEvaluate(expression, item, returnType, xpath, new XpathVariable[] { variable });
	}

	/**
	 * Calls xpath's evaluate and returns the result.
	 *
	 * @param expression xpath expression
	 * @param item document object
	 * @param returnType return type (xpath)
	 * @param xpath XPath object
	 * @param variables Xpath Variable
	 * @return Object returned by xpath call
	 * @throws XPathExpressionException the x path expression exception
	 */
	public static Object xpathEvaluate(String expression, Object item, QName returnType, XPath xpath, XpathVariable variables[]) throws XPathExpressionException {
		LenaXPathVariableResolver variableResolver = new LenaXPathVariableResolver();
		for (XpathVariable variable : variables) {
			if (variable == null)
				continue;
			variableResolver.addVariable(new QName(variable.getKey()), variable.getValue());
		}
		xpath.setXPathVariableResolver(variableResolver);
		XPathExpression xPathExpression = xpath.compile(expression);
		return xPathExpression.evaluate(item, returnType);
	}

	/**
	 * write document to xml
	 *
	 * @param document
	 * @param xmlPath xml file path
	 * @throws Exception
	 */
	public static void writeXmlDocument(Document document, String xmlPath) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(xmlPath);
			// 수정된 설정 정보를 실제 파일에 반영
			transformer.transform(source, result);
		}
		catch (Throwable e) {
			throw new LenaException("An error occured when saving file '" + xmlPath + "'", e);
		}
	}

	/**
	 * Create Node
	 *
	 * @param document target document
	 * @param nodeName new node name
	 * @param textContent new node data
	 * @return element element
	 */
	public static Element createNode(Document document, String nodeName, String textContent) {
		return createNode(document, nodeName, textContent, false);
	}

	/**
	 * Create Node
	 *
	 * @param document target document
	 * @param nodeName new node name
	 * @param textContent new node data
	 * @param isCData cdata or not
	 * @return element element
	 */
	public static Element createNode(Document document, String nodeName, String textContent, boolean isCData) {
		Element element = document.createElement(nodeName);
		String retTxtContent = textContent;

		if (retTxtContent == null) {
			retTxtContent = "";
		}

		if (isCData) {
			element.appendChild(document.createCDATASection(retTxtContent));
		}
		else {
			element.setTextContent(retTxtContent);
		}

		return element;
	}

	/**
	 * Search value of the tag in element
	 *
	 * @param element element
	 * @param tagName target tag
	 * @return value of the tag name
	 */
	public static String getValueByTagName(Element element, String tagName) {
		Node node = getElementByTagName(element, tagName);
		if (node == null) {
			return "";
		}

		return node.getTextContent().trim();
	}

	/**
	 * Search node of tag in element
	 *
	 * @param element element
	 * @param tagName target tag
	 * @return Node of the tag
	 */
	public static Element getElementByTagName(Element element, String tagName) {
		if (element == null) {
			return null;
		}

		NodeList nodeList = element.getElementsByTagName(tagName);
		if (nodeList == null || nodeList.getLength() == 0) {
			return null;
		}

		return (Element) nodeList.item(0);
	}
}
