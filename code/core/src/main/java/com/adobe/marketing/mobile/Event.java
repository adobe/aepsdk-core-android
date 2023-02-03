/*
  Copyright 2022 Adobe. All rights reserved.
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
import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.internal.util.MapExtensionsKt;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.EventDataUtils;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a single event
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
public final class Event {

    // Name of the event
    private String name;
    // Unique identifier for the event
    private String uniqueIdentifier;
    // The `EventSource` for the event
    private String source;
    // The `EventType` for the event
    private String type;
    // Optional data associated with this event
    private Map<String, Object> data;
    // Time this event was created
    private long timestamp;
    // If `responseID` is not nil, then this event is a response event and `responseID` is the
    // `event.id` of the `triggerEvent`
    private String responseID;
    // Specifies the properties in the Event and its data that should be used in the hash for
    // EventHistory storage.
    private String[] mask;

    /** Event Builder */
    public static class Builder {

        private final Event event;
        private boolean didBuild;

        /**
         * Builder constructor with required {@code Event} attributes as parameters
         *
         * @param name required {@code String} to be set as event name; should not be null or empty
         *     string
         * @param type required {@code String} to be set as event type; should not be null or empty
         *     string
         * @param source required {@code String} to be set as event source; should not be null or
         *     empty string
         */
        public Builder(final String name, final String type, final String source) {
            this(name, type, source, null);
        }

        /**
         * Builder constructor with required {@code Event} attributes as parameters
         *
         * @param name required {@code String} to be set as event name; should not be null or empty
         *     string
         * @param type required {@code String} to be set as event type; should not be null or empty
         *     string
         * @param source required {@code String} to be set as event source; should not be null or
         *     empty string
         * @param mask {@code String[]} event mask
         */
        public Builder(
                final String name, final String type, final String source, final String[] mask) {
            event = new Event();
            event.name = name;
            event.uniqueIdentifier = UUID.randomUUID().toString();
            event.type = type;
            event.source = source;
            event.responseID = null;
            event.mask = mask;
            didBuild = false;
        }

        /**
         * Sets the data for this {@code Event}. The keys should be of type {@code String}.
         *
         * <p>Note: Custom classes are not supported as value types. These values will be skipped
         * from the {@code Event} data.
         *
         * <p>The accepted value types are:
         *
         * <ul>
         *   <li>{@code Boolean}
         *   <li>{@code Byte}
         *   <li>{@code Collection<Object>}
         *   <li>{@code Double}
         *   <li>{@code Float}
         *   <li>{@code Integer}
         *   <li>{@code String}
         *   <li>{@code List<Object>}
         *   <li>{@code Long}
         *   <li>{@code Map<String, Object>}
         *   <li>{@code Short}
         *   <li>null
         * </ul>
         *
         * @param data Data associated with this {@link Event}
         * @return this Event {@link Builder}
         * @throws UnsupportedOperationException if this method is called after {@link
         *     Builder#build()} was called
         */
        public Builder setEventData(final Map<String, Object> data) {
            throwIfAlreadyBuilt();

            try {
                event.data = EventDataUtils.immutableClone(data);
            } catch (final Exception e) {
                Log.warning(
                        CoreConstants.LOG_TAG,
                        "EventBuilder",
                        "Event data couldn't be serialized, empty data was set instead %s",
                        e);
            }

            return this;
        }

        /**
         * Builds and returns the {@code Event} object. It returns null if the event's type or
         * source are null
         *
         * @return the constructed {@link Event} or null if the type or source are null
         * @throws UnsupportedOperationException if this method is called again
         */
        public Event build() {
            throwIfAlreadyBuilt();
            didBuild = true;

            if (event.type == null || event.source == null) {
                return null;
            }

            if (event.timestamp == 0) {
                event.timestamp = System.currentTimeMillis();
            }

            return event;
        }

        /**
         * Sets the uniqueIdentifier for this {@code Event}. If no unique identifier is set, one is
         * generated when the {@code Event} is created.
         *
         * @param uniqueIdentifier {@code String} event uniqueIdentifier
         * @return this Event {@link Builder}
         * @throws UnsupportedOperationException if this method is called after {@link
         *     Builder#build()} was called
         */
        Builder setUniqueIdentifier(final String uniqueIdentifier) {
            if (uniqueIdentifier == null) {
                return this;
            }

            throwIfAlreadyBuilt();
            event.uniqueIdentifier = uniqueIdentifier;
            return this;
        }

        /**
         * Sets this as response for request {@code Event} *
         *
         * @param requestEvent {@code Event} event
         * @return this Event {@link Builder}
         * @throws UnsupportedOperationException if this method is called after {@link
         *     Builder#build()} was called
         */
        public Builder inResponseToEvent(final Event requestEvent) {
            throwIfAlreadyBuilt();

            if (requestEvent == null) {
                throw new NullPointerException("requestEvent is null");
            }
            event.responseID = requestEvent.uniqueIdentifier;
            return this;
        }

        /**
         * Sets responseId for this {@code Event} *
         *
         * @param responseId {@code String} event uniqueIdentifier
         * @return this Event {@link Builder}
         * @throws UnsupportedOperationException if this method is called after {@link
         *     Builder#build()} was called
         */
        Builder setResponseId(final String responseId) {
            throwIfAlreadyBuilt();
            event.responseID = responseId;
            return this;
        }

        /**
         * Set the timestamp for this event
         *
         * @param timestamp long event timestamp
         * @return this Event {@link Builder}
         * @throws UnsupportedOperationException if this method is called after {@link
         *     Builder#build()} was called
         */
        Builder setTimestamp(final long timestamp) {
            throwIfAlreadyBuilt();
            event.timestamp = timestamp;
            return this;
        }

        private void throwIfAlreadyBuilt() {
            if (didBuild) {
                throw new UnsupportedOperationException(
                        "Event - attempted to call methods on Event.Builder after build() was"
                                + " called");
            }
        }
    }

    /** Private constructor. Use builder to create an event */
    @SuppressWarnings("unused")
    private Event() {}

    /**
     * Copies an {@code Event}.
     *
     * @deprecated Use the {#link Builder} to create a new event. The current API returns {@code
     *     this}.
     * @return {@code this}
     */
    @Deprecated
    public Event copy() {
        return this;
    }

    /**
     * Clones the current {@link Event} with updated data
     *
     * @param newData data associated with the new {@code Event}
     * @return new cloned {@code Event} with provided data
     */
    public Event cloneWithEventData(final Map<String, Object> newData) {
        Event newEvent =
                new Event.Builder(this.name, this.type, this.source, this.mask)
                        .setEventData(newData)
                        .build();
        newEvent.uniqueIdentifier = this.uniqueIdentifier;
        newEvent.timestamp = this.timestamp;
        newEvent.responseID = this.responseID;
        return newEvent;
    }

    /**
     * Returns the {@code Event} name
     *
     * @return {@code String} representing the {@link Event} name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the {@code Event} uniqueIdentifier
     *
     * @return {@code String} representing the {@link Event} uniqueIdentifier
     */
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * Returns the {@code Event} source value
     *
     * @return {@code String} representing the {@link Event} source
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the {@code Event} type value
     *
     * @return {@code String} representing the {@link Event} type
     */
    public String getType() {
        return type;
    }

    /**
     * Retrieves the event data for current {@code Event}
     *
     * @return {@code Map<String, Object>} with the {@link Event} data key-value pairs. Returns null
     *     if the data is null or if an error occurred while processing
     */
    public Map<String, Object> getEventData() {
        return data;
    }

    /**
     * @return {@link Event} timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Pair ID for events dispatched by the receiver(s) in response to this event
     *
     * @return String response pair ID
     */
    public String getResponseID() {
        return responseID;
    }

    /** Pair ID for events dispatched by the receiver(s) in response to this event */
    @Deprecated
    void setResponseID(final String responseID) {
        this.responseID = responseID;
    }

    /**
     * @return event timestamp in seconds
     */
    public long getTimestampInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(timestamp);
    }

    /**
     * @return {@code String[]} containing properties in the Event and its data that should be used
     *     in the hash for EventHistory storage.
     */
    public String[] getMask() {
        return mask;
    }

    @NonNull @Override
    public String toString() {
        final String NEWLINE = "\n";
        final String COMMA = ",";
        final StringBuilder sb = new StringBuilder();

        sb.append("{").append(NEWLINE);
        sb.append("    class: Event").append(COMMA).append(NEWLINE);
        sb.append("    name: ").append(name).append(COMMA).append(NEWLINE);
        sb.append("    uniqueIdentifier: ").append(uniqueIdentifier).append(COMMA).append(NEWLINE);
        sb.append("    source: ").append(source).append(COMMA).append(NEWLINE);
        sb.append("    type: ").append(type).append(COMMA).append(NEWLINE);
        sb.append("    responseId: ").append(responseID).append(COMMA).append(NEWLINE);
        sb.append("    timestamp: ").append(timestamp).append(COMMA).append(NEWLINE);
        String dataAsStr = data == null ? "{}" : MapExtensionsKt.prettify(data);
        sb.append("    data: ").append(dataAsStr).append(COMMA).append(NEWLINE);
        sb.append("    mask: ").append(Arrays.toString(mask)).append(COMMA).append(NEWLINE);
        sb.append("}");

        return sb.toString();
    }
}
