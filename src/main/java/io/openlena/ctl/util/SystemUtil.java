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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.openlena.ctl.exception.LenaException;

/**
 * General System utilities.
 *
 * @author Pinepond
 */
public class SystemUtil {
	/**
	 * Returns current timestamp
	 *
	 * @return current timestamp
	 */
	public static String getTimestamp() {
		return getDate(new SimpleDateFormat("yyyyMMddHHmmssSSS"));
	}

	/**
	 * Returns the current date according to the format.
	 *
	 * @param format Date format
	 * @return current date
	 */
	public static String getDate(String format) {
		return getDate(new SimpleDateFormat(format));
	}

	/**
	 * Returns the current date according to the format.
	 * 
	 * @param df Date format
	 * @return current date
	 */
	private static String getDate(DateFormat df) {
		String dateStr = df.format(new Date());

		return dateStr;
	}
	
	/**
	 * default jvmRoute명을 생성하여 반환한다.	
	 * @param hostname
	 * @param servicePort
	 * @return
	 */
	public static String getDefaultJvmRoute(String hostname, String servicePort){
		StringBuilder jvmRoute = new StringBuilder();
		
		// generate value for hostname
		String md5Hostname = CipherUtil.md5(hostname);
		int md5CutLength = 12;
		if(md5Hostname.length() > md5CutLength){
			md5Hostname = md5Hostname.substring(0, md5CutLength);
		}
		jvmRoute.append(md5Hostname);

		// generate value for port
		if(!StringUtil.isNumeric(servicePort)){
			throw new LenaException("Service Port should be numeric.");
		}
		jvmRoute.append(new StringBuilder().append(Integer.valueOf(servicePort) * 2).reverse());
		
		return jvmRoute.toString();
	}
	
	/**
	 * default jvmRoute명을 생성하여 반환한다.	
	 * @param servicePort
	 * @return
	 */
	public static String getDefaultJvmRoute(String servicePort){
		return getDefaultJvmRoute(EnvUtil.getHostname(), servicePort);
	}
}
