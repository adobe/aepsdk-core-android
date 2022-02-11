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

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a single event
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
public final class Event {
	private String      name;
	private String      uniqueIdentifier;
	private EventSource source;
	private EventType   type;
	private String      pairID;
	private String      responsePairID;
	private EventData   data;
	private long        timestamp;
	private int         eventNumber;
	// Specifies the properties in the Event and its data that should be used in the hash for EventHistory storage.
	private String[]	mask;

	/**
	 * Convenience event for retrieving the oldest shared state
	 **/
	static final Event SHARED_STATE_OLDEST = new Event(0);

	/**
	 * Convenience event for retrieving the newest shared state
	 **/
	static final Event SHARED_STATE_NEWEST = new Event(Integer.MAX_VALUE);

	/**
	 * Event Builder
	 */
	public static class Builder {
		private Event event;
		private boolean didBuild;

		/**
		 * Builder constructor with required {@link Event} attributes as parameters.
		 *
		 * @param name   {@code String} event name
		 * @param type   {@link EventType} event type
		 * @param source {@link EventSource} event source
		 */
		Builder(final String name, final EventType type, final EventSource source) {
			this(name, type, source, null);
		}

		/**
		 * Builder constructor with required {@link Event} attributes as parameters and a {@code String[]} mask.
		 *
		 * @param name   {@code String} event name
		 * @param type   {@link EventType} event type
		 * @param source {@link EventSource} event source
		 * @param mask	 {@code String[]} event mask
		 */
		Builder(final String name, final EventType type, final EventSource source, final String[] mask) {
			event = new Event();
			event.name = name;
			event.uniqueIdentifier = UUID.randomUUID().toString();
			event.type = type;
			event.source = source;
			event.data = new EventData();
			event.responsePairID = UUID.randomUUID().toString();
			event.eventNumber = 0;
			event.mask = mask;
			didBuild = false;
		}

		/**
		 * Builder constructor with required {@code Event} attributes as parameters
		 *
		 * @param name   required {@code String} to be set as event name; should not be null or empty string
		 * @param type   required {@code String} to be set as event type; should not be null or empty string
		 * @param source required {@code String} to be set as event source; should not be null or empty string
		 */
		public Builder(final String name, final String type, final String source) {
			this(name, EventType.get(type), EventSource.get(source), null);
		}

		/**
		 * Builder constructor with required {@code Event} attributes as parameters
		 *
		 * @param name   required {@code String} to be set as event name; should not be null or empty string
		 * @param type   required {@code String} to be set as event type; should not be null or empty string
		 * @param source required {@code String} to be set as event source; should not be null or empty string
		 * @param mask	 {@code String[]} event mask
		 */
		public Builder(final String name, final String type, final String source, final String[] mask) {
			this(name, EventType.get(type), EventSource.get(source), mask);
		}

		/**
		 * Sets the data for this {@code Event}. The keys should be of type {@code String}.
		 * <p>
		 * Note: Custom classes are not supported as value types. These values will be skipped from the {@code Event} data.
		 *
		 * The accepted value types are:
		 * <ul>
		 *     	<li>{@code Boolean}</li>
		 * 		<li>{@code Byte}</li>
		 * 		<li>{@code Collection<Object>}</li>
		 * 		<li>{@code Double}</li>
		 * 		<li>{@code Float}</li>
		 * 		<li>{@code Integer}</li>
		 * 		<li>{@code String}</li>
		 *		<li>{@code List<Object>}</li>
		 * 		<li>{@code Long}</li>
		 * 		<li>{@code Map<String, Object>}</li>
		 * 		<li>{@code Short}</li>
		 * 		<li>null</li>
		 * </ul>
		 *
		 * @param data Data associated with this {@link Event}
		 * @return this Event {@link Builder}
		 * @throws UnsupportedOperationException if this method is called after {@link Builder#build()} was called
		 */
		public Builder setEventData(final Map<String, Object> data) {
			throwIfAlreadyBuilt();

			try {
				event.data = EventData.fromObjectMap(data);
			} catch (final Exception e) {
				Log.warning("EventBuilder", "Event data couldn't be serialized, empty data was set instead %s", e);
				event.data = new EventData();
			}

			return this;
		}

