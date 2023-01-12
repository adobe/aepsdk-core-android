# Lifecycle API Usage

This document lists the APIs provided by Lifecycle extension, along with sample code snippets on how to properly use the APIs.

For more in-depth information about the Lifecycle extension, visit the [official SDK documentation on Lifecycle](https://developer.adobe.com/client-sdks/documentation/mobile-core/lifecycle/).


#### Registering Lifecycle extension

`Lifecycle.EXTENSION` represents a reference to the `LifecycleExtension` class that can be registered with `MobileCore` via its `registerExtensions` api.

##### Java

```java
import com.adobe.marketing.mobile.Lifecycle;

MobileCore.registerExtensions(Arrays.asList(Lifecycle.EXTENSION, ...), new AdobeCallback<Object>() {
    // handle callback
});
```

##### Kotlin

```kotlin
import com.adobe.marketing.mobile.Lifecycle

MobileCore.registerExtensions(Arrays.asList(Lifecycle.EXTENSION, ...)){
    // handle callback
}
```


#### Getting Lifecycle extension version

##### Java

```java
final String lifecycleExtensionVersion = Lifecycle.extensionVersion();
```

##### Kotlin

```kotlin
val lifecycleExtensionVersion: String = Lifecycle.extensionVersion()
```
