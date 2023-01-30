# Building Extensions

Extensions allow developers to extend the Experience Platform SDKs with their code. Building an extension includes listening for and dispatching events, reading the shared state of any registered extension, and sharing the state of the current extension. The application can use the extension to monitor for information that Adobe does not expose by default. It can also use the extension to modify Experience Platform SDK internal operations, for example, by adding additional data to messages that are sent or by sending data to other systems.

This document covers the high level concepts about developing your own extension.

#### Defining an Extension

For an extension to be registered with the `EventHub`, it must conform to the `Extension` class. The `Extension` protocol defines an `Extension` as a type which provides a initializer which takes in a `ExtensionApi` instance and override the necessary methods. 

See [`Extension.java`](../../code/core/src/main/java/com/adobe/marketing/mobile/Extension.java) for the complete definition.

#### Events

##### Purpose of an `Event`

- Triggering actions in the Experience Platform SDKs. Events are used by the extensions to signal when specific actions should occur, for example, to send an Analytics hit. Extensions can send the same types of events that the Experience Platform SDKs would send internally to trigger these actions.
- Triggering actions in another extension. Some applications might have multiple extensions, and some of these extensions might have their events defined that trigger actions.

##### Creating an `Event`

###### Java:
```java
Map<String, Object> eventData = new HashMap<>();
eventData.put("mykey", "myvalue")
Event event = new Event.Builder("MyEvent", EventType.ANALYTICS, EventSource.REQUEST_CONTENT)
              .setEventData(eventData)
              .build();
```

###### Kotlin:
```kotlin
val eventData = mapOf("mykey" to "myvalue")
val event = Event.Builder("MyEvent", EventType.ANALYTICS, EventSource.REQUEST_CONTENT)
            .setEventData(eventData)
            .build()
```

##### Creating a response `Event`

###### Java:
```java
Event triggerEvent = ...;
Event responseEvent = new Event.Builder("Configuration Response Event", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
                .inResponseToEvent(triggerEvent)
                .setEventData(eventData)
                .build();
```

###### Kotlin:
```kotlin
val triggerEvent: Event = ...
val responseEvent = Event.Builder("Configuration Response Event", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
            .inResponseToEvent(triggerEvent)
            .setEventData(eventData)
            .build()
```

#### Dispatching Events

Extensions can dispatch an `Event` to the `EventHub` via the `dispatch(Event event)` API, which is provided by default to all classes that implement `Extension`. This API will result in all listeners whose `EventType` and `EventSource` match to be invoked with the `event`.

###### Java:
```java
Event event = new Event.Builder("MyEvent", EventType.ANALYTICS, EventSource.REQUEST_CONTENT)
              .setEventData(eventData)
              .build();
getApi().dispatch(event);
```

###### Kotlin:
```kotlin
val event = Event.Builder("MyEvent", EventType.ANALYTICS, EventSource.REQUEST_CONTENT)
            .setEventData(eventData)
            .build()
api.dispatch(event)
```

##### Dispatching response Events

Occasionally, an extension may want to dispatch a response event for a given `Event`, to do this an extension must first create the response `Event` from the trigger `Event`, then dispatch it through the `EventHub`, this will notify any response listeners registered for `triggerEvent`

###### Java:
```java
Event responseEvent = new Event.Builder("Configuration Response Event", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
                .inResponseToEvent(triggerEvent)
                .setEventData(eventData)
                .build();
getApi().dispatch(responseEvent)
```

###### Kotlin:
```kotlin
val responseEvent = Event.Builder("Configuration Response Event", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
            .inResponseToEvent(triggerEvent)
            .setEventData(eventData)
            .build()
api.dispatch(responseEvent)
```

#### Listening for Events

Extensions can listen for events that are dispatched through the `EventHub` with a listener. Listeners define which events they are interested in being notified about through an `EventType` and `EventSource`. ExtensionEventListener is a functional interface which takes in an `Event` as a parameter and does not return a value.

