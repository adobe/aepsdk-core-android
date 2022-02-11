package com.adobe.marketing.mobile;

import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("all")
public class MockEventHubModuleTest extends EventHub {
	private static final int EVENT_WAIT_MS = 100;

	private final List<Event> events;
	private final Map<String, List<String>> ignoredEvents;
	private final List<String> ignoredEventTypes;
	private final List<String> ignoredEventSources;
	private final List<String> ignoredStateNames;
	private CountDownLatch expectedEventsLatch;
	private CountDownLatch expectedIgnoredEventsLatch;

	private CountUpLatch eventsCountUpLatch;

	private boolean ignoreAllEvents;
	private boolean ignoreAllStateChangeEvents;
	private int allEventsCount;

	private Object lock = new Object();

	public ReprocessEventsHandler reprocessEventsHandler;
	public boolean replaceRulesAndEvaluateEventsHasBeenCalled;
	private static final String STANDARD_STATE_CHANGE_EVENTNAME = "Shared state change";
	private static final String XDM_STATE_CHANGE_EVENTNAME = "Shared state change (XDM)";


	/**
	 * Returns an instance of the com.adobe.marketing.mobile.Event Hub
	 *
	 * @param name     the name of the {@code com.adobe.marketing.mobile.EventHub} to be created - for logging purposes
	 * @param services instance of {@code PlatformServices} class to provide platform-specific functionality
	 * @throws IllegalArgumentException If platform services is null
	 */
	public MockEventHubModuleTest(final String name, final PlatformServices services) throws IllegalArgumentException {
		super(name, services);
		events = new ArrayList<Event>();
		ignoredEventSources = new ArrayList<String>();
		ignoredEventTypes = new ArrayList<String>();
		ignoredEvents = new HashMap<String, List<String>>();
		ignoredStateNames = new ArrayList<String>();
		expectedEventsLatch = new CountDownLatch(0);
		eventsCountUpLatch = new CountUpLatch();
		allEventsCount = 0;
	}

	@Override
	public void dispatch(final Event e) {
		if (!shouldIgnoreEvent(e)) {
			Log.debug("Received Event", e.getEventType().getName() + "  " + e.getEventSource().getName());
			events.add(e);
			expectedEventsLatch.countDown();
			eventsCountUpLatch.countUp();
		} else {
			Log.debug("Ignored Event", e.getEventType().getName() + "  " + e.getEventSource().getName());
		}

		allEventsCount++;
		super.dispatch(e);
	}

	public void setExpectedEventCount(final int count) {
		expectedEventsLatch = new CountDownLatch(count);
	}

	public void ignoreAllEvents() {
		ignoreAllEvents = true;
	}

	public void clearIgnoredEventFilters() {
		ignoreAllEvents = false;
		ignoreAllStateChangeEvents = false;
		ignoredEventTypes.clear();
		ignoredEventSources.clear();
		ignoredEvents.clear();
		ignoredStateNames.clear();
	}

	public void ignoreEvents(final EventType eventType, final EventSource eventSource) {
		if (eventSource == null && eventType != null) {
			ignoredEventTypes.add(eventType.getName());
		} else if (eventSource != null && eventType == null) {
			ignoredEventSources.add(eventSource.getName());
		} else {
			List<String> ignoredTypesForSource = ignoredEvents.get(eventSource.getName());

			if (ignoredTypesForSource == null) {
				ignoredTypesForSource = new ArrayList<String>();
			}

			ignoredTypesForSource.add(eventType.getName());
			ignoredEvents.put(eventSource.getName(), ignoredTypesForSource);
		}
	}

	private boolean shouldIgnoreEvent(final Event e) {
		if (e == null || ignoreAllEvents || ignoredEventTypes.contains(e.getEventType())
				|| ignoredEventSources.contains(e.getEventSource())) {
			return true;
		}

		String eventName = e.getName();
		EventData eventData = e.getData();

		if (STANDARD_STATE_CHANGE_EVENTNAME.equals(eventName) || XDM_STATE_CHANGE_EVENTNAME.equals(eventName)) {
			if (ignoreAllStateChangeEvents) {
				return true;
			}

			if (eventData != null &&
					ignoredStateNames.contains(eventData.getString("stateOwner"))) {
				return true;
			}
		}

		EventSource eventSource = e.getEventSource();
		EventType eventType = e.getEventType();
		List<String> ignoredEventsForType = ignoredEvents.get(eventSource.getName());
		return ignoredEventsForType != null && ignoredEventsForType.contains(eventType.getName());
	}

	public void createSharedState(final String stateName, final int eventNumber, final EventData data) {
		createSharedState(stateName, eventNumber, data, SharedStateType.STANDARD);
	}

	public void createSharedState(final String stateName, final int eventNumber, final EventData data,
								  final SharedStateType sharedStateType) {
		try {
			super.createSharedState(new SharedStateModule(this, getPlatformServices(), stateName), eventNumber, data,
									sharedStateType);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	public void updateSharedState(final String stateName, final int eventNumber, final EventData data) {
		updateSharedState(stateName, eventNumber, data, SharedStateType.STANDARD);
	}

	public void updateSharedState(final String stateName, final int eventNumber, final EventData data,
								  final SharedStateType sharedStateType) {
		try {
			super.updateSharedState(new SharedStateModule(this, getPlatformServices(), stateName), eventNumber, data,
									sharedStateType);
		} catch (InvalidModuleException e) {
			e.printStackTrace();
		}
	}

	public List<Event> getEvents(final int count) {

		eventsCountUpLatch.await(count);
		List<Event> copy = new ArrayList<Event>();

		synchronized (lock) {
			copy.addAll(events);
		}

		return copy;
	}

	public EventData getSharedState(final String stateName, final int eventNumber) {
		return getSharedState(stateName, eventNumber, SharedStateType.STANDARD);
	}

	public EventData getSharedState(final String stateName, final int eventNumber, final SharedStateType sharedStateType) {
		Event event = new Event.Builder("Test", EventType.HUB, EventSource.NONE).build();
		event.setEventNumber(eventNumber);
		return super.getSharedEventState(stateName, event, new SharedStateModule(this, getPlatformServices(), "Test"),
										 sharedStateType);
	}

	private void waitForEvents() {
		waitForEvents(EVENT_WAIT_MS);
	}

	public void clearEvents() {
		waitForEvents();

		synchronized (lock) {
			events.clear();
			expectedEventsLatch = new CountDownLatch(0);
			eventsCountUpLatch = new CountUpLatch();
			allEventsCount = 0;
		}
	}
	private void waitForEvents(final int milliseconds) {
		try {
			expectedEventsLatch.await(milliseconds, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void clearEvents(final int milliseconds) {
		waitForEvents(milliseconds);
		this.clearEvents();
	}

	public List<Event> getEvents() {
		waitForEvents();
		return events;
	}

	public void replaceRulesAndEvaluateEvents(final Module module, final List<Rule> rules,
			final ReprocessEventsHandler reprocessEventsHandler) {
		this.reprocessEventsHandler = reprocessEventsHandler;
		this.replaceRulesAndEvaluateEventsHasBeenCalled = true;
		super.replaceRulesAndEvaluateEvents(module, rules, reprocessEventsHandler);
	}





	public void ignoreStateChangeEvents(final String stateName) {
		ignoredStateNames.add(stateName);
	}

	public void ignoreAllStateChangeEvents() {
		ignoreAllStateChangeEvents = true;
	}

	public int getAllEventsCount() {
		return allEventsCount;
	}
}
