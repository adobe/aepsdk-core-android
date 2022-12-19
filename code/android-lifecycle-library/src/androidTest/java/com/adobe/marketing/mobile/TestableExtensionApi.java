/*
  Copyright 2021 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile;

import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class TestableExtensionApi extends ExtensionApi {

    public Map<String, ExtensionEventListener> listeners = new HashMap<>();
    public List<Event> dispatchedEvents = new LinkedList<>();
    public List<Map<String, Object>> createdSharedState = new LinkedList<>();
    public List<Map<String, Object>> createdXDMSharedState = new LinkedList<>();
    public Map<String, SharedStateResult> mockedSharedState = new HashMap<>();
    public Map<String, SharedStateResult> mockedXDMSharedState = new HashMap<>();
    public Set<String> ignoredEvents = new HashSet<>();
    private final Map<EventSpec, CountDownLatch> expectedEvents = new HashMap<>();

    public TestableExtensionApi() {}

    @Override
    public void registerEventListener(
            @NonNull String eventType,
            @NonNull String eventSource,
            @NonNull ExtensionEventListener eventListener) {
        listeners.put(eventType + "-" + eventSource, eventListener);
    }

    @Override
    public void dispatch(@NonNull Event event) {
        if (shouldIgnore(event)) {
            return;
        }
        dispatchedEvents.add(event);

        EventSpec eventSpec = new EventSpec(event.getSource(), event.getType());
        if (expectedEvents.containsKey(eventSpec)) {
            CountDownLatch countDownLatch = expectedEvents.get(eventSpec);
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }
        }
    }

    @Override
    public void startEvents() {}

    @Override
    public void stopEvents() {}

    @Override
    public void createSharedState(@NonNull Map<String, Object> state, Event event) {
        createdSharedState.add(state);
    }

    @Override
    public SharedStateResolver createPendingSharedState(Event event) {
        return state -> createdSharedState.add(state);
    }

    @Override
    public SharedStateResult getSharedState(
            @NonNull String extensionName,
            Event event,
            boolean barrier,
            @NonNull SharedStateResolution resolution) {
        if (event != null) {
            SharedStateResult result =
                    mockedSharedState.get(extensionName + "-" + event.getUniqueIdentifier());
            return result != null ? result : mockedSharedState.get(extensionName);
        }
        return mockedSharedState.get(extensionName);
    }

    @Override
    public void createXDMSharedState(@NonNull Map<String, Object> state, Event event) {
        createdXDMSharedState.add(state);
    }

    @Override
    public SharedStateResolver createPendingXDMSharedState(Event event) {
        return state -> createdXDMSharedState.add(state);
    }

    @Override
    public SharedStateResult getXDMSharedState(
            @NonNull String extensionName,
            Event event,
            boolean barrier,
            @NonNull SharedStateResolution resolution) {
        if (event != null) {
            SharedStateResult result =
                    mockedXDMSharedState.get(extensionName + "-" + event.getUniqueIdentifier());
            return result != null ? result : mockedXDMSharedState.get(extensionName);
        }
        return mockedXDMSharedState.get(extensionName);
    }

    @Override
    public void unregisterExtension() {}

    @Override
    public void getHistoricalEvents(
            @NonNull EventHistoryRequest[] eventHistoryRequests,
            boolean enforceOrder,
            @NonNull EventHistoryResultHandler<Integer> handler) {}

    @Override
    public <T extends ExtensionListener> boolean registerEventListener(
            String eventType,
            String eventSource,
            Class<T> extensionListenerClass,
            ExtensionErrorCallback<ExtensionError> errorCallback) {
        return false;
    }

    @Override
    public <T extends ExtensionListener> boolean registerWildcardListener(
            Class<T> extensionListenerClass, ExtensionErrorCallback<ExtensionError> errorCallback) {
        return false;
    }

    @Override
    public boolean setSharedEventState(
            Map<String, Object> state,
            Event event,
            ExtensionErrorCallback<ExtensionError> errorCallback) {
        return false;
    }

    @Override
    public boolean setXDMSharedEventState(
            Map<String, Object> state,
            Event event,
            ExtensionErrorCallback<ExtensionError> errorCallback) {
        return false;
    }

    @Override
    public Map<String, Object> getSharedEventState(
            String stateName, Event event, ExtensionErrorCallback<ExtensionError> errorCallback) {
        return null;
    }

    @Override
    public Map<String, Object> getXDMSharedEventState(
            String stateName, Event event, ExtensionErrorCallback<ExtensionError> errorCallback) {
        return null;
    }

    @Override
    public boolean clearXDMSharedEventStates(ExtensionErrorCallback<ExtensionError> errorCallback) {
        return false;
    }

    @Override
    public boolean clearSharedEventStates(ExtensionErrorCallback<ExtensionError> errorCallback) {
        return false;
    }

    /**
     * Ignores the events from being dispatched by event hub.
     *
     * @param eventType {@code String} event type of event to be ignored
     * @param eventSource {@code String} event source of event to be ignored
     */
    public void ignoreEvent(String eventType, String eventSource) {
        ignoredEvents.add(eventType + "-" + eventSource);
    }

    /** Removes all the ignored events. */
    public void resetIgnoredEvents() {
        ignoredEvents.clear();
    }

    /**
     * Determines if the event is to be ignored and not dispatched by event hub
     *
     * @param event {@code Event} instance
     * @return true if event is to be ignored, false otherwise
     */
    public boolean shouldIgnore(Event event) {
        return ignoredEvents.contains(event.getType() + "-" + event.getSource());
    }

    /**
     * Simulate the events that are being sent to event hub, if there is a listener registered for
     * that type of event, that listener will receive the event
     *
     * @param event {@code Event}
     */
    public void simulateComingEvent(Event event) {
        ExtensionEventListener listener = listeners.get(event.getType() + "-" + event.getSource());
        if (listener != null) {
            listener.hear(event);
        }
        ExtensionEventListener wildcardListener =
                listeners.get(EventType.WILDCARD + "-" + EventSource.WILDCARD);
        if (wildcardListener != null) {
            wildcardListener.hear(event);
        }
    }

    /**
     * Get the listener that is registered for the specific event source and type
     *
     * @param eventType event type
     * @param eventSource event source
     * @return {@code ExtensionEventListener} instance
     */
    public ExtensionEventListener getListener(String eventType, String eventSource) {
        return listeners.get(eventType + "-" + eventSource);
    }

    /**
     * Simulate the shared state of an extension for a matching event
     *
     * @param extensionName extension name
     * @param event matching {@code Event} instance
     * @param status {@code SharedStateStatus} instance
     * @param data shared state value
     */
    public void simulateSharedState(
            String extensionName, Event event, SharedStateStatus status, Map<String, Object> data) {
        mockedSharedState.put(
                extensionName + "-" + event.getUniqueIdentifier(),
                new SharedStateResult(status, data));
    }

    /**
     * Simulate the shared state of an certain extension ignoring the event id
     *
     * @param extensionName extension name
     * @param status {@code SharedStateStatus} instance
     * @param data shared state value
     */
    public void simulateSharedState(
            String extensionName, SharedStateStatus status, Map<String, Object> data) {
        mockedSharedState.put(extensionName, new SharedStateResult(status, data));
    }

    /**
     * Simulate the XDM shared state of an extension for a matching event
     *
     * @param extensionName extension name
     * @param event {@code Event} instance
     * @param status {@code SharedStateStatus} instance
     * @param data shared state value
     */
    public void simulateXDMSharedState(
            String extensionName, Event event, SharedStateStatus status, Map<String, Object> data) {
        mockedXDMSharedState.put(
                extensionName + "-" + event.getUniqueIdentifier(),
                new SharedStateResult(status, data));
    }

    /**
     * Simulate the XDM shared state of an certain extension ignoring the event id
     *
     * @param extensionName extension name
     * @param status {@code SharedStateStatus} instance
     * @param data shared state value
     */
    public void simulateXDMSharedState(
            String extensionName, SharedStateStatus status, Map<String, Object> data) {
        mockedSharedState.put(extensionName, new SharedStateResult(status, data));
    }

    /** Clear the events and shared states that have been created by the current extension */
    public void resetDispatchedEventAndCreatedSharedState() {
        dispatchedEvents.clear();
        createdSharedState.clear();
        createdXDMSharedState.clear();
    }

    /**
     * Add an event to the list of expected events.
     *
     * @param type the type of the event.
     * @param source the source of the event.
     * @param count the number of events expected to be received.
     */
    public void setExpectedEvent(final String type, final String source, final int count) {
        EventSpec eventSpec = new EventSpec(source, type);
        expectedEvents.put(eventSpec, new CountDownLatch(count));
    }

    public Map<EventSpec, CountDownLatch> getExpectedEvents() {
        return expectedEvents;
    }

    /** Class defining {@link Event} specifications, contains Event's source and type. */
    public static class EventSpec {

        public final String source;
        public final String type;

        public EventSpec(final String source, final String type) {
            if (source == null || source.isEmpty()) {
                throw new IllegalArgumentException("Event Source cannot be null or empty.");
            }

            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException("Event Type cannot be null or empty.");
            }

            // Normalize strings
            this.source = source.toLowerCase();
            this.type = type.toLowerCase();
        }

        @NonNull @Override
        public String toString() {
            return "type '" + type + "' and source '" + source + "'";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            EventSpec eventSpec = (EventSpec) o;
            return Objects.equals(source, eventSpec.source) && Objects.equals(type, eventSpec.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, type);
        }
    }
}
