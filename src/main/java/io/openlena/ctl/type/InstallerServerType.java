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

package io.openlena.ctl.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Erick Yu
 *
 */
public enum InstallerServerType {
	LENA_WEB("lena-web"), LENA_WAS("lena-was");

	private static final Logger LOGGER = LoggerFactory.getLogger(InstallerServerType.class);

	private String serverType;

	InstallerServerType(String serverType) {
		this.serverType = serverType;
	}

	/**
	 * @return serverType
	 */
	public String getServerType() {
		return serverType;
	}

	/**
	 * @param serverType
	 * @return appropriate server type
	 */
	public static InstallerServerType getInstallServerType(String serverType) {
		InstallerServerType type = null;
		try {
			type = InstallerServerType.valueOf(serverType.replace("-", "_").toUpperCase());
		}
		catch (Exception e) {
			LOGGER.debug("Fail in getting install server type");
		}
		return type;
	}
}
