/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 ******************************************************************************/

package com.adobe.marketing.mobile;

import java.util.List;

/**
 * Class to represent IdentityExtension network call json response.
 */
class IdentityResponseObject {
	/**
	 * Blob value as received in the visitor id service network response json.
	 * <p>
	 * Expected value type: {@link String}
	 */
	String blob;

	/**
	 * Marketing cloud id value as received in the visitor id service network response json.
	 * <p>
	 * Expected value type: {@link String}
	 */
	String mid;

	/**
	 * Location value as received in the visitor id service network response json.
	 * <p>
	 * Expected value type: {@link String}
	 */
	String hint;

	/**
	 * Error value as received in the visitor id service network response json.
	 * <p>
	 * Expected value type: {@link String}
	 */
	String error;

	/**
	 * ttl value as received in the visitor id service network response json.
	 * <p>
	 * Expected value type: {@link Long}
	 */
	long ttl;

	/**
	 * ArrayList of global opt out as received in the visitor id service network response json.
	 * <p>
	 * Expected value type: {@code List}
	 */
	List<String> optOutList;


	/**
	 * Constructor initializes blob, mid, hint, error, optOutList to null, and ttl to its defualt value of DEFAULT_TTL_VALUE.
	 */
	IdentityResponseObject() {
		blob = null;
		mid = null;
		hint = null;
		error = null;
		ttl = IdentityConstants.Defaults.DEFAULT_TTL_VALUE;
		optOutList = null;
	}


}
