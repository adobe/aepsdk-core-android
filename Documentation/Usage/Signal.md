# Signal API Usage

This document lists the APIs provided by Signal extension, along with sample code snippets on how to properly use the APIs.

For more in-depth information about the Signal extension, visit the [official SDK documentation on Signal](https://developer.adobe.com/client-sdks/documentation/mobile-core/signal/).


#### Registering Signal extension

`Signal.EXTENSION` represents a reference to the `SignalExtension` class that can be registered with `MobileCore` via its `registerExtensions` api.

##### Java

```java
import com.adobe.marketing.mobile.Signal;

MobileCore.registerExtensions(Arrays.asList(Signal.EXTENSION, ...), new AdobeCallback<Object>() {
    // handle callback
});
```

##### Kotlin

```kotlin
import com.adobe.marketing.mobile.Signal

MobileCore.registerExtensions(Arrays.asList(Signal.EXTENSION, ...)){
    // handle callback
}
```


#### Getting Signal extension version

##### Java

```java
final String signalExtensionVersion = Signal.extensionVersion();
```

##### Kotlin

```kotlin
val signalExtensionVersion: String = Signal.extensionVersion()
```
