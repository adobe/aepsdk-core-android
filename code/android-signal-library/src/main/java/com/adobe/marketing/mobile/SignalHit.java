/* ***********************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/


package com.adobe.marketing.mobile;

/*
    Define the fields for signal request
 */
class SignalHit extends AbstractHit {
	String url;
	String body;
	String contentType;
	int timeout;

	/**
	 *  Determine the Http command based off the request body
	 *
	 * @return HttpCommand.POST if the body has content, otherwise HttpCommand.GET
	 */
	NetworkService.HttpCommand getHttpCommand() {
		return StringUtils.isNullOrEmpty(body) ? NetworkService.HttpCommand.GET : NetworkService.HttpCommand.POST;
	}
}