```java
@FunctionalInterface
public interface ExtensionEventListener {
    void hear(@NonNull final Event event);
}
```

> Note: Registering listeners should be done in the `onRegistered()` function of an extension.

##### Listener Example

###### Java:
```java
// receiveConfigurationRequest is invoked whenever the `EventHub` dispatches an event with type configuration and source request content
getApi().registerEventListener(EventType.CONFIGURATION, EventSource.REQUEST_CONTENT, this::receiveConfigurationRequest);

// Can also be implemented with a closure
private void receiveConfigurationRequest(Event event) {
   // handle event
}
```

###### Kotlin:
```kotlin
// receiveConfigurationRequest is invoked whenever the `EventHub` dispatches an event with type configuration and source request content
api.registerEventListener(EventType.CONFIGURATION, EventSource.REQUEST_CONTENT, this::receiveConfigurationRequest);

// Can also be implemented with a closure
private fun receiveConfigurationRequest(e: Event) {
   // handle event
}
```

##### Wildcard Listeners

Some extensions may have the requirement to be notified of all events that are dispatched from the `EventHub`, in this case, a `wildcard` `EventType` and `EventSource` are available.

###### Java:
```java
// Invoked for all events that are dispatched from the `EventHub`
getApi().registerEventListener(EventType.WILDCARD, EventSource.WILDCARD, event -> {
    // handle event    
});
```

###### Kotlin:
```kotlin
// Invoked for all events that are dispatched from the `EventHub`
api.registerEventListener(EventType.WILDCARD, EventSource.WILDCARD) {
    // handle event
}
```

#### Extension Public APIs

##### Registration 

To register extensions with `MobileCore.registerExtensions(...)` API, they must expose a static `EXTENSION` property with the class extending the `Extension` class as 

###### Java
```java
public class CustomExtension {

    public static final Class<? extends Extension> EXTENSION = CLASS_EXTENDING_EXTENSION.class;

    ...
}

// Apps can easily register the extension using this property
List<Class<? extends Extension>> extensions = Arrays.asList(<OTHER_EXTENSIONS>, CustomExtension.EXTENSION);
MobileCore.registerExtensions(extensions, value -> {
    // Registration is  complete. 
});

```

###### Kotlin
```kotlin
object CustomExtension {

    val EXTENSION: Class<out Extension> = CLASS_EXTENDING_EXTENSION::class.java
    
    ...
}

// Apps can easily register the extension using this property
val extensions = listOf(<OTHER_EXTENSIONS>, CustomExtension.EXTENSION)
MobileCore.registerExtensions(extensions) {
    // Registration is  complete
}
```

##### Defining the public APIs for an `Extension`

Extensions should define their public APIs in a seperate class. All APIs should be static, and for APIs that return a value, _most_ should provide those values in the form of an asynchronous callback. Each API definition should provide clear documentation about it's behavior and the required parameters.

##### Public API Definition Example

###### Java

```java
/// Defines the public interface for the Identity extension
public class Identity {
    /**
     * Appends Adobe visitor data to a URL string.
     * 
     * @param baseURL {@code String} URL to which the visitor info needs to be appended
     * @param callback {@code AdobeCallback} invoked with the updated URL {@code String}; when an
     *     {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
     *     eventuality of an unexpected error or if the default timeout (500ms) is met before the
     *     Identity URL variables are retrieved
     */
    public static void appendVisitorInfoForURL(
            @NonNull final String baseURL, @NonNull final AdobeCallback<String> callback) {
        ...
    }
}
```

###### Kotlin
```kotlin
/// Defines the public interface for the Identity extension
object Identity {
    /**
     * Appends Adobe visitor data to a URL string.
     * 
     * @param baseURL {@code String} URL to which the visitor info needs to be appended
     * @param callback {@code AdobeCallback} invoked with the updated URL {@code String}; when an
     *     {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
     *     eventuality of an unexpected error or if the default timeout (500ms) is met before the
     *     Identity URL variables are retrieved
     */
    fun appendVisitorInfoForURL(baseURL: String, callback: AdobeCallback<String>) {
        ...
    }
}
```



