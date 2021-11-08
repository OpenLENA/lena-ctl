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

import java.util.List;

import io.openlena.ctl.installer.Installer;
import io.openlena.ctl.type.InstallerCommandType;
import io.openlena.ctl.type.InstallerServerType;

/**
 * Mapper class for mapping Web Installer or WAS Insatller.
 * @author Erick Yu
 *
 */
public class InstallerMapper {

	/**
	 * @param commandList
	 * @return Server create Installer
	 */
	public static Installer getInstaller(List<String> commandList) {
		String command = commandList.get(0);
		String serverType = commandList.get(1);

		InstallerServerType installerServerType = null;
		InstallerCommandType installerCommandType = null;

		installerServerType = InstallerServerType.getInstallServerType(serverType);
		installerCommandType = InstallerCommandType.valueOf(command.toUpperCase());

		switch (installerServerType) {
		case LENA_WEB:
			switch (installerCommandType) {
				case CREATE:
					return new LenaWebCreateInstaller(installerCommandType, installerServerType);
				case CLONE:
					return new LenaWebServerCloneInstaller(installerCommandType, installerServerType);
			}
		case LENA_WAS:
			switch (installerCommandType) {
				case CREATE:
					return new LenaWasCreateInstaller(installerCommandType, installerServerType);
				case CLONE:
					return new LenaWasServerCloneInstaller(installerCommandType, installerServerType);
			}
		}
		return null;
	}
}
