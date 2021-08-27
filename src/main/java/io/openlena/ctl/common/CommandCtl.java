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

package io.openlena.ctl.common;

/**
 * Object that can check which command user input.
 * @author Erick Yu
 * 
 */
public class CommandCtl {
	public static final String INSTALLER = "INSTALLER";
	public static final String CONFIGURATOR = "CONFIGURATOR";
	public static final String EXECUTOR = "EXECUTOR";

	InstallCommandCtl installCommandCtl = null;

	public CommandCtl() {
		installCommandCtl = new InstallCommandCtl();
	}

	/**
	 * Check which command inputed.
	 * @param command
	 * @return Appropriate CommandCtl by user command
	 */
	public String commandChecker(String command) {
		String result = "";
		if (installCommandCtl.containsCommand(command)) {
			result = INSTALLER;
		}
		return result;
	}
}
