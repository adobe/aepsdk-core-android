# Getting Started

This guide walks through how to get up and running with the AEP Android SDK with only a few lines of code.

> Existing ACP SDK customers should check out the [migration document](../Migration.md).

## Set up a Mobile Property

Set up a mobile property as outlined in the Adobe Experience Platform [docs](https://developer.adobe.com/client-sdks/documentation/getting-started/create-a-mobile-property/)

## Get the Experience Platform SDK

If you cannot access the Mobile Install Instructions dialog box in the Data Collection UI, complete the following sections to get the Adobe Experience Platform SDK. If you already completed the steps in the Mobile Install Instructions dialog box, no need to complete these steps.

## Installation instructions

1. Each extension needs to be added as a dependency to the mobile application project. The following examples will add the Mobile Core, Lifecycle, Identity, Signal and Profile extensions.

```java
implementation 'com.adobe.marketing.mobile:userprofile:2.+'
implementation 'com.adobe.marketing.mobile:core:2.+'
implementation 'com.adobe.marketing.mobile:identity:2.+'
implementation 'com.adobe.marketing.mobile:signal:2.+'
implementation 'com.adobe.marketing.mobile:lifecycle:2.+'
```

> **Warning**
> Using dynamic dependency versions is not recommended for production apps. Refer to this [page](./gradle-dependencies.md) for managing gradle dependencies.

2. Next you'll need to import SDK libraries into your project and register them for initialization. Extensions are registered with Mobile Core so that they can dispatch and listen for events.

### Java

```java
public class MainApp extends Application {
    private static final String APP_ID = "YOUR_APP_ID";

    @Override
    public void onCreate() {
        super.onCreate();

        MobileCore.setApplication(this);
        MobileCore.setLogLevel(LoggingMode.VERBOSE);
        MobileCore.configureWithAppID(APP_ID);

        List<Class<? extends Extension>> extensions = Arrays.asList(
                Lifecycle.EXTENSION,
                Signal.EXTENSION,
                Identity.EXTENSION,
                UserProfile.EXTENSION);
        MobileCore.registerExtensions(extensions, o -> {
            Log.d(LOG_TAG, "AEP Mobile SDK is initialized");
        });
    }
}
```

### Kotlin

```kotlin
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileCore.setApplication(this)
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        MobileCore.configureWithAppID("YOUR_APP_ID")

        val extensions = listOf(Lifecycle.EXTENSION, Signal.EXTENSION, Identity.EXTENSION, UserProfile.EXTENSION)
        MobileCore.registerExtensions(extensions) {
            Log.d(LOG_TAG, "AEP Mobile SDK is initialized")
        }
    }

}
```

## Implement Lifecycle Metrics

Lifecycle metrics is an optional, yet valuable feature provided by the Adobe Experience Platform SDK. It offers out-of-the-box, application lifecycle information about an app user. These metrics contain information on the app user's engagement lifecycle, such as device information, install or upgrade information, session start and pause times, and more.

Within the `Activity.onResume()` function, start Lifecycle data collection:

### Java

```java
@Override
public void onResume() {
    super.onResume();
    MobileCore.setApplication(getApplication());
    MobileCore.lifecycleStart(null);
}
```

### Kotlin

```kotlin
override fun onResume() {
    super.onResume()
    MobileCore.setApplication(application)
    MobileCore.lifecycleStart(null)
}
```

Setting the application is only necessary on activities that are entry points for your application. However, setting the application on each Activity has no negative impact and ensures that the SDK always has the necessary reference to your application. As a result, you should call `setApplication` on each of your activities.
You can use the `onPause` function to pause the lifecycle data collection:
To ensure accurate session and crash reporting, this call must be added to every Activity.

### Java

```java
@Override
public void onPause() {
    super.onPause();
    MobileCore.lifecyclePause();
}
```

### Kotlin

```kotlin
override fun onPause() {
    super.onPause()
    MobileCore.lifecyclePause()
}
```

## Sample Apps

To download more examples of integrating the AEP Android SDK, head over to the sample app resources.

[View Samples](https://github.com/adobe/aepsdk-sample-app-android)

## Next Steps

- Get familiar with the various APIs offered by the AEP SDK by checking out the [Mobile Core API reference](./api-reference.md).
- Validate SDK implementation with [Assurance](../Debugging.md).
- To build an extension on-top of the AEP SDK, check out the [Building Extensions documentation](../EventHub/BuildingExtensions.md).
