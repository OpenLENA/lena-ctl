/*
 * Copyright 2022 LA:T Development Team.
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

package io.lat.ctl.exception;

/**
 * Exception class defined by LENA Development Team.
 * @author Erick Yu
 *
 */
public class LatException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LatException() {
		super();
	}
	
	public LatException(String message) {
		super(message);
	}

	public LatException(String message, Throwable cause) {
		super(message, cause);
	}

	public LatException(Throwable cause) {
		super(cause);
	}
}
