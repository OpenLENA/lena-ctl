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

import java.security.MessageDigest;

/**
 * Encryption utilities.
 *
 * @author Pinepond
 */
public class CipherUtil {

	/**
	 * Encrypt plainText String.
	 *
	 * @param plainText plaintext string to hash.
	 * @return encrypted String from the plainText.
	 */
	public static String md5(String plainText) {
		String md5Text = null;

		if (plainText != null) {
			try {
				byte[] byteArray = plainText.getBytes();
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				md5.update(byteArray);

				byte[] md5Bytes = md5.digest();
				StringBuffer buf = new StringBuffer();

				for (int i = 0; i < md5Bytes.length; i++) {
					if ((md5Bytes[i] & 0xff) < 0x10) {
						buf.append("0");
					}
					buf.append(Long.toString(md5Bytes[i] & 0xff, 16));
				}

				md5Text = buf.toString();
			}
			catch (Throwable t) {
				return plainText;
			}
		}

		return md5Text;
	}
}
