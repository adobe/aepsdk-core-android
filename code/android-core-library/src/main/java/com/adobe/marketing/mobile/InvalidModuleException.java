/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */
package com.adobe.marketing.mobile;

/**
 * Describes an exception that can occur when a module cannot be found or registered with an {@code EventHub}
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
class InvalidModuleException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor, generally used to create an instance of this Exception with simple reason
	 *
	 * @param    message    Message to associate with this instance of the {@code InvalidModuleException}
	 */
	InvalidModuleException(final String message) {
		super(message);
	}

	/**
	 * Optional constructor, used to create an instance of this Exception with a reason and an internal exception
	 *
	 * @param    message    Message to associate with this instance of the {@code InvalidModuleException}
	 * @param    baseException    exception to wrap
	 */
	InvalidModuleException(final String message, final Throwable baseException) {
		super(message, baseException);
	}
}
