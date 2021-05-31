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

/**
 * Read jvm properties and load them into the EnvUtil object.
 *
 * @author Pinepond
 */
public class EnvUtil {

	private static final String javaHome;
	private static final String userJavaHome;
	private static final String lenaHome;
	private static final String hostname;
	private static final String runUser;
	private static final String userHome;
	private static final String logHome;
	private static final String resultFormat;
	private static final boolean rootUserAllowed;

	static {
		javaHome = System.getProperty("java.home");
		userJavaHome = System.getProperty("user_java.home", javaHome);
		lenaHome = System.getProperty("lena.home");
		hostname = System.getProperty("hostname");
		runUser = System.getProperty("run_user");
		userHome = System.getProperty("user.home");
		logHome = System.getProperty("log.home", FileUtil.getConcatPath(lenaHome, "logs", "lena-installer"));
		resultFormat = System.getProperty("result.format", "text");
		rootUserAllowed = (System.getProperty("root_user.allowed", "false").equals("true"));
	}

	/**
	 * Returns JAVA HOME Path of current jvm process. (JRE Path)
	 *
	 * @return javahome
	 */
	public static String getJavahome() {
		return javaHome;
	}

	/**
	 * Returns JAVA HOME Paht of User input through console.
	 *
	 * @return userJavahome
	 */
	public static String getUserJavahome() {
		return userJavaHome;
	}

	/**
	 * Returns path of lena installed directory.
	 *
	 * @return lenaHome
	 */
	public static String getLenaHome() {
		return lenaHome;
	}

	/**
	 * Returns The Hostname of machine running this process.
	 *
	 * @return hostname
	 */
	public static String getHostname() {
		return hostname;
	}

	/**
	 * Returns User who performed this process
	 *
	 * @return runuser
	 */
	public static String getRunuser() {
		return runUser;
	}

	/**
	 * Returns the path of User Home
	 *
	 * @return userHome
	 */
	public static String getUserhome() {
		return userHome;
	}

	/**
	 * Retruns the path of Log
	 *
	 * @return logHome
	 */
	public static String getLogHome() {
		return logHome;
	}

	/**
	 * Returns the result format - text or json
	 *
	 * @return resultFormat
	 */
	public static String getResultFormat() {
		return resultFormat;
	}

	/**
	 * check allow root user or not
	 *
	 * @return <code>true</code> if allow root user, otherwise
	 * <code>false</code>
	 */
	public static boolean isRootUserAllowed() {
		return rootUserAllowed;
	}

	/**
	 * Returns value of system property key
	 *
	 * @param key system property name
	 * @return value of system property key
	 */
	public static String getSystemProperty(String key) {
		return System.getProperty(key);
	}

	/**
	 * Returns value of system property key,
	 * if can not find key , return default value
	 *
	 * @param key system property name
	 * @param def default value
	 * @return value of system property key
	 */
	public static String getSystemProperty(String key, String def) {
		return System.getProperty(key, def);
	}
}
