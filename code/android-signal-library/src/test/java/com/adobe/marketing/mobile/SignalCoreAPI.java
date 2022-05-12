///****************************************************************************
// *
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2018 Adobe Systems Incorporated
// * All Rights Reserved.
// *
// * NOTICE:  All information contained herein is, and remains
// * the property of Adobe Systems Incorporated and its suppliers,
// * if any.  The intellectual and technical concepts contained
// * herein are proprietary to Adobe Systems Incorporated and its
// * suppliers and are protected by trade secret or copyright law.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe Systems Incorporated.
// *
// ***************************************************************************/
//
//package com.adobe.marketing.mobile;
//import java.util.Map;
//
//class SignalCoreAPI {
//	private MockEventHubModuleTest eventHub;
//	SignalCoreAPI(final MockEventHubModuleTest eventHub) {
//		this.eventHub = eventHub;
//	}
//
//	void collectPii(final Map<String, String> data) {
//		if (data == null || data.isEmpty()) {
//			return;
//		}
//
//		final EventData eventData = new EventData()
//		.putStringMap(SignalConstants.EventDataKeys.Signal.SIGNAL_CONTEXT_DATA, data);
//		eventHub.dispatch(new Event.Builder("CollectPII", EventType.SIGNAL,
//											EventSource.REQUEST_CONTENT).setData(eventData).build());
//
//	}
//}