		/**
		 * Builds and returns the {@code Event} object. It returns null if the event's type or source are null
		 *
		 * @return the constructed {@link Event} or null if the type or source are null
		 * @throws UnsupportedOperationException if this method is called after {@link Builder#build()} was called
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
		 * Sets the data associated with this {@code Event}
		 *
		 * @param data {@link EventData} for this event
		 * @return this Event {@link Builder}
		 * @throws UnsupportedOperationException if this method is called after {@link Builder#build()} was called
		 */
		Builder setData(final EventData data) {
			throwIfAlreadyBuilt();
			event.data = data;
			return this;
		}

		/**
		 * Sets the uniqueIdentifier for this {@code Event}.
		 * If no unique identifier is set, one is generated when the {@code Event} is created.
		 *
		 * @param uniqueIdentifier {@code String} event uniqueIdentifier
		 * @return this Event {@link Builder}
		 * @throws UnsupportedOperationException if this method is called after {@link Builder#build()} was called
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
		 * Sets the pairId for this {@code Event}
		 *
		 * @param pairId {@code String} event pairId
		 * @return this Event {@link Builder}
		 * @throws UnsupportedOperationException if this method is called after {@link Builder#build()} was called
		 */
		Builder setPairID(final String pairId) {
			throwIfAlreadyBuilt();
			event.pairID = pairId;
			return this;
		}

		/**
		 * Sets the responsePairId for this {@code Event}
		 *
		 * @param responsePairId {@code String} event responsePairId
		 * @return this Event {@link Builder}
		 * @throws UnsupportedOperationException if this method is called after {@link Builder#build()} was called
		 */
		Builder setResponsePairID(final String responsePairId) {
			throwIfAlreadyBuilt();
			event.responsePairID = responsePairId;
			return this;
		}

		/**
		 * Set the timestamp for this event
		 *
		 * @param timestamp long event timestamp
		 * @return this Event {@link Builder}
		 * @throws UnsupportedOperationException if this method is called after {@link Builder#build()} was called
		 */
		Builder setTimestamp(final long timestamp) {
			throwIfAlreadyBuilt();
			event.timestamp = timestamp;
			return this;
		}

		/**
		 * Sets the event number for this {@code Event}
		 *
		 * @param number {@code int} containing event number for this {@link Event}
		 * @return this Event {@link Builder}
		 * @throws UnsupportedOperationException if this method is called after {@link Builder#build()} was called
		 */
		Builder setEventNumber(final int number) {
			throwIfAlreadyBuilt();
			event.eventNumber = number;
			return this;
		}

		private void throwIfAlreadyBuilt() {
			if (didBuild) {
				throw new UnsupportedOperationException("Event - attempted to call methods on Event.Builder after build() was called");
			}
		}
	}

	/**
	 * Private constructor. Use builder to create an event
	 */
	@SuppressWarnings("unused")
	private Event() {}

	/**
	 * Private constructor used by the static SHARED_STATE_NEWEST and SHARED_STATE_OLDEST
	 * constants.
	 *
	 * @param number event number
	 **/
	private Event(final int number) {
		this.eventNumber = number;
	}

	/**
	 * Copies an {@code Event}.
	 *
	 * @deprecated Use the {#link Builder} to create a new event. The current API returns {@code this}.
	 *
	 * @return  {@code this}
	 */
	@Deprecated
	public Event copy() {
		return this;
	}

	/**
	 * Returns the {@code Event} name
	 * @return {@code String} representing the {@link Event} name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the {@code Event} uniqueIdentifier
	 * @return {@code String} representing the {@link Event} uniqueIdentifier
	 */
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	/**
	 * Returns the {@code Event} source value
	 * @return {@code String} representing the {@link Event} source
	 */
	public String getSource() {
		return source.getName();
	}

	/**
	 * Returns the {@code Event} type value
	 * @return {@code String} representing the {@link Event} type
	 */
	public String getType() {
		return type.getName();
	}

	/**
	 * Retrieves the event data for current {@code Event}
	 * @return {@code Map<String, Object>} with the {@link Event} data key-value pairs. Returns null if the data is null
	 * 			or if an error occurred while processing
	 */
	public Map<String, Object> getEventData() {
		try {
			return data.toObjectMap();
		} catch (final Exception e) {
			Log.warning("EventBuilder", "An error occurred while retrieving the event data for %s and %s, %s", type.getName(),
						source.getName(), e);
		}

		return null;
	}