##### Implementing your public APIs

Most implementations of public APIs should be lightweight, usually just dispatching an `Event` to your extension, and occasionally listening for a response `Event` to provide a return value.

##### APIs that don't return a value

APIs that only result in an action being taken and no value being returned can usually be implemented in just a few lines. In the following example the `Configuration` extension is listening for an `Event` of type `EventType.configuration` and source `EventSource.requestContent` with the app id payload. When the `Configuration` extension receives this `Event` it will carry out the required processing to configure the SDK with the given `appId` and potentially dispatch other events and update it's shared state.

###### Java

```java
public static void configureWithAppID(@NonNull final String appId) {
    if (appId == null) {
        Log.error(CoreConstants.LOG_TAG, LOG_TAG, "configureWithAppID failed - appId is null.");
        return;
    }

    Map<String, Object> eventData = new HashMap<>();
    eventData.put("CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID", appId);

    Event event =
            new Event.Builder(
                            "Configure with AppID",
                            EventType.CONFIGURATION,
                            EventSource.REQUEST_CONTENT)
                    .setEventData(eventData)
                    .build();
    MobileCore.dispatchEvent(event);
}
```

###### Kotlin

```kotlin
fun configureWithAppID(appId: String) {
    val eventData = mapOf("CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID" to appId)
    val event = Event.Builder(
            "Configure with AppID", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
        .setEventData(eventData)
        .build();
    MobileCore.dispatchEvent(event)
}
}
```

##### APIs that return a value

For APIs that return a value, response listeners should be used. In the following example the API dispatches an `Event` to the `Configuration` extension, which results in a response `Event` being dispatched with the privacy status stored in the event data, subsequently notifying the response listener.

###### Java

```java
public static void getPrivacyStatus(@NonNull final AdobeCallback<MobilePrivacyStatus> callback) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(
            CoreConstants.EventDataKeys.Configuration
                    .CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG,
            true);
    Event event =
            new Event.Builder(
                            "PrivacyStatusRequest",
                            EventType.CONFIGURATION,
                            EventSource.REQUEST_CONTENT)
                    .setEventData(eventData)
                    .build();

    MobileCore.dispatchEventWithResponseCallback(event, API_TIMEOUT_MS, new AdobeCallbackWithError<Event>() {
            @Override
            public void fail(final AdobeError error) {
                // Handle failure
            }

            @Override
            public void call(final Event event) {                
                // Handle success
            }
    });
}
```

###### Kotlin

```kotlin
fun getPrivacyStatus(callback: AdobeCallback<MobilePrivacyStatus?>) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[CoreConstants.EventDataKeys.Configuration] = true
    val event = Event.Builder("PrivacyStatusRequest", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
        .setEventData(eventData)
        .build()

    MobileCore.dispatchEventWithResponseCallback(event, API_TIMEOUT_MS, 
        object : AdobeCallbackWithError<Event?> {
            override fun fail(error: AdobeError) {
                // Handle failure
            }

            fun call(event: Event) {
                // Handle success
            }
        });
    }
```

The general pattern for getters follows:

1. Create an `Event` which will result in your extension dispatching a response `Event`.
2. Register a response listener for the newly created request `Event`.
3. Dispatch the request `Event`.
4. Handle the returned value within the response listener.

#### Event Processing

One of the most fundamental responsibilities for an extension is to process incoming events; these events often represent APIs being invoked. Extensions should only process one `Event` at a time, in a synchronous manner.

##### `readyForEvent`

In many situations when processing an `Event` you will depend upon shared state from antother extension, for example a valid configuration from the Configuration extension. When implementing the `Extension` protocol, you have the option to impelemnt `readyForEvent(Event() -> Bool`, this function is invoked by the `EventHub` each time an `Event` is dispatched and gives extensions the ability to state if they have all the dependencies required to process that specfic `Event`.  

