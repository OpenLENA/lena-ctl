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

package io.openlena.ctl.resolver;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

/**
 * Resolver to handle xpath variable
 * 
 */
public class LenaXPathVariableResolver implements XPathVariableResolver {

	private Map<QName, Object> variableMap;

	public LenaXPathVariableResolver() {
		variableMap = new HashMap<QName, Object>();
	}

	/**
	 * External methods to add parameter
	 * 
	 * @param name Parameter name
	 * @param value Parameter value
	 */
	public void addVariable(QName name, Object value) {
		variableMap.put(name, value);
	}

	public Object resolveVariable(QName variableName) {
		return variableMap.get(variableName);
	}

}