	/**
	 * @return {@link Event} timestamp in milliseconds
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return event source as {@link EventSource}
	 */
	EventSource getEventSource() {
		return source;
	}

	/**
	 * @return event type as {@link EventType}
	 */
	EventType getEventType() {
		return type;
	}

	/**
	 * @return event parameters
	 */
	EventData getData() {
		return data;
	}

	/**
	 * Event number relative to the internal event hub (will be zero if not sent to event hub)
	 *
	 * @return {@code int} representation of the current event number
	 */
	int getEventNumber() {
		return eventNumber;
	}

	/**
	 * Pair ID for this event. Should be set if this event is intended to be delivered to a specific listener
	 *
	 * @return String event pair ID
	 */
	String getPairID() {
		return pairID;
	}

	/**
	 * Pair ID for events dispatched by the receiver(s) in response to this event
	 *
	 * @return String response pair ID
	 */
	String getResponsePairID() {
		return responsePairID;
	}

	/**
	 * @return event timestamp in seconds
	 */
	long getTimestampInSeconds() {
		return TimeUnit.MILLISECONDS.toSeconds(timestamp);
	}

	/**
	 * @return {@code int} representation of event mask that would match this {@code Event}
	 */
	int getEventMask() {
		return Event.generateEventMask(this.type, this.source, this.pairID);
	}

	/**
	 * Sets event number, should only be used by eventHub ingesting this event
	 *
	 * @param number int event number that this event is on the event hub.
	 */
	void setEventNumber(final int number) {
		this.eventNumber = number;
	}

	/**
	 * @return {@code String[]} containing properties in the Event and its data that should be used in the hash for EventHistory storage.
	 */
	public String[] getMask() {
		return mask;
	}

	/**
	 * Sets the pairId for this {@code Event}, should only be used by extensions which receive a built event
	 *
	 * @param pairId {@code String} event pairId
	 */
	void setPairId(final String pairId) {
		this.pairID = pairId;
	}

	/**
	 * Generates event hash for the given type and source or the pairing id
	 *
	 * @param pairId unique identifier use for {@link OneTimeListener} (can be null)
	 * @param type   the {@link EventType} associated with a {@link ModuleEventListener}
	 * @param source the {@link EventSource} associated with a {@link ModuleEventListener}
	 *
	 * @return integer hash created from input params
	 */
	static int generateEventMask(final EventType type, final EventSource source, final String pairId) {
		if (!StringUtils.isNullOrEmpty(pairId)) {
			return pairId.hashCode();

		}

		return ("" + type.getName() + source.getName()).hashCode();
	}

	@Override
	public String toString() {
		final String NEWLINE = "\n";
		final String COMMA = ",";
		final StringBuilder sb = new StringBuilder();

		sb.append("{").append(NEWLINE);
		sb.append("    class: Event").append(COMMA).append(NEWLINE);
		sb.append("    name: ").append(name).append(COMMA).append(NEWLINE);
		sb.append("    eventNumber: ").append(eventNumber).append(COMMA).append(NEWLINE);
		sb.append("    uniqueIdentifier: ").append(uniqueIdentifier).append(COMMA).append(NEWLINE);
		sb.append("    source: ").append(source.getName()).append(COMMA).append(NEWLINE);
		sb.append("    type: ").append(type.getName()).append(COMMA).append(NEWLINE);
		sb.append("    pairId: ").append(pairID).append(COMMA).append(NEWLINE);
		sb.append("    responsePairId: ").append(responsePairID).append(COMMA).append(NEWLINE);
		sb.append("    timestamp: ").append(timestamp).append(COMMA).append(NEWLINE);
		sb.append("    data: ").append(data.prettyString(2)).append(NEWLINE);
		sb.append("    mask: ").append(Arrays.toString(mask)).append(COMMA).append(NEWLINE);
		sb.append("    fnv1aHash: ").append(data.toFnv1aHash(mask)).append(NEWLINE);
		sb.append("}");

		return sb.toString();
	}
}
