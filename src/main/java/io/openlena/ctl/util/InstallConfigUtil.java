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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Read installer.info and load them into the InstallConfigUtil object.
 *
 * @author Pinepond
 */
public class InstallConfigUtil {
	private static Properties properties = null;

	/**
	 * load installer.info
	 */
	private static void load() {
		properties = new Properties();
		FileInputStream fis = null;
		String filePath = FileUtil.getConcatPath(EnvUtil.getLenaHome(), "etc", "info", "installer.info");
		try {
			fis = new FileInputStream(filePath);
			properties.load(fis);
		}
		catch (IOException e) {
		}
		finally {
			FileUtil.close(fis);
		}
	}

	/**
	 * Search value of the property key
	 *
	 * @param key property key
	 * @return value of the property key
	 */
	public static String getProperty(String key) {
		return getProperty(key, null);
	}

	/**
	 * returns value of the property key
	 * if key doesn't exit, returns defualt value
	 *
	 * @param key property key
	 * @param def defualt value
	 * @return
	 */
	public static String getProperty(String key, String def) {
		if (properties == null) {
			load();
		}

		return properties.getProperty(key, def);
	}
}