For example, in the Identity extension when processing an `Event` of type `genericIdentity` it requires a valid configuration to exist, so in our implementation of `readyForEvent` we determine if a valid configuration exists before handling the `Event`.

```java
@Override
public boolean readyForEvent(@NonNull final Event event) {
    SharedStateResult result = getApi().getSharedState(
        IdentityConstants.EventDataKeys.Configuration.MODULE_NAME,
        event,
        false,
        SharedStateResolution.LAST_SET
    );
    return result != null && result.status == SharedStateStatus.SET
}
```

Once the extension has signaled that it is ready for a given `Event`, the corresponding listener in the extension is notified of the `Event`. `Events` are dispatched to listeners in a synchronous fashion per extension, ensuring that any given extension cannot process more than one `Event` at a time.

#### Shared States

Extensions use events and shared states to communicate with each other. The events allow extensions to be relatively decoupled, but shared states are necessary when an extension has a dependency on data provided by another extension.

A Shared State is composed of the following:

- The name of the extension who owns it
- The status of the Shared State defined as a `SharedStateStatus` (none, pending, set)
- An `Event` , which is an event that contains data that an extension wants to expose to other extensions

**Important**: Every `Event` does not result in an updated shared state. Shared states have to be explicitly set, which causes the `EventHub` to notify other extensions that your extension has published a new shared state.

##### Updating Shared State

By default, every extension is provided with an API to update their shared state with new data. Pass in the data and optional `Event` associated with the shared state, and the `EventHub` will update your shared state and dispatch an `Event` notifying other extensions that a new shared state for your extension is available.

```java
 /**
     * Creates a new shared state for this extension. If event is null, one of two behaviors will be
     * observed:
     *
     * <ul>
     *   <li>If this extension has not previously published a shared state, shared state will be
     *       versioned at 0
     *   <li>If this extension has previously published a shared state, shared state will be
     *       versioned at the latest
     * </ul>
     *
     * @param state {@code Map<String, Object>} representing current state of this extension
     * @param event The {@link Event} for which the state is being set. Passing null will set the
     *     state for the next shared state version
     */
public abstract void createSharedState(
            @NonNull final Map<String, Object> state, @Nullable final Event event);
```

##### Creating and Updating a Pending Shared State

In some cases, an extension may want to declare that its shared state is currently pending. For example, an extension may be doing some data manipulation, but in the meantime, the extension may invalidate its existing shared state and notify other extensions that the extension is currently working on providing a new shared state. This can be done with the API `func createPendingSharedState(event: Event?) -> SharedStateResolver`. This function creates a pending shared state versioned at an optional `Event` and returns a closure, which is to be invoked with your updated shared state data once available.

###### Pending Shared State Example

```java
// set your current Shared State to pending
SharedStateResolver pendingResolver = createPendingSharedState(event);

// compute your new Shared State data
Map<String, Object> updatedSharedStateData = computeSharedState()

// resolve your pending Shared State
pendingResolver.resolve(updatedSharedStateData)
```

##### Reading Shared State from another Extension

All extensions are provided a default API to read shared state from another extension. Simply pass in the name of the extension and the optional `Event` to get an extension's shared state.

```java
/**
 * Gets the shared state data for a specified extension.
 *
 * @param extensionName extension name for which to retrieve data. See documentation for the
 *     list of available states.
 * @param event the {@link Event} for which the state is being requested. Passing null will
 *     retrieve latest state available.
 * @param barrier If true, the {@code EventHub} will only return {@code set} if extensionName
 *     has moved past event.
 * @param resolution the {@link SharedStateResolution} to resolve for return {@code
 *     SharedStateResult} for the requested extensionName and event
 */
public abstract SharedStateResult getSharedState(
            @NonNull final String extensionName,
            @Nullable final Event event,
            final boolean barrier,
            @NonNull final SharedStateResolution resolution);
```

The resolution is used to fetch a specific type of shared state. Using `.any` will fetch the last shared state with any status, while using `.lastSet`, will fetch the last shared state with a status of `.set`. This is useful if you would like to read the cached config before the remote config has been downloaded. 

