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
 * General String utilities.
 *
 * @author Pinepond
 */
public class StringUtil {
	/**
	 * Check CharSequence is blank or not
	 *
	 * @param cs CharSequence
	 * @return true if the CharSequence is null or length is 0, otherwise false
	 */
	public static boolean isBlank(CharSequence cs) {
		if (cs == null) {
			return true;
		}

		int strLen = cs.length();
		if (strLen == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check CharSequence is numeric or not
	 * @param CharSequence
	 * @return true or false
	 */
	public static boolean isNumeric(CharSequence cs) {
		if (cs == null || cs.length() == 0) {
			return false;
		}
		int sz = cs.length();
		for (int i = 0; i < sz; i++) {
			if (!Character.isDigit(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check csArray is numeric or not
	 * @param CharSequence
	 * @return true or false
	 */
	public static boolean isNumeric(CharSequence ... csArray) {
		for(CharSequence cs : csArray){
			if(!isNumeric(cs)){
				return false;
			}
		}
		return true;
	}
}
