///* *****************************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2018 Adobe
// * All Rights Reserved.
// *
// * NOTICE: All information contained herein is, and remains
// * the property of Adobe and its suppliers, if any. The intellectual
// * and technical concepts contained herein are proprietary to Adobe
// * and its suppliers and are protected by all applicable intellectual
// * property laws, including trade secret and copyright laws.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe.
// ******************************************************************************/
//
//package com.adobe.marketing.mobile.identity;
//
//import com.adobe.marketing.mobile.Event;
//import com.adobe.marketing.mobile.EventSource;
//import com.adobe.marketing.mobile.EventType;
//import com.adobe.marketing.mobile.Log;
//
//class DispatcherConfigurationRequestContentIdentity extends ModuleEventDispatcher<IdentityExtension> {
//
//	/**
//	 * Constructor
//	 *
//	 * @param hub {@link EventHub} instance used by this dispatcher.
//	 * @param extension parent {@link IdentityExtension} that owns this dispatcher.
//	 */
//	DispatcherConfigurationRequestContentIdentity(final EventHub hub, final IdentityExtension extension) {
//		super(hub, extension);
//	}
//
//	/**
//	 * Dispatches IdentityExtension event for Configuration Update.
//	 *
//	 * @param event_data {@link EventData} event data for Configuration extension to update configuration.
//	 */
//	void dispatchConfigUpdateRequest(final EventData event_data) {
//
//		final Event event = new Event.Builder("Configuration Update From IdentityExtension",
//											  EventType.CONFIGURATION, EventSource.REQUEST_CONTENT).setData(event_data).build();
//
//		dispatch(event);
//		Log.trace(IdentityExtension.LOG_SOURCE,
//				  "dispatchConfigUpdateRequest : Configuration Update event has been added to event hub : %s", event);
//
//	}
//}
