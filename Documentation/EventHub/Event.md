# Event Hub Event

This document outlines the specification for an Event Hub `Event`. These events are dispatched from the Event Hub and received by listeners that are registered with the Event Hub.

### Event Specification

| Name              | Type               | Description                                                  |
| ----------------- | ------------------ | ------------------------------------------------------------ |
| name              | String             | Event name used primarily for logging                        |
| uniqueIdentifier  | String             | UUID which uniquely identifies this event                    |
| type              | String             | See [Event Type](#event-type)                                |
| source            | String             | See [Event Source](#event-source)                            |
| data              | Map<String, Object>| Immutable dictionary holding one or more key value pairs that are associated with the event. |
| timestamp         | long               | The time that this event was generated                       |
| responseID        | String             | If this event was generated in response to a previous event, this value holds the `id` of the triggering event. |
| mask              | String[]           | kv pairs in event data that should be used for EventHistory storage. |

### Event Type

Every `Event` has an `EventType` string associated with it, which defines what kind of event it is and determines who is notified when this event occurs.

For a full list of possible event types, see [EventType.java](../../code/core/src/main/java/com/adobe/marketing/mobile/EventType.java).

### Event Source

Along with an `EventType`, an `Event` has a `EventSource` string associated with it, which defines where the event originated and is used to determine who is notified when this event occurs.

For a full list of possible event sources, see [EventSource.java](../../code/core/src/main/java/com/adobe/marketing/mobile/EventSource.java).

### Creating an `Event`

Creating a new `Event` is easy:

#### Java:
```java
Map<String, Object> eventData = new HashMap<>();
eventData.put("mykey", "myvalue")
Event event = new Event.Builder("MyEvent", EventType.ANALYTICS, EventSource.REQUEST_CONTENT)
              .setEventData(eventData)
              .build();
```

#### Kotlin:
```kotlin
val event = Event.Builder("MyEvent", EventType.ANALYTICS, EventSource.REQUEST_CONTENT)
            .setEventData(mapOf("mykey" to "myvalue"))
            .build()
```

