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

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

class SignalExtension extends InternalModule {

	private static final String LOGTAG = "SignalExtension";
	private final ConcurrentLinkedQueue<Event> unprocessedEvents;
	private SignalHitsDatabase signalHitsDatabase;

	/**
	 * Constructor for an internal module, must be called by inheritors.
	 *
	 * @param hub         <code>com.adobe.marketing.mobile.EventHub</code> instance of eventhub that owns this module
	 * @param services    <code>PlatformServices</code> instance
	 */
	SignalExtension(final EventHub hub, final PlatformServices services) {
		super(SignalConstants.EventDataKeys.Signal.MODULE_NAME, hub, services);
		registerListener(EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT, ListenerRulesEngineResponseContentSignal.class);
		registerListener(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT,
						 ListenerConfigurationResponseContentSignal.class);
		this.signalHitsDatabase = new SignalHitsDatabase(services);
		this.unprocessedEvents = new ConcurrentLinkedQueue<Event>();
	}

	/**
	 * Constructor for testing purposes.
	 *
	 * @param hub         <code>com.adobe.marketing.mobile.EventHub</code> instance of eventhub that owns this module
	 * @param services    <code>PlatformServices</code> instance
	 * @param database    <code>SignalHitsDatabase</code> instance
	 */
	SignalExtension(final EventHub hub, final PlatformServices services, final SignalHitsDatabase database) {
		this(hub, services);
		this.signalHitsDatabase = database;
	}

	/**
	 * queue the signal event (postback or pii), triggered by Rules Engine, and try to process the events in queue.
	 *
	 * @param event original signal event{@code event} containing consequence data.
	 */
	void handleSignalConsequenceEvent(final Event event) {
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				Log.trace(LOGTAG, "Handling signal consequence event, number: %s", event.getEventNumber());
				unprocessedEvents.add(event);
				tryProcessQueuedEvent();
			}
		});
	}

	/**
	 * Removes the URL from the {@code event}'s {@code EventData} and passes them to the {@code UIService} to open.
	 * <p>
	 * Calling this method does nothing if there is no {@value SignalConstants.EventDataKeys.Signal#RULES_RESPONSE_CONTENT_OPENURL_URLS}
	 * key in the {@link EventData}, or if the {@link UIService} is unavailable.
	 *
	 * @param event {@link Event} containing the URLs to process
	 */
	void handleOpenURLConsequenceEvent(final Event event) {
		getExecutor().execute(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				final EventData eventData = event == null ? null : event.getData();

				if (eventData == null) {
					return;
				}

				Log.trace(LOGTAG, "Handling signal open url consequence event, number: %s", event.getEventNumber());

				final Map<String, Variant> signalConsequence = eventData.optVariantMap(
							SignalConstants.EventDataKeys.RuleEngine.CONSEQUENCE_TRIGGERED,
							null);

				if (signalConsequence == null || signalConsequence.isEmpty()) {
					Log.debug(LOGTAG, "Null or empty signal consequence. Return");
					return;
				}


				Map<String, Variant> consequenceDetail = Variant.optVariantFromMap(signalConsequence,
						SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL).optVariantMap(null);

				if (consequenceDetail == null || consequenceDetail.isEmpty()) {
					Log.debug(LOGTAG, "Null or empty signal consequence detail. Return");
					return;
				}

				final String url = Variant.optVariantFromMap(consequenceDetail,
								   SignalConstants.EventDataKeys.Signal.RULES_RESPONSE_CONTENT_OPENURL_URLS).optString("");

				if (StringUtils.isNullOrEmpty(url)) {
					Log.debug(LOGTAG, "Tried to process an OpenURL event, but no URL were found in EventData.");
					return;
				}

				if (getPlatformServices() == null) {
					Log.debug(LOGTAG, "%s (Platform Services), Unable to process an OpenURL event.", Log.UNEXPECTED_NULL_VALUE);
					return;
				}

				final UIService uiService = getPlatformServices().getUIService();

				if (uiService == null) {
					Log.debug(LOGTAG, "%s (UIService), Unable to process OpenURL event.", Log.UNEXPECTED_NULL_VALUE);
					return;
				}

				uiService.showUrl(url);
			}
		});
	}

	/**
	 * Clear the queue if opt-out, and pass the status to database.
	 *
	 * @param privacyStatus the new privacy status
	 */
	void updatePrivacyStatus(final MobilePrivacyStatus privacyStatus) {
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				if (privacyStatus == MobilePrivacyStatus.OPT_OUT) {
					unprocessedEvents.clear();
				}

				signalHitsDatabase.updatePrivacyStatus(privacyStatus);
				tryProcessQueuedEvent();
			}
		});
	}

	/**
	 * try to process the event in queue
	 */
	void tryProcessQueuedEvent() {
		while (!unprocessedEvents.isEmpty()) {
			final Event currentEvent = unprocessedEvents.peek();
			boolean isEventProcessed = processSignalEvent(currentEvent);

			if (isEventProcessed) {
				unprocessedEvents.poll();
			} else {
				break;
			}
		}
	}

	/**
	 * check if configuration shared state is ready, if so go ahead and process the signal event. Otherwise just return false.
	 *
	 * @param event the signal event
	 * @return true if the event has been processed, otherwise false.
	 */
	boolean processSignalEvent(final Event event) {
		EventData configurationSharedState = getSharedEventState(SignalConstants.EventDataKeys.Configuration.MODULE_NAME,
											 event);

		//configuration
		if (configurationSharedState == EventHub.SHARED_STATE_PENDING) {
			Log.debug(LOGTAG, "Can not handle signal consequence. Shared state for Configuration module is not ready.");
			return false;
		}


		final MobilePrivacyStatus privacyStatus = MobilePrivacyStatus.fromString(
					configurationSharedState.optString(SignalConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
							MobilePrivacyStatus.UNKNOWN.getValue()));

		if (privacyStatus ==  MobilePrivacyStatus.OPT_OUT) {
			Log.debug(LOGTAG, "Privacy status is OPT OUT. Signal processed without queuing the hit.");
			return true;
		}

		// all good to go.
		final EventData eventData = event == null ? null : event.getData();

		if (eventData == null) {
			return true;
		}

		final Map<String, Variant> signalConsequence = eventData.optVariantMap(
					SignalConstants.EventDataKeys.RuleEngine.CONSEQUENCE_TRIGGERED, null);

		if (signalConsequence == null || signalConsequence.isEmpty()) {
			Log.debug(LOGTAG, "Null or empty signal consequence. Return");
		} else {
			SignalTemplate signalTemplate = SignalTemplate.createSignalTemplateFromConsequence(signalConsequence);

			if (signalTemplate != null) {
				signalHitsDatabase.queue(signalTemplate.getSignalHit(), event.getTimestamp(), privacyStatus);
			}
		}

		return true;
	}
}
