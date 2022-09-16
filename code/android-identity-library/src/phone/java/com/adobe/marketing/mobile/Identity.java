/* ******************************************************************************
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

import com.adobe.marketing.mobile.identity.IdentityCore;

import java.util.List;
import java.util.Map;

public class Identity {
	private final static String TAG = "Identity";
	private static IdentityCore identityCore;
	private final static String EXTENSION_VERSION = "1.3.2";
	private static final String NULL_CONTEXT_MESSAGE = "Context must be set before calling SDK methods";

	private Identity() {

	}

	public static String extensionVersion() {
		return EXTENSION_VERSION;
	}

	/**
	 * Registers the Identity extension with the {@code MobileCore}
	 *
	 * <p>
	 *
	 * This will allow the extension to send and receive events to and from the SDK.
	 *
	 * @throws InvalidInitException If the registration was not successful.
	 */
	public static void registerExtension() throws InvalidInitException {
		final Core core = MobileCore.getCore();

		if (core == null) {
			throw  new InvalidInitException();
		}

		try {
			//MobileCore may not be loaded or present (because may be Core extension was not
			//available). In that case, the Identity extension will not initialize itself
			identityCore = new IdentityCore(core.eventHub, new IdentityModuleDetails());
		} catch (Exception e) {
			throw new InvalidInitException();
		}
	}

	// =======================================================================
	// Identity Methods
	// =======================================================================
	/**
	 * Updates the given customer IDs with the Adobe Experience Cloud ID Service.
	 *
	 * Synchronizes the provided customer identifiers to the Adobe Experience Cloud ID Service.
	 * If a customer ID type matches an existing ID type and identifier, then it is updated with the new ID
	 * authentication state. New customer IDs are added. All given customer IDs are given the default
	 * authentication state of {@link com.adobe.marketing.mobile.VisitorID.AuthenticationState#UNKNOWN}.
	 * <p>
	 * These IDs are preserved between app upgrades, are saved and restored during the standard application backup process,
	 * and are removed at uninstall.
	 * <p>
	 * If the current SDK privacy status is {@link MobilePrivacyStatus#OPT_OUT}, then calling this method results
	 * with no operations being performed.
	 *
	 * @param identifiers {@code Map<String, String>} containing identifier type as the key and identifier as the value.
	 *                    Both identifier type and identifier should be non empty and non null values,
	 *                    otherwise they will be ignored
	 */
	public static void syncIdentifiers(final Map<String, String> identifiers) {
		if (identityCore == null) {
			Log.error(TAG, "syncIdentifiers(ids) : Unable to sync Visitor identifiers because (%s).", NULL_CONTEXT_MESSAGE);
			return;
		}

		if (identifiers == null || identifiers.isEmpty()) {
			Log.warning(TAG, "syncIdentifiers(ids) : Unable to sync Visitor identifiers, provided map was null or empty.");
			return;
		}

		Log.trace(TAG, "syncIdentifiers(ids) : Processing a request to sync Visitor identifiers.");
		identityCore.syncIdentifiers(identifiers);
	}

	/**
	 * Updates the given customer IDs with the Adobe Experience Cloud ID Service.
	 *
	 * Synchronizes the provided customer identifiers to the Adobe Experience Cloud ID Service.
	 * If a customer ID type matches an existing ID type and identifiers, then it is updated with the new ID
	 * authentication state. New customer IDs are added.
	 * <p>
	 * These IDs are preserved between app upgrades, are saved and restored during the standard application backup process,
	 * and are removed at uninstall.
	 * <p>
	 * If the current SDK privacy status is {@link MobilePrivacyStatus#OPT_OUT}, then calling this method results
	 * with no operations being performed.
	 *
	 * @param identifiers {@code Map<String, String>} containing identifier type as the key and identifier as the value.
	 *                    Both identifier type and identifier should be non empty and non null values,
	 *                    otherwise they will be ignored
	 * @param authenticationState {@code VisitorIDAuthenticationState} value indicating authentication state for the user
	 */
	public static void syncIdentifiers(final Map<String, String> identifiers,
									   final VisitorID.AuthenticationState authenticationState) {
		if (identityCore == null) {
			Log.error(TAG, "syncIdentifiers(ids, state) : Unable to sync Visitor identifiers because (%s)", NULL_CONTEXT_MESSAGE);
			return;
		}

		if (identifiers == null || identifiers.isEmpty()) {
			Log.warning(TAG, "syncIdentifiers(ids, state) : Unable to sync Visitor identifiers, provided map was null or empty");
			return;
		}

		Log.trace(TAG, "syncIdentifiers(ids, state) : Processing a request to sync Visitor identifiers.");
		identityCore.syncIdentifiers(identifiers, authenticationState);
	}

	/**
	 * Updates the given customer ID with the Adobe Experience Cloud ID Service.
	 * <p>
	 * Synchronizes the provided customer identifier type key and value with the given
	 * authentication state to the Adobe Experience Cloud ID Service.
	 * If the given customer ID type already exists in the service, then
	 * it is updated with the new ID and authentication state. Otherwise a new customer ID is added.
	 * <p>
	 * This ID is preserved between app upgrades, is saved and restored during the standard application backup process,
	 * and is removed at uninstall.
	 * <p>
	 * If the current SDK privacy status is {@link MobilePrivacyStatus#OPT_OUT}, then calling this method results
	 * with no operations being performed.
	 *
	 * @param identifierType {@code String} containing identifier type; should not be null or empty
	 * @param identifier {@code String} containing identifier value; should not be null or empty
	 * @param authenticationState {@code VisitorIDAuthenticationState} value indicating authentication state for the user
	 */
	public static void syncIdentifier(final String identifierType,
									  final String identifier,
									  final VisitorID.AuthenticationState authenticationState) {
		if (identityCore == null) {
			Log.error(TAG, "syncIdentifier : Unable to sync Visitor identifiers because (%s)", NULL_CONTEXT_MESSAGE);
			return;
		}

		if (StringUtils.isNullOrEmpty(identifierType)) {
			Log.warning(TAG, "syncIdentifier : Unable to sync Visitor identifier due to null or empty identifierType");
			return;
		}

		Log.trace(TAG, "syncIdentifier : Processing a request to sync Visitor identifier.");
		identityCore.syncIdentifier(identifierType, identifier, authenticationState);
	}

	/**
	 * Appends Adobe visitor data to a URL string.
	 * <p>
	 * If the provided URL is null or empty, it is returned as is. Otherwise, the following information is added to the
	 * {@link String} returned in the {@link AdobeCallback}:
	 * <ul>
	 *     <li>The {@code adobe_mc} attribute is an URL encoded list containing:
	 *         <ul>
	 *             <li>Experience Cloud ID (ECID)</li>
	 *             <li>Experience Cloud Org ID</li>
	 *             <li>Analytics Tracking ID, if available from Analytics</li>
	 *             <li>A timestamp taken when this request was made</li>
	 *         </ul>
	 *     </li>
	 *     <li>The optional {@code adobe_aa_vid} attribute is the URL encoded Analytics Custom Visitor ID, if available from Analytics.</li>
	 * </ul>
	 *
	 * @param baseURL {@code String} URL to which the visitor info needs to be appended
	 * @param callback {@code AdobeCallback} invoked with the updated URL {@code String};
	 *        when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
	 *        eventuality of an unexpected error or if the default timeout (500ms) is met before the Identity URL variables are retrieved
	 */
	public static void appendVisitorInfoForURL(final String baseURL, final AdobeCallback<String> callback) {
		if (identityCore == null) {
			Log.error(TAG, "appendVisitorInfoForURL : Unable to append Visitor information to URL because (%s)",
					  NULL_CONTEXT_MESSAGE);

			returnExtensionNotInitializedError(callback);
			return;
		}

		Log.trace(TAG, "appendVisitorInfoForURL : Processing a request to append Adobe visitor data to a URL string.");
		identityCore.appendToURL(baseURL, callback);
	}

	/**
	 * Gets Visitor ID Service variables in URL query parameter form for consumption in hybrid app.
	 * <p>
	 * This method will return an appropriately formed {@link String} containing Visitor ID Service URL variables.
	 * There will be no leading {@literal &} or {@literal ?} punctuation, as the caller is responsible for placing it in their resulting
	 * {@link java.net.URI} in the correct location.
	 * <p>
	 * If an error occurs while retrieving the URL string, {@code callback} will be called with null.
	 * Otherwise, the following information is added to the
	 * {@link String} returned in the {@link AdobeCallback}:
	 *  <ul>
	 *      <li>The {@code adobe_mc} attribute is an URL encoded list containing:
	 *          <ul>
	 *              <li>Experience Cloud ID (ECID)</li>
	 *              <li>Experience Cloud Org ID</li>
	 *              <li>Analytics Tracking ID, if available from Analytics</li>
	 *              <li>A timestamp taken when this request was made</li>
	 *          </ul>
	 *      </li>
	 *      <li>The optional {@code adobe_aa_vid} attribute is the URL encoded Analytics Custom Visitor ID, if available from Analytics.</li>
	 *  </ul>
	 *
	 * @param callback {@link AdobeCallback} which will be called containing Visitor ID Service URL parameters;
	 *        when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
	 *        eventuality of an unexpected error or if the default timeout (500ms) is met before the Identity URL variables are retrieved
	 */
	public static void getUrlVariables(final AdobeCallback<String> callback) {
		if (identityCore == null) {
			Log.error(TAG, "getUrlVariables : Unable to retrieve Visitor information as URL query parameter string because (%s)",
					  NULL_CONTEXT_MESSAGE);

			returnExtensionNotInitializedError(callback);
			return;
		}

		Log.trace(TAG, "getUrlVariables : Processing the request to get Visitor information as URL query parameters.");
		identityCore.getUrlVariables(callback);
	}

	/**
	 * Returns all customer identifiers which were previously synced with the Adobe Experience Cloud.
	 *
	 * @param callback {@link AdobeCallback} invoked with the list of {@link VisitorID} objects;
	 *         when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
	 *         eventuality of an unexpected error or if the default timeout (500ms) is met before the customer identifiers are retrieved
	 */
	public static void getIdentifiers(final AdobeCallback<List<VisitorID>> callback) {
		if (identityCore == null) {
			Log.error(TAG, "getIdentifiers : Unable to get Visitor identifiers because (%s)", NULL_CONTEXT_MESSAGE);

			returnExtensionNotInitializedError(callback);
			return;
		}

		Log.trace(TAG, "getIdentifiers : Processing a request to get all customer identifiers.");
		identityCore.getIdentifiers(callback);
	}

	/**
	 * Retrieves the Adobe Experience Cloud Visitor ID from the Adobe Experience Cloud ID Service.
	 * <p>
	 * The Adobe Experience Cloud ID (ECID) is generated at initial launch and is stored and used from that point forward.
	 * This ID is preserved between app upgrades, is saved and restored during the standard application backup process,
	 * and is removed at uninstall.
	 *
	 * @param callback {@link AdobeCallback} invoked with the ECID {@code String};
	 *        when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
	 *        eventuality of an unexpected error or if the default timeout (500ms) is met before the ECID is retrieved
	 */
	public static void getExperienceCloudId(final AdobeCallback<String> callback) {
		if (identityCore == null) {
			Log.error(TAG, "getExperienceCloudId : Unable to get ECID because (%s)", NULL_CONTEXT_MESSAGE);

			returnExtensionNotInitializedError(callback);
			return;
		}

		Log.trace(TAG, "getExperienceCloudId : Processing the request to get ECID.");
		identityCore.getExperienceCloudId(callback);
	}

	// end Identity Methods

	/**
	 * For testing
	 */
	static void resetIdentityCore() {
		identityCore = null;
	}

	/**
	 * When an {@link AdobeCallbackWithError} is provided, the fail method will be called with {@link AdobeError#EXTENSION_NOT_INITIALIZED}.
	 * @param callback should not be null, should be instance of {@code AdobeCallbackWithError}
	 */
	private static void returnExtensionNotInitializedError(final AdobeCallback callback) {
		if (callback == null) {
			return;
		}

		final AdobeCallbackWithError adobeCallbackWithError = callback instanceof AdobeCallbackWithError ?
				(AdobeCallbackWithError) callback : null;

		if (adobeCallbackWithError != null) {
			adobeCallbackWithError.fail(AdobeError.EXTENSION_NOT_INITIALIZED);
		}
	}

}
