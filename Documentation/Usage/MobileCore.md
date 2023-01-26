# Mobile Core API Usage

This document lists the APIs provided by MobileCore, along with sample code snippets on how to properly use the APIs.

For more in-depth information about the Mobile Core, visit the [official SDK documentation on Mobile Core](https://developer.adobe.com/client-sdks/documentation/mobile-core/).

#### Importing Mobile Core

##### Java

```java
import com.adobe.marketing.mobile.MobileCore;
```

##### Kotlin

```kotlin
import com.adobe.marketing.mobile.MobileCore
```


#### Getting MobileCore version

##### Java

```java
final String coreVersion = MobileCore.extensionVersion();
```

##### Kotlin

```kotlin
val coreVersion: String = MobileCore.extensionVersion()
```


#### Setting the log level

The SDK log verbosity can be adjusted to one of the following modes: `ERROR`, `WARNING`, `DEBUG`, `VERBOSE`.

##### Java

```java
MobileCore.setLogLevel(LoggingMode.VERBOSE);
```

##### Kotlin

```kotlin
MobileCore.setLogLevel(LoggingMode.VERBOSE)
```


#### Retrieving the current log level

##### Java

```java
final LoggingMode loggingMode = MobileCore.getLogLevel();
```

##### Kotlin

```kotlin
val loggingMode: LoggingMode = MobileCore.getLogLevel()
```

#### Setting the wrapper type

The wrapper type can be set to one of the follwing types: `NONE`, `REACT_NATIVE`, `FLUTTER`, `CORDOVA`, `UNITY`, `XAMARIN`.

##### Java

```java
MobileCore.setWrapperType(WrapperType.REACT_NATIVE);
```

##### Kotlin

```kotlin
MobileCore.setWrapperType(WrapperType.REACT_NATIVE)
```


#### Initializing MobileCore with Android Application instance

Use the `setApplication` api to pass the Android Application instance to SDK. This allows the SDK to monitor the lifecycle of your Android application.

##### Java

```java
public class YourApp extends Application {

   @Override
   public void onCreate() {
      super.onCreate();

      MobileCore.setApplication(this);
   }
}
```

##### Kotlin

```kotlin
class YourApp : Application() {
    override fun onCreate() {
        super.onCreate()

        MobileCore.setApplication(this)
    }
}
```


#### Retrieving the registered Application

You can use the `getApplication()` api to get the Android Application instance that was previously set via `MobileCore.setApplication()`


##### Java

```java
final Application app = MobileCore.getApplication();
```

##### Kotlin

```kotlin
val app = MobileCore.getApplication()
```


#### Registering extensions and starting the SDK

##### Java

```java
import com.adobe.marketing.mobile.Identity
import com.adobe.marketing.mobile.Signal
...

MobileCore.registerExtensions(
    Arrays.asList(Identity.EXTENSION, Signal.EXTENSION, ...), new AdobeCallback<Object>() {
    // handle callback
});
```

##### Kotlin

```kotlin
import com.adobe.marketing.mobile.Identity
import com.adobe.marketing.mobile.Signal
...

MobileCore.registerExtensions(Arrays.asList(Identity.EXTENSION, Signal.EXTENSION, ...)){
    // handle callback
}
```


####  Registering an EventListener

##### Java

```java
MobileCore.registerEventListener(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, new AdobeCallback<Event>() {
    @Override
    public void call(Event value) {
        // handle callback
    }
});
```

##### Kotlin

```kotlin
MobileCore.registerEventListener(EventType.ANALYTICS, EventSource.REQUEST_CONTENT) { event ->
    // handle callback
}
```


####  Configuring the SDK with an app id

`MobileCore.configureWithAppId` api can be used to download and apply the configuration for the provided app Id.

##### Java

```java
MobileCore.configureWithAppId("YOUR_APP_ID");
```

##### Kotlin

```kotlin
MobileCore.configureWithAppId("YOUR_APP_ID")
```


####  Configuring the SDK with a bundled file asset

You can bundle a JSON configuration file in the app's `assets` folder to replace or complement the configuration that was downloaded by using the Configure with App ID per environment approach.

##### Java

```java
MobileCore.configureWithFileInAssets("SampleBundledJSONConfigFile.json");
```

##### Kotlin

```kotlin
MobileCore.configureWithFileInAssets("SampleBundledJSONConfigFile.json")
```


####  Configuring the SDK with a file path

##### Java

```java
MobileCore.configureWithFileInPath("absolute/path/to/YourJSONConfigfile.json");
```

##### Kotlin

```kotlin
MobileCore.configureWithFileInPath("absolute/path/to/YourJSONConfigfile.json")
```


####  Updating the configuration programmatically

You can update the configuration programmatically by passing configuration keys and values to override the existing configuration via `MobileCore.updateConfiguration()` api
Keys that are not found on the current configuration are added when this method is followed. Null values are allowed and replace existing configuration values.

##### Java

```java
Map<String, Object> data = new HashMap<>();
data.put("global.privacy", "optedout");

MobileCore.updateConfiguration(data);
```

##### Kotlin

```kotlin
val data: Map<String, Any?> = mapOf(
    "global.privacy" to "optedout",
    "sampleKey" to "sampleValue"
)

MobileCore.updateConfiguration(data)
```


####  Clearing programmatically updated configuration

You can clear programmatic configuration changes made using `MobileCore.updteConfiguration()` api via `MobileCore.clearUpdatedConfiguration()` api.

##### Java

```java
MobileCore.clearUpdatedConfiguration();
```

##### Kotlin

```kotlin
MobileCore.clearUpdatedConfiguration()
```


#### Dispatch an Event

##### Java

```java
final Map<String, Object> eventData = new HashMap<>();
eventData.put("sampleKey", "sampleValue");

final Event sampleEvent = new Event.Builder("SampleEventName", "SampleEventType", "SampleEventSource")
                          .setEventData(eventData)
                          .build();

MobileCore.dispatchEvent(sampleEvent);
```

##### Kotlin

```kotlin
val eventData: Map<String, Any?> = mapOf("sampleKey" to "sampleValue")

val sampleEvent = Event.Builder("Sample Event Name", "Sample EventType", "Sample Event Source")
                  .setEventData(eventData)
                  .build()

MobileCore.dispatchEvent(sampleEvent)
```


#### Dispatch an Event with response callback

##### Java

```java
final Map<String, Object> eventData = new HashMap<>();
eventData.put("sampleKey", "sampleValue");

final Event sampleEvent = new Event.Builder("SampleEventName", "SampleEventType", "SampleEventSource")
                          .setEventData(eventData)
                          .build();

MobileCore.dispatchEventWithResponseCallback(sampleEvent, 5000L, new AdobeCallbackWithError<Event>() {
    // implement callback
});
```

##### Kotlin

```kotlin
val eventData: Map<String, Any?> = mapOf("sampleKey" to "sampleValue")
val sampleEvent = Event.Builder("SampleEventName", "SampleEventType", "SampleEventSource")
                       .setEventData(eventData)
                       .build()

MobileCore.dispatchEvent(sampleEvent, 5000L) {
    // implement callback
}
```


#### Setting an advertising identifier

##### Java

```java
MobileCore.setAdvertisingIdentifier("YOUR_ADVERTISING_IDENTIFIER");
```

##### Kotlin

```kotlin
MobileCore.setAdvertisingIdentifier("YOUR_ADVERTISING_IDENTIFIER")
```


#### Setting a push identifier

##### Java

```java
MobileCore.setPushIdentifier("YOUR_PUSH_IDENTIFIER");
```

##### Kotlin

```kotlin
MobileCore.setPushIdentifier("YOUR_PUSH_IDENTIFIER")
```


#### Collecting PII data

##### Java

```java
final Map<String, String> piiData = new HashMap<>();
piiData.put("piiDataKey", "piiDataValue");

MobileCore.collectPii(piiData);
```

##### Kotlin

```kotlin
val piiData: Map<String, Any?> = mapOf("piiDataKey" to "sampleValue")
MobileCore.collectPii(piiData)
```


#### Collecting Message info 

##### Java

```java
final Map<String, Object> messageInfo = new HashMap<>();
messageInfo.put("sampleKey", "sampleValue");

MobileCore.collectMessageInfo(messageInfo);
```

##### Kotlin

```kotlin
val messageInfo: Map<String, Any?> = mapOf("sampleKey" to "sampleValue")

MobileCore.collectMessageInfo(messageInfo)
```


#### Setting privacy status

Privacy status can be set to one of the following values `OPT_IN`, `OPT_OUT`, `UNKNOWN`.

##### Java

```java
MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_IN);
```

##### Kotlin

```kotlin
MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_IN)
```


#### Retrieving current privacy status

##### Java

```java
MobileCore.getPrivacyStatus(new AdobeCallback<MobilePrivacyStatus>() {
    @Override
    public void call(MobilePrivacyStatus privacyStatus) {
        // handle callback
    }
});
```

##### Kotlin

```kotlin
MobileCore.getPrivacyStatus{ privacyStatus ->
    // handle callback
}
```


#### Reading the SDK identities

##### Java

```java
MobileCore.getSdkIdentities(new AdobeCallback<String>() {
    @Override
    public void call(String sdkIdentitiesJson) {
        // handle callback
    }
});
```

##### Kotlin

```kotlin
MobileCore.getSdkIdentities { sdkIdentitiesJson -> 
    // handle callback
}
```


#### Reset SDK identities

##### Java

```java
MobileCore.resetIdentities();
```

##### Kotlin

```kotlin
MobileCore.resetIdentities()
```


#### Track an action

##### Java

```java
final Map<String, String> sampleContextData = new HashMap<>();
messageInfo.put("sampleKey", "sampleValue");

MobileCore.trackAction("SampleActionName", sampleContextData);
```

##### Kotlin

```kotlin
val sampleContextData: Map<String, String?> = mapOf("sampleKey" to "sampleValue")
MobileCore.trackAction("SampleActionName", sampleContextData)
```


#### Track state

##### Java

```java
final Map<String, String> sampleContextData = new HashMap<>();
messageInfo.put("sampleKey", "sampleValue");

MobileCore.trackState("SampleState", sampleContextData);
```

##### Kotlin

```kotlin
val sampleContextData: Map<String, String?> = mapOf("sampleKey" to "sampleValue")
MobileCore.trackAction("SampleState", sampleContextData)
```


#### Starting a Lifecycle session

##### Java

```java
final Map<String, String> sampleContextData = new HashMap<>();
messageInfo.put("sampleKey", "sampleValue");

MobileCore.lifecycleStart(sampleContextData);
```

##### Kotlin

```kotlin
val sampleContextData: Map<String, String?> = mapOf("sampleKey" to "sampleValue")
MobileCore.lifecycleStart(sampleContextData)
```


#### Pausing a Lifecycle session

##### Java

```java
MobileCore.lifecyclePause();
```

##### Kotlin

```kotlin
MobileCore.lifecyclePause()
```
