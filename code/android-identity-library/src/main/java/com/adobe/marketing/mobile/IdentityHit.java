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

/**
 * Extends {@link AbstractHit} and represents a record in the database that holds IdentityExtension requests
 */
final class IdentityHit extends AbstractHit {
	/**
	 * {@link String} containing optional pair ID of a potential one-time listener
	 */
	String pairId;

	/**
	 * {@code int} containing the corresponding {@link Event#eventNumber} represented by this instance
	 */
	int eventNumber;

	/**
	 * {@code boolean} whether the identity request should use HTTPS or HTTP
	 */
	boolean configSSL;

	/**
	 * {@link String} containing the URL to be sent to the ECID Service
	 */
	String url;

	/**
	 * Constructor initializes {@link #pairId} and {@link #url} to null, {@link #eventNumber} to -1,
	 * and {@link #configSSL} to true
	 */
	IdentityHit() {
		pairId = null;
		url = null;
		eventNumber = -1;
		configSSL = true;
	}
}
