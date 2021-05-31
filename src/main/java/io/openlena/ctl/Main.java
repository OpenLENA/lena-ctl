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

package io.openlena.ctl;

import java.util.ArrayList;
import java.util.List;
import io.openlena.ctl.common.CommandCtl;
import io.openlena.ctl.installer.Installer;
import io.openlena.ctl.installer.InstallerMapper;

/**
 * Main of LENA CTL
 * @author Erick Yu
 *
 */
public class Main {

	public static void main(String[] args) {

		List<String> commandList = new ArrayList<String>();

		for (String tmp : args) {
			commandList.add(tmp);
		}

		if (commandList.size() < 2) {
			System.exit(1);
		}

		String command = commandList.get(0); 

		CommandCtl commandCtl = new CommandCtl();
		String commandMapper = commandCtl.commandChecker(command);

		if (commandMapper.equals(CommandCtl.INSTALLER)) {
			Installer installer = InstallerMapper.getInstaller(commandList); 
			installer.execute(args);
		}
	}
}
