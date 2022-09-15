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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IdentityCore {

	private static final String LOG_TAG = "IdentityCore";
	private static final String REQUEST_IDENTITY_EVENT_NAME = "IdentityRequestIdentity";
	private static final int PUBLIC_API_TIME_OUT_MILLISECOND = 500; //ms
	private EventHub eventHub;

	/**
	 * Registers the {@code IdentityExtension} to the given {@code EventHub} instance.
	 * @param eventHub the {@link EventHub} to register an {@link IdentityExtension}
	 *
	 * @see IdentityExtension
	 */
	IdentityCore(final EventHub eventHub, final ModuleDetails moduleDetails) {
		if (eventHub == null) {
			Log.error(LOG_TAG,
					  "IdentityCore : Unable to initialize the Identity Core because there is no EventHub instance found.");
			return;
		}

		this.eventHub = eventHub;

		try {
			eventHub.registerModule(IdentityExtension.class, moduleDetails);
		} catch (InvalidModuleException e) {
			Log.error(LOG_TAG, "IdentityCore : Unable to register Identity Core due to: (%s). ", e);
		}

		Log.trace(LOG_TAG, "IdentityCore : Identity Core has been initialized and registered successfully.");
	}

	/**
	 * Initiates an Identity Request event for fetching experience cloud id
	 *
	 * @param callback {@link AdobeCallback}, the sdk would call with  experience cloud id {@code String} value as the parameter.
	 */
	void getExperienceCloudId(final AdobeCallback<String> callback) {
		createIdentityRequestWithOneTimeCallbackWithCallbackParam(IdentityConstants.EventDataKeys.Identity.VISITOR_ID_MID, null,
				callback, new StringVariantSerializer());
	}

	/**
	 * Initiates an Identity Request event for fetching IDs
	 *
	 * @param callback {@link AdobeCallback}, the sdk would call with the list of visitor id objects as the parameter.
	 */
	void getIdentifiers(final AdobeCallback<List<VisitorID>> callback) {
		createIdentityRequestWithOneTimeCallbackWithCallbackParam(IdentityConstants.EventDataKeys.Identity.VISITOR_IDS_LIST,
				null,
				callback, VisitorID.LIST_VARIANT_SERIALIZER);
	}

	/**
	 * Initiates an Identity Request event for appending visitor ids to a base url.
	 *
	 * @param baseURL  URL to which the visitor info needs to be appended to.
	 * @param callback {@link AdobeCallback}, the sdk would call with the updated url string value as the parameter.
	 */
	void appendToURL(final String baseURL, final AdobeCallback<String> callback) {
		EventData eventData = new EventData();
		eventData.putString(IdentityConstants.EventDataKeys.Identity.BASE_URL, baseURL);
		createIdentityRequestWithOneTimeCallbackWithCallbackParam(IdentityConstants.EventDataKeys.Identity.UPDATED_URL,
				eventData,
				callback, new StringVariantSerializer());
	}

	/**
	 * Initiates an Identity Request event for retrieving the visitor ids as a url parameter string.
	 *
	 * @param callback {@link AdobeCallback}, the sdk would call with the url string value as the parameter.
	 */
	void getUrlVariables(final AdobeCallback<String> callback) {
		EventData eventData = new EventData();
		eventData.putBoolean(IdentityConstants.EventDataKeys.Identity.URL_VARIABLES, true);
		createIdentityRequestWithOneTimeCallbackWithCallbackParam(IdentityConstants.EventDataKeys.Identity.URL_VARIABLES,
				eventData,
				callback, new StringVariantSerializer());
	}

	/**
	 * Initiates an Identity Request event for syncing identifierType, identifier with an authentication state.
	 *
	 * @param identifierType      identifier type value.
	 * @param identifier          id value
	 * @param authenticationState a valid AuthenticationState value.
	 */
	void syncIdentifier(final String identifierType, final String identifier,
						final VisitorID.AuthenticationState authenticationState) {

		if (StringUtils.isNullOrEmpty(identifierType)) {
			return;
		}

		HashMap<String, String> identifiers = new HashMap<String, String>();
		identifiers.put(identifierType, identifier);
		dispatchIDSyncEvent(identifiers, authenticationState);
	}

	/**
	 * Initiates an Identity Request event for syncing a collection of identifers.
	 *
	 * @param identifiers that need to be synced where the key represents the idType and the value represents the id.
	 */
	void syncIdentifiers(final Map<String, String> identifiers) {
		dispatchIDSyncEvent(identifiers, VisitorID.AuthenticationState.UNKNOWN);
	}

	/**
	 * Initiates an Identity Request event for syncing Identifers with an authentication state.
	 *
	 * @param identifiers         that need to be synced where the key represents the idType and the value represents
	 *                            the id.
	 * @param authenticationState a valid AuthenticationState value.
	 */
	void syncIdentifiers(final Map<String, String> identifiers,
						 final VisitorID.AuthenticationState authenticationState) {
		dispatchIDSyncEvent(identifiers, authenticationState);
	}

	/**
	 * Returns the push identifier.
	 * Returns null if there is no push identifier set yet.
	 *
	 * @param callback {@link AdobeCallback}, the sdk would call with the push identifier string value as the parameter.
	 */
	void getPushIdentifier(final AdobeCallback<String> callback) {
		createIdentityRequestWithOneTimeCallbackWithCallbackParam(IdentityConstants.EventDataKeys.Identity.PUSH_IDENTIFIER,
				null,
				callback, new StringVariantSerializer());
	}

	/**
	 * Returns the advertising identifier.
	 * Returns null if there is no advertising identifier set yet.
	 *
	 * @param callback {@link AdobeCallback}, the sdk would call with the advertising identifier string value as the parameter.
	 */
	void getAdvertisingIdentifier(final AdobeCallback<String> callback) {
		createIdentityRequestWithOneTimeCallbackWithCallbackParam(
			IdentityConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
			null, callback, new StringVariantSerializer());
	}

	private <T> void createIdentityRequestWithOneTimeCallbackWithCallbackParam(final String identifierKey,
			final EventData eventData,
			final AdobeCallback<T> callback,
			final VariantSerializer<T> valueSerializer) {
		if (callback == null) {
			return;
		}

		Event event;

		// do not want to set event data to null.
		if (eventData == null) {
			event = new Event.Builder(REQUEST_IDENTITY_EVENT_NAME, EventType.IDENTITY,
									  EventSource.REQUEST_IDENTITY).build();
		} else {
			event = new Event.Builder(REQUEST_IDENTITY_EVENT_NAME, EventType.IDENTITY,
									  EventSource.REQUEST_IDENTITY).setData(eventData).build();
		}

		final AdobeCallbackWithError adobeCallbackWithError = callback instanceof AdobeCallbackWithError ?
				(AdobeCallbackWithError)callback : null;

		eventHub.registerOneTimeListener(event.getResponsePairID(), new Module.OneTimeListenerBlock() {
			@Override
			public void call(final Event e) {
				EventData eventData = e.getData();
				callback.call(eventData.optTypedObject(identifierKey, null, valueSerializer));
			}
		}, adobeCallbackWithError, PUBLIC_API_TIME_OUT_MILLISECOND);

		eventHub.dispatch(event);
		Log.trace(LOG_TAG,
				  "createIdentityRequestWithOneTimeCallbackWithCallbackParam : Identity request event has been added to the event hub : %s",
				  event);

	}

	/**
	 * Marshalling the eventData for the sync identifiers event.
	 * Creates a request identity event with the syncIdentifier eventData and dispatches to the eventhub.
	 *
	 * @param identifiers that need to be synced where the key represents the idType and the value represents the id.
	 * @param authState   a valid AuthenticationState value.
	 */
	private void dispatchIDSyncEvent(final Map<String, String> identifiers,
									 final VisitorID.AuthenticationState authState) {
		EventData syncData = new EventData();
		syncData.putStringMap(IdentityConstants.EventDataKeys.Identity.IDENTIFIERS, identifiers);
		syncData.putInteger(IdentityConstants.EventDataKeys.Identity.AUTHENTICATION_STATE, authState.getValue());
		syncData.putBoolean(IdentityConstants.EventDataKeys.Identity.FORCE_SYNC, false);
		syncData.putBoolean(IdentityConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);

		Event event = new Event.Builder(REQUEST_IDENTITY_EVENT_NAME,
										EventType.IDENTITY,
										EventSource.REQUEST_IDENTITY)
		.setData(syncData)
		.build();
		eventHub.dispatch(event);
		Log.trace(LOG_TAG, "dispatchIDSyncEvent : Identity Sync event has been added to event hub : %s", event);
	}
}