#### XDM Shared States

XDM shared states allow extensions allow the Edge extension to collect XDM data from various mobile extensions when needed and allow for the creation of XDM data elements to be used in Launch rules. All XDM Shared state data should be modeled based on known / global XDM schema.

##### Updating XDM Shared State

By default, every extension is provided with an API to update their XDM shared state with new data. Pass in the data and optional `Event` associated with the XDM shared state, and the `EventHub` will update your shared state and dispatch an `Event` notifying other extensions that a new shared state for your extension is available. An extension can have none, one or multiple XDM schemas shared as XDM Shared state

```java
 /**
     * Creates a new shared state for this extension. If event is null, one of two behaviors will be
     * observed:
     *
     * <ul>
     *   <li>If this extension has not previously published a shared state, shared state will be
     *       versioned at 0
     *   <li>If this extension has previously published a shared state, shared state will be
     *       versioned at the latest
     * </ul>
     *
     * @param state {@code Map<String, Object>} representing current state of this extension
     * @param event The {@link Event} for which the state is being set. Passing null will set the
     *     state for the next shared state version
     */
public abstract void createXDMSharedState(
            @NonNull final Map<String, Object> state, @Nullable final Event event);
```

##### Creating and Updating a Pending XDM Shared State

In some cases, an extension may want to declare that its shared state is currently pending. For example, an extension may be doing some data manipulation, but in the meantime, the extension may invalidate its existing shared state and notify other extensions that the extension is currently working on providing a new shared state. This can be done with the API `func createPendingXDMSharedState(event: Event?) -> SharedStateResolver`. This function creates a pending shared state versioned at an optional `Event` and returns a closure, which is to be invoked with your updated XDM shared state data once available.

###### Pending Shared State Example

```java
// set your current Shared State to pending
SharedStateResolver pendingResolver = createXDMPendingSharedState(event);

// compute your new Shared State data
Map<String, Object> updatedSharedStateData = computeSharedState()

// resolve your pending Shared State
pendingResolver.resolve(updatedSharedStateData)
```

##### Reading XDM Shared State from another Extension

All extensions are provided a default API to read XDM shared state from another extension. Simply pass in the name of the extension and the optional `Event` to get an extension's shared state.

```java
/**
 * Gets the XDM shared state data for a specified extension.
 *
 * @param extensionName extension name for which to retrieve data. See documentation for the
 *     list of available states.
 * @param event the {@link Event} for which the state is being requested. Passing null will
 *     retrieve latest state available.
 * @param barrier If true, the {@code EventHub} will only return {@code set} if extensionName
 *     has moved past event.
 * @param resolution the {@link SharedStateResolution} to resolve for return {@code
 *     SharedStateResult} for the requested extensionName and event
 */
public abstract SharedStateResult getXDMSharedState(
            @NonNull final String extensionName,
            @Nullable final Event event,
            final boolean barrier,
            @NonNull final SharedStateResolution resolution);
```

The resolution is used to fetch a specific type of shared state. Using `.any` will fetch the last shared state with any status, while using `.lastSet`, will fetch the last shared state with a status of `.set`. This is useful if you would like to read the cached config before the remote config has been downloaded. 

##### Listening for Shared State Updates

In some instances an extension may want to be notified of when an extension publishes a new shared state, to do this an extension can register a listener which listens for an `Event` of type `hub` and source `sharedState`, then it can inspect the event data to determine which extension has published new shared state.

##### Java

```java
getApi().registerEventListener(EventType.HUB, EventSource.SHARED_STATE, this::handleSharedStateUpdate);

private void handleSharedStateUpdate(final Event event) {
    ...
}
```

##### Kotlin

```kotlin
api.registerEventListener(EventType.HUB, EventSource.SHARED_STATE, this::handleSharedStateUpdate);

private fun handleSharedStateUpdate(event: Event) {
    ...
}
```
